package org.vaadin.vol.client.wrappers;

import com.google.gwt.core.client.JavaScriptObject;

public class Projection extends JavaScriptObject {
	protected Projection() {
	}

	public static native Projection get(String projectionString) 
	/*-{
		return new $wnd.OpenLayers.Projection(projectionString);
	}-*/;

}
