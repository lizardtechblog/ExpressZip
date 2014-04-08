package org.vaadin.vol.client.wrappers;

import com.google.gwt.core.client.JavaScriptObject;

public class StyleMap extends JsObject {

    protected StyleMap() {
    };

    public static native StyleMap create()
    /*-{
        return new $wnd.OpenLayers.StyleMap();
    }-*/;

    public static native StyleMap create(Style style)
    /*-{
        return new $wnd.OpenLayers.StyleMap(style);
    }-*/;

    public static native StyleMap create(Style defaultStyle, Style selectStyle,
            Style temporaryStyle)
    /*-{
        return new $wnd.OpenLayers.StyleMap({"default" : defaultStyle,"select" : selectStyle,"temporary" : temporaryStyle});
    }-*/;

    public native final void setStyle(String intent, Style style)
    /*-{
        this.styles[intent] = style;
    }-*/;

    public native final void setExtendDefault(boolean b)
    /*-{
        this.extendDefault = b;
    }-*/;

    public native final void addUniqueValueRules(String intent,
            String property, JavaScriptObject lookup, Object object)
    /*-{
    	this.addUniqueValueRules(intent, property, lookup);
    }-*/;
}
