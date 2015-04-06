package es.vegamultimedia.standardform;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractField;
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
import es.vegamultimedia.standardform.DAO.BeanDAOException;
import es.vegamultimedia.standardform.DAO.SearchCriterion;
import es.vegamultimedia.standardform.DAO.SearchCriterion.SearchType;
import es.vegamultimedia.standardform.annotations.StandardForm;
import es.vegamultimedia.standardform.annotations.StandardFormField;
import es.vegamultimedia.standardform.components.PaginationBar;
import es.vegamultimedia.standardform.components.PaginationBar.PaginationListener;
import es.vegamultimedia.standardform.components.StandardTable;
import es.vegamultimedia.standardform.components.StandardTable.GeneratedColumn;
import es.vegamultimedia.standardform.model.Bean;

public class ListForm<BEAN extends Bean, KEY> extends CustomField<BEAN> {
	
	private static final long serialVersionUID = -8471432681552606031L;

	/**
	 * Interface for listening for a show event in a ListForm
	 */
	public interface ShowListListener<BEAN> {
		
		/**
		 * Called before creating the list
		 * @param listElements Showed elements in the list
		 * @param currentSearch Current used search for showing the list
		 */
		public abstract void beforeCreateList(List<BEAN> listElements, SearchCriterion[] currentSearch)
				throws BeanDAOException;
		
		/**
		 * Called after creating the list
		 * @param listElements Showed elements in the list
		 * @param currentSearch Current used search for showing the list
		 * @throws BeanDAOException 
		 */
		public abstract void afterCreateList(List<BEAN> listElements, SearchCriterion[] currentSearch)
				throws BeanDAOException;
	}
	
	/**
	 * Interface for listening for a delete event in a ListForm
	 */
	public interface DeleteListener<BEAN> {
		/**
		 * Called before deleting a bean
		 * @param bean Bean before deleting
		 * @throws BeanDAOException
		 */
		public abstract void beforeDelete(BEAN bean) throws BeanDAOException;
		
		/**
		 * Called after deleting a bean
		 * @param bean Deleted bean
		 * @throws BeanDAOException
		 */
		public abstract void afterDelete(BEAN bean) throws BeanDAOException;
	}

	// Show list Listener
    protected ShowListListener<BEAN> showListListener;
    
    // Delete listener
    protected DeleteListener<BEAN> deleteListener;
   
	// BeanUI that created this standard list form
	protected BeanUI<BEAN, KEY> beanUI;
	
	// StandardForm annotation bean
	protected StandardForm standardFormAnnotation;
	
	// Número de elementos a mostrar
	protected long numElements;
	
	// Lista de elementos
	protected List<BEAN> listElements;
	
	// Container del formulario
	protected BeanItemContainer<BEAN> container;
	
	// Indica si el formulario está deshabilitado
	protected boolean formEnabled;
	
	// Indica si se permite consulta
	protected boolean allowConsulting;
	
	// Columnas visibles personalizadas
	protected String[] customVisibledColumns;
	
	// Cabeceras de las columnas personalizadas
	protected HashMap<String, String> customColumnHeaders;
	
	// Panel principal
	protected Panel mainPanel;
	
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
	protected StandardTable<BEAN, KEY> table;
	
	// Layout para listado personalizado
	protected VerticalLayout listLayout;
	
	// Layout para los botones
	protected HorizontalLayout buttonsLayout;
	
	// Botón alta
	protected Button addButton;
	
	// Lista de columnas generadas
	protected ArrayList<GeneratedColumn> generatedColumns;

	public ListForm(BeanUI<BEAN, KEY> beanUI) throws BeanDAOException {
		this.beanUI = beanUI;
		
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
		
		// Panel principal
		mainPanel = new Panel();

		// Layout principal
		mainLayout = new VerticalLayout();
		mainPanel.setContent(mainLayout);
			
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
	}
	
	@Override
	protected Component initContent() {
		try {
			// Realizamos la búsqueda
			search();
			// Si no hay campos de búsqueda
			if (standardFormAnnotation.searchFields().length == 0) {
				// Ocultamos el panel de búsqueda
				searchPanel.setVisible(false);
			}
		} catch (BeanDAOException e) {
			e.printStackTrace();
			Notification.show("No se puede mostar el listado",
					e.getMessage(), Type.ERROR_MESSAGE);
		}
		return mainPanel;
	}

	@Override
	public Class<? extends BEAN> getType() {
		return beanUI.getBeanClass();
	}

    public void addButton(Component button) {
        buttonsLayout.addComponent(button);
    }
	
	/**
	 * Adds a show list listener
	 * TODO Note: In this moment only one listener is allowed!
	 * @param showListListener
	 */
    public void addShowListListener(ShowListListener<BEAN> showListListener) {
		this.showListListener = showListListener;
	}

