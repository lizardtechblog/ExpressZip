package org.vaadin.vol.client.wrappers;

import com.google.gwt.core.client.JavaScriptObject;

public class LonLat extends JavaScriptObject {

    protected LonLat() {
    };

    public native static LonLat create(double lon, double lat)
    /*-{
    	return new $wnd.OpenLayers.LonLat(lon, lat)
    }-*/;

    /**
     * TODO Projection type and better typed version of this method.
     * 
     * @param fromProjection
     * @param toProjection
     */
    public final native void transform(Projection fromProjection,
            Projection toProjection)
    /*-{
    	this.transform(fromProjection, toProjection);
    }-*/;

    public final native double getLat()
    /*-{
        return this.lat;
    }-*/;

    public final native double getLon()
    /*-{
        return this.lon;
    }-*/;
}
