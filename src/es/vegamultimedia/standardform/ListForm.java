package es.vegamultimedia.standardform;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

import es.vegamultimedia.standardform.DAO.BeanDAO;
import es.vegamultimedia.standardform.DAO.SearchCriterion;
import es.vegamultimedia.standardform.DAO.SearchCriterion.SearchType;
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
	
	/**
	 * Generated column of the list table
	 */
	public class GeneratedColumn {
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
	
	protected QueryListener<T> queryListener;
	
	// BeanUI that created this standard list form
	protected BeanUI<T, K> beanUI;
	
	// StandardForm annotation bean
	protected StandardForm standardFormAnnotation;
	
	// Lista de elementos
	protected List<T> listElements;
	
	// Container del formulario
	protected BeanItemContainer<T> container;
	
	// Layout principal
	protected VerticalLayout mainLayout;
	
	// Panel de búsqueda
	protected Panel searchPanel;
	
	// Campos de búsqueda
	protected Component[] searchFields;
	
	// Tipos de los campos de búsqueda
	@SuppressWarnings("rawtypes")
	protected Class[] searchFieldTypes;
	
	// Etiqueta de información de búsqueda
	protected Label searchInfo;
	
	// Tabla para listado por defecto
	protected Table table;
	
	// Layout para listado personalizado
	protected VerticalLayout listLayout;
	
	// Nombre columna editar
	protected String nombreColumnaEditarConsultar;
	
	// Botón alta
	protected Button addButton;
	
	// Lista de columnas generadas
	protected ArrayList<GeneratedColumn> generatedColumns;
	
	public ListForm(BeanUI<T, K> beanUI, QueryListener<T> queryListener) {
		this.beanUI = beanUI;
		this.queryListener = queryListener;
		
		// Obtenemos la anotación StandardForm del bean
		standardFormAnnotation = beanUI.getBeanClass().getAnnotation(StandardForm.class);
		
		// Asignamos el título al panel
		setCaption(standardFormAnnotation.listViewName());
		
		// Añadimos estilo personalizado
		addStyleName("standardform");

		// Si el bean NO tiene anotación StandardForm
		if (!(standardFormAnnotation instanceof StandardForm)) {
			Notification.show("Faltan metadatos",
					"El bean " + beanUI.getBeanClass().getSimpleName() +" no permite formulario de listado",
					Type.ERROR_MESSAGE);
			return;
		}
		
		// Inicializamos generatedColumns
		generatedColumns = new ArrayList<GeneratedColumn>();

		// Layout principal
		mainLayout = new VerticalLayout();
		setContent(mainLayout);
			
		// Creamos el panel de busqueda
		createSearchPanel();
		
		// Creamos el listLayout
		listLayout = new VerticalLayout();
		mainLayout.addComponent(listLayout);
		
		// Si no hay panel de búsqueda
		if (searchPanel.getContent() == null) {
			try {
				// Obtenemos los elementos
				listElements = loadData();
			} catch (Exception e) {
				Notification.show("No se pueden obtener los elementos",
						e.getMessage(), Type.ERROR_MESSAGE);
				e.printStackTrace();
				return;
			}
		}
		
		// Creamos el listado
		createList();
		
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
			mainLayout.addComponent(addButton);
		}
	}
	
	/**
	 * Adds the listener
	 */
	public void addQueryListener(QueryListener<T> queryListener) {
		this.queryListener = queryListener;
	}

	/**
	 * Adds the list to the listLayout:
	 * A custom component for each element or a table for all the elements by default
	 */
	protected void createList() {
		// Si NO tiene customRowListComponent
		if (standardFormAnnotation.customRowListComponent().isEmpty()) {
			// Columnas de la tabla
			List<String> visibledColumns;
			// Creamos una tabla para mostrar los elementos
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
			
			container = new BeanItemContainer<T>(beanUI.getBeanClass(), listElements);
			table.setContainerDataSource(container);
			
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
			
			// Añadimos las columnas generadas personalizadas
			for (GeneratedColumn column : generatedColumns) {
				Object id = column.getId();
				ColumnGenerator columnGenerator = column.getColumnGenerator();
				table.addGeneratedColumn(id, columnGenerator);
			}
			
			listLayout.addComponent(table);
		}
		// Si tiene customRowListComponent
		else {
			try {
				// Obtenemos el customComponent para mostrar cada elemento
				// La anotación es el nombre de la clase del beanDAO
				String customClassName = standardFormAnnotation.customRowListComponent();
				// Obtenemos la clase del Componente personalizado
				Class<?> customComponentClass = Class.forName(customClassName);
				// Obtenemos el constructor con el parámetro del tipo del bean
				Constructor<?> constructor = customComponentClass.getConstructor(beanUI.getBeanClass());
				if (listElements != null) {
					for (T element : listElements) {
						@SuppressWarnings("unchecked")
						CustomField<T> rowComponent = (CustomField<T>) constructor.newInstance(element);
						listLayout.addComponent(rowComponent);
					}
				}
			} catch (ClassNotFoundException e) {
				Notification.show("Error",
					"No está implementado el componente personalizado para mostrar el listado de"
						+ " los elementos del bean " + beanUI.getBeanClass().getSimpleName(),
					Type.ERROR_MESSAGE);
				e.printStackTrace();
			} catch (NoSuchMethodException | SecurityException e) {
				Notification.show("Error",
					"No está implementado el constructor del componente personalizado para mostrar "
						+ "los elementos del bean " + beanUI.getBeanClass().getSimpleName(),
						Type.ERROR_MESSAGE);
				e.printStackTrace();
			} catch (Exception e) {
				Notification.show("Error",
					"No se puede crear el componente personalizado para mostrar "
						+ "los elementos del bean " + beanUI.getBeanClass().getSimpleName(),
					Type.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates the search panel
	 */
	protected void createSearchPanel() {
		// Creamos el panel de búsqueda
		searchPanel = new Panel("Buscar");
		// Si no hay campos de búsqueda
		if (standardFormAnnotation.searchFields()[0].isEmpty()) {
			// No creamos panel de búsqueda
			return;
		}
		// Añadimos el panel de búsqueda
		mainLayout.addComponent(searchPanel);
		// Creamos el layour de búsqueda
		FormLayout searchLayout = new FormLayout();
		searchPanel.setContent(searchLayout);
		// Inicializamos los arrays searchFields y searchFieldTypes
		searchFields = new Component[standardFormAnnotation.searchFields().length];
		searchFieldTypes = new Class[standardFormAnnotation.searchFields().length];
		// Obtenemos todos los campos del bean
		Field[] beanFields = Utils.getBeanFields(beanUI.getBeanClass());
		// Recorremos los nombres de los campos de búsqueda
		for (int i=0; i<standardFormAnnotation.searchFields().length; i++) {
			String fieldName = standardFormAnnotation.searchFields()[i];
			// Obtenemos el campo que coincide con el nombre
			for (Field beanField : beanFields) {
				if (beanField.getName().equals(fieldName)) {
					searchFieldTypes[i] = beanField.getType();
					searchFields[i] = getSearchField(beanField);
					// Comprobamos por seguridad que el campo no es null
					if (searchFields[i] != null) {
						// Lo añadimos al layout de búsqueda
						searchLayout.addComponent(searchFields[i]);
					}
					break;
				}
			}
		}
		// Añadimos el botón para buscar
		Button searchButton = new Button("Buscar");
		searchButton.setClickShortcut(KeyCode.ENTER);
		searchButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				search();
			}
		});
		searchLayout.addComponent(searchButton);
		// Añadimos etiqueta de información
		searchInfo = new Label("No se ha realizado ninguna búsqueda", ContentMode.HTML);
		searchLayout.addComponent(searchInfo);
	}

	/**
	 * Gets a search field for the specified bean field
	 * @param beanField
	 * @return
	 */
	protected Component getSearchField(Field beanField) {
		Component searchField = null;
		// Obtenemos la anotación StandardFormField
		StandardFormField standardFormField =
				beanField.getAnnotation(StandardFormField.class);
		// Obtenemos el caption
		String caption = Utils.getCaption(beanField, standardFormField);
		@SuppressWarnings("rawtypes")
		Class tipoCampo = beanField.getType();
		// Si el campo es de tipo String
		if (tipoCampo == String.class) {
			// Creamos un campo de texto
			searchField = new TextField(caption);
			// Asignamos longitud máxima
			int maxLength = Utils.getMaxLengthField(beanField);
			((AbstractTextField) searchField).setMaxLength(maxLength);
		}
		// Si el campo es de tipo enunerado
		else if (tipoCampo.isEnum()) {
			// Obtenemos los elementos del enumerado
			Object[] elementosEnum = tipoCampo.getEnumConstants();
			// Creamos un combo box con los elementos
			searchField = new ComboBox(caption, Arrays.asList(elementosEnum));
			// Asignamos los captions del enum select
			Utils.setCaptionsEnumSelect((AbstractSelect) searchField, tipoCampo, elementosEnum);
		}
		// Si el campo es de tipo Bean
		else if (Utils.isSubClass(tipoCampo, Bean.class)) {
			try {
				// Obtenemos una instancia del BeanDAO anidado
				@SuppressWarnings("unchecked")
				BeanDAO<? extends Bean, K> beanDAO = Utils.getBeanDAO(tipoCampo, beanUI.getBeanDAO());
				// Obtenemos todos los elementos del bean anidado
				List<?> elementosBean = beanDAO.getAllElements();
				// Creamos un combo box con los elementos
				searchField = new ComboBox(caption, elementosBean);
			} catch (Exception ignorada) {
				ignorada.printStackTrace();
			}
		}
		// TODO Falta implementar campos de tipo fecha y booleanos
		// Si hay campo
		if (searchField != null) {
			// Asignamos el nombre del campo como id
			searchField.setId(beanField.getName());
			searchField.addStyleName("standardform-field");
		}
		return searchField;
	}
	
	/**
	 * Makes the search. This method is called when the user clicks on the search button
	 */
	protected void search() {
		int numCriteria = 0;
		SearchCriterion[] temporalCriteria = new SearchCriterion[searchFields.length];
		String[] campoCriteria = new String[searchFields.length];
		String[] valorCriteria = new String[searchFields.length];
		// Recorremos los campos de búsqueda
		for (int i=0; i<searchFields.length; i++) {
			// Comprobamos por seguridad que el campo no es null
			if (searchFields[i] == null) {
				break;
			}
			String nombreCampo = searchFields[i].getId();
			// Si es un campo de texto
			if (searchFields[i] instanceof TextField) {
				// Obtenemos el valor introducido en el campo
				String valorCampo = ((TextField)searchFields[i]).getValue().trim();
				// Si hay algún valor
				if (!valorCampo.isEmpty()) {
					// Añadimos criterio de búsqueda de tipo TEXT
					temporalCriteria[numCriteria] =
						new SearchCriterion(nombreCampo, valorCampo, SearchType.TEXT);
					campoCriteria[numCriteria] = searchFields[i].getCaption();
					valorCriteria[numCriteria] = valorCampo;
					numCriteria++;
				}
			}
			// Si es un ComboBox
			else if (searchFields[i] instanceof ComboBox) {
				// Obtenemos el elemento seleccionado
				Object elementoSeleccionado = ((ComboBox)searchFields[i]).getValue();
				// Si hay algún elemento seleccionado
				if (elementoSeleccionado != null) {
					// Si el campo es de tipo enum
					if (searchFieldTypes[i].isEnum()) {
						// Añadimos criterio de búsqueda de tipo ENUM
						temporalCriteria[numCriteria] = 
							new SearchCriterion(nombreCampo, elementoSeleccionado, SearchType.ENUM);
					}
					// Si el campo es de tipo bean
					else if (Utils.isSubClass(searchFieldTypes[i], Bean.class)) {
						// Añadimos criterio de búsqueda de tipo BEAN
						temporalCriteria[numCriteria] = 
							new SearchCriterion(nombreCampo, elementoSeleccionado, SearchType.BEAN);
					}
					campoCriteria[numCriteria] = searchFields[i].getCaption();
					valorCriteria[numCriteria] =
						((ComboBox)searchFields[i]).getItemCaption(elementoSeleccionado);
					numCriteria++;
				}
			}
		}
		// Creamos el array definitivo
		SearchCriterion[] searchCriteria = new SearchCriterion[numCriteria];
		for (int i=0,j=0; i<temporalCriteria.length; i++) {
			if (temporalCriteria[i] != null) {
				searchCriteria[j++] = temporalCriteria[i];
			}
		}
		// Hacemos la búsqueda
		listElements = beanUI.getBeanDAO().getElements(searchCriteria);
		// Eliminamos los componentes del listLayout
		listLayout.removeAllComponents();
		// Creamos un nuevo listado
		createList();
		
		// Mostramos información de búsqueda
		String info;
		if (numCriteria == 0) {
			info = "Se muestran los elementos sin aplicar ningún filtro.";
		}
		else {
			info = "Se muestran los elementos que cumplen los siguientes filtros:";
			for (int i=0; i<temporalCriteria.length; i++) {
				// Si el criterio es null
				if (temporalCriteria[i] == null) {
					// Pasamos al siguiente
					continue;
				}
				info +=  "<br/><b>" + campoCriteria[i] + "</b>";
				switch (temporalCriteria[i].getTypeCriteria()) {
				case TEXT:
					info += " contiene el texto ";
					break;
				case ENUM: case BEAN:
					info += " es igual a ";
				}
				info += "<b>\"" + valorCriteria[i] + "\"</b>.";
			}
		}
		searchInfo.setValue(info);
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
	protected List<T> loadData() {
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
		return listaElementos;
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
		if (addButton != null) {
			addButton.setVisible(false);
		}
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
	
	/**
	 * Hides the search panel
	 */
	public void hideSearchPanel() {
		searchPanel.setVisible(false);
	}
	
	/**
	 * Adds a generated column to the table
	 * @param id
	 * @param generatedColumn
	 */
	public void addGeneratedColumn(Object id, ColumnGenerator generatedColumn) {
		generatedColumns.add(new GeneratedColumn(id, generatedColumn));
	}
}
