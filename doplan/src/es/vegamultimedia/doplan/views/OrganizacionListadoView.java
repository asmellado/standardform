package es.vegamultimedia.doplan.views;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

import es.vegamultimedia.doplan.DoplanUI;
import es.vegamultimedia.doplan.model.Organizacion;

public class OrganizacionListadoView extends FormLayout implements View {
	
	private static final long serialVersionUID = 1L;

	public static final String NOMBRE = "organizaciones";
	
	private BeanItemContainer<Organizacion> container;
	private Table tabla;
	
	public OrganizacionListadoView() {
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
				mostrarDetalle(new Organizacion());
			}
			
		});
		
		layout.addComponent(tabla);
		layout.addComponent(botónAlta);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		cargarDatos();
	}
	
	private void cargarDatos() {
		try {
			EntityManager entityManager = ((DoplanUI)getUI()).getEntityManager();
			Query query = entityManager.createNamedQuery(Organizacion.QUERY_OBTENER_TODAS);
			List<Organizacion> listaOrganizaciones = query.getResultList();
			container = new BeanItemContainer<Organizacion>(Organizacion.class, listaOrganizaciones);
			tabla.setContainerDataSource(container);
			tabla.setVisibleColumns(new Object[]{"nombre", "localidad", "personaContacto", "emailContacto",
					"Editar", "Eliminar"});
		} catch (Exception e) {
			Notification.show("No se pueden obtener los elementos", e.getMessage(), Type.ERROR_MESSAGE);
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
					Organizacion organizacionSeleccionada = container.getItem(itemId).getBean();
					mostrarDetalle(organizacionSeleccionada);
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
			                	Organizacion organizacionSeleccionada = container.getItem(itemId).getBean();
			                	try {
				                	EntityManager entityManager = ((DoplanUI)getUI()).getEntityManager();
				                	EntityTransaction transaction = entityManager.getTransaction();
									transaction.begin();
				                	entityManager.remove(organizacionSeleccionada);
				                	transaction.commit();
				                	Notification.show("El elemento se ha eliminado correctamente");
				                	cargarDatos();
			                	} catch (Exception e) {
			    					Notification.show("No se ha podido eliminar el elemento", e.getMessage(), Type.ERROR_MESSAGE);
			    				}			                	
			                }
			            }
			        });
				}
			});
			return button;
		}
	}
	
	private void mostrarDetalle(Organizacion organizacion) {
		Navigator navigator = ((DoplanUI)getUI()).getNavigator();
		navigator.addView(OrganizacionDetalleView.NOMBRE, new OrganizacionDetalleView(organizacion));
		navigator.navigateTo(OrganizacionDetalleView.NOMBRE);
	}

}
