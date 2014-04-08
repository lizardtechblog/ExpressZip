package org.vaadin.vol.client.wrappers.layer;

public class WebMapServiceLayerStyled extends WebMapServiceLayer {

    protected WebMapServiceLayerStyled() {
    };

    public native final static WebMapServiceLayerStyled create(String display,
            String url, String layers, String format, boolean isBaseLayer,
            boolean transparent, double opacity, boolean singleTile, String sld)
    /*-{ 
      	var params = {};
    	if(layers) params.layers = layers;
    	if(format) params.format = format;
    	params.transparent = transparent;
    	if(sld) params.sld_body = sld;
      	var options = {};
    	options.isBaseLayer = isBaseLayer;
    	options.opacity = opacity;
    	options.singleTile = singleTile;
     	return new $wnd.OpenLayers.Layer.WMS.Post(display, url, params, options);
    }-*/;

    public native final void setOpacity(Double opacity)
    /*-{
    	this.setOpacity(opacity);
    }-*/;

}
