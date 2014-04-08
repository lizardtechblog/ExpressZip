package org.vaadin.vol.client.wrappers.handler;

public class PathHandler extends Handler {
	protected PathHandler() {}
	
	public final native static PathHandler get()
	/*-{
		return $wnd.OpenLayers.Handler.Path;
	 }-*/;

}
