package es.vegamultimedia.standardform.test;

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

import es.vegamultimedia.doplan.views.InicioView;
import es.vegamultimedia.standardform.annotations.StandardForm;

@Theme("doplan")
public class DoplanUI extends UI {
	
	private static final long serialVersionUID = 8674520219333051040L;
	private Navigator navigator;
	private transient EntityManager entityManager;

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = DoplanUI.class)
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
		final MenuItemDoplan[][] menuItem = new MenuItemDoplan[][]{
			new MenuItemDoplan[]{
					new MenuItemDoplan<Object, View>("Administración", null, null),
					new MenuItemDoplan<Organizacion, OrganizacionListadoView>(
							"Organizaciones", Organizacion.class, OrganizacionListadoView.class),
					new MenuItemDoplan<Localidad, LocalidadListadoView>(
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

			@Override
			public void itemClick(ItemClickEvent event) {
				String elementoSeleccionadoId = (String) event.getItemId();
				for (int i=0;i<menuItem.length;i++) {
					if (elementoSeleccionadoId.equals(menuItem[i][0].getCaption())) {
						return;
					}
					for (int j=1;j<menuItem[i].length;j++) {
						if (elementoSeleccionadoId.equals(menuItem[i][j].getCaption())) {
							try {
								@SuppressWarnings("unchecked")
								Class<View> classView = (Class<View>)menuItem[i][j].getViewClass();
								@SuppressWarnings("unchecked")
								Class<Object> classBean = (Class<Object>)menuItem[i][j].getBeanClass();
								View vista = classView.newInstance();
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