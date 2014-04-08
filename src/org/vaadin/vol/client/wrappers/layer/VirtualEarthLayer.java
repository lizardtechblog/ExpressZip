package org.vaadin.vol.client.wrappers.layer;


public class VirtualEarthLayer extends Layer {
	
	protected VirtualEarthLayer(){};
	
	public native final static VirtualEarthLayer create(String displayName)
	/*-{
	 	if(!displayName){
	 			displayName = "Virtual Earth";
	 	}
	 	
		return new $wnd.OpenLayers.Layer.VirtualEarth(displayName);
	}-*/;

}
