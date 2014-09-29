package es.vegamultimedia.standardform.test;

import com.vaadin.navigator.View;

import es.vegamultimedia.standardform.ListView;

public class OrganizacionListadoView extends ListView<Organizacion> implements View {
	
	private static final long serialVersionUID = -3803544946191687420L;

	@Override
	protected Class<Organizacion> getBeanClass() {
		return Organizacion.class;
	}

	@Override
	protected OrganizacionDetalleView getDetalleView(Organizacion elemento) {
		return new OrganizacionDetalleView(elemento);
	}

}
