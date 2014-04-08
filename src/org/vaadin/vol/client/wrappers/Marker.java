package org.vaadin.vol.client.wrappers;


public class Marker extends AbstractOpenLayersWrapper {

	protected Marker(){};
	
	public native static Marker create(LonLat lonlat, Icon icon)
	/*-{
		return new $wnd.OpenLayers.Marker(lonlat, icon);
	}-*/;

	/**
	 * TODO better typed listeners and parameters.
	 * 
	 * @param gwtOlHandler
	 */
	public final void addClickHandler(GwtOlHandler gwtOlHandler) {
		registerHandler("click", gwtOlHandler);
	}
}
