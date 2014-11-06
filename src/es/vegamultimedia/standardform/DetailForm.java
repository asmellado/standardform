package es.vegamultimedia.standardform;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
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
public class DetailForm<T extends Bean> extends Panel {
	
	// BeanUI that created this standard detail form
	protected BeanUI<T> beanUI;
	
	// Binder del formulario
	protected BeanFieldGroup<T> binder;
	
	// Bean actual
	protected T bean;
	
	// Formulario
	protected FormLayout form;
	
	// Campos de Vaadin del formulario
	protected Component[] formFields;
	
	// Indica que estamos en modo alta
	protected boolean addMode;
	
	@SuppressWarnings("unchecked")
	public DetailForm(BeanUI<T> currentBeanUI, T currentBean)
			throws InstantiationException, IllegalAccessException {
		beanUI = currentBeanUI;
		bean = currentBean;
		
		try {
			// Inicializamos el elemento actual
			if (bean == null) {
				addMode = true;
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
	
			Button botónGuardar = new Button("Guardar");
			botónGuardar.setClickShortcut(KeyCode.ENTER);
			botónGuardar.addClickListener(new ClickListener(){
	
				private static final long serialVersionUID = 1L;
	
				@Override
				public void buttonClick(ClickEvent event) {
					save(event);
				}
			});
			form.addComponent(botónGuardar);
			
			Button botónCancelar = new Button("Cancelar");
			botónCancelar.addClickListener(new ClickListener(){
	
				private static final long serialVersionUID = 1L;
	
				@Override
				public void buttonClick(ClickEvent event) {
					showListForm();
				}
			});
			form.addComponent(botónCancelar);
			
		} catch (Exception e) {
			Notification.show("Se ha producido un error", e.getMessage(), Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets every fields of the current bean, adding every fields of every superclass
	 * @param currentBean
	 * @return
	 */
	protected java.lang.reflect.Field[] getBeanFields(Bean currentBean) {
		// Obtenemos los campos del bean elementoActual
		java.lang.reflect.Field[] currentBeanFields = currentBean.getClass().getDeclaredFields();
		// Añadimos los campos de las superclases hasta llegar a Object
		Class<?> superclass = currentBean.getClass().getSuperclass();
		while (superclass != Object.class) {
			java.lang.reflect.Field[] fields = superclass.getDeclaredFields();
			ArrayList<java.lang.reflect.Field> beanFieldsList = new ArrayList<java.lang.reflect.Field>();
			beanFieldsList.addAll(Arrays.asList(currentBeanFields));
			for (java.lang.reflect.Field field : fields) {
				beanFieldsList.add(field);
			}
			currentBeanFields = beanFieldsList.toArray(new java.lang.reflect.Field[beanFieldsList.size()]);
			superclass = superclass.getSuperclass();
		}
		return currentBeanFields;
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
			try {
				// Si el campo es un bean anidado
				if (fieldBean.getType().asSubclass(Bean.class) != null) {
					// Creamos el objeto del bean anidado
					Bean nestedBean = newBean((Class<? extends Bean>) fieldBean.getType());
					// Obtenemos el método "Set" del campo actual
					Method setMethod = Utils.getSetMethod(bean.getClass(), fieldBean);
					// Llamamos al método set para asignar el bean anidado vacío
					setMethod.invoke(bean, nestedBean);
				}
			} catch (ClassCastException ignorada) { }
		}
		return bean;
	}

	// Obtiene un array de campos de Vaadin a partir del bean que se le pasa como argumento
	// Este método es recursivo para los beans "embebidos" de MongoDB
	/**
	 * Returns an array of Vaadin fields from the argument currentBean.
	 * This method is recursiv
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
		java.lang.reflect.Field[] currentBeanFields = getBeanFields(currentBean);
		
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
				// Si es un campo deshabilitado
				case DISABLED:
					// Si estamos en modo modificación
					if (!addMode) {
						// Mostramos el campo deshabilitado
						currentFields[i] = binder.buildAndBind(caption, prefixParentBean + currentBeanFields[i].getName());
						currentFields[i].setEnabled(false);
					}
					// En alta no se muestra el campo
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
					Class<? extends Bean> embeddedBeanClass = (Class<? extends Bean>)currentBeanFields[i].getType();
					// Obtenemos el bean anidado
					Bean embeddedBean = (Bean) Utils.getFieldValue(currentBean, currentBeanFields[i]);
					// Si el campo del embeddedBean del elementoActual es null
					if (embeddedBean == null) {
						// Creamos un nuevo objeto embeddedBean vacío
						embeddedBean = embeddedBeanClass.newInstance();
						// Asignamos el embeddedBean al elementoActual
						Method setMethod = Utils.getSetMethod(currentBean.getClass(), currentBeanFields[i]);
						setMethod.invoke(currentBean, embeddedBean);
					}
					
					// Creamos un formulario anidado para albergar todos los campos del bean anidado
					FormLayout embeddedForm = new FormLayout();
					// Obtenemos los campos llamando recursivamente a esta función
					Component[] embeddedFields = getFormFields(embeddedBean, 
							prefixParentBean + currentBeanFields[i].getName() + ".");
					// Añadimos los campos al formulario
					for (Component field: embeddedFields) {
						embeddedForm.addComponent(field);
					}
					// Creamos un panel con el caption
					Panel panel = new Panel(caption);
					// Asignamos el formulario al panel
					panel.setContent(embeddedForm);
					// Añadimos el panel al formulario principal
					currentFields[i] = panel;
					break;
				default:
					break;
				}
				// Comprobamos por precaución si se ha creado el campo
				if (currentFields[i] != null) {
					// Se especifica la representación del null
					if (currentFields[i] instanceof AbstractTextField) {
						((AbstractTextField)currentFields[i]).setNullRepresentation("");
					}
					// Se especifica la anchura del campo
					// TODO Pendiente de hacer con estilos css
//					currentFields[i].addStyleName("sf-field");
					currentFields[i].setWidth(30, Unit.EM);
					try {
						// Si es un BeanMongo y el campo tiene anotación Id
						if (currentBean.getClass().asSubclass(BeanMongo.class) != null &&
								currentBeanFields[i].getAnnotation(Id.class) != null) {
							// Si estamos en modo modificación
							if (!addMode)
								// se deshabilita el campo
								currentFields[i].setEnabled(false);
						}
					}
					catch (Exception ignorada) { }
					
					// Asignamos el valor por defecto
					setDefaultValue(currentBeanFields[i], currentFields[i], standardFormField);

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
		if (addMode && standardFormField instanceof StandardFormField &&
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
		// Si el tipo de campos es un ArrayList
		else if (tipoBean == ArrayList.class) {
			return StandardFormField.Type.MULTIPLE_SELECTION;
		}
		// Si el tipo de campo es otro Bean
		try {
			if (tipoBean.asSubclass(Bean.class) != null)
				// Si el tipo de DAO es Mongo y el campo NO tiene la anotación @Reference
				if (standardForm.daoType() == DAOType.MONGO &&
						beanField.getAnnotation(Reference.class) == null) {
					return StandardFormField.Type.EMBEDDED;
				}
				else {
					// Retorna el tipo COMBO_BOX
					return StandardFormField.Type.COMBO_BOX;
				}
		} catch (ClassCastException ignorada) { }
		return null;
	}

	/**
	 * Shows the ListForm inside the same component as this detailForm
	 */
	protected void showListForm() {
		Panel vistaListado = beanUI.getListForm();
		ComponentContainer contentPanel = (ComponentContainer)getParent();
		contentPanel.replaceComponent(this, vistaListado);
	}
	
	/**
	 * Saves the bean persistly using the BeanDAO
	 * If there is some validation error, it shows a warning notification
	 * If it catches another exception, it ishows a error notification
	 * @param event
	 */
	protected void save(ClickEvent event) {
		try {
			// Dado que los campos de selección no están incluídos en el binder, tenemos que hacer commit a mano
			commitSelectFields(bean, formFields);
			// Hacemos commit del resto de campos
			binder.commit();
			// Almacenamos la entidad en base de datos de forma persistente
			beanUI.getBeanDAO().save(bean);
			// Si todo ha ido bien, mostramos mensaje informativo
			Notification.show("El elemento se ha actualizado correctamente");
			// Y mostramos el listado
			showListForm();
		} catch (CommitException e) {
			Notification.show("No se puede guardar\n",
					"Algún campo no supera las validaciones. Por favor, revise el formulario",
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
		
		// Si es un arrayList
		if (field.getType() == ArrayList.class) {
			// Obtenemos la clase parametrizada del arrayList
			java.lang.reflect.Type genericType = field.getGenericType();
			// El tipo de elementos es el primer (y único) tipo parametrizado del array de tipos
			java.lang.reflect.Type[] parameterizedtypes =
					((ParameterizedType)genericType).getActualTypeArguments();
			tipoElementos = (Class) parameterizedtypes[0];
		}
		// Si NO es un arrayList obtenemos directamente el tipo de elementos
		else {
			tipoElementos = field.getType();
		}
		
		// Si es un bean anidado
		try {
			if (tipoElementos.asSubclass(Bean.class) != null) {
				// Obtenemos todos los elementos del bean anidado
				// Obtenemos la clase del Bean anidado
				Class<? extends Bean> claseBeanAnidado = (Class<? extends Bean>)tipoElementos;
				// Obtenemos una instancia del BeanDAO anidado
				BeanDAO<? extends Bean> beanDAO = Utils.getBeanDAO(claseBeanAnidado, beanUI.getBeanDAO());
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
				// Establecemos la propiedad que se muestra
				campoSelect.setItemCaptionMode(ItemCaptionMode.PROPERTY);
				// TODO La propiedad a mostrar debería ser parametrizable. Ahora se hardcodea a "nombre"
				campoSelect.setItemCaptionPropertyId("nombre");
				
				// Añadimos un validador de tipo BeanValidator para el campo
				campoSelect.addValidator(new BeanValidator(currentBean.getClass(), field.getName()));
			}
		} catch (ClassCastException ignorada) { }
		
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
	 * Execute a commit for every Vaadin select fields in the form.
	 * It is neccesary because the selects fields aren't included inside the binder
	 * This method is recursive, so it executes the commit for every nested beans
	 * @param currentElement
	 * @param currentFormFields
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws CommitException
	 */
	@SuppressWarnings("unchecked")
	protected void commitSelectFields(Bean currentElement, Component[] currentFormFields)
			throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException, CommitException {
		java.lang.reflect.Field[] beanFields = getBeanFields(currentElement);
		for (int i=0;i<beanFields.length;i++) {
			// Si el tipo de campo es COMBO_BOX u OPTION_GROUP
			if (currentFormFields[i] instanceof ComboBox ||
					currentFormFields[i] instanceof OptionGroup) {
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
					Utils.setFieldValue(currentElement, beanFields[i], elementoSeleccionado);
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
					Utils.setFieldValue(currentElement, beanFields[i], arrayListElementosSeleccionados);
				}
			}
			// Si es un campo EMBEDDED, hay que hacer commit recursivamente
			// TODO Sería más correcto usar el tipo de campo EMBEDDED, en vez del tipo de componente de Vaadin
			else if (currentFormFields[i] instanceof Panel) {
				try{
					// Nos aseguramos de que es un bean anidado
					if (beanFields[i].getType().asSubclass(Bean.class) != null) {
						// Obtenemos el beanAnidado
						Bean beanAnidado = (Bean) Utils.getFieldValue(currentElement, beanFields[i]);
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
				} catch (ClassCastException ignorada) {
				} catch (Exception e) {
					// Si el método getFieldValue() lanza una excepción mostramos traza
					e.printStackTrace();
				}
			}
		}
	}
}