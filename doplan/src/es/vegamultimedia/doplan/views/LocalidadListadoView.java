package es.vegamultimedia.doplan.views;

import es.vegamultimedia.doplan.model.Localidad;
import es.vegamultimedia.standardform.DetalleView;
import es.vegamultimedia.standardform.ListadoView;

public class LocalidadListadoView extends ListadoView<Localidad> {

	private static final long serialVersionUID = 6572100088833480615L;

	@Override
	protected Class<Localidad> getBeanClass() {
		return Localidad.class;
	}

	@Override
	protected DetalleView<Localidad> getDetalleView(Localidad elemento) {
		return new LocalidadDetalleView(elemento);
	}

}