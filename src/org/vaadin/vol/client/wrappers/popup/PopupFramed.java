package org.vaadin.vol.client.wrappers.popup;

import org.vaadin.vol.client.wrappers.GwtOlHandler;
import org.vaadin.vol.client.wrappers.LonLat;
import org.vaadin.vol.client.wrappers.Size;

import com.google.gwt.core.client.JavaScriptObject;

public class PopupFramed extends Popup {

    protected PopupFramed() {
    };

    public native static PopupFramed create(String id, LonLat lonlat,
            Size size, String contenHtml, JavaScriptObject anchor,
            boolean closable, GwtOlHandler onclosehandler)
    /*-{

    	if(onclosehandler) {
    		var f = function(evt) {
    			onclosehandler.@org.vaadin.vol.client.wrappers.GwtOlHandler::onEvent(Lcom/google/gwt/core/client/JsArray;)(arguments);
    		};
    	}
    	return new $wnd.OpenLayers.Popup.Framed(id, lonlat, size, contenHtml,  null, closable, f);
    }-*/;

}
