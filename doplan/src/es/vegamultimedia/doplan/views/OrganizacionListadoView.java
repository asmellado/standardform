package es.vegamultimedia.doplan.views;

import com.vaadin.navigator.View;

import es.vegamultimedia.doplan.formularioestandar.ListadoView;
import es.vegamultimedia.doplan.model.Organizacion;

public class OrganizacionListadoView extends ListadoView<Organizacion> implements View {
	
	private static final long serialVersionUID = -3803544946191687420L;

	@Override
	protected Class<Organizacion> getBeanClass() {
		return Organizacion.class;
	}

	@Override
	public String getNombre() {
		return "organizaciones";
	}

	@Override
	protected Object[] getVisibledColumns() {
		return new Object[]{"nombre", "localidad", "personaContacto", "emailContacto",
				"Editar", "Eliminar"};
	}

	@Override
	protected OrganizacionDetalleView getDetalleView(Organizacion elemento) {
		return new OrganizacionDetalleView(elemento);
	}

}
