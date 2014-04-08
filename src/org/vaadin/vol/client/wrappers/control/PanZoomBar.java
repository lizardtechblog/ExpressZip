package org.vaadin.vol.client.wrappers.control;


public class PanZoomBar extends Control {
	protected PanZoomBar() {};
	
	public static native PanZoomBar create() 
	/*-{
		return control = new $wnd.OpenLayers.Control.PanZoomBar({panIcons:false});		
	}-*/;

}
