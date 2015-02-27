package es.vegamultimedia.standardform.components;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

import es.vegamultimedia.standardform.BeanUI;
import es.vegamultimedia.standardform.Utils;
import es.vegamultimedia.standardform.annotations.StandardForm;
import es.vegamultimedia.standardform.annotations.StandardFormEnum;
import es.vegamultimedia.standardform.annotations.StandardFormField;
import es.vegamultimedia.standardform.model.Bean;

public class StandardTable<T extends Bean, K> extends Table {
	
	private static final long serialVersionUID = 3412364081573747242L;

	/**
     * Interface for showing the detail form for a bean 
     */
    public interface ShowDetailListener<T extends Bean> {
        /**
         * Shows the DetailForm
         * @param bean
         */
    	public abstract void showDetailForm(T bean);
    }
	
    /**
	 * Generated column of this StandardTable
	 */
	public static class GeneratedColumn {
		private Object id;
		private ColumnGenerator columnGenerator;
		public GeneratedColumn(Object id, ColumnGenerator generatedColumn) {
			this.id = id;
			this.columnGenerator = generatedColumn;
		}
		public Object getId() {
			return id;
		}
		public ColumnGenerator getColumnGenerator() {
			return columnGenerator;
		}
	}
	
	// Escuchador para mostrar el detailForm
	protected ShowDetailListener<T> showDetailListener;
	
	// BeanUI that created this standard list form
	protected BeanUI<T, K> beanUI;
	
	// Container del formulario
	protected BeanItemContainer<T> container;
	
	// Cabeceras de las columnas personalizadas
	protected HashMap<String, String> customColumnHeaders;
	
	/**
	 * Constructor used for a StandardTable inside a DetailForm
	 * @param container
	 * @param beanUI
	 * @param formEnabled
	 */
	public StandardTable(String caption,
			BeanItemContainer<T> container,
			BeanUI<T, K> beanUI) {
		this(container, beanUI, false, false,
				null, null, null, null);
		setCaption(caption);
		setPageLength(3);
	}

