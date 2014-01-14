package es.vegamultimedia.doplan.views;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
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
				} catch (Exception e) {
					Notification.show("No se ha podido realizar la operación", e.getMessage(), Type.ERROR_MESSAGE);
				}
				mostrarListado();
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
		navigator.addView(OrganizacionView.NOMBRE, new OrganizacionView());
		navigator.navigateTo(OrganizacionView.NOMBRE);
	}

}
