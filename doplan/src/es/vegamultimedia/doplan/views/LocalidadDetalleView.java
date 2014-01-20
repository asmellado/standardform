package es.vegamultimedia.doplan.views;

import es.vegamultimedia.doplan.model.Localidad;
import es.vegamultimedia.standardform.Campo;
import es.vegamultimedia.standardform.DetalleView;
import es.vegamultimedia.standardform.ListadoView;

public class LocalidadDetalleView extends DetalleView<Localidad> {

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
	public String getNombre() {
		return "localidad";
	}

	@Override
	protected Campo[] getCampos() {
		return new Campo[]{
				new Campo("Nombre", "nombre")};
	}

	@Override
	protected ListadoView<Localidad> getListadoView() {
		return new LocalidadListadoView();
	}
}