package org.vaadin.vol.client.wrappers.layer;


public class XYZLayer extends Layer {

    protected XYZLayer() {
    };

    public native final static XYZLayer create(String display,
            String uri, boolean sphericalMercator, String attribution)
    /*-{
        var params = {};
        params.sphericalMercator = sphericalMercator;
        if(attribution) params.attribution = attribution;
        return new $wnd.OpenLayers.Layer.XYZ(display, uri, params);
    }-*/;

}
