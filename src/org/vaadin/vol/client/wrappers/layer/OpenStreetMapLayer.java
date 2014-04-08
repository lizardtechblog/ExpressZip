package org.vaadin.vol.client.wrappers.layer;

public class OpenStreetMapLayer extends Layer {

    protected OpenStreetMapLayer() {
    };

    /*
     * TODO refactor redundant code
     */

    public native final static OpenStreetMapLayer createWithUrl(
            String displayName, String projection, String url)
    /*-{
        var options = {transitionEffect: 'resize'};
        if(projection) {
                options.projection = projection;
        }
        
        if(!displayName) displayName = "OpenStreetMap";
        if (url)
                return new $wnd.OpenLayers.Layer.OSM(displayName,[url],options)
        else
                return new $wnd.OpenLayers.Layer.OSM(displayName,null,options);
        
    }-*/;

    public native final static OpenStreetMapLayer create(String displayName,
            String projection)
    /*-{
    	var options = {transitionEffect: 'resize'};
    	if(projection) {
    		options.projection = projection;
    	}
    	
    	if(!displayName) displayName = "OpenStreetMap";
    	return new $wnd.OpenLayers.Layer.OSM(displayName,null,options);
    	
    }-*/;

    public native final static OpenStreetMapLayer createCycleMap(
            String displayName, String projection)
    /*-{
        var options = {transitionEffect: 'resize'};
        if(projection) {
                options.projection = projection;
        }
        
        if(!displayName) displayName = "OpenStreetMap";
        return new $wnd.OpenLayers.Layer.OSM.CycleMap(displayName,null,options);
    }-*/;

}
