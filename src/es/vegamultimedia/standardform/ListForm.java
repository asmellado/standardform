package es.vegamultimedia.standardform;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

import es.vegamultimedia.standardform.annotations.StandardForm;
import es.vegamultimedia.standardform.annotations.StandardFormEnum;
import es.vegamultimedia.standardform.annotations.StandardFormField;
import es.vegamultimedia.standardform.model.Bean;

@SuppressWarnings("serial")
public class ListForm<T extends Bean> extends Panel {
	
	// BeanUI that created this standard list form
	protected BeanUI<T> beanUI;
	
	// Container del formulario
	protected BeanItemContainer<T> container;
	
	// Formulario
	FormLayout form;
	
	// Tabla del formulario
	protected Table tabla;
	
	public ListForm(BeanUI<T> beanUI) {
		this.beanUI = beanUI;
		
		// Columnas de la tabla
		List<String> visibledColumns;

		// Obtenemos la anotación ListForm del bean
		StandardForm listForm = beanUI.getBeanClass().getAnnotation(StandardForm.class);
		
		// Asignamos el título al panel
		setCaption(listForm.listViewName());

		// Si el bean NO tiene anotación StandardForm
		if (!(listForm instanceof StandardForm)) {
			Notification.show("Faltan metadatos",
					"El bean " + beanUI.getBeanClass().getSimpleName() +" no permite formulario de listado",
					Type.ERROR_MESSAGE);
			return;
		}
		
		// Creamos el formulario para albergar todos los campos del bean
		form = new FormLayout();
		setContent(form);
		
		// Layout
		HorizontalLayout layout = new HorizontalLayout();
		form.addComponent(layout);
		
		// Tabla
		tabla = new Table(){
			// Damos formato de sólo fecha a los campos de tipo Date 
		    @Override
		    protected String formatPropertyValue(Object rowId, Object colId, Property<?> property) {
		        if (property.getType() == Date.class) {
		            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		            return df.format((Date)property.getValue());
		        }
		        else if (property.getType().isEnum()) {
		        	// Obtenemos los elementos del enumerado
					Object[] elementosEnum = property.getType().getEnumConstants();
					// Recorremos todos los elementos del enumerado
					for (Object elementoEnum: elementosEnum) {
						// Se obtiene anotación StandardFormEnum del elemento
						try {
							java.lang.reflect.Field elementoField = property.getType().getField(elementoEnum.toString());
							StandardFormEnum anotación = elementoField.getAnnotation(StandardFormEnum.class);
							// Si tiene anotación StandardFormEnum informada
							if (anotación != null && anotación.value().length() != 0) {
								// Si el enumerado coincide con el valor la propiedad
								if (elementoEnum == property.getValue())
									// Se retorna el valor de la anotación
									return anotación.value();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
		        }
		        return super.formatPropertyValue(rowId, colId, property);
		    }
		};
		tabla.setImmediate(true);
		
		try {
			// Obtenemos los elementos
			cargarDatos();
			
			// Si no se especifican las columnas visibles
			if (listForm.columns()[0].isEmpty()) {
				// Se muestran todas excepto el id
				visibledColumns = new ArrayList<String>();
				// Obtenemos todos los campos del bean
				Field[] fields = beanUI.getBeanClass().getDeclaredFields();
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
						Field beanField = beanUI.getBeanClass().getDeclaredField(nombreColumn);
						// Lo añadimos a la cabecera
						addHeaderColumn(beanField);
					} catch (Exception e) {
						Notification.show("Metadatos incorrectos", 
								"No existe en el bean " + beanUI.getBeanClass().getSimpleName() + 
								" el campo " + nombreColumn + " y se ha especificado como columna visible.",
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
		} catch (Exception e) {
			Notification.show("No se pueden obtener los elementos",
					e.getMessage(), Type.ERROR_MESSAGE);
			e.printStackTrace();
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
		List<T> listaElementos = beanUI.getBeanDAO().getAllElements();
		container = new BeanItemContainer<T>(beanUI.getBeanClass(), listaElementos);
		tabla.setContainerDataSource(container);
	}
	
	protected void mostrarDetalle(T element) {
		Panel vistaDetalle;
		try {
			// Obtenemos el elemento de base de datos
			T elemento = beanUI.beanDAO.get(Utils.getId(element));
			vistaDetalle = beanUI.getDetailForm(elemento);
			ComponentContainer contentPanel = (ComponentContainer)getParent();
			contentPanel.replaceComponent(this, vistaDetalle);
		} catch (Exception e) {
			Notification.show("No se puede crear el formulario de detalle",
					e.getMessage(), Type.ERROR_MESSAGE);
			e.printStackTrace();
		}

	}
	
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
			                		beanUI.getBeanDAO().remove(elementoSeleccionado);
				                	Notification.show("El elemento se ha eliminado correctamente");
				                	// Volvemos a cargar la tabla con los elementos
				                	form.removeAllComponents();
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
