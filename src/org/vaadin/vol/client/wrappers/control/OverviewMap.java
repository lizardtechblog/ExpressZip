package org.vaadin.vol.client.wrappers.control;

import org.vaadin.vol.client.wrappers.layer.Layer;


public class OverviewMap extends Control {
	protected OverviewMap() {};
	
	public static native OverviewMap create(Layer targetLayer) 
	/*-{
	     	var options = {
    		layers: [targetLayer]
    	};
		return new $wnd.OpenLayers.Control.OverviewMap(options);
	}-*/;

}
