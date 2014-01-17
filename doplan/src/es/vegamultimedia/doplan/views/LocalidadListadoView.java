package es.vegamultimedia.doplan.views;

import es.vegamultimedia.doplan.formularioestandar.DetalleView;
import es.vegamultimedia.doplan.formularioestandar.ListadoView;
import es.vegamultimedia.doplan.model.Localidad;

public class LocalidadListadoView extends ListadoView<Localidad> {

	private static final long serialVersionUID = 6572100088833480615L;

	@Override
	protected Class<Localidad> getBeanClass() {
		return Localidad.class;
	}

	@Override
	public String getNombre() {
		return "localidad";
	}

	@Override
	protected Object[] getVisibledColumns() {
		return new Object[]{"nombre", "Editar", "Eliminar"};
	}

	@Override
	protected DetalleView<Localidad> getDetalleView(Localidad elemento) {
		return new LocalidadDetalleView(elemento);
	}

}