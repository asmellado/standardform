package es.vegamultimedia.standardform;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import es.vegamultimedia.standardform.DAO.BeanDAO;
import es.vegamultimedia.standardform.DAO.SearchCriterion;
import es.vegamultimedia.standardform.DAO.SearchCriterion.SearchType;
import es.vegamultimedia.standardform.annotations.StandardForm;
import es.vegamultimedia.standardform.annotations.StandardFormField;
import es.vegamultimedia.standardform.components.StandardTable;
import es.vegamultimedia.standardform.components.StandardTable.GeneratedColumn;
import es.vegamultimedia.standardform.components.StandardTable.ShowDetailListener;
import es.vegamultimedia.standardform.model.Bean;

public class ListForm<T extends Bean, K> extends Panel implements ShowDetailListener<T> {
	
	private static final long serialVersionUID = -8471432681552606031L;

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
     * Event BeforeCreateList
     */
    public interface BeforeCreateList<T extends Bean> {
        public abstract void beforeCreateList(List<T> elements);
    }
	
	protected BeforeCreateList<T> beforeCreateList; 
	
	protected QueryListener<T> queryListener;
	
	// BeanUI that created this standard list form
	protected BeanUI<T, K> beanUI;
	
	// StandardForm annotation bean
	protected StandardForm standardFormAnnotation;
	
	// Lista de elementos
	protected List<T> listElements;
	
	// Container del formulario
	protected BeanItemContainer<T> container;
	
	// Indica si el formulario está deshabilitado
	protected boolean formEnabled;
	
	// Indica si se permite consulta
	protected boolean allowConsulting;
	
	// Columnas visibles personalizadas
	protected String[] customVisibledColumns;
	
	// Cabeceras de las columnas personalizadas
	protected HashMap<String, String> customColumnHeaders;
	
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
	protected StandardTable<T, K> table;
	
	// Layout para listado personalizado
	protected VerticalLayout listLayout;
	
	// Layout para los botones
	protected HorizontalLayout buttonsLayout;
	
	// Botón alta
	protected Button addButton;
	
	// Lista de columnas generadas
	protected ArrayList<GeneratedColumn> generatedColumns;

	public ListForm(BeanUI<T, K> beanUI, QueryListener<T> queryListener) {
		this.beanUI = beanUI;
		this.queryListener = queryListener;
		this.beforeCreateList = null;
		
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
		
		// Inicializamos atributos
		formEnabled = true;
		allowConsulting = true;
		customVisibledColumns = null;
		customColumnHeaders = new HashMap<String, String>();
		generatedColumns = new ArrayList<GeneratedColumn>();

		// Layout principal
		mainLayout = new VerticalLayout();
		setContent(mainLayout);
			
		// Creamos el panel de busqueda
		createSearchPanel();
		
		buttonsLayout = new HorizontalLayout();
		
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
			buttonsLayout.addComponent(addButton);
		}
        mainLayout.addComponent(buttonsLayout);
        
        // Creamos el listLayout
     	listLayout = new VerticalLayout();
     	mainLayout.addComponent(listLayout);
        
        // Si es búsqueda inmediata 
 		if (standardFormAnnotation.immediateSearch()) {
 			// Mostramos todos los elementos
 			showAllElements();
 			// Si no hay campos de búsqueda
 			if (standardFormAnnotation.searchFields().length == 0) {
 				// Ocultamos el panel de búsqueda
 				searchPanel.setVisible(false);
 			}
 		}
	}
	
	/**
	 * Adds the listener
	 */
    public void addQueryListener(QueryListener<T> queryListener) {
        this.queryListener = queryListener;
    }

    public void addButton(Button b) {
        buttonsLayout.addComponent(b);
    }
    
    public void setBeforeCreateList(BeforeCreateList<T> beforeCreateList) {
        this.beforeCreateList = beforeCreateList;
    }
	
	

	/**
	 * Adds the list to the listLayout:
	 * A custom component for each element or a table for all the elements by default
	 */
	protected void createList() {
		// Eliminamos los componentes del listLayout (por si no es la primera vez)
		listLayout.removeAllComponents();
		if(beforeCreateList!=null) {
		    beforeCreateList.beforeCreateList(listElements);
		}
		// Si NO tiene customRowListComponent
		if (standardFormAnnotation.customRowListComponent().isEmpty()) {

			if (!formEnabled) {
				// Ocultamos el botón añadir
				if (addButton != null) {
					addButton.setVisible(false);
				}
			}
			container = new BeanItemContainer<T>(beanUI.getBeanClass(), listElements);
			// Creamos una nueva tabla (pues Vaadin da problemas si reusamos la misma)
			table = new StandardTable<T, K>(container, beanUI, formEnabled, allowConsulting, 
					customVisibledColumns, customColumnHeaders, generatedColumns, this);
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
			private static final long serialVersionUID = 6657565320103922397L;

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
	@Override
	@SuppressWarnings("unchecked")
	public void showDetailForm(T element) {
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
	
	/**
	 * Disables this Listform:
	 * Removes Edit and Delete columns, adds Consult column and hides the Add button.
	 * Note: You must call the method refreshList() after calling this method.
	 * @param allowConsulting if true, it shows the consult column. If false, it doesn't show the
	 * consult column
	 */
	public void disableForm(boolean allowConsulting) {
		formEnabled = false;
		this.allowConsulting = allowConsulting;
	}
	
	/**
	 * Forbides consulting elements in the table: it doesn't show the edit or consult column.
	 */
	public void forbidConsulting() {
		allowConsulting = false;
	}
	
	/**
	 * Shows all the elements in the list
	 */
	protected void showAllElements() {
		try {
			listElements = loadData();
		} catch (Exception e) {
			Notification.show("No se pueden obtener los elementos",
					e.getMessage(), Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
		// Creamos la lista
		createList();
	}
	
	/**
	 * Hides the search panel.
	 */
	public void hideSearchPanel() {
		// Si no es búsqueda inmediata
		if (!standardFormAnnotation.immediateSearch()) {
			// Obtenemos todos los elementos automáticamente
			showAllElements();
		}
        // Ocultamos el panel de búsqueda
		searchPanel.setVisible(false);
	}

	
	/**
	 * Adds a generated column to the table.
	 * @param id
	 * @param generatedColumn
	 */
	public void addGeneratedColumn(Object id, ColumnGenerator generatedColumn) {
		generatedColumns.add(new GeneratedColumn(id, generatedColumn));
	}

	/**
	 * Sets custom visibles columns.
	 * @param customVisibledColumns
	 */
	public void setCustomVisibledColumns(String[] customVisibledColumns) {
		this.customVisibledColumns = customVisibledColumns;
	}
	
	/**
     * Sets the column header for the specified column.
     */
    public void setColumnHeader(String propertyId, String header) {
    	customColumnHeaders.put(propertyId, header);
    }
    
    /**
     * Refreshes the list. You must call this method if there has been a change in the list
     */
    public void refreshList() {
		// Creamos un nuevo listado
		createList();
    }
}
