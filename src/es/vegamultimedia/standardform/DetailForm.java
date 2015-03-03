package es.vegamultimedia.standardform;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.query.Query;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.BeanValidator;
import com.vaadin.data.validator.NullValidator;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

import es.vegamultimedia.standardform.DAO.BeanDAO;
import es.vegamultimedia.standardform.DAO.BeanMongoDAO;
import es.vegamultimedia.standardform.annotations.StandardForm;
import es.vegamultimedia.standardform.annotations.StandardForm.DAOType;
import es.vegamultimedia.standardform.annotations.StandardFormField;
import es.vegamultimedia.standardform.components.FileComponent;
import es.vegamultimedia.standardform.components.FileComponent.FileUploader;
import es.vegamultimedia.standardform.components.SearchField;
import es.vegamultimedia.standardform.components.StandardTable;
import es.vegamultimedia.standardform.model.Bean;
import es.vegamultimedia.standardform.model.BeanMongo;
import es.vegamultimedia.standardform.model.File;
import es.vegamultimedia.standardform.model.Image;

@SuppressWarnings("serial")
public class DetailForm<T extends Bean, K> extends Panel {
	
	/**
	 * Interface for listening for a event in a DetailForm
	 */
	public interface SaveListener{
		/**
		 * Called before saving the bean
		 */
		public abstract void beforeSave(Bean bean, boolean insertMode) throws SaveException;
		/**
		 * Called after saving the bean
		 */
		public abstract void afterSave(Bean bean, boolean insertMode);
	}
	
	private SaveListener saveListener;
	
	// BeanUI that created this standard detail form
	protected BeanUI<T, K> beanUI;
	
	// Mapa de binders del formulario.
	// La clave para el binder principal es "" y para los binders de los bean anidados
	// el prefijo del campo
	protected HashMap<String, BeanFieldGroup<?>> binderMap;
	
	// Bean actual
	protected T bean;
	
	// Formulario
	protected FormLayout form;
	
	// Campos de Vaadin del formulario
	protected Component[] formFields;
	
	// Indica que estamos en modo alta
	protected boolean insertMode;
	
	// Layout para los botones
	protected HorizontalLayout buttonsLayout;
	
	// Botón guardar
	protected Button saveButton;
	
	// Botón cancelar
	protected Button cancelButton;
	
	/**
	 * Create a complete DetailForm for updating an existing bean or for inserting a new bean
	 * with OK and Cancel buttons
	 * @param currentBeanUI
	 * @param currentBean Existing bean o null for a new bean
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public DetailForm(BeanUI<T, K> currentBeanUI, T currentBean)
			throws InstantiationException, IllegalAccessException {
		this(currentBeanUI, currentBean, true);
	}
	
	/**
	 * Create a DetailForm for updating an existing bean or for inserting a new bean
	 * @param currentBeanUI
	 * @param currentBean Existing bean o null for a new bean
	 * @param withOKAndCancelButtons if false, the form doesn't have OK neither cancel button
	 * It may be used for building custom forms
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public DetailForm(BeanUI<T, K> currentBeanUI, T currentBean, boolean withOKAndCancelButtons)
			throws InstantiationException, IllegalAccessException {
		beanUI = currentBeanUI;
		bean = currentBean;
		
		try {
			// Inicializamos el elemento actual
			if (bean == null) {
				insertMode = true;
				bean = (T) newBean(beanUI.getBeanClass());
			}
			
			// Creamos el mapa de binders
			binderMap = new HashMap<String, BeanFieldGroup<?>>();
			// Creamos el binder principal para el bean
			BeanFieldGroup<T> binder = new BeanFieldGroup<T>(beanUI.getBeanClass());
			binder.setItemDataSource(bean);
			// Añadimos el binder principal al mapa de binders
			binderMap.put("", binder);
			
			// Creamos el formulario para albergar todos los campos del bean
			form = new FormLayout();
			setContent(form);
			
			// Obtenemos la anotación StandardForm del elementoActual
			StandardForm standardForm = bean.getClass().getAnnotation(StandardForm.class);
			
			// Asignamos el título al panel
			setCaption(standardForm.detailViewName());
			
			// Añadimos estilo personalizado
			addStyleName("standardform");
			
			// Obtenemos los campos del formulario
			formFields = getFormFields(bean, binder, "");
			
			buttonsLayout = new HorizontalLayout();
			form.addComponent(buttonsLayout);
			
			// Si se desean botones aceptar y cancelar
			if (withOKAndCancelButtons) {
				// Si estamos en alta o se permite edición
				if (insertMode || standardForm.allowsEditing()) {
					// Añadimos el botón guardar
					saveButton = new Button("Guardar");
					saveButton.setId("saveButton");
					saveButton.setClickShortcut(KeyCode.ENTER);
					saveButton.addClickListener(new ClickListener(){
						@Override
						public void buttonClick(ClickEvent event) {
							save(event);
						}
					});
					buttonsLayout.addComponent(saveButton);
				}
				
				cancelButton = new Button("Cancelar");
				cancelButton.setId("cancelButton");
				cancelButton.addClickListener(new ClickListener(){
					@Override
					public void buttonClick(ClickEvent event) {
						showListForm();
					}
				});
				buttonsLayout.addComponent(cancelButton);
			}
		} catch (Exception e) {
			Notification.show("Se ha producido un error", e.getMessage(), Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds the SaveListener
	 */
	public void addSaveListener(SaveListener saveListener) {
		this.saveListener = saveListener;
	}
	
