package org.vaadin.vol.client.wrappers.control;


public class ScaleLine extends Control {
	protected ScaleLine() {};
	
	public static native ScaleLine create() 
	/*-{
		return new $wnd.OpenLayers.Control.ScaleLine();
	}-*/;

}
