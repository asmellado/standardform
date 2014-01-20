package es.vegamultimedia.standardform;

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

@SuppressWarnings("serial")
public abstract class ListadoView<T> extends FormLayout implements View {
	
	protected BeanItemContainer<T> container;
	protected Table tabla;

	@Override
	public void enter(ViewChangeEvent event) {
		HorizontalLayout layout = new HorizontalLayout();
		addComponent(layout);
		// Tabla
		tabla = new Table();
		//tabla.setSelectable(true);
		tabla.setImmediate(true);
		tabla.addGeneratedColumn("Editar", new EditarColumnGenerator());
		tabla.addGeneratedColumn("Eliminar", new EliminarColumnGenerator());
		
		// Botón Alta
		Button botónAlta = new Button("Alta");
		botónAlta.addClickListener(new ClickListener(){

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				mostrarDetalle(null);
			}
			
		});
		
		layout.addComponent(tabla);
		layout.addComponent(botónAlta);
		cargarDatos();
	}
	
	protected void cargarDatos() {
		try {
			EntityManager entityManager = ((DoplanUI)getUI()).getEntityManager();
			String consulta = "SELECT e FROM " + getBeanClass().getSimpleName() + " e";
			Query query = entityManager.createQuery(consulta);
			List<T> listaElementos = query.getResultList();
			container = new BeanItemContainer<T>(getBeanClass(), listaElementos);
			tabla.setContainerDataSource(container);
			tabla.setVisibleColumns(this.getVisibledColumns());
		} catch (Exception e) {
			Notification.show("No se pueden obtener los elementos", e.getMessage(), Type.ERROR_MESSAGE);
		}
	}
	
	protected void mostrarDetalle(T elemento) {
		Navigator navigator = ((DoplanUI)getUI()).getNavigator();
		DetalleView<T> vistaDetalle = getDetalleView(elemento);
		navigator.addView(vistaDetalle.getNombre(), vistaDetalle);
		navigator.navigateTo(vistaDetalle.getNombre());
	}
	
	/**
	 * Retorna la clase del bean (T)
	 * @return
	 */
	abstract protected Class<T> getBeanClass();

	
	/**
	 * Obtiene el nombre de la vista, que se muestra en la URL
	 * @return
	 */
	abstract public String getNombre();
	
	/**
	 * Retorna un array con los nombres de las columnas de la tabla
	 * @return
	 */
	abstract protected Object[] getVisibledColumns();
	
	/**
	 * Retorna la View que muestra el detalle
	 * @return
	 */
	abstract protected DetalleView<T> getDetalleView(T elemento);
	
	/*
	 * Clase para la columna Editar 
	 */
	public class EditarColumnGenerator implements Table.ColumnGenerator {

		private static final long serialVersionUID = 1L;

		@Override
		public Object generateCell(Table source, final Object itemId, Object columnId) {
			Button button = new Button("Editar");
			button.setStyleName(BaseTheme.BUTTON_LINK);
			button.addClickListener(new ClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					T elementoSeleccionado = container.getItem(itemId).getBean();
					mostrarDetalle(elementoSeleccionado);
				}
			});
			return button;
		}
	}
	
	/*
	 * Clase para la columna Eliminar 
	 */
	public class EliminarColumnGenerator implements Table.ColumnGenerator {

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
			                	T elementoSeleccionada = container.getItem(itemId).getBean();
			                	try {
				                	EntityManager entityManager = ((DoplanUI)getUI()).getEntityManager();
				                	EntityTransaction transaction = entityManager.getTransaction();
									transaction.begin();
				                	entityManager.remove(elementoSeleccionada);
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
}
