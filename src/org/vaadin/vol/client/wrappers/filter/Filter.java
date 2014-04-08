package org.vaadin.vol.client.wrappers.filter;

import com.google.gwt.core.client.JavaScriptObject;

public class Filter extends JavaScriptObject {

    protected Filter() {
    };

    public native static Filter create()
    /*-{
    	return new $wnd.OpenLayers.Filter();
    }-*/;
}
