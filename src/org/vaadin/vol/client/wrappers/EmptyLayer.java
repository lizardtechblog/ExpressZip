
package org.vaadin.vol.client.wrappers;

import org.vaadin.vol.client.wrappers.layer.WebMapServiceLayer;

public class EmptyLayer extends WebMapServiceLayer {

    protected EmptyLayer() {
    }

    public static native EmptyLayer createLayer(boolean isBaseLayer)
    /*-{
        var options = {};
        options.isBaseLayer = isBaseLayer;
        options.displayInLayerSwitcher = false;
        return new $wnd.OpenLayers.Layer('empty', options);
    }-*/;

}
