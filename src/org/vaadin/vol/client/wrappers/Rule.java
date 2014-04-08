package org.vaadin.vol.client.wrappers;

import com.google.gwt.core.client.JavaScriptObject;

public class Rule extends JavaScriptObject {
	protected Rule(){};

	public static native Rule create(int width, int height) 
	/*-{
		return new $wnd.OpenLayers.Rule();
	}-*/;


	public static native Rule create(JavaScriptObject options)
	/*-{
		return new $wnd.OpenLayers.Rule(options);
	}-*/;
	
//	public native final addSymbolizers()
}
