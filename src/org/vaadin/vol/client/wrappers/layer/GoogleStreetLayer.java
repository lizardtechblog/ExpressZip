package org.vaadin.vol.client.wrappers.layer;


public class GoogleStreetLayer extends Layer {
	
	protected GoogleStreetLayer(){};
	
	public native final static GoogleStreetLayer create(String displayName, String projection)
	/*-{

		var options = {numZoomLevels: 22, sphericalMercator: true};
		if(projection) {
			options.projection = projection;
		}
		
		if(!displayName) displayName = "Google Streets";
		
		return new $wnd.OpenLayers.Layer.Google(displayName,
    				options);
	}-*/;

}
