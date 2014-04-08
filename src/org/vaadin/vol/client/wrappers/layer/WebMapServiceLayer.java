package org.vaadin.vol.client.wrappers.layer;

public class WebMapServiceLayer extends Layer {

    protected WebMapServiceLayer() {
    };

    public native final static WebMapServiceLayer create(String display,
            String url, String layers, String format, String cqlFilter, String styles,
            boolean isBaseLayer, boolean transparent, double opacity,
            boolean singleTile, String projection)
    /*-{
    	var params = {};
    	if(layers) params.layers = layers;
    	if(format) params.format = format;
    	if(cqlFilter) params.CQL_FILTER = cqlFilter;
        if(projection) params.projection = projection;
        if(styles) params.styles = styles;
    	params.transparent = transparent;
    	var options = {};
    	options.isBaseLayer = isBaseLayer;
    	options.opacity = opacity;
    	options.singleTile = singleTile;

    	return new $wnd.OpenLayers.Layer.WMS(display, url, params, options);
    }-*/;
}
