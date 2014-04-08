package org.vaadin.vol.client.wrappers.layer;

import org.vaadin.vol.client.wrappers.Bounds;
import org.vaadin.vol.client.wrappers.Size;

public class ImageLayer extends Layer {

    protected ImageLayer() {
    };

    public native final static ImageLayer create(String display, String url,
            Bounds b, Size s, boolean isBaseLayer)
    /*-{
       var options = {};
    	options.isBaseLayer = isBaseLayer;
    	return new $wnd.OpenLayers.Layer.Image(display, url, b, s, options);
    }-*/;

}
