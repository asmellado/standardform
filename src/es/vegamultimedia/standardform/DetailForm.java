package es.vegamultimedia.standardform;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

import es.vegamultimedia.standardform.DAO.BeanDAO;
import es.vegamultimedia.standardform.annotations.StandardForm;
import es.vegamultimedia.standardform.annotations.StandardForm.DAOType;
import es.vegamultimedia.standardform.annotations.StandardFormField;
import es.vegamultimedia.standardform.model.Bean;

@SuppressWarnings("serial")
public class DetailForm<T extends Bean> extends FormLayout {
	
	// BeanUI that created this standard detail form
	protected BeanUI<T> beanUI;
	
	// Binder del formulario
	protected BeanFieldGroup<T> binder;
	
	// Bean actual
	protected T elemento;
	
	// Campos del bean actual
	protected java.lang.reflect.Field[] beanFields;
	
	// Campos de Vaadin del formulario
	@SuppressWarnings("rawtypes")
	protected Field[] formFields;
	
	public DetailForm(BeanUI<T> beanUI, T currenElement)
			throws InstantiationException, IllegalAccessException {
		this.beanUI = beanUI;
		elemento = currenElement;
		if (elemento == null) {
			elemento = beanUI.getBeanClass().newInstance();
		}
		binder = new BeanFieldGroup<T>(beanUI.getBeanClass());
		binder.setItemDataSource(elemento);
		
		// Obtenemos la anotación StandardForm del elemento
		StandardForm standardForm = elemento.getClass().getAnnotation(StandardForm.class);
		
		// Obtenemos los campos del bean elemento
		beanFields = elemento.getClass().getDeclaredFields();
		
		// Creamos el array de campos del formulario con el número de campos del bean 
		formFields = new Field[beanFields.length];
		
		try {
		// Recorremos los campos del bean 
			for (int i=0;i<beanFields.length;i++) {
				String caption;
				// Obtenemos la anotación DetailField
				StandardFormField detailField = beanFields[i].getAnnotation(StandardFormField.class);
				// Obtenemos el tipo de campo en función de los metadatos
				StandardFormField.Type tipo = getTypeFormField(standardForm, beanFields[i], detailField);
				// Si se ha encontrado un tipo
				if (tipo != null) {
					// Si no hay anotación DetailField para este campo o el caption es ""
					if (!(detailField instanceof StandardFormField) ||
							detailField.caption().length() == 0) {
						// Asignamos como caption el nombre del campo con la primera letra en mayúscula
						caption = Utils.capitalizeFirstLetter(beanFields[i].getName());
					}
					else {
						caption = detailField.caption();
					}
					// Comprobamos el tipo de campo
					switch (tipo) {
					// Si es un campo de selección con un bean anidado
					case COMBO_BOX:
					case OPTION_GROUP:
						// En el caso de un campo con bean anidado, tenemos que crear el campo a mano
						// con todas las opciones y seleccionar el elemento actual
						formFields[i] = obtenerCampoSelección(beanFields[i], tipo, caption);
						// No añadimos el campo al binder porque no funciona correctamente en este caso
						break;
					// Si es un área de texto
					case TEXT_AREA:
						// Creamos el campo a mano y lo añadimos al binder
						formFields[i] = new TextArea(caption);
						binder.bind(formFields[i], beanFields[i].getName());
						break;
					// Si es un campo "normal"
					case TEXT_FIELD:
					case CHECK_BOX:
						// Construimos el campo directamente con el binder
						formFields[i] = binder.buildAndBind(caption, beanFields[i].getName());
						// Si es un campo de texto TextField, especificamos la longitud máxima (Vaadin no lo hace)
						if (formFields[i] instanceof TextField) {
							// Obtenemos la anotación Size del campo del bean
							Size size = beanFields[i].getAnnotation(Size.class);
							// Si hay anotación DetailField
							if (size instanceof Size) {
								((TextField)formFields[i]).setMaxLength(size.max());
							}
						}
						break;
					// Si es un campo de fecha
					case DATE:
						// Creamos el campo a mano y lo añadimos al binder
						formFields[i] = new PopupDateField(caption);
						// Deshabilitamos el campo de texto
						((PopupDateField)formFields[i]).setTextFieldEnabled(false);
						binder.bind(formFields[i], beanFields[i].getName());
						break;
					default:
						break;
					}
					// Comprobamos por precaución si se ha creado el campo
					if (formFields[i] != null) {
						// Se especifica la representación del null
						if (formFields[i] instanceof AbstractTextField) {
							((AbstractTextField)formFields[i]).setNullRepresentation("");
						}
						// Añadimos el campo al formulario
						addComponent(formFields[i]);
						// Si hay ayuda
						if (detailField instanceof StandardFormField && !detailField.help().isEmpty()) {
							// Añadimos etiqueta con la ayuda
							addComponent(new Label(detailField.help()));
						}
					}
				}
			}
	
			Button botónGuardar = new Button("Guardar");
			botónGuardar.setClickShortcut(KeyCode.ENTER);
			botónGuardar.addClickListener(new ClickListener(){
	
				private static final long serialVersionUID = 1L;
	
				@Override
				public void buttonClick(ClickEvent event) {
					guardar(event);
				}
			});
			addComponent(botónGuardar);
			
			Button botónCancelar = new Button("Cancelar");
			botónCancelar.addClickListener(new ClickListener(){
	
				private static final long serialVersionUID = 1L;
	
				@Override
				public void buttonClick(ClickEvent event) {
					mostrarListado();
				}
			});
			addComponent(botónCancelar);
			
		} catch (Exception e) {
			Notification.show("Se ha producido un error", e.getMessage(), Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * Obtiene el tupo de campo
	 * @param standardForm Anotación standardForm del bean
	 * @param beanField Campo del bean
	 * @param detailField Anotación standardFormField del campo del bean
	 * @return Tipo de campo obtenido
	 */
	private StandardFormField.Type getTypeFormField(
			StandardForm standardForm, java.lang.reflect.Field beanField, StandardFormField detailField) {
		// Si hay anotación DetailField para este campo y el type no es DEFAULT
		if ((detailField instanceof StandardFormField) &&
				detailField.type() != StandardFormField.Type.DEFAULT) {
			// Retornamos el tipo especificado
			return detailField.type();
		}
		// En caso contrario obtenemos el tipo por defecto según el tipo de bean
		// Si el tipo de campos es un String
		else if (beanField.getType() == String.class) {
			// Si tiene anotación Lob
			if (beanField.getAnnotation(Lob.class) != null)
				// retorna el tipo TEXT_AREA
				return StandardFormField.Type.TEXT_AREA;
			// En caso contrario retorna el tipo TEXT_FIELD 
			return StandardFormField.Type.TEXT_FIELD;
		}
		// Si el tipo de campo es boolean
		else if (beanField.getType() == Boolean.TYPE)
			return StandardFormField.Type.CHECK_BOX;
		// Si el tipo de campo el Date
		else if (beanField.getType() == Date.class)
			// Si tiene anotación Temporal con el valor TemporalType.DATE
			if (beanField.getAnnotation(Temporal.class) != null &&
				beanField.getAnnotation(Temporal.class).value() == TemporalType.DATE)
				// Retorna el tipo DATE
				return StandardFormField.Type.DATE;
			// TODO No se soporta de momento TemporalType.TIME ni TemporalType.TIMESTAMP
			else
				return null;
		// Si el tipo de campo es otro Bean
		try {
			if (beanField.getType().asSubclass(Bean.class) != null)
				// Si el tipo de DAO es Mongo y el campo NO tiene la anotación @Reference
				if (standardForm.daoType() == DAOType.MONGO &&
						beanField.getAnnotation(Reference.class) == null) {
					// TODO Debería mostrar un subformulario con todos los campos del bean anidado
					return null;
				}
				else {
					// Retorna el tipo COMBO_BOX
					return StandardFormField.Type.COMBO_BOX;
				}
		} catch (ClassCastException ignorada) { }
		return null;
	}

	private void mostrarListado() {
		FormLayout vistaListado = beanUI.getListForm();
		ComponentContainer contentPanel = (ComponentContainer)getParent();
		contentPanel.replaceComponent(this, vistaListado);
	}
	
	private void guardar(ClickEvent event) {
		try {
			// Dado que los campos de selección no están incluídos en el binder, tenemos que hacer commit a mano
			commitCamposSelección();
			// Hacemos commit del resto de campos
			binder.commit();
			// Almacenamos la entidad en base de datos de forma persistente
			beanUI.getBeanDAO().save(elemento);
			// Si todo ha ido bien, mostramos mensaje informativo
			Notification.show("El elemento se ha actualizado correctamente");
			// Y mostramos el listado
			mostrarListado();
		} catch (CommitException e) {
			Notification.show("No se puede guardar\n",
					"Algún campo no supera las validaciones. Por favor, revise el formulario", Type.WARNING_MESSAGE);
		} catch (Exception e) {
			Notification.show("No se ha podido realizar la operación", e.getMessage(), Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	// En el caso de un campo con bean anidado, tenemos que crear el campo a mano
	// con todas las opciones y seleccionar el elemento actual
	@SuppressWarnings({"rawtypes", "unchecked" })
	private AbstractSelect obtenerCampoSelección(
				java.lang.reflect.Field field, 
				es.vegamultimedia.standardform.annotations.StandardFormField.Type tipo, 
				String caption)
				throws NoSuchMethodException, IllegalAccessException,
				InvocationTargetException, ClassNotFoundException,
				IllegalArgumentException, InstantiationException {
		// El tipo de campo debe ser COMBO_BOX u OPTION_GROUP
		if (tipo != StandardFormField.Type.COMBO_BOX &&
			tipo != StandardFormField.Type.OPTION_GROUP) {
			return null;
		}
		AbstractSelect campoSelect;
		
		// Obtenemos todos los elementos del bean anidado
		// Obtenemos la clase del Bean anidado
		Class<? extends Bean> claseBeanAnidado = (Class<? extends Bean>)field.getType();
		// Obtenemos una instancia del BeanDAO anidado
		BeanDAO<? extends Bean> beanDAO = Utils.getBeanDAO(claseBeanAnidado, beanUI.getBeanDAO());
		// Obtenemos todos los elementos del bean anidado
		List<? extends Bean> listaElementos = beanDAO.getAllElements();
		
		// Creamos un contenedor con todos los elementos
		BeanItemContainer container =
				new BeanItemContainer(claseBeanAnidado, listaElementos);
		
		// Creamos el campo en función del tipo
		if (tipo == StandardFormField.Type.COMBO_BOX) {
			// Creamos un combo box con el contenedor 
			campoSelect = new ComboBox(caption, container);
		}
		else {
			// Creamos un Option Group con el contenedor 
			campoSelect = new OptionGroup(caption, container);
		}
		// Establecemos la propiedad que se muestra
		campoSelect.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		campoSelect.setItemCaptionPropertyId("nombre");
		
		// Añadimos un validador de tipo BeanValidator para el campo
		campoSelect.addValidator(new BeanValidator(beanUI.getBeanClass(), field.getName()));
		
		// Obtenemos el valor del elemento actual del bean anidado para seleccionarlo
		// Obtenemos el nombre del campo y ponemos la primera letra en mayúscula
		String nombreCampo = Utils.capitalizeFirstLetter(field.getName());
		// Obtenemos el método "get" del campo actual
		Method getMethod = elemento.getClass().getDeclaredMethod("get"+nombreCampo);
		// Llamamos al método
		Object beanAnidado = getMethod.invoke(elemento);
		// Seleccionamos el elemento actual del bean anidado
		if (beanAnidado != null) {
			campoSelect.setValue(beanAnidado);
		}
		return campoSelect;
	}

	// Dado que los campos de selección no están incluídos en el binder, tenemos que hacer commit a mano
	private void commitCamposSelección() throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException, CommitException {
		for (int i=0;i<beanFields.length;i++) {
			// Si el tipo de campo es COMBO_BOX u OPTION_GROUP
			if (formFields[i] instanceof ComboBox ||
					formFields[i] instanceof OptionGroup) {
				// Obtenemos el nombre del campo actual
				String nombreCampo = beanFields[i].getName();
				// Ponemos la primera letra en mayúscula
				nombreCampo = nombreCampo.substring(0, 1).toUpperCase() + nombreCampo.substring(1);
				// Obtenemos el método "set" del campo actual
				Method getMethod = elemento.getClass().getDeclaredMethod("set"+nombreCampo, beanFields[i].getType());
				// Obtenemos el elemento seleccionado en el campo de selección
				Object elementoSeleccionado = ((AbstractSelect)formFields[i]).getValue();
				// Comprobamos si el campo es obligatorio y no hay ningún elemento seleccionado
				if (beanFields[i].getAnnotation(NotNull.class) instanceof NotNull
						&& elementoSeleccionado==null) {
					formFields[i].setRequiredError("Obligatorio");
					throw new CommitException("El campo es obligatorio");
				}
				// Obtenemos el bean del binder
				T binderBean = binder.getItemDataSource().getBean();
				// Asignamos el bean del binder el elemento seleccionado en el combo box
				getMethod.invoke(binderBean, elementoSeleccionado);
			}
		}
	}
}