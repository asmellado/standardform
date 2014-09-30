package es.vegamultimedia.standardform.test;

import javax.persistence.EntityManager;

import com.vaadin.navigator.Navigator;

import es.vegamultimedia.standardform.views.DetailView;
import es.vegamultimedia.standardform.views.ListView;

public class LocalidadDetalleView extends DetailView<Localidad> {

	public LocalidadDetalleView(EntityManager entityManager,
			Navigator navigator, Localidad elementoActual) {
		super(entityManager, navigator, elementoActual);
	}

	private static final long serialVersionUID = 2993841191220612788L;

	@Override
	protected Class<Localidad> getBeanClass() {
		return Localidad.class;
	}

	@Override
	protected Localidad getBeanVacio() {
		return new Localidad();
	}

	@Override
	protected ListView<Localidad> getListadoView() {
		return new LocalidadListadoView(entityManager, navigator);
	}
}