	/**
	 * Constructor used for a ListForm
	 * @param container
	 * @param beanUI
	 * @param formEnabled
	 * @param allowConsulting
	 * @param customVisibledColumns
	 * @param customColumnHeaders
	 * @param customGeneratedColumns
	 * @param showDetailListener
	 */
	public StandardTable(BeanItemContainer<T> container,
			BeanUI<T, K> beanUI,
			boolean formEnabled,
			boolean allowConsulting, 
			String[] customVisibledColumns,
			HashMap<String, String> customColumnHeaders,
			ArrayList<GeneratedColumn> customGeneratedColumns,
			ShowDetailListener<T> showDetailListener) {
		
		// Comprobación de parámetros null
		if (customColumnHeaders == null) {
			customColumnHeaders = new HashMap<String, String>();
		}
		if (customGeneratedColumns == null) {
			customGeneratedColumns = new ArrayList<GeneratedColumn>();
		}
		// Inicializamos atributos
		this.container = container;
		this.beanUI = beanUI;
		this.showDetailListener = showDetailListener;
		this.customColumnHeaders = customColumnHeaders;

		// Columnas de la tabla
		List<String> visibledColumns;
		
		// Obtenemos la anotación StandardForm del bean
		StandardForm standardFormAnnotation = beanUI.getBeanClass().getAnnotation(StandardForm.class);

		
		setImmediate(true);
		// TODO Parametrizar la longitud de la página
		setPageLength(10);
		
		setContainerDataSource(container);
		
		// Si se ha especificado columnas visibles personalizadas
		if (customVisibledColumns != null) {
			// Obtenemos todos los campos del bean
			Field[] beanFields = Utils.getBeanFields(beanUI.getBeanClass());
			// Se muestran las columnas especificadas (comprobando para cada una si existe el campo)
			visibledColumns = new ArrayList<String>();
			// Recorremos los campos
			for (int i=0; i<customVisibledColumns.length; i++) {
				// Obtenemos el campo que coincide con el nombre de la columna
				for (Field beanField : beanFields) {
					if (beanField.getName().equals(customVisibledColumns[i])) {
						// Añadimos el campo a las columnas visibles
						visibledColumns.add(customVisibledColumns[i]);
						// Añadimos la cabecera de la columna
						addHeaderColumn(beanField);
						break;
					}
				}
			}
		}
		// Si no se especifican las columnas visibles en la standardFormAnnotation
		else if (standardFormAnnotation.columns()[0].isEmpty()) {
			// Se muestran todas excepto el id
			visibledColumns = new ArrayList<String>();
			// Obtenemos sólo los campos declarados en el bean
			Field[] beanFields = beanUI.getBeanClass().getDeclaredFields();
			// Recorremos los campos
			for (int i=0; i<beanFields.length; i++) {
				// Si no es el id
				if (!beanFields[i].getName().equals("id")) {
					// Añadimos el campo a las columnas visibles
					visibledColumns.add(beanFields[i].getName());
					// Añadimos la cabecera de la columna
					addHeaderColumn(beanFields[i]);
				}
			}
		}
		// Si se especifican las columnas visibles en la standardFormAnnotation
		else {
			// Hacemos visibles las columnas especificadas
			visibledColumns = new ArrayList<String>(Arrays.asList(standardFormAnnotation.columns()));
			// Obtenemos todos los campos del bean
			Field[] beanFields = Utils.getBeanFields(beanUI.getBeanClass());
			// Para cada columna, añadimos su cabecera
			for(String nombreColumn : visibledColumns) {
				// Obtenemos el campo que coincide con el nombre de la columna
				for (Field beanField : beanFields) {
					if (beanField.getName().equals(nombreColumn)) {
						// Lo añadimos a la cabecera
						addHeaderColumn(beanField);
						break;
					}
				}
			}
		}
		
		String nombreColumnaEditarConsultar;
		// Si se permite edición
		if (standardFormAnnotation.allowsEditing()) {
			nombreColumnaEditarConsultar = "Editar";
		}
		else {
			nombreColumnaEditarConsultar = "Consultar";
		}
		// Si se permite consultar
		if (allowConsulting) {
			// Añadimos columna para editar o consultar 
			// (Llamamos al método de la superclase para no registrarlo en generatedColumns)
			addGeneratedColumn(nombreColumnaEditarConsultar,
					new EditColumnGenerator(nombreColumnaEditarConsultar));
			visibledColumns.add(nombreColumnaEditarConsultar);
		}
		// Si se permite eliminar
		if (standardFormAnnotation.allowsDeleting()) {
			// Añadimos columna para eliminar7
			// (Llamamos al método de la superclase para no registrarlo en generatedColumns)
			addGeneratedColumn("Eliminar", new DeleteColumnGenerator());
			visibledColumns.add("Eliminar");
		}
		setVisibleColumns(visibledColumns.toArray());
		
		// Añadimos las columnas generadas personalizadas
		for (GeneratedColumn column : customGeneratedColumns) {
			Object id = column.getId();
			ColumnGenerator columnGenerator = column.getColumnGenerator();
			addGeneratedColumn(id, columnGenerator);
		}
		// Si el formulario está deshabilitado
		if (!formEnabled) {
			// Quitamos columnas editar, consultar y eliminar
			removeGeneratedColumn("Editar");
			removeGeneratedColumn("Consultar");
			removeGeneratedColumn("Eliminar");
			// Si se permite consultar
			if (allowConsulting) {
				// Añadimos la columna consultar
				addGeneratedColumn("Consultar", new EditColumnGenerator("Consultar"));
			}
		}
	}
	