	/**
	 * Return a new bean instance. Its nested beans are instantiated recursively
	 * @param beanClass
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	@SuppressWarnings("unchecked")
	protected Bean newBean(Class<? extends Bean> beanClass)
			throws InstantiationException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException, 
			NoSuchMethodException, SecurityException {
		Bean bean = beanClass.newInstance();
		// Recorremos todos los campos
		for (java.lang.reflect.Field fieldBean : beanClass.getDeclaredFields()) {
			// Si el campo es un bean anidado
			if (Utils.isSubClass(fieldBean.getType(), Bean.class)) {
				// Comprobamos que no es de la misma clase que el bean actual para evitar bucle infinito
				if (beanClass != fieldBean.getType()) {
					// Creamos el objeto del bean anidado
					Bean nestedBean = newBean((Class<? extends Bean>) fieldBean.getType());
					// Obtenemos el método "Set" del campo actual
					Method setMethod = Utils.getSetMethod(bean.getClass(), fieldBean);
					// Llamamos al método set para asignar el bean anidado vacío
					setMethod.invoke(bean, nestedBean);
				}
			}
		}
		return bean;
	}

	/**
	 * Returns an array of Vaadin fields from the argument currentBean.
	 * This method is recursive for nested beans
	 * @param currentBean
	 * @param currentBinder 
	 * @param prefixParentBean
	 * @return
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Component[] getFormFields(Bean currentBean,
			BeanFieldGroup<?> currentBinder, String prefixParentBean)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, ClassNotFoundException,
			InstantiationException {
		
		// Obtenemos la anotación StandardForm del elementoActual
		StandardForm standardForm = currentBean.getClass().getAnnotation(StandardForm.class);
		
		// Obtenemos los campos del bean elementoActual
		java.lang.reflect.Field[] currentBeanFields = Utils.getBeanFields(currentBean.getClass());
		
		// Creamos el array de campos del formulario con el número de campos del bean actual
		Component[] currentFields = new Component[currentBeanFields.length];
		
		// Recorremos los campos del bean actual
		for (int i=0;i<currentBeanFields.length;i++) {
			String caption;
			// Obtenemos la anotación StandardFormField
			StandardFormField standardFormField = currentBeanFields[i].getAnnotation(StandardFormField.class);
			// Obtenemos el tipo de campo en función de los metadatos
			StandardFormField.Type tipo = getTypeFormField(standardForm, currentBeanFields[i], standardFormField);
			// Si se ha encontrado un tipo
			if (tipo != null) {
				// Obtenemos el caption
				caption = Utils.getCaption(currentBeanFields[i], standardFormField);
				// Comprobamos el tipo de campo
				switch (tipo) {
				// Si es un campo de selección
				case COMBO_BOX:
				case OPTION_GROUP:
				case MULTIPLE_SELECTION:
					// En este caso tenemos que crear el campo a mano
					// con todas las opciones y seleccionar el elemento actual
					currentFields[i] = getSelectField(currentBean, prefixParentBean, currentBeanFields[i],
							tipo, caption);
					currentBinder.bind((Field) currentFields[i], currentBeanFields[i].getName());
					break;
				// Si es un área de texto
				case TEXT_AREA:
					// Creamos el campo a mano y lo añadimos al binder
					currentFields[i] = new TextArea(caption);
					currentBinder.bind((Field) currentFields[i], currentBeanFields[i].getName());
					break;
				// Si es un campo "normal"
				case TEXT_FIELD:
				case NUM_FIELD:
				case CHECK_BOX:
					// Construimos el campo directamente con el binder
					currentFields[i] = currentBinder.buildAndBind(caption,
							currentBeanFields[i].getName());
					// Si es un campo de texto TextField, especificamos la longitud máxima (Vaadin no lo hace)
					if (currentFields[i] instanceof TextField) {
						// TODO Parametrizar locale
						((TextField)currentFields[i]).setLocale(new Locale("es", "ES"));
						// Asignamos el tamaño máximo del campo
						int maxLength = Utils.getMaxLengthField(currentBeanFields[i]);
						((TextField)currentFields[i]).setMaxLength(maxLength);
					}
					break;
				// Si es un campo de fecha
				case DATE:
				case DATETIME:
				case TIME:
					// Creamos el campo a mano y lo añadimos al binder
					currentFields[i] = new PopupDateField(caption);
					// Deshabilitamos el campo de texto
					((PopupDateField)currentFields[i]).setTextFieldEnabled(false);
					// TODO Parametrizar locale
					((PopupDateField)currentFields[i]).setLocale(new Locale("es", "ES"));
					currentBinder.bind((Field) currentFields[i], currentBeanFields[i].getName());
					break;
				// Si es un campo de tipo archivo o imagen
				case FILE: case IMAGE:
					// Podemos usar como tipo File porque Image es subclase de File
					File standardFormFile =
						(File) Utils.getFieldValue(bean, currentBeanFields[i]);
					String id = prefixParentBean + currentBeanFields[i].getName();
					currentFields[i] = new FileComponent(caption, standardFormFile, id, insertMode);
					break;
				// Si es un campo de búsqueda o embedded
				case SEARCH: case EMBEDDED:
					// Obtenemos la clase del Bean anidado
					Class<? extends Bean> embeddedBeanClass =
					(Class<? extends Bean>)currentBeanFields[i].getType();
					// Obtenemos el bean anidado
					Bean embeddedBean = (Bean) Utils.getFieldValue(currentBean, currentBeanFields[i]);
					// Campo de tipo búsqueda
					if (tipo == StandardFormField.Type.SEARCH) {
						if (insertMode) {
							embeddedBean = null;
						}
						BeanDAO searchDAO = 
    							Utils.getBeanDAO(embeddedBeanClass, beanUI.getBeanDAO());
						BeanUI searchBeanUI = new BeanUI(embeddedBeanClass, searchDAO);
						currentFields[i] = new SearchField(caption, searchBeanUI, embeddedBean);
						break;
					}
					// Campo de tipo embedded
					// Si el campo del embeddedBean del elementoActual es null
					if (embeddedBean == null) {
						// Creamos un nuevo objeto embeddedBean vacío
						embeddedBean = embeddedBeanClass.newInstance();
						// Asignamos el embeddedBean al elementoActual
						Utils.setFieldValue(currentBean, currentBeanFields[i], embeddedBean);
					}
					// Creamos un formulario anidado para albergar todos los campos del bean anidado
					FormLayout embeddedForm = new FormLayout();
					// Creamos un binder para el elemento anidado
					BeanFieldGroup<? extends Bean> embeddedBinder = new BeanFieldGroup(embeddedBeanClass);
					BeanItem embeddedItem = new BeanItem(embeddedBean);
					embeddedBinder.setItemDataSource(embeddedItem);
					// Lo añadimos al mapa de binders
					binderMap.put(prefixParentBean + currentBeanFields[i].getName(), embeddedBinder);
					// Obtenemos los campos llamando recursivamente a esta función
					Component[] embeddedFields = getFormFields(embeddedBean, embeddedBinder,
							prefixParentBean + currentBeanFields[i].getName()  + ".");
					// Añadimos los campos al formulario
					for (Component field: embeddedFields) {
						// Comprobamos que existe (los campos deshabilitados no se crean en alta)
						if (field != null) {
							embeddedForm.addComponent(field);
						}
					}
					// Creamos un panel con el caption
					Panel panel = new Panel("<b>"+caption+"<b/>");
					// Asignamos el formulario al panel
					panel.setContent(embeddedForm);
					// Añadimos el panel al formulario principal
					currentFields[i] = panel;
					break;
				case TABLE:
					// Obtenemos la clase parametrizada del arrayList
					java.lang.reflect.Type parametrizedType = Utils.getParametrizedType(currentBeanFields[i]);
					// Obtenemos la colección de elementos
					Collection collection = (Collection) Utils.getFieldValue(currentBean, currentBeanFields[i]);
					// Si la colección está vacía
					if (collection == null) {
						// Si la colección es un set
						if (Utils.isOrImplementsInterface(currentBeanFields[i].getType(), Set.class)) {
							// Creamos un nuevo set vacío
							collection = new HashSet();
						}
						// Si no, la colección debe ser una lista
						else {
							// Creamos un nueva lista vacía
							collection = new ArrayList();
						}
						// Asignamos el embeddedBean al elementoActual
						Utils.setFieldValue(currentBean, currentBeanFields[i], collection);
					}
					// Creamos el container y el beanUI
					BeanItemContainer container = new BeanItemContainer((Class)parametrizedType, collection);
					BeanUI tableBeanUI = new BeanUI((Class)parametrizedType, null);
					// Creamos la tabla
					currentFields[i] = new StandardTable(caption, container, tableBeanUI);
					break;
				case MONGO_ID:
					// Si estamos en modo modificación
					if (!insertMode) {
						// Creamos un campo de texto oculto
						// Esto permite que pueda ser mostrado para los administradores de la aplicación
						currentFields[i] = new TextField(caption);
						// Obtenemos  valor para mostrarlo
						ObjectId objectId=
								(ObjectId) Utils.getFieldValue(currentBean, currentBeanFields[i]);
						((TextField)currentFields[i]).setValue(objectId.toString());
						// Deshabilitamos y hacemos invisible el campo
						currentFields[i].setVisible(false);
						currentFields[i].setEnabled(false);
					}
				default:
					break;
				}
				// Si se ha creado el campo
				if (currentFields[i] != null) {
					// Asignamos al campo como id el nombre del campo del bean actual
					currentFields[i].setId(prefixParentBean + currentBeanFields[i].getName());
					
					if (currentFields[i] instanceof AbstractField) {
						((AbstractField)currentFields[i]).setImmediate(true);
					}
					
					// Si no es un embedded field
					if (tipo != StandardFormField.Type.EMBEDDED) {
						// Se le añade al campo el estilo standardform-field
						currentFields[i].addStyleName("standardform-field");
					}
				
					// Si es un campo, no oculto y tiene anotación NotNull
					if (currentFields[i] instanceof AbstractField &&
							(
									(standardFormField instanceof StandardFormField &&
											!standardFormField.hidden()) ||
									!(standardFormField instanceof StandardFormField )
							) &&
							currentBeanFields[i].getAnnotation(NotNull.class) instanceof NotNull) {
						// Se marca el campo como obligatorio
						((AbstractField)currentFields[i]).setRequired(true);
					}
					
					// Si no estamos en alta y no se permite edición
					if (!insertMode && !standardForm.allowsEditing()) {
						// Deshabilitamos el campo
						currentFields[i].setEnabled(false);
					}
					
					// Se especifica la representación del null
					if (currentFields[i] instanceof AbstractTextField) {
						((AbstractTextField)currentFields[i]).setNullRepresentation("");
					}
					// Si es un BeanMongo y el campo tiene anotación Id
					if (Utils.isSubClass(currentBean.getClass(), BeanMongo.class) &&
						currentBeanFields[i].getAnnotation(Id.class) != null) {
						// Si estamos en modo modificación
						if (!insertMode) {
							// se deshabilita el campo
							currentFields[i].setEnabled(false);
						}
					}
					
					// Asignamos el valor por defecto
					setDefaultValue(currentBeanFields[i], currentFields[i], standardFormField);
					
					// Si es un campo oculto
					if (standardFormField instanceof StandardFormField &&
							standardFormField.hidden()) {
						if (currentFields[i] instanceof AbstractField) {
							((AbstractField)currentFields[i]).removeAllValidators();
						}
						// Se hace invisible y se deshabilita
						currentFields[i].setVisible(false);
						currentFields[i].setEnabled(false);
					}
					
					// Si es un campo deshabilitado
					if (standardFormField instanceof StandardFormField &&
							standardFormField.disabled()) {
						// Mostramos el campo deshabilitado
						currentFields[i].setEnabled(false);
						// Se eliminan los validadores
						if (currentFields[i] instanceof AbstractField) {
							((AbstractField)currentFields[i]).removeAllValidators();
						}
						// Si estamos en modo inserción
						if (insertMode) {
							// Ocultamos el campo
							currentFields[i].setVisible(false);
						}
					}
					
					// Añadimos el campo al formulario
					form.addComponent(currentFields[i]);
					// Si hay ayuda
					if (standardFormField instanceof StandardFormField && !standardFormField.help().isEmpty()) {
						// Añadimos etiqueta con la ayuda
						form.addComponent(new Label(standardFormField.help()));
					}
				}
			}
		}
		return currentFields;
	}

	/**
	 * If the current field has a defaultValue annotation, sets the default value
	 * @param currentBeanField
	 * @param currentField
	 * @param standardFormField
	 */
	protected void setDefaultValue(java.lang.reflect.Field currentBeanField,
			Component currentField, 
			StandardFormField standardFormField) {
		// Si estamos en modo alta y hay anotación defaultValue
		if (insertMode && standardFormField instanceof StandardFormField &&
				standardFormField.defaultValue().length() != 0) {
			// Si es un campo de texto
			if (currentField instanceof AbstractTextField) {
				// Asignamos el valor al campo
				((AbstractTextField)currentField).setValue(standardFormField.defaultValue());;
			}
			// Si es booleano
			else if (currentField instanceof CheckBox) {
				// Si el valor por defecto es "true"
				if (standardFormField.defaultValue().equals("true")) {
					// Marcamos el check box
					((CheckBox)currentField).setValue(true);
				}
					
			}
			// Si es un enumerado
			else if (currentBeanField.getType().isEnum() &&
					currentField instanceof AbstractSelect) {
				// Obtenemos todos los elementos del campo de selección
				Collection<?> elementos = ((AbstractSelect)currentField).getItemIds();
				// Seleccionamos el elemento que coincida con el valor por defecto
				for (Object elemento : elementos) {
					if (elemento.toString().equals(standardFormField.defaultValue())) {
						((AbstractSelect)currentField).setValue(elemento);
					}
				}
			}
		}
	}

