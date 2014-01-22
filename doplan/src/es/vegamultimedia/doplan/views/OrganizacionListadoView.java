package es.vegamultimedia.doplan.views;

import com.vaadin.navigator.View;

import es.vegamultimedia.doplan.model.Organizacion;
import es.vegamultimedia.standardform.ListadoView;

public class OrganizacionListadoView extends ListadoView<Organizacion> implements View {
	
	private static final long serialVersionUID = -3803544946191687420L;

	@Override
	protected Class<Organizacion> getBeanClass() {
		return Organizacion.class;
	}

	@Override
	protected OrganizacionDetalleView getDetalleView(Organizacion elemento) {
		return new OrganizacionDetalleView(elemento);
	}

}
