package es.vegamultimedia.doplan.views;

import es.vegamultimedia.doplan.formularioestandar.Campo;
import es.vegamultimedia.doplan.formularioestandar.DetalleView;
import es.vegamultimedia.doplan.formularioestandar.ListadoView;
import es.vegamultimedia.doplan.model.Localidad;

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