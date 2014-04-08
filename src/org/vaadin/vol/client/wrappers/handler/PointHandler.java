package org.vaadin.vol.client.wrappers.handler;

public class PointHandler extends Handler {
	protected PointHandler() {}
	
	public final native static PointHandler get()
	/*-{
		return $wnd.OpenLayers.Handler.Point;
	 }-*/;

}
