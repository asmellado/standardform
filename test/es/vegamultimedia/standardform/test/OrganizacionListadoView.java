package es.vegamultimedia.standardform.test;

import javax.persistence.EntityManager;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;

import es.vegamultimedia.standardform.views.ListView;

public class OrganizacionListadoView extends ListView<Organizacion> implements View {
	
	public OrganizacionListadoView(EntityManager entityManager,
			Navigator navigator) {
		super(entityManager, navigator);
	}

	private static final long serialVersionUID = -3803544946191687420L;

	@Override
	protected Class<Organizacion> getBeanClass() {
		return Organizacion.class;
	}

	@Override
	protected OrganizacionDetalleView getDetalleView(Organizacion elemento) {
		return new OrganizacionDetalleView(entityManager, navigator, elemento);
	}

}
