package org.vaadin.vol.client.wrappers.layer;

import org.vaadin.vol.client.wrappers.StyleMap;

public class WebFeatureServiceLayer extends VectorLayer {

	protected WebFeatureServiceLayer() {
	};

    public native final static WebFeatureServiceLayer create(String display,
			String url, String featureType, String ns, String projection, StyleMap styleMap)
	/*-{	    
	    var options = {
                'strategies': [new $wnd.OpenLayers.Strategy.BBOX()],
                'protocol': new $wnd.OpenLayers.Protocol.WFS({
                    'url': url,
                    'featureType': featureType,
                    'featureNS': ns
                })
            };	    
            if (projection) {
                options.projection = projection;
            }
            if(styleMap) {
                options.styleMap = styleMap;
            }
            return new $wnd.OpenLayers.Layer.Vector(display, options);
	}-*/;

    public static native final void setProxy(String stringAttribute) 
    /*-{
        $wnd.OpenLayers.ProxyHost = stringAttribute;
    }-*/;

}
