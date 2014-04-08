package org.vaadin.vol.client.wrappers.control;


public class LayerSwitcher extends Control {
	protected LayerSwitcher() {};
	
	public static native LayerSwitcher create() 
	/*-{
		return new $wnd.OpenLayers.Control.LayerSwitcher();
	}-*/;

}
