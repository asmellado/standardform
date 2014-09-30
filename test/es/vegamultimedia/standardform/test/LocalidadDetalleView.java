package es.vegamultimedia.standardform.test;

import es.vegamultimedia.standardform.views.DetailView;
import es.vegamultimedia.standardform.views.ListView;

public class LocalidadDetalleView extends DetailView<Localidad> {

	private static final long serialVersionUID = 2993841191220612788L;

	public LocalidadDetalleView(Localidad elementoActual) {
		super(elementoActual);
	}

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
		return new LocalidadListadoView();
	}
}