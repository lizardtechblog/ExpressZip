package org.vaadin.vol.client.wrappers.handler;

import com.google.gwt.core.client.JavaScriptObject;

public class RegularPolygonHandler extends Handler {
    protected RegularPolygonHandler() {
    }

    public final native static RegularPolygonHandler get()
    /*-{
    	return $wnd.OpenLayers.Handler.RegularPolygon;
     }-*/;

    public static final native JavaScriptObject getRectangleOptions()
    /*-{
        return { handlerOptions: { sides: 4, snapAngle: 90, irregular: true, persist: true}};
    }-*/;

	public static final native JavaScriptObject getCircleOptions()
	/*-{
        return { handlerOptions: { sides: 36, irregular: false}};
    }-*/;

}