	/**
	 * Gets the StandardFormField type
	 * @param standardForm StandardForm annotation of the bean
	 * @param beanField Campo del bean
	 * @param standardFormField StandardFormField annotation of the bean field
	 * @return Tipo de campo obtenido
	 */
	protected StandardFormField.Type getTypeFormField(
			StandardForm standardForm, java.lang.reflect.Field beanField, StandardFormField standardFormField) {
		// Si hay anotación DetailField para este campo y no se debe crear el campo
		if ((standardFormField instanceof StandardFormField) &&
				!standardFormField.createField()) {
			// No se crea el campo
			return null;
		}
		// Si hay anotación DetailField para este campo y el type no es DEFAULT
		if ((standardFormField instanceof StandardFormField) &&
				standardFormField.type() != StandardFormField.Type.DEFAULT) {
			// Retornamos el tipo especificado
			return standardFormField.type();
		}
		// En caso contrario obtenemos el tipo por defecto según el tipo de bean
		Class<?> tipoBean = beanField.getType();
		// Si el tipo de campos es un String
		if (tipoBean == String.class) {
			// Si el tipo de DAO es JPA y el campo tiene anotación Lob
			if (standardForm.daoType() == DAOType.JPA && 
					beanField.getAnnotation(Lob.class) != null)
				// retorna el tipo TEXT_AREA
				return StandardFormField.Type.TEXT_AREA;
			// En caso contrario retorna el tipo TEXT_FIELD 
			return StandardFormField.Type.TEXT_FIELD;
		}
		// Si el tipo de campo es boolean
		else if (tipoBean == Boolean.TYPE ||
				tipoBean == Boolean.class)
			return StandardFormField.Type.CHECK_BOX;
		// Si el tipo de campos es numérico
		else if (tipoBean == Byte.TYPE || tipoBean == Byte.class ||
					tipoBean == Short.TYPE || tipoBean == Short.class ||
					tipoBean == Integer.TYPE || tipoBean == Integer.class ||
					tipoBean == Long.TYPE || tipoBean == Long.class ||					
					tipoBean == Float.TYPE || tipoBean == Float.class ||
					tipoBean == Double.TYPE || tipoBean == Double.class)
			return StandardFormField.Type.NUM_FIELD;
		// Si el tipo de campo el Date
		else if (tipoBean == Date.class) {
			// Si el tipo de DAO es Mongo
			if (standardForm.daoType() == DAOType.MONGO) {
				// Retorna el tipo DATE
				return StandardFormField.Type.DATE;
			}
			// Si el tipo de DAO es JPA, obtenemos la anotación Temporal
			Temporal temporal = beanField.getAnnotation(Temporal.class);
			// Retornamos el tipo en función de la anotación temporal
			if (temporal != null && temporal.value() == TemporalType.TIMESTAMP)
				return StandardFormField.Type.DATETIME;
			else if (temporal != null && temporal.value() == TemporalType.TIME)
				return StandardFormField.Type.TIME;
			// Si no hay anotación temporal o es Date
			return StandardFormField.Type.DATE;
		}
		// Si el tipo de campos es un enumerado
		else if (tipoBean.isEnum()) {
			// Retorna el tipo COMBO_BOX
			return StandardFormField.Type.COMBO_BOX;
		}
		// Si el tipo de campos es un Set o un List
		else if (Utils.isOrImplementsInterface(tipoBean, Set.class) ||
				Utils.isOrImplementsInterface(tipoBean, List.class)) {
			// Obtenemos la clase parametrizada
			java.lang.reflect.Type parametrizedType = Utils.getParametrizedType(beanField);
			// Si el tipo parametrizado es un Bean
			if (Utils.isSubClass((Class<?>) parametrizedType, Bean.class))
				return StandardFormField.Type.TABLE;
			else
				return StandardFormField.Type.MULTIPLE_SELECTION;
		}
		// Si el tipo de campo es otro Bean
		else if (Utils.isSubClass(tipoBean, Bean.class)) {
			// Si el tipo de DAO es Mongo y el campo NO tiene la anotación @Reference
			if (standardForm.daoType() == DAOType.MONGO &&
					beanField.getAnnotation(Reference.class) == null) {
				return StandardFormField.Type.EMBEDDED;
			}
			else {
				// Retorna el tipo COMBO_BOX
				return StandardFormField.Type.SEARCH;
			}
		}
		// Si el tipo es standardForm.model.File
		else if (tipoBean == File.class) {
			return StandardFormField.Type.FILE;
		}
		// Si el tipo es standardForm.model.Image
		else if (tipoBean == Image.class) {
			return StandardFormField.Type.IMAGE;
		}
		// Si el tipo es ObjectId usado por MongoBD
		else if (tipoBean == ObjectId.class) {
			return StandardFormField.Type.MONGO_ID;
		}
		// Si no es ninguno de los anteriores, no se crea el campo
		return null;
	}

