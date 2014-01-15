package es.vegamultimedia.doplan.views;

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

public class OrganizacionDetalleView extends FormLayout implements View {
	
	private static final long serialVersionUID = 382920535352843108L;

	public static final String NOMBRE = "organizacion";
	
	private BeanFieldGroup<Organizacion> binder;
	private Organizacion organizacion;
	
	public OrganizacionDetalleView(Organizacion organizacionActual) {
		organizacion = organizacionActual;
		binder = new BeanFieldGroup<Organizacion>(Organizacion.class);
		binder.setItemDataSource(organizacion);
		
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
		
		addComponent(binder.buildAndBind("Nombre", "nombre"));
		addComponent(binder.buildAndBind("Persona de Contacto", "personaContacto"));
		addComponent(binder.buildAndBind("Email de Contacto", "emailContacto"));

		Button botónGuardar = new Button("Guardar");
		botónGuardar.addClickListener(new ClickListener(){

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					binder.commit();
					organizacion = binder.getItemDataSource().getBean();
					EntityManager entityManager = ((DoplanUI)getUI()).getEntityManager();
					EntityTransaction transaction = entityManager.getTransaction();
					transaction.begin();
					entityManager.persist(organizacion);
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

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

	}
	
	private void mostrarListado() {
		Navigator navigator = ((DoplanUI)getUI()).getNavigator();
		navigator.addView(OrganizacionListadoView.NOMBRE, new OrganizacionListadoView());
		navigator.navigateTo(OrganizacionListadoView.NOMBRE);
	}

}
