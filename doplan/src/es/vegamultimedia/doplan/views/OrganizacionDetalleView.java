package es.vegamultimedia.doplan.views;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;

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
					JDBCConnectionPool pool = ((DoplanUI)getUI()).getPool();
					Connection connection = pool.reserveConnection();
					PreparedStatement statement;
					if (organizacion.getId() == 0) {
						// Alta
						statement = connection.prepareStatement(
								"INSERT INTO organizacion "
										+ "(nombre, localidad_id, persona_contacto, email_contacto) "
										+ "VALUES (?,?,?,?)");
						statement.setString(1, organizacion.getNombre());
						statement.setLong(2, organizacion.getLocalidad().getId());
						statement.setString(3, organizacion.getPersonaContacto());
						statement.setString(4, organizacion.getEmailContacto());
					} else {
						// Modificación
						statement = connection.prepareStatement(
								"UPDATE organizacion "
										+ "SET nombre = '" + organizacion.getNombre() + "'"
										+ ", persona_contacto = '" + organizacion.getPersonaContacto() + "'"
										+ ", email_contacto = '" + organizacion.getEmailContacto() + "'"
										+ " WHERE id = " + organizacion.getId());
					}
					int rows = statement.executeUpdate();
					connection.setAutoCommit(true);
					if (rows == 1) {
						Notification.show("El elemento se ha actualizado correctamente");
					}
					else {
						Notification.show("El elemento no se ha podido actualizar");
					}
					statement.close();
				} catch (SQLException e) {
					Notification.show("Se ha producido un error:\n" + e.getMessage());
					e.printStackTrace();
				} catch (CommitException e) {
					Notification.show("No se ha podido guardar");
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