	/**
	 * Shows the ListForm inside the same component as this detailForm
	 */
	public void showListForm() {
		Component vistaListado = beanUI.buidListForm();
		ComponentContainer contentPanel = (ComponentContainer)getParent();
		contentPanel.replaceComponent(this, vistaListado);
	}
	
	/**
	 * Saves the bean persistly using the BeanDAO
	 * If there is some validation error, it shows a warning notification
	 * If it catches another exception, it ishows a error notification
	 * @param event
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void save(ClickEvent event) {
		try {
		    try{
    			// Dado que los campos de selección no están incluídos en el binder, tenemos que hacer commit a mano
    			commitSelectFields(bean, formFields);
    			// Hacemos commit de los campos de tipo archivo
    			commitFileImageFields();
    			// Hacemos commit del resto de campos
    			// Recorremos el binderMap para hacer commit de cada binder
    			for (Entry<String, BeanFieldGroup<?>> entry : binderMap.entrySet()) {
    				 BeanFieldGroup<?> binder = entry.getValue();
    				 binder.commit();
    			}
    			// Si estamos en modo inserción
    			if (insertMode) {
    				// Obtenemos la clave (id)
    				Object id = Utils.getId(bean);
    				// Si el id no es null
    				if (id != null) {
    					// Comprobamos si existe ya un bean con la misma clave
        				Bean beanExistente = beanUI.getBeanDAO().get((K) id);
        				if (beanExistente != null) {
        					Notification.show("Error", 
        						"Ya existe un registro con la misma clave.\n"
        						+ "No se puede realizar el alta",
        						Type.ERROR_MESSAGE);
        					return;
        				}
    				}
    			}
    			
    			// Si hay escuchador
    			if (saveListener != null) {
    				// Llamamos al método beforeSave(), antes de que se guarde el bean
    				saveListener.beforeSave(bean, insertMode);
    			}
    			
    			// Buscamos si hay un campo de tipo EMBEDDED pero que es una referencia a otro bean
    			// Obtenemos la anotación StandardForm
    			StandardForm standardForm = bean.getClass().getAnnotation(StandardForm.class);
    			// Recorremos los campos
    			java.lang.reflect.Field[] beanFields = Utils.getBeanFields(bean.getClass());
    			for (int i=0;i<beanFields.length;i++) {
    				// Obtenemos la anotación StandardFormField
    				StandardFormField standardFormField = beanFields[i].getAnnotation(StandardFormField.class);
    				// Obtenemos el tipo de campo en función de los metadatos
    				StandardFormField.Type tipo = getTypeFormField(standardForm, beanFields[i], standardFormField);
    				// Si el tipo de campo es EMBEDDED y NO es un EMBEDDED de Morphia
    				if (tipo == StandardFormField.Type.EMBEDDED &&
    						beanFields[i].getAnnotation(Embedded.class) == null) {
    					// TODO Sólo se permite un nivel de anidamiento
    					// Obtenemos el DAO
    					BeanDAO dao = 
    							Utils.getBeanDAO((Class)(beanFields[i].getType()), beanUI.getBeanDAO());
    					// Obtenemos el bean anidado
    					Bean embeddedBean = (Bean) Utils.getFieldValue(bean, beanFields[i]);
    					// Tenemos que insertar o actualizar el bean
    					if (insertMode) {
    						// Obtenemos la clave (id)
    	    				Object id = Utils.getId(embeddedBean);
    	    				// Si el id no es null
    	    				if (id != null) {
    	    					// Comprobamos si existe ya un bean anidado con la misma clave
	    						Bean beanExistente = dao.get(Utils.getId(embeddedBean));
	    						if (beanExistente != null) {
	    							Notification.show("Error", 
	    								"Ya existe un elemento para el campo " + standardFormField.caption()
	    									+ " con la misma clave.\n"
	    									+ "No se puede realizar el alta",
	    								Type.ERROR_MESSAGE);
	    							return;
	    						}
    	    				}
    						dao.insert(embeddedBean);
    					}
    					else {
    						dao.update(embeddedBean);
    					}
    				}
    			}
    			
    			// Si estamos en modo inserción, insertamos el bean en base de datos
    			if (insertMode) {
    				beanUI.getBeanDAO().insert(bean);
    			}
    			// Si no, actualizamos el bean en base de datos
    			else {
    				beanUI.getBeanDAO().update(bean);
    			}
    			
    			// Si hay escuchador
    			if (saveListener != null) {
    				// Llamamos al método aferSave(), después de guardar el bean
    				saveListener.afterSave(bean, insertMode);
    			}
    			
    			// Si todo ha ido bien, mostramos mensaje informativo
    			String texto;
    			if (insertMode) {
    				texto = "El elemento se ha insertado correctamente";
    			}
    			else {
    				texto = "El elemento se ha actualizado correctamente";
    			}
    			Notification.show("Información",
    					texto,
    					Type.TRAY_NOTIFICATION);
    			// Y mostramos el listado
    			showListForm();
		    } catch(Exception e) {
		        // Buscar si el bean tiene un mensaje para este tipo de excepción
		        StandardForm standardForm = bean.getClass().getAnnotation(StandardForm.class);
		        String mensaje = null;
		        for(Class<? extends Exception> claseExcepcion: standardForm.catchSaveExceptions()) {
		            if(claseExcepcion.isInstance(e)) {
		                try {
		                    Method method = bean.getClass().getMethod("saveExceptionListener", Exception.class);
		                    mensaje = (String)method.invoke(bean, e);
		                } catch (Exception e2) {}
		            }
		        }
                // Si se he conseguido un mensaje de error, muestro el mensaje, si no es
		        // así, relanzo la excepción.
		        if(mensaje!=null) {
		            Notification.show("Error de validación", 
                                      mensaje,
                                      Type.WARNING_MESSAGE);
		        } else {
		            throw e;
		        }
		    }
		} catch (CommitException e) {
			e.printStackTrace();
			Notification.show("Error de validación",
//					"Algún campo no supera las validaciones. Por favor, revise el formulario",
					(e.getCause() == null) ? e.getMessage() : e.getCause().getMessage(),
					Type.WARNING_MESSAGE);
		} catch (java.util.ConcurrentModificationException e) {
			Notification.show("Error", 
					"No se puede guardar.\n"
					 + "Este elemento ha sido modificado mientras lo estaba editando. Inténtelo de nuevo",
					Type.ERROR_MESSAGE);
			// Mostramos el listado
			showListForm();
		} catch (SaveException e) {
			Notification.show("Error",
					"No se puede grabar el registro.\n" + e.getMessage(),
					Type.ERROR_MESSAGE);
		} catch (ConstraintViolationException e) {
			Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
			String message = "";
			for (ConstraintViolation c : constraintViolations) {
				message += c.getRootBeanClass().getName() + "." + c.getPropertyPath()
						+ " " + c.getMessage() + "\n";
			}
			Notification.show("Error en constraints",
				message,
				Type.ERROR_MESSAGE);
			e.printStackTrace();
		}catch (Exception e) {
			Notification.show("Error",
					"No se ha podido realizar la operación.\n" + e.getMessage(),
					Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	/**
	 * Executes a commit for every file and image fields in the form.
	 * It is neccesary because the file and image fields aren't included inside the binder
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws CommitException
	 */
	private void commitFileImageFields() throws NoSuchMethodException,SecurityException,
		IllegalAccessException, IllegalArgumentException, InvocationTargetException, CommitException {
		// Recorremos los campos
		java.lang.reflect.Field[] beanFields = Utils.getBeanFields(bean.getClass());
		for (int i=0;i<beanFields.length;i++) {
			if (formFields[i] instanceof FileComponent) {
				FileComponent fileComponent = (FileComponent) formFields[i];
				FileUploader fileUploader = fileComponent.getFileUploader();
				// Si no se ha subido archivo
				if (fileUploader == null ||
						fileUploader.getByteArrayOutputStream() == null ||
						fileUploader.getByteArrayOutputStream().size() == 0) {
					// Obtenemos el valor del campo en el bean
					Object currentValue = Utils.getFieldValue(bean, beanFields[i]);
					// Si el campo no estaba informado previamente y es requerido
					if (currentValue == null &&
							beanFields[i].getAnnotation(NotNull.class) instanceof NotNull) {
						throw new CommitException("Debe subir un archivo");
					}
				}
				// Si el campo es de tipo File
				else if (beanFields[i].getType() == File.class){
					// Creamos un objeto file con los datos del fileUploader
					File file = new File();
					file.setBytes(fileUploader.getByteArrayOutputStream().toByteArray());
					file.setFilename(fileUploader.getFilename());
					file.setMimeType(fileUploader.getMimeType());
					// Asignamos el objeto file al campo del bean
					Utils.setFieldValue(bean, beanFields[i], file);
				}
				else if (beanFields[i].getType() == Image.class) {
					// Creamos un objeto image con los datos del fileUploader
					Image image = new Image();
					image.setBytes(fileUploader.getByteArrayOutputStream().toByteArray());
					image.setFilename(fileUploader.getFilename());
					image.setMimeType(fileUploader.getMimeType());
					// Asignamos el objeto file al campo del bean
					Utils.setFieldValue(bean, beanFields[i], image);
				}
			}
		}
	}