	/**
	 * Adds a delete listener
	 * TODO Note: In this moment only one listener is allowed!
	 * @param deleteListener
	 */
    public void addDeleteListener(DeleteListener<BEAN> deleteListener) {
		this.deleteListener = deleteListener;
	}

	/**
	 * Adds the list to the listLayout:
	 * A custom component for each element or a table for all the elements by default
	 * @throws BeanDAOException 
	 */
	protected void createList() throws BeanDAOException {
		// Eliminamos los componentes del listLayout (por si no es la primera vez)
		listLayout.removeAllComponents();
		
		// Si existe escuchador showListListener, llamamos al método beforeCreateList() 
		if(showListListener != null) {
			showListListener.beforeCreateList(listElements, beanUI.getCurrentSearch());
		}
		
		// Pagination
		PaginationBar pagination = new PaginationBar(numElements, beanUI.getFirstElement(),
				beanUI.getElementsPerPage(), new PaginationListener() {
			@Override
			public void paginate(int firstElement) {
				try {
					// Actualizamos la página actual y realizamos una nueva búsqueda
					beanUI.setFirstElement(firstElement);
					search();
				} catch (BeanDAOException e) {
					Notification.show("Error",
							"No se puede mostrar el listado.\n" + e.getMessage(),
							Type.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}

			@Override
			public void setElementsPerPage(int elementsPerPage) {
				// Establecemos el nuevo número de elementos por página y realizamos una nueva búsqueda
				try {
					beanUI.setElementsPerPage(elementsPerPage);
					search();
				} catch (BeanDAOException e) {
					Notification.show("Error",
							"No se puede mostrar el listado.\n" + e.getMessage(),
							Type.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
		});
		listLayout.addComponent(pagination);
		
		// Si NO tiene customRowListComponent
		if (standardFormAnnotation.customRowListComponent().isEmpty()) {

			if (!formEnabled) {
				// Ocultamos el botón añadir
				if (addButton != null) {
					addButton.setVisible(false);
				}
			}
			container = new BeanItemContainer<BEAN>(beanUI.getBeanClass(), listElements);
			// Creamos una nueva tabla (pues Vaadin da problemas si reusamos la misma)
			table = new StandardTable<BEAN, KEY>(container, beanUI, formEnabled, allowConsulting, 
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
					for (BEAN element : listElements) {
						@SuppressWarnings("unchecked")
						CustomField<BEAN> rowComponent = (CustomField<BEAN>) constructor.newInstance(element);
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
		// Si existe escuchador showListListener, llamamos al método afterCreateList() 
		if (showListListener != null) {
			showListListener.afterCreateList(listElements, beanUI.getCurrentSearch());
		}
	}

	/**
	 * Creates the search panel
	 */
	protected void createSearchPanel() {
		// Creamos el panel de búsqueda
		searchPanel = new Panel("Buscar");
		searchPanel.addStyleName("standardform-searchpanel");
		// Añadimos el panel de búsqueda
		mainLayout.addComponent(searchPanel);
		// Creamos el layout de búsqueda
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchPanel.setContent(searchLayout);
		// Creamos el layout para los campos
		FormLayout searchFieldsLayout = new FormLayout();
		searchLayout.addComponent(searchFieldsLayout);
		// Layout para botón buscar y leyenda
		VerticalLayout buttonLayout = new VerticalLayout();
		searchLayout.addComponent(buttonLayout);
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
						searchFieldsLayout.addComponent(searchFields[i]);
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
				try {
					// Se muestra la primera página con la búsqueda actual
					beanUI.setFirstElement(0);
					updateSearchCriteria();
					search();
				} catch (BeanDAOException e) {
					e.printStackTrace();
					// TODO
				}
			}
		});
		buttonLayout.addComponent(searchButton);
		// Añadimos etiqueta de información
		searchInfo = new Label("", ContentMode.HTML);
		buttonLayout.addComponent(searchInfo);
		// Actualizamos el texto de información de búsqueda
		updateSearchInfo();
	}

	/**
	 * Gets a search field for the specified bean field and the value of its current searchCriterion
	 * @param beanField
	 * @param searchCriterion 
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
				BeanDAO<? extends Bean, KEY> beanDAO = Utils.getBeanDAO(tipoCampo, beanUI.getBeanDAO());
				// Obtenemos todos los elementos del bean anidado
				List<?> elementosBean = beanDAO.getElements(null, 0, 0);
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
			// Si hay criterios de búsqueda, los recorremos
			if (beanUI.getCurrentSearch() != null) {
				for (SearchCriterion search : beanUI.getCurrentSearch()) {
					// Si hay criterio para el campo actual
					if (search != null && search.getNameField().equals(beanField.getName())) {
						// Asignamos al campo el valor del criterio actual
						if (searchField instanceof AbstractSelect) {
							((AbstractSelect) searchField).setValue(search.getValueField());						
						}
						else if (searchField instanceof AbstractField) {
							((AbstractTextField) searchField).setValue((String) search.getValueField());
						}
						break;
					}
				}
			}
		}
		return searchField;
	}
	
	/**
	 * Updates the search criteria and the search label from the search fields values.
	 * This method is called when the user clicks on the search button
	 */
	protected void updateSearchCriteria() {
		int numCriteria = 0;
		SearchCriterion[] temporalCriteria = new SearchCriterion[searchFields.length];
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
						new SearchCriterion(nombreCampo, searchFields[i].getCaption(),
								valorCampo, SearchType.TEXT);
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
							new SearchCriterion(nombreCampo, searchFields[i].getCaption(),
									elementoSeleccionado, SearchType.ENUM);
					}
					// Si el campo es de tipo bean
					else if (Utils.isSubClass(searchFieldTypes[i], Bean.class)) {
						// Añadimos criterio de búsqueda de tipo BEAN
						temporalCriteria[numCriteria] = 
							new SearchCriterion(nombreCampo, searchFields[i].getCaption(),
									elementoSeleccionado, SearchType.BEAN);
					}
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
		// Guardamos los criterios de búsqueda en el BeanUI
		beanUI.setCurrentSearch(searchCriteria);
		// Actualizamos la información de búsqueda
		updateSearchInfo();
	}
	
	/**
	 * Updates the search info text from the search criteria
	 */
	protected void updateSearchInfo() {
		// Mostramos información de búsqueda
		SearchCriterion[] searchCriteria = beanUI.getCurrentSearch();
		String info;
		if (searchCriteria == null || searchCriteria.length == 0) {
			info = "Se muestran los elementos sin aplicar ningún filtro.";
		}
		else {
			info = "Se muestran los elementos que cumplen los siguientes filtros:";
			for (SearchCriterion searchCriterion : searchCriteria) {
				// Si el criterio es null
				if (searchCriterion == null) {
					// Pasamos al siguiente
					continue;
				}
				info +=  "<br/><b>" + searchCriterion.getCaptionField() + "</b>";
				switch (searchCriterion.getTypeCriteria()) {
				case TEXT:
					info += " contiene el texto ";
					break;
				case ENUM: case BEAN:
					info += " es igual a ";
				}
				info += "<b>\"" + searchCriterion.getValueField() + "\"</b>.";
			}
		}
		searchInfo.setValue(info);
	}
	
	/**
	 * Makes a search with the current search criteria and the current page.
	 * Updates the list with the found elements
	 * @throws BeanDAOException 
	 */
	protected void search() throws BeanDAOException {
		// Obtenemos el número de elementos con el BeanDAO
		numElements = beanUI.getBeanDAO().getcountElements(beanUI.getCurrentSearch());
		// Obtenemos los elementos haciendo la búsqueda en el BeanDAO
		listElements = beanUI.getBeanDAO().getElements(
				beanUI.getCurrentSearch(), 
				beanUI.getFirstElement(),
				beanUI.getElementsPerPage());
		// Creamos un nuevo listado
		createList();
	}

	/**
	 * Shows the DetailForm for a bean inside the same component as this ListForm
	 * Before, it gets again the bean using the BeanDAO (just in case it has changed)
	 * @param element
	 */
	@SuppressWarnings("unchecked")
	public void showDetailForm(BEAN element) {
		Component vistaDetalle;
		try {
			// Si hay elemento
			if (element != null) {
				// Obtenemos el elemento de base de datos
				element = beanUI.beanDAO.get((KEY) Utils.getId(element));
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
	 * Deletes a bean when the user orders it
	 * @param bean
	 */
	public void deleteBean(final BEAN bean) {
		ConfirmDialog.show(getUI(), "Confirmación", 
				"¿Está seguro de que desea eliminar el elemento?",
		        "Sí", "No", new ConfirmDialog.Listener() {
			private static final long serialVersionUID = 1L;

			public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                	try {
                		// Si hay deleteListener llamamos a beforeDelete()
                		if (deleteListener != null) {
                			deleteListener.beforeDelete(bean);
                		}
                		// Eliminamos el bean en la base de datos
                		beanUI.getBeanDAO().remove(bean);
                		// Si hay deleteListener llamamos a afterDelete()
                		if (deleteListener != null) {
                			deleteListener.afterDelete(bean);
                		}
	                	Notification.show("Información",
	                			"El elemento se ha eliminado correctamente",
	                			Type.TRAY_NOTIFICATION);
	                	// Eliminamos el bean de la tabla
	                	container.removeItem(bean);
                	} catch (Exception e) {
    					Notification.show("No se ha podido eliminar el elemento",
    							e.getMessage(), Type.ERROR_MESSAGE);
    				}			                	
                }
            }
        });
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
	 * Hides the search panel.
	 */
	public void hideSearchPanel() {
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
     * Refreshes the list creating a new one.
     * You must call this method only if you have made a change in the list
     * @throws BeanDAOException 
     */
    public void refreshList() throws BeanDAOException {
    	// Realizamos una nueva búsqueda y se genera de nuevo el listado
    	search();
    }
}