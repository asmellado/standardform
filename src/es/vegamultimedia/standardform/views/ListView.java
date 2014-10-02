package es.vegamultimedia.standardform.views;

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

import es.vegamultimedia.standardform.Utils;
import es.vegamultimedia.standardform.annotations.StandardForm;
import es.vegamultimedia.standardform.annotations.StandardFormField;
import es.vegamultimedia.standardform.model.Bean;

@SuppressWarnings("serial")
public abstract class ListView<T extends Bean> extends FormLayout implements View {
	
	// EntityManager
	protected EntityManager entityManager;
	
	// Navigator
	protected Navigator navigator;
	
	// Container del formulario
	protected BeanItemContainer<T> container;
	
	// Tabla del formulario
	protected Table tabla;
	
	public ListView(EntityManager entityManager, Navigator navigator) {
		this.entityManager = entityManager;
		this.navigator = navigator;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// Columnas de la tabla
		List<String> visibledColumns;

		// Obtenemos la anotación ListForm del bean
		StandardForm listForm = getBeanClass().getAnnotation(StandardForm.class);

		// Si el bean NO tiene anotación StandardForm
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
					// Añadimos la cabecera de la columna
					addHeaderColumn(fields[i]);
				}
			}
		}
		// Si se especifican las columnas visibles
		else {
			// Hacemos visibles las columnas especificadas
			visibledColumns = new ArrayList<String>(Arrays.asList(listForm.columns()));
			// Para cada columna, añadimos su cabecera
			for(String nombreColumn : visibledColumns) {
				try {
					// Obtenemos el campo que coincide con el nombre de la columna
					Field beanField = getBeanClass().getDeclaredField(nombreColumn);
					// Lo añadimos a la cabecera
					addHeaderColumn(beanField);
				} catch (Exception e) {
					Notification.show("Metadatos incorrectos", 
							"No existe en el bean " + getBeanClass().getName() + 
							" un campo " + nombreColumn + " que se ha especificado como columna visible.",
							Type.ERROR_MESSAGE);
					e.printStackTrace();
					return;
				}
			}
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

	private void addHeaderColumn(Field beanField) {
		// Obtnemos la anotación StandarFormField del campo
		StandardFormField standardFormField = beanField.getAnnotation(StandardFormField.class);
		// Si la columna tiene caption
		if (standardFormField != null && standardFormField.caption().length() != 0)
			// Ponemos el caption como cabecera
			tabla.setColumnHeader(beanField.getName(), standardFormField.caption());
		else
			// Si no, ponemos el nombre del campo con la primera letra en mayúscula
			tabla.setColumnHeader(beanField.getName(), Utils.capitalizeFirstLetter(beanField.getName()));
	}
	
	protected void cargarDatos() {
		try {
			String consulta = "SELECT e FROM " + getBeanClass().getSimpleName() + " e";
			Query query = entityManager.createQuery(consulta);
			@SuppressWarnings("unchecked")
			List<T> listaElementos = query.getResultList();
			container = new BeanItemContainer<T>(getBeanClass(), listaElementos);
			tabla.setContainerDataSource(container);
		} catch (Exception e) {
			Notification.show("No se pueden obtener los elementos",
					e.getMessage(), Type.ERROR_MESSAGE);
		}
	}
	
	protected void mostrarDetalle(T elemento) {
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
