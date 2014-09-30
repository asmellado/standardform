package es.vegamultimedia.standardform.test;

import java.lang.reflect.Constructor;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import es.vegamultimedia.standardform.annotations.StandardForm;
import es.vegamultimedia.standardform.views.ListView;

@Theme("standardform")
public class StandardFormUI extends UI {
	
	private static final long serialVersionUID = 8674520219333051040L;
	private Navigator navigator;
	private transient EntityManager entityManager;

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = StandardFormUI.class)
	public static class Servlet extends VaadinServlet {

		private static final long serialVersionUID = -4452007484858639919L;
	}

	@Override
	protected void init(VaadinRequest request) {
		// Layout principal
		final VerticalLayout mainLayout = new VerticalLayout();
		setContent(mainLayout);
		
		// Cabecera
		mainLayout.addComponent(new Label("DOPLAN CABECERA"));
		
		// Zona central
		HorizontalLayout zonaCentral = new HorizontalLayout();
		mainLayout.addComponent(zonaCentral);
		
		// Menú
		Tree menu = initMenu();
		zonaCentral.addComponent(menu);

		// Panel de contenido
		Panel contentPanel = new Panel();
		zonaCentral.addComponent(contentPanel);
		
		// Pie
		mainLayout.addComponent(new Panel("DOPLAN PIE"));
		
		// Establecemos al navegador sobre el panel de contenido
		navigator = new Navigator(this, contentPanel);
		
		// Registro de Vista de inicio
		navigator.addView("", new InicioView());
		navigator.navigateTo("");
	}

	public Navigator getNavigator() {
		return navigator;
	}

	public EntityManager getEntityManager() throws IllegalStateException {
		if (entityManager == null) {
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("standardform");
			entityManager = emf.createEntityManager();
		}
		return entityManager;
	}
	
	private Tree initMenu() {
		// Array de menú items del árbol
		@SuppressWarnings("rawtypes")
		final MenuItemStandardForm[][] menuItem = new MenuItemStandardForm[][]{
			new MenuItemStandardForm[]{
					new MenuItemStandardForm<Object, View>("Administración", null, null),
					new MenuItemStandardForm<Organizacion, OrganizacionListadoView>(
							"Organizaciones", Organizacion.class, OrganizacionListadoView.class),
					new MenuItemStandardForm<Localidad, LocalidadListadoView>(
							"Localidades", Localidad.class, LocalidadListadoView.class)
			}
		};
		Tree tree = new Tree();
		// Añadir opciones al árbol
		// Recorremos los menús del árbol
		for (int i=0;i<menuItem.length;i++) {
			String menu = menuItem[i][0].getCaption();
			tree.addItem(menu);
			// Recorremos los items
			for (int j=1;j<menuItem[i].length;j++) {
				// Añadimos el ítem
				String opcion = menuItem[i][j].getCaption();
				tree.addItem(opcion);
				tree.setParent(opcion, menu);
				tree.setChildrenAllowed(opcion, false);
			}
			// Expandimos el menú
			tree.expandItemsRecursively(menu);
		}
		tree.addItemClickListener(new ItemClickListener() {
			private static final long serialVersionUID = -837890934570017036L;

			// Método llamado cuando se pulsa un elemento del menú
			@Override
			public void itemClick(ItemClickEvent event) {
				String elementoSeleccionadoId = (String) event.getItemId();
				// Recorremos todo el menú para localizar el elemento seleccionado
				for (int i=0;i<menuItem.length;i++) {
					// Si es un elemento padre (de primer nivel del árbol)
					if (elementoSeleccionadoId.equals(menuItem[i][0].getCaption())) {
						// No hacemos nada
						return;
					}
					// Recorremos los elementos hijos (segundo nivel del árbol) 
					for (int j=1;j<menuItem[i].length;j++) {
						// Si encontramos el elemento
						if (elementoSeleccionadoId.equals(menuItem[i][j].getCaption())) {
							// Obtenemos los datos del elemento para navegar hasta la vista de listado
							try {
								// Obtenemos la clase de la ListView
								@SuppressWarnings("unchecked")
								Class<View> classView = (Class<View>)menuItem[i][j].getViewClass();
								// Obtenemos la clase del bean
								@SuppressWarnings("unchecked")
								Class<Object> classBean = (Class<Object>)menuItem[i][j].getBeanClass();
								// Obtenemos el constructor de la ListView
								Constructor constructor = classView.getConstructor(EntityManager.class, Navigator.class);
								// Creamos un objeto de la vista
								@SuppressWarnings("rawtypes")
								View vista = (ListView)constructor.newInstance(getEntityManager(), getNavigator());
								String name = classBean.getAnnotation(StandardForm.class).listViewName();
								navigator.addView(name, vista);
								navigator.navigateTo(name);
							} catch (Exception e) {
								Notification.show("Se ha producido un error", e.getMessage(), Type.ERROR_MESSAGE);
							}
							return;
						}
					}
				}
			}

		});
		return tree;
	}
}