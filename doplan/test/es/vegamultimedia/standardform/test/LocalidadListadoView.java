package es.vegamultimedia.standardform.test;

import es.vegamultimedia.standardform.DetailView;
import es.vegamultimedia.standardform.ListView;

public class LocalidadListadoView extends ListView<Localidad> {

	private static final long serialVersionUID = 6572100088833480615L;

	@Override
	protected Class<Localidad> getBeanClass() {
		return Localidad.class;
	}

	@Override
	protected DetailView<Localidad> getDetalleView(Localidad elemento) {
		return new LocalidadDetalleView(elemento);
	}

}