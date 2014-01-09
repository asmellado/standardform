package es.vegamultimedia.doplan.views;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

import es.vegamultimedia.doplan.DoplanUI;
import es.vegamultimedia.doplan.model.Localidad;
import es.vegamultimedia.doplan.model.Organizacion;

public class OrganizacionView extends FormLayout implements View {
	
	private static final long serialVersionUID = 1L;

	public static final String NOMBRE = "organizacion";
	
	private SQLContainer container;
	private BeanFieldGroup<Organizacion> binder;
	private FormLayout formulario;
	private Table tabla;
	private Organizacion organizacion;
	
	public OrganizacionView() {
		HorizontalLayout layout = new HorizontalLayout();
		addComponent(layout);
		// Tabla
		tabla = new Table();
		tabla.setSelectable(true);
		tabla.setImmediate(true);
		tabla.addGeneratedColumn("Editar", new EditarColumnGenerator());
		tabla.addGeneratedColumn("Eliminar", new EliminarColumnGenerator());
		
		// Botón Alta
		Button botónAlta = new Button("Alta");
		botónAlta.addClickListener(new ClickListener(){

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				Localidad localidad = new Localidad(
						1,
						"Sevilla");
				organizacion = new Organizacion(
						0,
						localidad,
						"",
						"",
						"");
				binder.setItemDataSource(organizacion);
				formulario.setVisible(true);
			}
			
		});
		
		// Formulario
		formulario = new FormLayout();
		binder = new BeanFieldGroup<Organizacion>(Organizacion.class);
		
		formulario.addComponent(binder.buildAndBind("Nombre", "nombre"));
		formulario.addComponent(binder.buildAndBind("Persona de Contacto", "personaContacto"));
		formulario.addComponent(binder.buildAndBind("Email de Contacto", "emailContacto"));

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
						cargarDatos();
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
				formulario.setVisible(false);
				cargarDatos();
			}
		});
		formulario.addComponent(botónGuardar);
		Button botónCancelar = new Button("Cancelar");
		botónCancelar.addClickListener(new ClickListener(){

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				formulario.setVisible(false);
			}
		});
		formulario.addComponent(botónCancelar);
		formulario.setVisible(false);
		
		layout.addComponent(tabla);
		layout.addComponent(botónAlta);
		layout.addComponent(formulario);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		cargarDatos();
	}
	
	private void cargarDatos() {
		try {
			JDBCConnectionPool pool = ((DoplanUI)getUI()).getPool();
			FreeformQuery query = new FreeformQuery("SELECT * FROM " + Organizacion.NOMBRE_VISTA, pool, "id");
			container = new SQLContainer(query);
			tabla.setContainerDataSource(container);
			tabla.setVisibleColumns(new Object[]{"nombre", "localidad", "persona_contacto", "email_contacto",
					"Editar", "Eliminar"});
		} catch (SQLException e) {
			Notification.show("No se pudo realizar una conexión con la base de datos");
		}
	}
	
	/*
	 * Clase para la columna Editar 
	 */
	class EditarColumnGenerator implements Table.ColumnGenerator {

		private static final long serialVersionUID = 1L;

		@Override
		public Object generateCell(Table source, final Object itemId, Object columnId) {
			Button button = new Button("Editar");
			button.setStyleName(BaseTheme.BUTTON_LINK);
			button.addClickListener(new ClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					Item item = container.getItem(itemId);
					Localidad localidad = new Localidad(
							(Long)item.getItemProperty("localidad_id").getValue(),
							(String)item.getItemProperty("localidad").getValue());
					organizacion = new Organizacion(
							(Long)item.getItemProperty("id").getValue(),
							localidad,
							(String)item.getItemProperty("nombre").getValue(),
							(String)item.getItemProperty("persona_contacto").getValue(),
							(String)item.getItemProperty("email_contacto").getValue());
					binder.setItemDataSource(organizacion);
					formulario.setVisible(true);
				}
			});
			return button;
		}
	}
	
	/*
	 * Clase para la columna Eliminar 
	 */
	class EliminarColumnGenerator implements Table.ColumnGenerator {

		private static final long serialVersionUID = 1L;

		@Override
		public Object generateCell(Table source, final Object itemId, Object columnId) {
			Button button = new Button("Eliminar");
			button.setStyleName(BaseTheme.BUTTON_LINK);
			button.addClickListener(new ClickListener() {
				
				private static final long serialVersionUID = 1L;
				
				@Override
				public void buttonClick(ClickEvent event) {
					ConfirmDialog.show(getUI(), "Confirmación", 
							"¿Está seguro de que desea eliminar el elemento?",
					        "Sí", "No", new ConfirmDialog.Listener() {
						private static final long serialVersionUID = 1L;

						public void onClose(ConfirmDialog dialog) {
			                if (dialog.isConfirmed()) {
			    				try {
			    					JDBCConnectionPool pool = ((DoplanUI)getUI()).getPool();
			    					Connection connection = pool.reserveConnection();
			    					PreparedStatement statement = connection.prepareStatement(
			    							"DELETE FROM organizacion WHERE id="+itemId);
			    					int rows = statement.executeUpdate();
			    					connection.setAutoCommit(true);
			    					if (rows == 1) {
			    						Notification.show("El elemento se ha eliminado correctamente");
			    						cargarDatos();
			    					}
			    					else {
			    						Notification.show("El elemento no se ha podido eliminar");
			    					}
			    					statement.close();
			    				} catch (SQLException e) {
			    					Notification.show("Se ha producido un error:\n" + e.getMessage());
			    					e.printStackTrace();
			    				}
			                }
			            }
			        });
				}
			});
			return button;
		}
	}

}
