package es.vegamultimedia.standardform.views;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.BeanValidator;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

import es.vegamultimedia.standardform.annotations.StandardForm;
import es.vegamultimedia.standardform.annotations.StandardFormField;
import es.vegamultimedia.standardform.model.Bean;
import es.vegamultimedia.standardform.test.StandardFormUI;

@SuppressWarnings("serial")
public abstract class DetailView<T extends Bean> extends FormLayout implements View {
	
	// Binder del formulario
	private BeanFieldGroup<T> binder;

	// Bean actual
	private T elemento;
	
	// Campos del bean actual
	private java.lang.reflect.Field[] beanFields;
	
	// Campos de Vaadin del formulario
	@SuppressWarnings("rawtypes")
	private Field[] formFields;
	
	public DetailView(T elementoActual) {
		elemento = elementoActual;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		if (elemento == null) {
			elemento = getBeanVacio();
		}
		binder = new BeanFieldGroup<T>(getBeanClass());
		binder.setItemDataSource(elemento);
		
		// Obtenemos los campos del bean elemento
		beanFields = elemento.getClass().getDeclaredFields();
		
		// Creamos el array de campos del formulario con el número de campos del bean 
		formFields = new Field[beanFields.length];
		
		try {
		// Recorremos los campos del bean 
			for (int i=0;i<beanFields.length;i++) {
				// Obtenemos la anotación DetailField
				StandardFormField detailField = beanFields[i].getAnnotation(StandardFormField.class);
				// Si hay anotación DetailField
				if (detailField instanceof StandardFormField) {
					// Comprobamos el tipo de campo
					switch (detailField.type()) {
					// Si es un campo de selección con un bean anidado
					case COMBO_BOX:
					case OPTION_GROUP:
						// En el caso de un campo con bean anidado, tenemos que crear el campo a mano
						// con todas las opciones y seleccionar el elemento actual
						formFields[i] = obtenerCampoSelección(beanFields[i], detailField);
						// No añadimos el campo al binder porque no funciona correctamente en este caso
						break;
					// Si es un área de texto
					case TEXT_AREA:
						// Creamos el campo a mano y lo añadimos al binder
						formFields[i] = new TextArea(detailField.caption());
						binder.bind(formFields[i], beanFields[i].getName());
						break;
					// Si es un campo "normal"
					case TEXT_FIELD:
					case CHECK_BOX:
						// Construimos el campo directamente con el binder
						formFields[i] = binder.buildAndBind(detailField.caption(), beanFields[i].getName());
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
						if (!detailField.help().isEmpty()) {
							// Añadimos etiqueta con la ayuda
							addComponent(new Label(detailField.help()));
						}
					}
				}
			}
	
			Button botónGuardar = new Button("Guardar");
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

	private void mostrarListado() {
		Navigator navigator = ((StandardFormUI)getUI()).getNavigator();
		ListView<T> vistaListado = getListadoView();
		String name = getBeanClass().getAnnotation(StandardForm.class).listViewName();
		navigator.addView(name, vistaListado);
		navigator.navigateTo(name);
	}
	
	private void guardar(ClickEvent event) {
		EntityTransaction transaction = null;
		try {
			// Dado que los campos de selección no están incluídos en el binder, tenemos que hacer commit a mano
			commitCamposSelección();
			// Hacemos commit del resto de campos
			binder.commit();
			// Almacenamos la entidad en base de datos de forma persistente
			elemento = binder.getItemDataSource().getBean();
			EntityManager entityManager = ((StandardFormUI)getUI()).getEntityManager();
			transaction = entityManager.getTransaction();
			transaction.begin();
			entityManager.persist(elemento);
			transaction.commit();
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
			if (transaction!=null) {
				transaction.rollback();
			}
		}
	}
	
	// En el caso de un campo con bean anidado, tenemos que crear el campo a mano
	// con todas las opciones y seleccionar el elemento actual
	private AbstractSelect obtenerCampoSelección(java.lang.reflect.Field field, StandardFormField detailField)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		// El tipo de campo debe ser COMBO_BOX u OPTION_GROUP
		if (detailField.type() != StandardFormField.Type.COMBO_BOX &&
			detailField.type() != StandardFormField.Type.OPTION_GROUP) {
			return null;
		}
		AbstractSelect campoSelect;
		// Obtenemos todos los elementos del bean anidado
		EntityManager entityManager = ((StandardFormUI)getUI()).getEntityManager();
		// Obtenemos el Bean anidado
		Class<Object> claseBeanAnidado = (Class<Object>)field.getType();
		// Obtenemos los elementos del bean anidado
		String consulta = "SELECT e FROM " + claseBeanAnidado.getSimpleName() + " e";
		Query query = entityManager.createQuery(consulta);
		List<Object> listaElementos = query.getResultList();
		// Creamos un contenedor con todos los elementos
		BeanItemContainer<Object> container = new BeanItemContainer<Object>(claseBeanAnidado, listaElementos);
		
		// Creamos el campo en función del tipo
		if (detailField.type() == StandardFormField.Type.COMBO_BOX) {
			// Creamos un combo box con el contenedor 
			campoSelect = new ComboBox(detailField.caption(), container);
		}
		else {
			// Creamos un Option Group con el contenedor 
			campoSelect = new OptionGroup(detailField.caption(), container);
		}
		// Establecemos la propiedad que se muestra
		campoSelect.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		campoSelect.setItemCaptionPropertyId("nombre");
		
		// Añadimos un validador de tipo BeanValidator para el campo
		campoSelect.addValidator(new BeanValidator(getBeanClass(), field.getName()));
		
		// Obtenemos el valor del elemento actual del bean anidado para seleccionarlo
		// Obtenemos el nombre del campo
		String nombreCampo = field.getName();
		// Ponemos la primera letra en mayúscula
		nombreCampo = nombreCampo.substring(0, 1).toUpperCase() + nombreCampo.substring(1);
		// Obtenemos el método "get" del campo actual
		Method getMethod = elemento.getClass().getDeclaredMethod("get"+nombreCampo, null);
		// Llamamos al método
		Object beanAnidado = getMethod.invoke(elemento, null);
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
			// Obtenemos la anotación DetailField
			StandardFormField detailField = beanFields[i].getAnnotation(StandardFormField.class);
			// Si el tipo de campo es COMBO_BOX u OPTION_GROUP
			if (detailField instanceof StandardFormField && 
					(detailField.type() == StandardFormField.Type.COMBO_BOX ||
						detailField.type() == StandardFormField.Type.OPTION_GROUP)) {
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
	
	/**
	 * Retorna la clase del bean (T)
	 * @return
	 */
	abstract protected Class<T> getBeanClass();

	/**
	 * Retorna un Bean (T) vacío
	 * @return
	 */
	abstract protected T getBeanVacio();
	
	/**
	 * Retorna la View que muestra el detalle
	 * @return
	 */
	abstract protected ListView<T> getListadoView();

}
