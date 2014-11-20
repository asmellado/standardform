package es.vegamultimedia.standardform;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.BeanValidator;
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
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

import es.vegamultimedia.standardform.DAO.BeanDAO;
import es.vegamultimedia.standardform.annotations.StandardForm;
import es.vegamultimedia.standardform.annotations.StandardForm.DAOType;
import es.vegamultimedia.standardform.annotations.StandardFormEnum;
import es.vegamultimedia.standardform.annotations.StandardFormField;
import es.vegamultimedia.standardform.model.Bean;
import es.vegamultimedia.standardform.model.BeanMongo;

@SuppressWarnings("serial")
public class DetailForm<T extends Bean, K> extends Panel {
	
	/**
	 * Interface for listening for a event in a DetailForm 
	 */
	public interface SaveListener{
		/**
		 * Called before saving the bean
		 */
		public abstract void beforeSave(Bean bean, boolean insertMode);
		/**
		 * Called after saving the bean
		 */
		public abstract void afterSave(Bean bean, boolean insertMode);
	}
	
	private SaveListener saveListener;
	
	// BeanUI that created this standard detail form
	protected BeanUI<T, K> beanUI;
	
	// Binder del formulario
	protected BeanFieldGroup<T> binder;
	
	// Bean actual
	protected T bean;
	
	// Formulario
	protected FormLayout form;
	
	// Campos de Vaadin del formulario
	protected Component[] formFields;
	
	// Indica que estamos en modo alta
	protected boolean insertMode;
	
	// Botón guardar
	protected Button saveButton;
	
	// Botón cancelar
	protected Button cancelButton;
	
