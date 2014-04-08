package org.vaadin.vol.client.wrappers.layer;


public class GoogleHybridLayer extends Layer {
	
	protected GoogleHybridLayer(){};
	
	public native final static GoogleHybridLayer create(String displayName, String projection)
	/*-{

		var options = {type: $wnd.google.maps.MapTypeId.HYBRID, numZoomLevels: 22, sphericalMercator: true};
		if(projection) {
			options.projection = projection;
		}
		
		if(!displayName) displayName = "Google Hybrid";
		
		return new $wnd.OpenLayers.Layer.Google(displayName, options);
	}-*/;

}
