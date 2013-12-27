package es.vegamultimedia.doplan.views;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;

import es.vegamultimedia.doplan.model.Localidad;
import es.vegamultimedia.doplan.model.Organizacion;

@SuppressWarnings("serial")
public class OrganizacionView extends FormLayout implements View {
	
	public static final String NOMBRE = "organizacion";
	
	private final BeanItemContainer<Organizacion> container;
	private BeanFieldGroup<Organizacion> binder;
	FormLayout formulario;
	
	public OrganizacionView() {
		HorizontalLayout layout = new HorizontalLayout();
		addComponent(layout);
		// Tabla
		Table tabla = new Table();
		container = new BeanItemContainer<Organizacion>(Organizacion.class);
		container.addNestedContainerProperty("localidad.nombre");
		tabla.setContainerDataSource(container);
		tabla.setVisibleColumns(new Object[]{"nombre", "localidad.nombre", "personaContacto", "emailContacto"});
		tabla.setSelectable(true);
		tabla.setImmediate(true);
		tabla.addGeneratedColumn("Editar", new EditarColumnGenerator());
		cargarDatos();
		// Formulario
		formulario = new FormLayout();
		binder = new BeanFieldGroup<Organizacion>(Organizacion.class);
		formulario.addComponent(binder.buildAndBind("Nombre", "nombre"));
		formulario.addComponent(binder.buildAndBind("Persona de Contacto", "personaContacto"));
		formulario.addComponent(binder.buildAndBind("Email de Contacto", "emailContacto"));
		Button botónGuardar = new Button("Guardar");
		botónGuardar.addClickListener(new ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Guardar en base de datos				
				try {
					binder.commit();
				} catch (CommitException e) {
					Notification.show("No se ha podido guardar");
				}
				formulario.setVisible(false);
			}
		});
		formulario.addComponent(botónGuardar);
		Button botónCancelar = new Button("Cancelar");
		botónCancelar.addClickListener(new ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				formulario.setVisible(false);
			}
		});
		formulario.addComponent(botónCancelar);
		formulario.setVisible(false);
		
		layout.addComponent(tabla);
		layout.addComponent(formulario);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		cargarDatos();
	}
	
	private void cargarDatos() {
		container.removeAllItems();
		// TODO Obtener los datos de la base de datos
		Localidad sevilla = new Localidad(1, "Sevilla");
		container.addItem(
				new Organizacion(1, sevilla, "VegaMultimedia", 
						"Alejandro Sánchez", "alejandro.sanchez@vegamultimedia.es"));
	}
	
	/*
	 * Clase para la columna Editar 
	 */
	class EditarColumnGenerator implements Table.ColumnGenerator {
		@Override
		public Object generateCell(Table source, final Object itemId, Object columnId) {
			Button button = new Button("Editar");
			button.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					binder.setItemDataSource((Organizacion) itemId);
					formulario.setVisible(true);
				}
			});
			return button;
		}
	}

}
