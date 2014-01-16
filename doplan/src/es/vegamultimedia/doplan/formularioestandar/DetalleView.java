package es.vegamultimedia.doplan.formularioestandar;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.DefaultFieldGroupFieldFactory;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

import es.vegamultimedia.doplan.DoplanUI;
import es.vegamultimedia.doplan.model.Organizacion;

@SuppressWarnings("serial")
public abstract class DetalleView<T> extends FormLayout implements View {
	
	private BeanFieldGroup<T> binder;
	private T elemento;
	
	public DetalleView(T elementoActual) {
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
		binder.setFieldFactory(new DefaultFieldGroupFieldFactory(){

			private static final long serialVersionUID = 1L;

			@Override
			public <T extends Field> T createField(Class<?> type,
					Class<T> fieldType) {
				T campo = super.createField(type, fieldType);
				// Si es un campo de texto se establece el NullRepresentation
				if (campo instanceof AbstractTextField) {
					((AbstractTextField)campo).setNullRepresentation("");
				}
				return campo;
			}
		});
		
		Campo[] campos = getCampos();
		
		for (Campo campo : campos) {
			addComponent(binder.buildAndBind(campo.getCaption(), campo.getName()));
		}

		Button botónGuardar = new Button("Guardar");
		botónGuardar.addClickListener(new ClickListener(){

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					binder.commit();
					elemento = binder.getItemDataSource().getBean();
					EntityManager entityManager = ((DoplanUI)getUI()).getEntityManager();
					EntityTransaction transaction = entityManager.getTransaction();
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
				}
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
	}
	
	private void mostrarListado() {
		Navigator navigator = ((DoplanUI)getUI()).getNavigator();
		ListadoView<T> vistaListado = getListadoView();
		navigator.addView(vistaListado.getNombre(), vistaListado);
		navigator.navigateTo(vistaListado.getNombre());
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
	 * Obtiene el nombre de la vista, que se muestra en la URL
	 * @return
	 */
	abstract public String getNombre();
	
	/**
	 * Retorna un array con los campos del formulario en el orden en el que deben mostrarse
	 * @return
	 */
	abstract protected Campo[] getCampos();
	
	/**
	 * Retorna la View que muestra el detalle
	 * @return
	 */
	abstract protected ListadoView<T> getListadoView();

}