	/**
	 * It creates a select field (Vaadin abstract select, ComboBox or Option Group) for a specific beanField
	 * It gets everty options using the BeanDAO, adds then to the abstract field and selects the current element(s)
	 * It suports nested beans, enums and ArrayLists (multiple selection)
	 * @param currentBean
	 * @param field
	 * @param type
	 * @param caption
	 * @return The select field (Vaadin abstract select, ComboBox or Option Group)
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws ClassNotFoundException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 */
	@SuppressWarnings({"rawtypes", "unchecked" })
	protected AbstractSelect getSelectField(
				final Bean currentBean,
				String prefixParentBean,
				final java.lang.reflect.Field field,
				es.vegamultimedia.standardform.annotations.StandardFormField.Type type,
				String caption)
			throws NoSuchMethodException, IllegalAccessException,
				InvocationTargetException, ClassNotFoundException,
				IllegalArgumentException, InstantiationException {
		// El tipo de campo debe ser COMBO_BOX, OPTION_GROUP ó MULTIPLE_SELECTION
		if (type != StandardFormField.Type.COMBO_BOX &&
			type != StandardFormField.Type.OPTION_GROUP &&
			type != StandardFormField.Type.MULTIPLE_SELECTION) {
			return null;
		}
		StandardFormField standardFormField = field.getAnnotation(StandardFormField.class);
		AbstractSelect campoSelect = null;
		final Class tipoElementos;
		Collection<? extends Bean> listaElementos;
		
		// Si es una colección (tipos soportados: List y Set)
		if (Utils.isOrImplementsInterface(field.getType(), Set.class) ||
				Utils.isOrImplementsInterface(field.getType(), List.class)) {
			// Obtenemos la clase parametrizada de la colección
			java.lang.reflect.Type parametrizedType = Utils.getParametrizedType(field);
			tipoElementos = (Class) parametrizedType;
		}
		// Si NO es una colección
		else {
			// Obtenemos directamente el tipo de elementos
			tipoElementos = field.getType();
		}
		// Si el tipo de elementos es un bean anidado
		if (Utils.isSubClass(tipoElementos, Bean.class)) {
			// Si no tiene un campo maestro, no es un campo oculto ni deshabilitado
			if (standardFormField == null ||
					(standardFormField != null &&
						standardFormField.nameMasterField().isEmpty() &&
						!standardFormField.disabled() &&
						!standardFormField.hidden())) {
				// Obtenemos una instancia del BeanDAO anidado
				BeanDAO<? extends Bean, K> beanDAO = Utils.getBeanDAO(tipoElementos, beanUI.getBeanDAO());
				// Obtenemos todos los elementos del bean anidado
				listaElementos = beanDAO.getAllElements();
			}
			// En caso contrario, si es un Set
			else if (Utils.isOrImplementsInterface(tipoElementos, Set.class)){
				// Creamos un Hashset vacío
				listaElementos = new HashSet();
			}
			// Si no, debe ser un List
			else {
				// Creamos una lista vacía
				listaElementos = new ArrayList();
			}

			// Creamos un contenedor con todos los elementos
			BeanItemContainer container =
					new BeanItemContainer(tipoElementos, listaElementos);
			
			// Creamos el campo en función del tipo
			if (type == StandardFormField.Type.COMBO_BOX) {
				// Creamos un combo box con el contenedor 
				campoSelect = new ComboBox(caption, container);
			}
			else {
				// Creamos un Option Group con el contenedor 
				campoSelect = new OptionGroup(caption, container);
				// Si es de selección múltiple
				if (type == StandardFormField.Type.MULTIPLE_SELECTION) {
					// Hacemos el campo de selección múltiple
					campoSelect.setMultiSelect(true);
				}
			}
			// El caption que se muestra es la representación del item
			// Nota: Los bean anidados deben sobreescribir el métoodo toString()
			campoSelect.setItemCaptionMode(ItemCaptionMode.EXPLICIT_DEFAULTS_ID);
			
			// Añadimos un validador de tipo BeanValidator para el campo
			campoSelect.addValidator(new BeanValidator(currentBean.getClass(), field.getName()));
			
			// Si tiene un campo maestro
			if (standardFormField != null &&
					!standardFormField.nameMasterField().isEmpty()) {
				// Configuramos los campos maestro y esclavo
				configureMasterSlaveSelects(currentBean, prefixParentBean, field,
						standardFormField, campoSelect, tipoElementos);
			}
		}
		
		// Si es un enumerado
		if (tipoElementos.isEnum()) {
			// Obtenemos los elementos del enumerado
			Class<?> enumeradoClass = tipoElementos;
			Object[] elementosEnum = enumeradoClass.getEnumConstants();
			
			// Creamos el campo en función del tipo
			if (type == StandardFormField.Type.COMBO_BOX) {
				// Creamos un combo box con los elementos
				campoSelect = new ComboBox(caption, Arrays.asList(elementosEnum));
			}
			else {
				// Creamos un Option Group con el contenedor 
				campoSelect = new OptionGroup(caption, Arrays.asList(elementosEnum));
				// Si es de selección múltiple
				if (type == StandardFormField.Type.MULTIPLE_SELECTION) {
					// Hacemos el campo de selección múltiple
					campoSelect.setMultiSelect(true);
				}
			}
			// Asignamos los captions del enum select
			Utils.setCaptionsEnumSelect(campoSelect, enumeradoClass, elementosEnum);
		}
		if (campoSelect != null) {
			// Obtenemos el valor del elemento actual para seleccionarlo
			Object beanAnidado = Utils.getFieldValue(currentBean, field);
			// Seleccionamos el elemento actual del bean anidado
			if (beanAnidado != null) {
				campoSelect.setValue(beanAnidado);
			}
		}
		return campoSelect;
	}

