package es.vegamultimedia.doplan;

import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import es.vegamultimedia.doplan.views.InicioView;
import es.vegamultimedia.doplan.views.OrganizacionView;

@Theme("doplan")
public class DoplanUI extends UI {
	
	private static final long serialVersionUID = 8674520219333051040L;
	private Navigator navigator;
	private JDBCConnectionPool pool;

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = DoplanUI.class)
	public static class Servlet extends VaadinServlet {
	}

	@Override
	protected void init(VaadinRequest request) {
		// Layout principal
		final VerticalLayout mainLayout = new VerticalLayout();
		setContent(mainLayout);
		
		// Cabecera
		MenuBar menu = new MenuBar();
		mainLayout.addComponent(menu);
				
		// Panel de contenido
		Panel contentPanel = new Panel();
		mainLayout.addComponent(contentPanel);
		
		// Establecemos al navegador sobre el panel de contenido
		navigator = new Navigator(this, contentPanel);
		
		// Registro de Vista de inicio
		navigator.addView("", new InicioView());
		navigator.navigateTo("");
		
		// Comando genérico para todo el menú
		MenuBar.Command comando = new MenuBar.Command() {

			@Override
			public void menuSelected(MenuItem selectedItem) {
				navigator.addView(OrganizacionView.NOMBRE, new OrganizacionView());
				navigator.navigateTo(OrganizacionView.NOMBRE);
			}
			
		};
		
		// Opciones del menú
		MenuBar.MenuItem menuAdministración = menu.addItem("Administración", null);
		menuAdministración.addItem("Organizaciones", comando);
		
	}

	public JDBCConnectionPool getPool() throws SQLException {
		if (pool == null) {
			pool = new SimpleJDBCConnectionPool(
			        "org.hsqldb.jdbc.JDBCDriver",
			        "jdbc:hsqldb:mem:sqlcontainer", "SA", "", 2, 5);
		}
		return pool;
	}

}