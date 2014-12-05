package es.vegamultimedia.standardform;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

import es.vegamultimedia.standardform.annotations.StandardForm;
import es.vegamultimedia.standardform.annotations.StandardFormEnum;
import es.vegamultimedia.standardform.annotations.StandardFormField;
import es.vegamultimedia.standardform.model.Bean;

@SuppressWarnings("serial")
public class ListForm<T extends Bean, K> extends Panel {
	
	/**
	 * Interface for loading the table elements
	 */
	public interface QueryListener<T extends Bean> {
		/**
		 * Called to obtain the table elements
		 */
		public abstract List<T> getElements();
	}
	
	protected QueryListener<T> queryListener;
	
	// BeanUI that created this standard list form
	protected BeanUI<T, K> beanUI;
	
	// StandardForm annotation bean
	StandardForm standardFormAnnotation;
	
	// Container del formulario
	protected BeanItemContainer<T> container;
	
	// Formulario
	protected FormLayout form;
	
	// Tabla del formulario
	protected Table table;
	
	// Nombre columna editar
	protected String nombreColumnaEditarConsultar;
	
	// Botón alta
	protected Button addButton;
	
	public ListForm(BeanUI<T, K> beanUI, QueryListener<T> queryListener) {
		this.beanUI = beanUI;
		this.queryListener = queryListener;
		
		// Columnas de la tabla
		List<String> visibledColumns;

		// Obtenemos la anotación StandardForm del bean
		standardFormAnnotation = beanUI.getBeanClass().getAnnotation(StandardForm.class);
		
		// Asignamos el título al panel
		setCaption(standardFormAnnotation.listViewName());
		
		// Añadimos estilo personalizado
		addStyleName("standard-form");

		// Si el bean NO tiene anotación StandardForm
		if (!(standardFormAnnotation instanceof StandardForm)) {
			Notification.show("Faltan metadatos",
					"El bean " + beanUI.getBeanClass().getSimpleName() +" no permite formulario de listado",
					Type.ERROR_MESSAGE);
			return;
		}
		
		// Creamos el formulario para albergar todos los campos del bean
		form = new FormLayout();
		setContent(form);
		
		// Layout
		VerticalLayout layout = new VerticalLayout();
		form.addComponent(layout);
		
		// Tabla
		table = new Table(){
			// Damos formato de sólo fecha a los campos de tipo Date 
		    @Override
		    protected String formatPropertyValue(Object rowId, Object colId, Property<?> property) {
		        if (property.getType() == Date.class) {
		            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		            return df.format((Date)property.getValue());
		        }
		        else if (property.getType().isEnum()) {
		        	// Obtenemos los elementos del enumerado
					Object[] elementosEnum = property.getType().getEnumConstants();
					// Recorremos todos los elementos del enumerado
					for (Object elementoEnum: elementosEnum) {
						// Se obtiene anotación StandardFormEnum del elemento
						try {
							java.lang.reflect.Field elementoField = property.getType().getField(elementoEnum.toString());
							StandardFormEnum anotación = elementoField.getAnnotation(StandardFormEnum.class);
							// Si tiene anotación StandardFormEnum informada
							if (anotación != null && anotación.value().length() != 0) {
								// Si el enumerado coincide con el valor la propiedad
								if (elementoEnum == property.getValue())
									// Se retorna el valor de la anotación
									return anotación.value();
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
		};
		table.setImmediate(true);
		// TODO Parametrizar la longitud de la página
		table.setPageLength(10);
		
		try {
			// Obtenemos los elementos
			loadData();
			
			// Si no se especifican las columnas visibles
			if (standardFormAnnotation.columns()[0].isEmpty()) {
				// Se muestran todas excepto el id
				visibledColumns = new ArrayList<String>();
				// Obtenemos todos los campos del bean
				Field[] fields = beanUI.getBeanClass().getDeclaredFields();
				// Recorremos los campos
				for (int i=0;i<fields.length;i++) {
					// Si no es el id
					if (!fields[i].getName().equals("id")) {
						// Añadimos el campo a las columnas visibles
						visibledColumns.add(fields[i].getName());
						// Añadimos la cabecera de la columna
						addHeaderColumn(fields[i]);
					}
				}
			}
			// Si se especifican las columnas visibles
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
			// Si se permite edición
			if (standardFormAnnotation.allowsEditing()) {
				nombreColumnaEditarConsultar = "Editar";
			}
			else {
				nombreColumnaEditarConsultar = "Consultar";
			}
			// Añadimos columna para editar o consultar
			table.addGeneratedColumn(nombreColumnaEditarConsultar,
					new EditColumnGenerator(nombreColumnaEditarConsultar));
			visibledColumns.add(nombreColumnaEditarConsultar);
			// Si se permite eliminar
			if (standardFormAnnotation.allowsDeleting()) {
				// Añadimos columna para eliminar
				table.addGeneratedColumn("Eliminar", new DeleteColumnGenerator());
				visibledColumns.add("Eliminar");
			}
			table.setVisibleColumns(visibledColumns.toArray());
			layout.addComponent(table);
		
			// Si se permite añadir
			if (standardFormAnnotation.allowsAdding()) {
				// Botón Alta
				addButton = new Button("Alta");
				addButton.addClickListener(new ClickListener(){
		
					private static final long serialVersionUID = 1L;
		
					@Override
					public void buttonClick(ClickEvent event) {
						showDetailForm(null);
					}
					
				});
				layout.addComponent(addButton);
			}
		} catch (Exception e) {
			Notification.show("No se pueden obtener los elementos",
					e.getMessage(), Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds the listener
	 */
	public void addQueryListener(QueryListener<T> queryListener) {
		this.queryListener = queryListener;
	}

	/**
	 * Adds a header column for a bean field
	 * @param beanField
	 */
	protected void addHeaderColumn(Field beanField) {
		// Obtenemos la anotación StandarFormField del campo
		StandardFormField standardFormField = beanField.getAnnotation(StandardFormField.class);
		// Si la columna tiene caption
		if (standardFormField != null && standardFormField.caption().length() != 0)
			// Ponemos el caption como cabecera
			table.setColumnHeader(beanField.getName(), standardFormField.caption());
		else
			// Si no, ponemos el nombre del campo con la primera letra en mayúscula
			table.setColumnHeader(beanField.getName(), Utils.capitalizeFirstLetter(beanField.getName()));
	}
	
	/**
	 * Gets every elements of this bean type using thee BeanDAO
	 */
	protected void loadData() {
		// TODO No obtener todos los elementos, sino trabajar con un cursor y paginar
		List<T> listaElementos;
		// Si hay escuchador queryListener
		if (queryListener != null) {
			// Llamamos a su método para obtener los elementos
			listaElementos = queryListener.getElements();
		}
		// En caso contrario
		else {
			// Obtenemos todos los elementos
			listaElementos = beanUI.getBeanDAO().getAllElements();
		}
		container = new BeanItemContainer<T>(beanUI.getBeanClass(), listaElementos);
		table.setContainerDataSource(container);
	}
	
	/**
	 * Shows the DetailForm for a bean inside the same component as this ListForm
	 * Before, it gets again the bean using the BeanDAO (just in case it has changed)
	 * @param element
	 */
	@SuppressWarnings("unchecked")
	protected void showDetailForm(T element) {
		Component vistaDetalle;
		try {
			// Si hay elemento
			if (element != null) {
				// Obtenemos el elemento de base de datos
				element = beanUI.beanDAO.get((K) Utils.getId(element));
			}
			vistaDetalle = beanUI.buildDetailForm(element);
			ComponentContainer contentPanel = (ComponentContainer)getParent();
			contentPanel.replaceComponent(this, vistaDetalle);
		} catch (Exception e) {
			Notification.show("No se puede crear el formulario de detalle",
					e.getMessage(), Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
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

				@Override
				public void buttonClick(ClickEvent event) {
					T elementoSeleccionado = container.getItem(itemId).getBean();
					showDetailForm(elementoSeleccionado);
				}
			});
			return button;
		}
	}
	
	/*
	 * Class for the delete column
	 */
	public class DeleteColumnGenerator implements Table.ColumnGenerator {

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

	public Table getTable() {
		return table;
	}
	
	/**
	 * Disables this Listform:
	 * Removes Edit and Delete columns, adds Consult column and hides the Add button
	 * @param allowConsulting if true, it shows the consult column. If false, it doesn't show the
	 * consult column
	 */
	public void disableForm(boolean allowConsulting) {
		// Quitamos columnas editar, consultar y eliminar
		table.removeGeneratedColumn("Editar");
		table.removeGeneratedColumn("Consultar");
		table.removeGeneratedColumn("Eliminar");
		// Si se permite consultar
		if (allowConsulting) {
			// Añadimos la columna consultar
			table.addGeneratedColumn("Consultar", new EditColumnGenerator("Consultar"));
		}
		// Ocultamos el botón añadir
		addButton.setVisible(false);
	}
	
	/**
	 * Hides the column with de id specified
	 * @param columnId It must be the fieldname of the bean
	 */
	public void hideVisibledColumn(String columnId) {
		ArrayList<Object> listVisibleColumns = new ArrayList<Object>();
		Field[] beanFields = Utils.getBeanFields(beanUI.getBeanClass());
		for (Object column : table.getVisibleColumns()) {
			// Si la columna visible no coincide con la que se quiere ocultar
			if (!columnId.equals(column)) {
				// Se añade a la nueva lista de columnas visibles
				listVisibleColumns.add(column);
				// Obtenemos el campo que coincide con el nombre de la columna
				for (Field beanField : beanFields) {
					if (beanField.getName().equals(column)) {
						// Lo añadimos a la cabecera
						addHeaderColumn(beanField);
						break;
					}
				}
			}
		}
		table.setVisibleColumns(listVisibleColumns.toArray());
	}
	
//	/**
//	 * Modify the name of the editColumn
//	 * @param nameColumn
//	 */
//	public void setNameEditColumn(String nameColumn) {
//		table.removeGeneratedColumn("Editar");
//		table.addGeneratedColumn(nameColumn, new EditColumnGenerator(nameColumn));
//	}
	
//	/**
//	 * Remove the delete column
//	 */
//	public void removeDeleteColumn() {
//		table.removeGeneratedColumn("Eliminar");
//	}
//	
//	/**
//	 * Hide the add button
//	 */
//	public void hideAddButton() {
//		addButton.setVisible(false);
//	}
}