	/**
	 * Configures the master-slave selects:
	 * Adds a ValueChangeListener to the master select than obtains the elements in the slave select
	 * Selects the current value in the master select
	 * @param currentBean
	 * @param prefixParentBean
	 * @param field
	 * @param standardFormField
	 * @param slaveSelect
	 * @param slaveBeanClass
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void configureMasterSlaveSelects(
			final Bean currentBean,
			String prefixParentBean,
			final java.lang.reflect.Field field,
			StandardFormField standardFormField,
			final AbstractSelect slaveSelect,
			final Class<? extends Bean> slaveBeanClass) {
		// Obtenemos el campo maestro
		final String nameMasterField = standardFormField.nameMasterField();
		final Component masterField = findFormField(prefixParentBean + nameMasterField);
		// Si no se encuentra o no es un select
		if (masterField == null ||
				!(masterField instanceof AbstractSelect)) {
			Notification.show("No se encuentra el campo maestro de " + field.getName(),
					Type.ERROR_MESSAGE);
			return;
		}
		AbstractSelect masterSelect = (AbstractSelect) masterField;
		// Añadimos un escuchador al campo maestro para cuando cambie su valor
		masterSelect.addValueChangeListener(new ValueChangeListener() {
			// En ese caso, tenemos que obtener los elementos del bean esclavo 
			// que tengan el valor seleccionado en el campo maestro
			@Override
			public void valueChange(ValueChangeEvent event) {
				Object value = event.getProperty().getValue();
				// Si no es un BeanMongo
				if (!Utils.isSubClass(currentBean.getClass(), BeanMongo.class)) {
					// TODO Pendiente de implementar para JPA
					return;
				}
				try {
					List<?> listaElementosEsclavo;
					// Si se ha seleccionado un elemento en el campo maestro
					if (value != null) {
						// Creamos un BeanMongoDAO para obtener el datastore
						BeanMongoDAO<? extends Bean, K> beanMongoDAO = (BeanMongoDAO<? extends Bean, K>)
							Utils.getBeanDAO(slaveBeanClass, beanUI.getBeanDAO());
						Datastore datastore = beanMongoDAO.getDatastore();
						// Creamos una query para obtener los elementos del bean esclavo 
						Query<?> query = datastore.createQuery(slaveBeanClass);
						
						// Obtenemos el campo Id del bean maestro
						java.lang.reflect.Field masterIdField =
								Utils.getIdField((Class<? extends Bean>) field.getType());
						if (masterIdField == null) {
							throw new IllegalArgumentException(
									"No se encuentra el campo Id de " + field.getType().getName());
						}
						// Obtenemos el id del valor seleccionado en el select maestro
						Object masterIdValue = Utils.getFieldValue((Bean) value, masterIdField);

						query = query.field(nameMasterField).equal(masterIdValue);
						listaElementosEsclavo = query.asList();
					}
					// En caso contrario, se crea una lista vacía
					else {
						listaElementosEsclavo = new ArrayList();
					}
					// Creamos un contenedor y se lo asignamos al campo esclavo
					BeanItemContainer containerEsclavo =
							new BeanItemContainer(slaveBeanClass, listaElementosEsclavo);
					slaveSelect.setContainerDataSource(containerEsclavo);
				} catch (Exception e) {
					Notification.show("No se pueden obtener los elementos del campo " + field.getName(),
							Type.ERROR_MESSAGE);
				}
			}
		});
		// Debemos forzar la selección del elemento en el campo maestro
		// para que se genere un ValueChangeEvent y se obtengan los elementos del campo esclavo
		try {
			// Eliminamos la posible selección (la hay si el campo maestro no es Transient)
			masterSelect.setValue(null);
			// Obtenemos el bean esclavo del bean actual
			Bean beanEsclavo = (Bean) Utils.getFieldValue(currentBean, field);
			// Se está informado el bean esclavo
			if (beanEsclavo != null) {
				// Obtenemos todos los campos del bean esclavo
				java.lang.reflect.Field[] beanFields = Utils.getBeanFields(beanEsclavo.getClass());
				for (java.lang.reflect.Field slaveField : beanFields) {
					if (slaveField.getName().equals(nameMasterField)) {
						// Obtenemos el valor del elemento maestro en el bean esclavo
						Object masterValue = Utils.getFieldValue(beanEsclavo, slaveField);
						// Seleccionamos el elemento en el campo maestro
						masterSelect.setValue(masterValue);
						break;
					}			
				}
			}
		} catch (Exception e) {
			Notification.show("No se puede seleccionar el elemento en el campo " + field.getName(),
					Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * Executes a commit for every Vaadin select fields in the form.
	 * It is neccesary because the selects fields aren't included inside the binder
	 * This method is recursive, so it executes the commit for every nested beans
	 * @param currentBean
	 * @param currentFormFields
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws CommitException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void commitSelectFields(Bean currentBean, Component[] currentFormFields)
			throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException, CommitException {
		java.lang.reflect.Field[] beanFields = Utils.getBeanFields(currentBean.getClass());
		for (int i=0;i<beanFields.length;i++) {
			// Si el campo no está oculto y su tipo es COMBO_BOX u OPTION_GROUP
			if (currentFormFields[i] != null &&
					currentFormFields[i].isVisible() && (
					currentFormFields[i] instanceof ComboBox ||
					currentFormFields[i] instanceof OptionGroup)) {
				AbstractSelect selectField = ((AbstractSelect)currentFormFields[i]);
				// Si el campo NO permite selección múltiple
				if (!selectField.isMultiSelect()) {
					// Obtenemos el elemento seleccionado en el campo de selección
					Object elementoSeleccionado = selectField.getValue();
					// Comprobamos si el campo es obligatorio y no hay ningún elemento seleccionado
					if (beanFields[i].getAnnotation(NotNull.class) instanceof NotNull
							&& elementoSeleccionado == null) {
						selectField.setRequiredError("Obligatorio");
						throw new CommitException("El campo es obligatorio");
					}
					// Asignamos al elemento actual el elemento seleccionado en el combo box
					Utils.setFieldValue(currentBean, beanFields[i], elementoSeleccionado);
				}
				// Si el campo sí permite selección múltiple
				else {
					Collection elementosSeleccionados;
					// Si es un Set
					if (Utils.isOrImplementsInterface(beanFields[i].getType(), Set.class)) {
						// Creamos un HashSet
						elementosSeleccionados = new HashSet();
					}
					// Si no, debe ser un List
					else {
						// Creamos un arrayList
						elementosSeleccionados = new ArrayList();
					}
					// Le añadimos los elementos seleccionados
					for (Object itemId : selectField.getItemIds()) {
						if (selectField.isSelected(itemId)) {
							elementosSeleccionados.add(itemId);
						}
					}
					// Comprobamos si el campo es obligatorio y no hay ningún elemento seleccionado
					if (beanFields[i].getAnnotation(NotNull.class) instanceof NotNull
							&& elementosSeleccionados.size() == 0) {
						selectField.setRequiredError("Obligatorio");
						throw new CommitException("Debe seleccionar al menos un elemento");
					}
					// Asignamos al elemento actual el arraylist de elementos seleccionados
					Utils.setFieldValue(currentBean, beanFields[i], elementosSeleccionados);
				}
			}
			// Si es un campo de tipo SEARCH
			else if (currentFormFields[i] instanceof SearchField) {
				SearchField searchField = (SearchField) currentFormFields[i];
				// Obtenemos el elemento seleccionado en el campo de selección
				Object elementoSeleccionado = searchField.getValue();
				// Comprobamos si el campo es obligatorio y no hay ningún elemento seleccionado
				if (beanFields[i].getAnnotation(NotNull.class) instanceof NotNull
						&& elementoSeleccionado == null) {
					searchField.setRequiredError("Obligatorio");
					throw new CommitException("El campo es obligatorio");
				}
				// Asignamos al elemento actual el elemento seleccionado en el combo box
				Utils.setFieldValue(currentBean, beanFields[i], elementoSeleccionado);
			}
			// Si es un campo EMBEDDED, hay que hacer commit recursivamente
			// TODO Sería más correcto usar el tipo de campo EMBEDDED, en vez del tipo de componente de Vaadin
			else if (currentFormFields[i] instanceof Panel) {
				// Nos aseguramos de que es un bean anidado
				if (Utils.isSubClass(beanFields[i].getType(), Bean.class)) {
					// Obtenemos el beanAnidado
					Bean beanAnidado = (Bean) Utils.getFieldValue(currentBean, beanFields[i]);
					// Obtenemos el contenido del panel
					Component content = ((Panel)currentFormFields[i]).getContent();
					// Obtenemos sus componentes en un ArrayList
					ArrayList<Component> arrayListEmbeddedFormFields = new ArrayList<Component>();
					for (Component c : (HasComponents)content) {
						arrayListEmbeddedFormFields.add(c);
					}
					// Convertimos el arrayList en un array
					Component[] embeddedFormFields = new Component[arrayListEmbeddedFormFields.size()];
					embeddedFormFields = arrayListEmbeddedFormFields.toArray(embeddedFormFields);
					// Llamamos recursivamente para hacer commit de los campos de selección anidados
					commitSelectFields(beanAnidado, embeddedFormFields);
				}
			}
		}
	}
	
	/**
	 * Gets the form field from a bean name field.
	 * Supports nested fields with dot notation.
	 * @param nameField
	 * @return The form Field or null if there is no bean name field
	 */
	public Component findFormField(String nameField) {
		return Utils.findComponentById(form, nameField);
	}
	
