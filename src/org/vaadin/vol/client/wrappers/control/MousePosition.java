package org.vaadin.vol.client.wrappers.control;


public class MousePosition extends Control {
	protected MousePosition() {};
	
	public static native MousePosition create() 
	/*-{
		return new $wnd.OpenLayers.Control.MousePosition();
	}-*/;

}
