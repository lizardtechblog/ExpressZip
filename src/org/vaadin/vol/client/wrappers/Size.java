package org.vaadin.vol.client.wrappers;

import com.google.gwt.core.client.JavaScriptObject;

public class Size extends JavaScriptObject {
    protected Size() {
    };

    public static native Size create(int width, int height)
    /*-{
    	return new $wnd.OpenLayers.Size(width, height);
    }-*/;

    public final native int getWidth()
    /*-{
            return this.w;
    }-*/;

    public final native int getHeight()
    /*-{
            return this.h;
    }-*/;

}
