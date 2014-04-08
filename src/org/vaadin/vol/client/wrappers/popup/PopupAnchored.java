package org.vaadin.vol.client.wrappers.popup;

import org.vaadin.vol.client.wrappers.GwtOlHandler;
import org.vaadin.vol.client.wrappers.LonLat;
import org.vaadin.vol.client.wrappers.Size;

import com.google.gwt.core.client.JavaScriptObject;

public class PopupAnchored extends Popup {

    protected PopupAnchored() {
    };

    public native static PopupAnchored create(String id, LonLat lonlat,
            Size size, String contenHtml, JavaScriptObject anchor,
            boolean closable, GwtOlHandler onclosehandler)
    /*-{

    	if(onclosehandler) {
    		var f = function(evt) {
    			// TODO add event ??
    			onclosehandler.@org.vaadin.vol.client.wrappers.GwtOlHandler::onEvent(Lcom/google/gwt/core/client/JsArray;)(arguments);
    		};
    	}
    	return new $wnd.OpenLayers.Popup.Anchored(id, lonlat, size, contenHtml, anchor.size ? anchor : anchor.icon, closable, f);
    }-*/;

}
