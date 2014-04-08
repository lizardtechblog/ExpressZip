package org.vaadin.vol.client.wrappers.handler;

public class PolygonHandler extends Handler {
	protected PolygonHandler() {}
	
	public final native static PolygonHandler get()
	/*-{
		return $wnd.OpenLayers.Handler.Polygon;
	 }-*/;

}
