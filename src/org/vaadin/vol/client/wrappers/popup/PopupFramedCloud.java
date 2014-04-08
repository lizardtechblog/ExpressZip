package org.vaadin.vol.client.wrappers.popup;

import org.vaadin.vol.client.wrappers.GwtOlHandler;
import org.vaadin.vol.client.wrappers.LonLat;
import org.vaadin.vol.client.wrappers.Size;

import com.google.gwt.core.client.JavaScriptObject;

public class PopupFramedCloud extends Popup {

    protected PopupFramedCloud() {
    };

    public native static PopupFramedCloud create(String id, LonLat lonlat,
            Size size, String contenHtml, JavaScriptObject anchor,
            boolean closable, GwtOlHandler onclosehandler)
    /*-{

    	if(onclosehandler) {
    		var f = function(evt) {
    			onclosehandler.@org.vaadin.vol.client.wrappers.GwtOlHandler::onEvent(Lcom/google/gwt/core/client/JsArray;)(arguments);
    		};
    	}
    	return new $wnd.OpenLayers.Popup.FramedCloud(id, lonlat, size, contenHtml, anchor ? (anchor.size ? anchor : anchor.icon) : null, closable, f);
    }-*/;

}
