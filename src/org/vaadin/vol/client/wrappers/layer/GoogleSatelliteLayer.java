package org.vaadin.vol.client.wrappers.layer;


public class GoogleSatelliteLayer extends Layer {
	
	protected GoogleSatelliteLayer(){};
	
	public native final static GoogleSatelliteLayer create(String displayName, String projection)
	/*-{

		var options = {type: $wnd.google.maps.MapTypeId.SATELLITE, numZoomLevels: 22, sphericalMercator: true};
		if(projection) {
			options.projection = projection;
		}
		
		if(!displayName) displayName = "Google Satellite";
		
		return new $wnd.OpenLayers.Layer.Google(displayName,
    				options);
	}-*/;

}