	/**
	 * Create a DetailForm for updating an existing bean or for inserting a new bean
	 * @param currentBeanUI
	 * @param currentBean Existing bean o null for a new bean
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public DetailForm(BeanUI<T, K> currentBeanUI, T currentBean)
			throws InstantiationException, IllegalAccessException {
		beanUI = currentBeanUI;
		bean = currentBean;
		
		try {
			// Inicializamos el elemento actual
			if (bean == null) {
				insertMode = true;
				bean = (T) newBean(beanUI.getBeanClass());
			}
			
			// Creamos el binder
			binder = new BeanFieldGroup<T>(beanUI.getBeanClass());
			binder.setItemDataSource(bean);
			
			// Creamos el formulario para albergar todos los campos del bean
			form = new FormLayout();
			setContent(form);
			
			// Obtenemos la anotación StandardForm del elementoActual
			StandardForm standardForm = bean.getClass().getAnnotation(StandardForm.class);
			
			// Asignamos el título al panel
			setCaption(standardForm.detailViewName());
			
			// Añadimos estilo personalizado
			addStyleName("standard-form");
			
			// Obtenemos los campos del formulario
			formFields = getFormFields(bean, "");
			
			// Si estamos en alta o se permite edición
			if (insertMode || standardForm.allowsEditing()) {
				// Añadimos el botón guardar
				saveButton = new Button("Guardar");
				saveButton.setId("saveButton");
				saveButton.setClickShortcut(KeyCode.ENTER);
				saveButton.addClickListener(new ClickListener(){
		
					private static final long serialVersionUID = 1L;
		
					@Override
					public void buttonClick(ClickEvent event) {
						save(event);
					}
				});
				form.addComponent(saveButton);
			}
			
			cancelButton = new Button("Cancelar");
			cancelButton.setId("cancelButton");
			cancelButton.addClickListener(new ClickListener(){
	
				private static final long serialVersionUID = 1L;
	
				@Override
				public void buttonClick(ClickEvent event) {
					showListForm();
				}
			});
			form.addComponent(cancelButton);
			
		} catch (Exception e) {
			Notification.show("Se ha producido un error", e.getMessage(), Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds the listener
	 */
	public void addListener(SaveListener saveListener) {
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
	 * @param prefixParentBean
	 * @return
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 */
	@SuppressWarnings("rawtypes")
	protected Component[] getFormFields(Bean currentBean, String prefixParentBean)
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
				// Si no hay anotación DetailField para este campo o el caption es ""
				if (!(standardFormField instanceof StandardFormField) ||
						standardFormField.caption().length() == 0) {
					// Asignamos como caption el nombre del campo con la primera letra en mayúscula
					caption = Utils.capitalizeFirstLetter(currentBeanFields[i].getName());
				}
				else {
					caption = standardFormField.caption();
				}
				// Comprobamos el tipo de campo
				switch (tipo) {
				// Si es un campo de selección
				case COMBO_BOX:
				case OPTION_GROUP:
				case MULTIPLE_SELECTION:
					// En este caso tenemos que crear el campo a mano
					// con todas las opciones y seleccionar el elemento actual
					currentFields[i] = getSelectField(currentBean, currentBeanFields[i], tipo, caption);
					// No añadimos el campo al binder porque no funciona correctamente en este caso
					break;
				// Si es un área de texto
				case TEXT_AREA:
					// Creamos el campo a mano y lo añadimos al binder
					currentFields[i] = new TextArea(caption);
					binder.bind((Field) currentFields[i], prefixParentBean + currentBeanFields[i].getName());
					break;
				// Si es un campo "normal"
				case TEXT_FIELD:
				case NUM_FIELD:
				case CHECK_BOX:
					// Construimos el campo directamente con el binder
					currentFields[i] = binder.buildAndBind(caption, prefixParentBean + currentBeanFields[i].getName());
					// Si es un campo de texto TextField, especificamos la longitud máxima (Vaadin no lo hace)
					if (currentFields[i] instanceof TextField) {
						// Obtenemos la anotación Size del campo del bean
						Size size = currentBeanFields[i].getAnnotation(Size.class);
						// Si hay anotación Size
						if (size instanceof Size) {
							((TextField)currentFields[i]).setMaxLength(size.max());
						}
						else {
							// Obtenemos la anotación Max del campo del bean
							Max max = currentBeanFields[i].getAnnotation(Max.class);
							if (max instanceof Max) {
								// Obtenemos el número de dígitos máximo posible
								int digitos = ((int)Math.floor(Math.log10(max.value()))) + 1;
								((TextField)currentFields[i]).setMaxLength(digitos);
							}
						}

					}
					break;
				// Si es un campo de fecha
				case DATE:
					// Creamos el campo a mano y lo añadimos al binder
					currentFields[i] = new PopupDateField(caption);
					// Deshabilitamos el campo de texto
					((PopupDateField)currentFields[i]).setTextFieldEnabled(false);
					binder.bind((Field) currentFields[i], prefixParentBean + currentBeanFields[i].getName());
					break;
				// Si es un campo embedded
				case EMBEDDED:
					// Obtenemos la clase del Bean anidado
					@SuppressWarnings("unchecked")
					Class<? extends Bean> embeddedBeanClass =
					(Class<? extends Bean>)currentBeanFields[i].getType();
					// Obtenemos el bean anidado
					Bean embeddedBean = (Bean) Utils.getFieldValue(currentBean, currentBeanFields[i]);
					// Si el campo del embeddedBean del elementoActual es null
					if (embeddedBean == null) {
						// Creamos un nuevo objeto embeddedBean vacío
						embeddedBean = embeddedBeanClass.newInstance();
						// Asignamos el embeddedBean al elementoActual
						Utils.setFieldValue(currentBean, currentBeanFields[i], embeddedBean);
					}					
					// Creamos un formulario anidado para albergar todos los campos del bean anidado
					FormLayout embeddedForm = new FormLayout();
					// Obtenemos los campos llamando recursivamente a esta función
					Component[] embeddedFields = getFormFields(embeddedBean, 
							prefixParentBean + currentBeanFields[i].getName() + ".");
					// Añadimos los campos al formulario
					for (Component field: embeddedFields) {
						// Comprobamos que existe (los campos deshabilitados no se crean en alta)
						if (field != null) {
							embeddedForm.addComponent(field);
						}
					}
					// Creamos un panel con el caption
					Panel panel = new Panel(caption);
					// Asignamos el formulario al panel
					panel.setContent(embeddedForm);
					// Añadimos el panel al formulario principal
					currentFields[i] = panel;
					break;
				case TABLE:
					// Obtenemos la clase parametrizada del arrayList
					java.lang.reflect.Type parametrizedType = Utils.getParametrizedType(currentBeanFields[i]);
					// Obtenemos la lista
					List lista = (List) Utils.getFieldValue(currentBean, currentBeanFields[i]);
					// Si la lista está vacía
					if (lista == null) {
						// Creamos un nueva lista vacía
						lista = new ArrayList();
						// Asignamos el embeddedBean al elementoActual
						Utils.setFieldValue(currentBean, currentBeanFields[i], lista);
					}
					// Creamos la tabla
					currentFields[i] = new Table(caption);
					((Table) currentFields[i]).setImmediate(true);
					((Table) currentFields[i]).setPageLength(3);
					// Creamos el container y se lo asignamos
					@SuppressWarnings("unchecked")
					BeanItemContainer container = new BeanItemContainer((Class)parametrizedType, lista);
					((Table) currentFields[i]).setContainerDataSource(container);
					break;
				default:
					break;
				}
				// Comprobamos por precaución si se ha creado el campo
				if (currentFields[i] != null) {
					// Asignamos al campo como id el nombre del campo del bean actual
					currentFields[i].setId(prefixParentBean + currentBeanFields[i].getName());
					
					// Si no es un embedded field ni una tabla
					if (tipo != StandardFormField.Type.EMBEDDED &&
							tipo != StandardFormField.Type.TABLE) {
						// Se especifica la anchura del campo
						// TODO Hacer con estilos css?
						currentFields[i].setWidth(30, Unit.EM);
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
						((AbstractField)currentFields[i]).removeAllValidators();
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
			// Si el tipo de DAO es JPA:
			// Si tiene anotación Temporal con el valor TemporalType.DATE
			if (beanField.getAnnotation(Temporal.class) != null &&
				beanField.getAnnotation(Temporal.class).value() == TemporalType.DATE)
				// Retorna el tipo DATE
				return StandardFormField.Type.DATE;
			// TODO No se soporta de momento TemporalType.TIME ni TemporalType.TIMESTAMP
			else
				return null;
		}
		// Si el tipo de campos es un enumerado
		else if (tipoBean.isEnum()) {
			// Retorna el tipo COMBO_BOX
			return StandardFormField.Type.COMBO_BOX;
		}
		// Si el tipo de campos es un List ó ArrayList
		else if (tipoBean == List.class ||
				tipoBean == ArrayList.class) {
			// Obtenemos la clase parametrizada del arrayList
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
				return StandardFormField.Type.COMBO_BOX;
			}
		}
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
			// Dado que los campos de selección no están incluídos en el binder, tenemos que hacer commit a mano
			commitSelectFields(bean, formFields);
			// Hacemos commit del resto de campos
			binder.commit();
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
				if (tipo == StandardFormField.Type.EMBEDDED) {
					// Obtenemos el DAO
					BeanDAO dao = 
							Utils.getBeanDAO((Class)(beanFields[i].getType()), beanUI.getBeanDAO());
					// Obtenemos el bean anidado
					Bean embeddedBean = (Bean) Utils.getFieldValue(bean, beanFields[i]);
					// Tenemos que insertar el documento
					if (insertMode) {
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
			Notification.show("El elemento se ha actualizado correctamente");
			// Y mostramos el listado
			showListForm();
		} catch (CommitException e) {
			e.printStackTrace();
			Notification.show("Error de validación",
//					"Algún campo no supera las validaciones. Por favor, revise el formulario",
					(e.getCause() == null) ? e.getMessage() : e.getCause().getMessage(),
					Type.WARNING_MESSAGE);
		} catch (java.util.ConcurrentModificationException e) {
			Notification.show("No se puede guardar",
					"Este elemento ha sido modificado mientras lo estaba editando. Inténtelo de nuevo",
					Type.ERROR_MESSAGE);
			// Mostramos el listado
			showListForm();
		} catch (Exception e) {
			Notification.show("No se ha podido realizar la operación", e.getMessage(), Type.ERROR_MESSAGE);
			e.printStackTrace();
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
				Bean currentBean,
				java.lang.reflect.Field field, 
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
		AbstractSelect campoSelect = null;
		Class tipoElementos;
		
		// Si es un List ó ArrayList
		if (field.getType() == ArrayList.class) {
			// Obtenemos la clase parametrizada del arrayList
			java.lang.reflect.Type parametrizedType = Utils.getParametrizedType(field);
			tipoElementos = (Class) parametrizedType;
		}
		// Si NO es un arrayList obtenemos directamente el tipo de elementos
		else {
			tipoElementos = field.getType();
		}
		
		// Si es un bean anidado
		if (Utils.isSubClass(tipoElementos, Bean.class)) {
			// Obtenemos todos los elementos del bean anidado
			// Obtenemos la clase del Bean anidado
			Class<? extends Bean> claseBeanAnidado = (Class<? extends Bean>)tipoElementos;
			// Obtenemos una instancia del BeanDAO anidado
			BeanDAO<? extends Bean, K> beanDAO = Utils.getBeanDAO(claseBeanAnidado, beanUI.getBeanDAO());
			// Obtenemos todos los elementos del bean anidado
			List<? extends Bean> listaElementos = beanDAO.getAllElements();
			
			// Creamos un contenedor con todos los elementos
			BeanItemContainer container =
					new BeanItemContainer(claseBeanAnidado, listaElementos);
			
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
			
			campoSelect.setItemCaptionMode(ItemCaptionMode.EXPLICIT_DEFAULTS_ID);
			// Recorremos todos los elementos del enumerado
			for (Object elementoEnum: elementosEnum) {
				// Se obtiene anotación StandardFormEnum del elemento
				try {
					java.lang.reflect.Field elementoField = enumeradoClass.getField(elementoEnum.toString());
					StandardFormEnum anotación = elementoField.getAnnotation(StandardFormEnum.class);
					// Si tiene anotación StandardFormEnum informada
					if (anotación != null && anotación.value().length() != 0)
						// Se asigna el valor como caption
						campoSelect.setItemCaption(elementoEnum, anotación.value());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
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
	@SuppressWarnings("unchecked")
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
					// Creamos un arrayList y le añadimos los elementos seleccionados
					@SuppressWarnings("rawtypes")
					ArrayList arrayListElementosSeleccionados = new ArrayList();
					for (Object itemId : selectField.getItemIds()) {
						if (selectField.isSelected(itemId)) {
							arrayListElementosSeleccionados.add(itemId);
						}
					}
					// Comprobamos si el campo es obligatorio y no hay ningún elemento seleccionado
					if (beanFields[i].getAnnotation(NotNull.class) instanceof NotNull
							&& arrayListElementosSeleccionados.size() == 0) {
						selectField.setRequiredError("Obligatorio");
						throw new CommitException("Debe seleccionar al menos un elemento");
					}
					// Asignamos al elemento actual el arraylist de elementos seleccionados
					Utils.setFieldValue(currentBean, beanFields[i], arrayListElementosSeleccionados);
				}
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
		// Ocultamos el botón guardar
		saveButton.setVisible(false);
		// Habilitamos el botón cancelar
		cancelButton.setEnabled(true);
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
	
	public FormLayout getForm() {
		return form;
	}

	public BeanFieldGroup<T> getBinder() {
		return binder;
	}

	public T getBean() {
		return bean;
	}
}