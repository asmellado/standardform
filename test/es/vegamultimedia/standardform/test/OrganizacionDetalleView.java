package es.vegamultimedia.standardform.test;

import com.vaadin.navigator.View;

import es.vegamultimedia.standardform.views.DetailView;

public class OrganizacionDetalleView extends DetailView<Organizacion> implements View {
	
	private static final long serialVersionUID = -4207660147800182949L;

	public OrganizacionDetalleView(Organizacion elementoActual) {
		super(elementoActual);
	}
	
	@Override
	protected Class<Organizacion> getBeanClass() {
		return Organizacion.class;
	}

	@Override
	protected OrganizacionListadoView getListadoView() {
		return new OrganizacionListadoView();
	}

	@Override
	protected Organizacion getBeanVacio() {
		return new Organizacion();
	}
}
