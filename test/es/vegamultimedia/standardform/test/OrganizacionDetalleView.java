package es.vegamultimedia.standardform.test;

import javax.persistence.EntityManager;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;

import es.vegamultimedia.standardform.views.DetailView;

public class OrganizacionDetalleView extends DetailView<Organizacion> implements View {
	
	public OrganizacionDetalleView(EntityManager entityManager,
			Navigator navigator, Organizacion elementoActual) {
		super(entityManager, navigator, elementoActual);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = -4207660147800182949L;

	@Override
	protected Class<Organizacion> getBeanClass() {
		return Organizacion.class;
	}

	@Override
	protected OrganizacionListadoView getListadoView() {
		return new OrganizacionListadoView(entityManager, navigator);
	}

	@Override
	protected Organizacion getBeanVacio() {
		return new Organizacion();
	}
}
