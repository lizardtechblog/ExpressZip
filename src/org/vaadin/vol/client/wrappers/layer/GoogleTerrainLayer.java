package org.vaadin.vol.client.wrappers.layer;


public class GoogleTerrainLayer extends Layer {
	
	protected GoogleTerrainLayer(){};
	
	public native final static GoogleTerrainLayer create(String displayName, String projection)
	/*-{

		var options = {type: $wnd.google.maps.MapTypeId.TERRAIN, numZoomLevels: 22, sphericalMercator: true};
		if(projection) {
			options.projection = projection;
		}
		
		if(!displayName) displayName = "Google Physical";
		
		return new $wnd.OpenLayers.Layer.Google(displayName, options);
	}-*/;

}
