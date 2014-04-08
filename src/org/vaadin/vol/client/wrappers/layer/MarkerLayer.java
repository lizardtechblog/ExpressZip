package org.vaadin.vol.client.wrappers.layer;

import org.vaadin.vol.client.wrappers.Marker;


public class MarkerLayer extends Layer {

	protected MarkerLayer() {
	};

	public native final static MarkerLayer create(String name)
	/*-{
		return new $wnd.OpenLayers.Layer.Markers(name);
	}-*/;

	public native final void addMarker(Marker marker) 
	/*-{
		this.addMarker(marker);
	}-*/;

	public native final void removeMarker(Marker marker) 
	/*-{
		this.removeMarker(marker);
	}-*/;

}
