package org.vaadin.vol.client.wrappers.filter;

import com.google.gwt.core.client.JavaScriptObject;

public class ComparisonFilter extends JavaScriptObject {

    protected ComparisonFilter() {
    };

    public native static ComparisonFilter create()
    /*-{
    	return new $wnd.OpenLayers.Filter.Comparison();
    }-*/;

    public native static ComparisonFilter create(JavaScriptObject comp)
    /*-{
    	return new $wnd.OpenLayers.Filter.Comparison(comp);
    }-*/;
}
