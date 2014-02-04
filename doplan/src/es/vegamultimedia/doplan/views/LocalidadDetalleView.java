package es.vegamultimedia.doplan.views;

import es.vegamultimedia.doplan.model.Localidad;
import es.vegamultimedia.standardform.DetailView;
import es.vegamultimedia.standardform.ListView;

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