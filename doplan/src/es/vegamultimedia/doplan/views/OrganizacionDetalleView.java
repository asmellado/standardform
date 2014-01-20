package es.vegamultimedia.doplan.views;

import com.vaadin.navigator.View;

import es.vegamultimedia.doplan.model.Organizacion;
import es.vegamultimedia.standardform.Campo;
import es.vegamultimedia.standardform.DetalleView;

public class OrganizacionDetalleView extends DetalleView<Organizacion> implements View {
	
	private static final long serialVersionUID = -4207660147800182949L;

	public OrganizacionDetalleView(Organizacion elementoActual) {
		super(elementoActual);
	}
	
	@Override
	protected Class<Organizacion> getBeanClass() {
		return Organizacion.class;
	}

	@Override
	public String getNombre() {
		return "organizacion";
	}

	@Override
	protected Campo[] getCampos() {
		return new Campo[]{
				new Campo("Nombre", "nombre"),
				new Campo("Persona de Contacto", "personaContacto"),
				new Campo("Email de Contacto", "emailContacto")};
	}

	@Override
	protected OrganizacionListadoView getListadoView() {
		return new OrganizacionListadoView();
	}

	@Override
	protected Organizacion getBeanVacio() {
		return new Organizacion();
	}
}
