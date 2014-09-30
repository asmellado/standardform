package es.vegamultimedia.standardform.test;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class InicioView extends VerticalLayout implements View {
	
	public static final String NOMBRE = "principal";
	
	public InicioView() {
		Label etiqueta = new Label("Bienvenido a Doplan");
		addComponent(etiqueta);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// NADA
	}

}