package es.vegamultimedia.standardform;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

import es.vegamultimedia.doplan.DoplanUI;
import es.vegamultimedia.doplan.model.Bean;
import es.vegamultimedia.standardform.annotations.StandardForm;
import es.vegamultimedia.standardform.annotations.StandardFormField;

@SuppressWarnings("serial")
public abstract class DetailView<T extends Bean> extends FormLayout implements View {
	
	private BeanFieldGroup<T> binder;
	private T elemento;
	@SuppressWarnings("rawtypes")
	private Field[] formFields;
	
	private java.lang.reflect.Field[] fields;
	
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
		
		// Creamos un FieldFactory personalizado para personalizar los campos 
		binder.setFieldFactory(new StandardFormFieldFactory());
		
		// Obtenemos los campos del bean elemento
		fields = elemento.getClass().getDeclaredFields();
		
		// Creamos el array de campos del formulario con el número de campos del bean 
		formFields = new Field[fields.length];
		
		try {
		// Recorremos los campos del bean 
			for (int i=0;i<fields.length;i++) {
				// Obtenemos la anotación DetailField
				StandardFormField detailField = fields[i].getAnnotation(StandardFormField.class);
				// Si hay anotación DetailField
				if (detailField instanceof StandardFormField) {
					// Comprobamos el tipo de campo
					switch (detailField.type()) {
					// Si el campo es un combo box
					case COMBO_BOX:
						// TODO COMBO BOXES NO INCLUIDAS EN EL BINDER
						// Si hacemos bind del campo, Vaadin muestra el combobox como una etiqueta. NO sabemos por qué
						// Dado que los combo boxes no están incluídos en el binder, tenemos que crear el combo box a mano
						// Y seleccionar el elemento actual
						formFields[i] = obtenerComboBox((Class<Object>)fields[i].getType(), detailField.caption(), fields[i].getName());
						break;
					// Si es un campo de texto
					case TEXT_FIELD:
						formFields[i] = binder.buildAndBind(detailField.caption(), fields[i].getName());
						break;
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
		Navigator navigator = ((DoplanUI)getUI()).getNavigator();
		ListView<T> vistaListado = getListadoView();
		String name = getBeanClass().getAnnotation(StandardForm.class).listViewName();
		navigator.addView(name, vistaListado);
		navigator.navigateTo(name);
	}
	
	private void guardar(ClickEvent event) {
		EntityTransaction transaction = null;
		try {
			// TODO COMBO BOXES NO INCLUIDAS EN EL BINDER 
			// Dado que los combo boxes no están incluídos en el binder, tenemos que hacer commit a mano
			commitComboBoxes();
			
			binder.commit();
			
			elemento = binder.getItemDataSource().getBean();
			EntityManager entityManager = ((DoplanUI)getUI()).getEntityManager();
			transaction = entityManager.getTransaction();
			transaction.begin();
			entityManager.persist(elemento);
			transaction.commit();
			Notification.show("El elemento se ha actualizado correctamente");
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
	
	// TODO COMBO BOXES NO INCLUIDAS EN EL BINDER
	// Si hacemos bind del campo, Vaadin muestra el combobox como una etiqueta. NO sabemos por qué
	// Dado que los combo boxes no están incluídos en el binder, tenemos que crear el combo box a mano
	// Y seleccionar el elemento actual
	private ComboBox obtenerComboBox(Class<Object> claseBeanAnidado, String etiquetaComboBox, String nombreCampo)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		// Obtenemos todos los elementos del bean anidado
		EntityManager entityManager = ((DoplanUI)getUI()).getEntityManager();
		String consulta = "SELECT e FROM " + claseBeanAnidado.getSimpleName() + " e";
		Query query = entityManager.createQuery(consulta);
		List<Object> listaElementos = query.getResultList();
		// Creamos un contenedor con todos los elementos
		BeanItemContainer<Object> container = new BeanItemContainer<Object>(claseBeanAnidado, listaElementos);

		// Creamos un combo box con el contenedor 
		ComboBox combo = new ComboBox(etiquetaComboBox, container);

		// Establecemos la propiedad que se muestra
		combo.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		combo.setItemCaptionPropertyId("nombre");
		
		// Seleccionamos el elemento actual del bean anidado
		// Ponemos la primera letra en mayúscula
		nombreCampo = nombreCampo.substring(0, 1).toUpperCase() + nombreCampo.substring(1);
		// Obtenemos el método "get" del campo actual
		Method getMethod = elemento.getClass().getDeclaredMethod("get"+nombreCampo, null);
		Object beanAnidado = getMethod.invoke(elemento, null);
		if (beanAnidado != null) {
			combo.setValue(beanAnidado);
		}
		return combo;
	}

	// TODO COMBO BOXES NO INCLUIDAS EN EL BINDER 
	// Dado que los combo boxes no están incluídos en el binder, tenemos que hacer commit a mano
	private void commitComboBoxes() throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException, CommitException {
		for (int i=0;i<fields.length;i++) {
			// Obtenemos la anotación DetailField
			StandardFormField detailField = fields[i].getAnnotation(StandardFormField.class);
			// Si el tipo de campo es ComboBox
			if (detailField instanceof StandardFormField && detailField.type() == StandardFormField.Type.COMBO_BOX) {
				// Obtenemos el nombre del campo actual
				String nombreCampo = fields[i].getName();
				// Ponemos la primera letra en mayúscula
				nombreCampo = nombreCampo.substring(0, 1).toUpperCase() + nombreCampo.substring(1);
				// Obtenemos el método "set" del campo actual
				Method getMethod = elemento.getClass().getDeclaredMethod("set"+nombreCampo, fields[i].getType());
				// Obtenemos el elemento seleccionado en el combo box
				Object elementoSeleccionadoCombo = ((ComboBox)formFields[i]).getValue();
				// Comprobamos si el campo es obligatorio y no hay ningún elemento seleccionado
				if (fields[i].getAnnotation(NotNull.class) instanceof NotNull
						&& elementoSeleccionadoCombo==null) {
					throw new CommitException("El campo es obligatorio");
				}
				// Obtenemos el bean del binder
				T binderBean = binder.getItemDataSource().getBean();
				// Asignamos el bean del binder el elemento seleccionado en el combo box
				getMethod.invoke(binderBean, elementoSeleccionadoCombo);
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