    @SuppressWarnings("unchecked")
	@Override
    protected String formatPropertyValue(Object rowId, Object colId, Property<?> property) {
    	if (property.getType() == String.class) {
    		return recortarTexto((String)property.getValue());
    	}
    	else if (property.getType() == Date.class) {
			// Formato por defecto
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			try {
				// Obtienemos anotación StandardFormField del campo
				java.lang.reflect.Field field = Utils.getBeanField((Class<Bean>)rowId.getClass(), (String)colId);
				StandardFormField anotación = field.getAnnotation(StandardFormField.class);
				// Si la anotación es DATETIME cambiamos el formato
				if (anotación != null && anotación.type() == StandardFormField.Type.DATETIME) {
					df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return df.format((Date)property.getValue());
        }
        else if (property.getType().isEnum()) {
        	// Obtenemos los elementos del enumerado
			Object[] elementosEnum = property.getType().getEnumConstants();
			// Recorremos todos los elementos del enumerado
			for (Object elementoEnum: elementosEnum) {
				// Se obtiene anotación StandardFormEnum del elemento
				try {
					java.lang.reflect.Field elementoField =
							property.getType().getField(((Enum<?>)elementoEnum).name());
					StandardFormEnum anotación = elementoField.getAnnotation(StandardFormEnum.class);
					// Si tiene anotación StandardFormEnum informada
					if (anotación != null && anotación.value().length() != 0) {
						// Si el enumerado coincide con el valor la propiedad
						if (elementoEnum == property.getValue())
							// Se retorna el valor de la anotación
							return recortarTexto(anotación.value());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
        }
        else if (property.getType() == Boolean.class ||
        		property.getType() == Boolean.TYPE) {
        	if ((property.getValue() != null) && (Boolean)property.getValue() == true) {
        		return "Sí";
        	}
        	else {
        		return "No";
        	}
        }
        return super.formatPropertyValue(rowId, colId, property);
    }
    
    private String recortarTexto(String texto) {
    	if (texto.length() < 20) {
    		return texto;
    	}
    	return texto.substring(0, 18) + "...";
    }

    /**
	 * Adds a header column for a bean field
	 * @param beanField
	 */
	protected void addHeaderColumn(Field beanField) {
		// Obtenemos la cabecera pesonalizada
		String cabeceraPersonalizada = customColumnHeaders.get(beanField.getName());
		// Si existe cabecera personalizada
		if (cabeceraPersonalizada != null) {
			// La ponemos como cabecera
			setColumnHeader(beanField.getName(), cabeceraPersonalizada);
			return;
		}
		// Obtenemos la anotación StandarFormField del campo
		StandardFormField standardFormField = beanField.getAnnotation(StandardFormField.class);
		// Si la columna tiene caption
		if (standardFormField != null && standardFormField.caption().length() != 0)
			// Ponemos el caption como cabecera
			setColumnHeader(beanField.getName(), standardFormField.caption());
		else
			// Si no, ponemos el nombre del campo con la primera letra en mayúscula
			setColumnHeader(beanField.getName(), Utils.capitalizeFirstLetter(beanField.getName()));
	}
	
	/*
	 * Class for the edit column 
	 */
	public class EditColumnGenerator implements Table.ColumnGenerator {
		
		private String nameColumn;
		
		public EditColumnGenerator(String nameColumn) {
			this.nameColumn = nameColumn;
		}

		private static final long serialVersionUID = 1L;

		@Override
		public Object generateCell(Table source, final Object itemId, Object columnId) {
			Button button = new Button(nameColumn);
			button.setStyleName(BaseTheme.BUTTON_LINK);
			button.addClickListener(new ClickListener() {

				private static final long serialVersionUID = 1L;

				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public void buttonClick(ClickEvent event) {
					T elementoSeleccionado =
							(T) ((BeanItemContainer)getContainerDataSource()).getItem(itemId).getBean();
					showDetailListener.showDetailForm(elementoSeleccionado);
				}
			});
			return button;
		}
	}
	
	/*
	 * Class for the delete column
	 */
	public class DeleteColumnGenerator implements ColumnGenerator {

		private static final long serialVersionUID = 1L;

		@Override
		public Object generateCell(Table source, final Object itemId, Object columnId) {
			Button button = new Button("Eliminar");
			button.setStyleName(BaseTheme.BUTTON_LINK);
			button.addClickListener(new ClickListener() {
				
				private static final long serialVersionUID = 1L;
				
				@Override
				public void buttonClick(ClickEvent event) {
					ConfirmDialog.show(getUI(), "Confirmación", 
							"¿Está seguro de que desea eliminar el elemento?",
					        "Sí", "No", new ConfirmDialog.Listener() {
						private static final long serialVersionUID = 1L;

						public void onClose(ConfirmDialog dialog) {
			                if (dialog.isConfirmed()) {
			                	T elementoSeleccionado = container.getItem(itemId).getBean();
			                	try {
			                		beanUI.getBeanDAO().remove(elementoSeleccionado);
				                	Notification.show("Información",
				                			"El elemento se ha eliminado correctamente",
				                			Type.TRAY_NOTIFICATION);
				                	// Eliminamos el item de la tabla
				                	container.removeItem(itemId);
			                	} catch (Exception e) {
			    					Notification.show("No se ha podido eliminar el elemento",
			    							e.getMessage(), Type.ERROR_MESSAGE);
			    				}			                	
			                }
			            }
			        });
				}
			});
			return button;
		}
	}
}
