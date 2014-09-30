package es.vegamultimedia.standardform.test;

import javax.persistence.EntityManager;

import com.vaadin.navigator.Navigator;

import es.vegamultimedia.standardform.views.DetailView;
import es.vegamultimedia.standardform.views.ListView;

public class LocalidadListadoView extends ListView<Localidad> {

	public LocalidadListadoView(EntityManager entityManager, Navigator navigator) {
		super(entityManager, navigator);
	}

	private static final long serialVersionUID = 6572100088833480615L;

	@Override
	protected Class<Localidad> getBeanClass() {
		return Localidad.class;
	}

	@Override
	protected DetailView<Localidad> getDetalleView(Localidad elemento) {
		return new LocalidadDetalleView(entityManager, navigator, elemento);
	}

}