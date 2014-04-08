package org.vaadin.vol.client.wrappers.layer;


public class YahooLayer extends Layer {
	
	protected YahooLayer(){};
	
	public native final static YahooLayer create(String displayName)
	/*-{
	  	 	if(!displayName){
	 			displayName = "Yahoo";
	 	}
		return new $wnd.OpenLayers.Layer.Yahoo(displayName);
	}-*/;

}