	/**
	 * Shows all the hidden fields form
	 */
	public void showHiddenFields() {
		Utils.iterateSubComponents(form, false, true);
	}
	
	/**
	 * Disable the form. Disables all the fields and hides the save button
	 */
	public void disableForm() {
		// Deshabilitamos todos los componentes
		Utils.iterateSubComponents(form, true, false);
		// Habilitamos el layout de botones
		buttonsLayout.setEnabled(true);
		// Ocultamos el botón guardar
		if (saveButton != null) {
			saveButton.setVisible(false);
		}
		// Habilitamos el botón cancelar
		if (cancelButton != null) {
			cancelButton.setEnabled(true);
		}
	}
	
	/**
	 * Hides a field from a bean name field
	 * @param nameField
	 * @return The field or null if there is no name field
	 */
	@SuppressWarnings("rawtypes")
	public Component hideField(String nameField) {
		Component component = findFormField(nameField);
		if (component != null) {
			component.setVisible(false);
			if (component instanceof AbstractField) {
				AbstractField formField = (AbstractField) component;
				formField.removeAllValidators();
				formField.setRequired(false);
			}
		}
		return component;
	}
	
	/**
	 * Shows and disables a field from a bean name field
	 * @param nameField
	 * @return The field or null if there is no name field
	 */
	@SuppressWarnings("rawtypes")
	public Component showAndDisableField(String nameField) {
		Component component = findFormField(nameField);
		if (component != null) {
			component.setVisible(true);
			component.setEnabled(false);
			if (component instanceof AbstractField) {
				AbstractField formField = (AbstractField) component;
				formField.removeAllValidators();
				formField.setRequired(false);
			}
		}
		return component;
	}
	
	/**
	 * Hides the cancel button
	 */
	public void hideCancelButton() {
		cancelButton.setVisible(false);
	}
	
	/**
	 * Gets the form
	 * @return
	 */
	public FormLayout getForm() {
		return form;
	}

	/**
	 * Gets the main binder of the speciefied key
	 * Note: If there are embedded fields, every embedded bean has its own binder.
	 * @param key "" for the main bean binder, field name for nested beans
	 * @return The binder for the key or null is there isn't binder for the key
	 */
	@SuppressWarnings("unchecked")
	public BeanFieldGroup<T> getBinder(String key) {
		return (BeanFieldGroup<T>) binderMap.get(key);
	}

	/**
	 * Gets the current bean
	 * @return
	 */
	public T getBean() {
		return bean;
	}
	
	/**
	 * Adds the specified button to the buttons layout
	 * @param button
	 */
	public void addButon(Button button) {
		// Habilitamos el ButtonsLayout por si está deshabilitado
		buttonsLayout.setEnabled(true);
		// Añadimos el botón
		buttonsLayout.addComponent(button);
	}
}