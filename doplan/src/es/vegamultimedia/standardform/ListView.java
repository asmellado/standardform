package es.vegamultimedia.standardform;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
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
import es.vegamultimedia.doplan.model.Bean;
import es.vegamultimedia.standardform.annotations.StandardForm;

@SuppressWarnings("serial")
public abstract class ListView<T extends Bean> extends FormLayout implements View {
	
	protected BeanItemContainer<T> container;
	protected Table tabla;

	@Override
	public void enter(ViewChangeEvent event) {
		// Columnas de la tabla
		List<String> visibledColumns;

		// Obtenemos la anotación ListForm del bean
		StandardForm listForm = getBeanClass().getAnnotation(StandardForm.class);

		// Si el bean NO tiene anotación ListForm
		if (!(listForm instanceof StandardForm)) {
			Notification.show("Faltan metadatos",
					"El bean " + getBeanClass().getSimpleName() +" no permite formulario de listado",
					Type.ERROR_MESSAGE);
			return;
		}
		
		// Layout
		HorizontalLayout layout = new HorizontalLayout();
		addComponent(layout);
		
		// Tabla
		tabla = new Table();
		tabla.setImmediate(true);
		
		// Obtenemos los elementos
		cargarDatos();
		
		// Si no se especifican las columnas visibles
		if (listForm.columns()[0].isEmpty()) {
			// Se muestran todas excepto el id
			visibledColumns = new ArrayList<String>();
			// Obtenemos todos los campos del bean
			Field[] fields = getBeanClass().getDeclaredFields();
			// Recorremos los campos
			for (int i=0;i<fields.length;i++) {
				// Si no es el id
				if (!fields[i].getName().equals("id")) {
					// Añadimos el campo a las columnas visibles
					visibledColumns.add(fields[i].getName());
				}
			}
		}
		else {
			// Hacemos visibles las columnas especificadas
			visibledColumns = new ArrayList<String>(Arrays.asList(listForm.columns()));
		}
		// Si se permite edición
		if (listForm.allowsEditing()) {
			// Añadimos columna para editar
			tabla.addGeneratedColumn("Editar", new EditarColumnGenerator());
			visibledColumns.add("Editar");
		}
		// Si se permite eliminar
		if (listForm.allowsDeleting()) {
			// Añadimos columna para eliminar
			tabla.addGeneratedColumn("Eliminar", new EliminarColumnGenerator());
			visibledColumns.add("Eliminar");
		}
		tabla.setVisibleColumns(visibledColumns.toArray());
		layout.addComponent(tabla);
	
		// Si se permite añadir
		if (listForm.allowsAdding()) {
			// Botón Alta
			Button botónAlta = new Button("Alta");
			botónAlta.addClickListener(new ClickListener(){
	
				private static final long serialVersionUID = 1L;
	
				@Override
				public void buttonClick(ClickEvent event) {
					mostrarDetalle(null);
				}
				
			});
			layout.addComponent(botónAlta);
		}
	}
	
	protected void cargarDatos() {
		try {
			EntityManager entityManager = ((DoplanUI)getUI()).getEntityManager();
			String consulta = "SELECT e FROM " + getBeanClass().getSimpleName() + " e";
			Query query = entityManager.createQuery(consulta);
			List<T> listaElementos = query.getResultList();
			container = new BeanItemContainer<T>(getBeanClass(), listaElementos);
			tabla.setContainerDataSource(container);
		} catch (Exception e) {
			Notification.show("No se pueden obtener los elementos",
					e.getMessage(), Type.ERROR_MESSAGE);
		}
	}
	
	protected void mostrarDetalle(T elemento) {
		Navigator navigator = ((DoplanUI)getUI()).getNavigator();
		DetailView<T> vistaDetalle = getDetalleView(elemento);
		String name = getBeanClass().getAnnotation(StandardForm.class).detailViewName();
		navigator.addView(name, vistaDetalle);
		navigator.navigateTo(name);
	}
	
	/**
	 * Retorna la clase del bean (T)
	 * @return
	 */
	abstract protected Class<T> getBeanClass();
	
	/**
	 * Retorna la View que muestra el detalle
	 * @return
	 */
	abstract protected DetailView<T> getDetalleView(T elemento);
	
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
			                	T elementoSeleccionado = container.getItem(itemId).getBean();
			                	try {
				                	EntityManager entityManager = ((DoplanUI)getUI()).getEntityManager();
				                	EntityTransaction transaction = entityManager.getTransaction();
									transaction.begin();
//									entityManager.merge(elementoSeleccionado);
				                	entityManager.remove(elementoSeleccionado);
				                	transaction.commit();
				                	Notification.show("El elemento se ha eliminado correctamente");
				                	// Volvemos a cargar la tabla con los elementos
				                	removeAllComponents();
				                	enter(null);
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
