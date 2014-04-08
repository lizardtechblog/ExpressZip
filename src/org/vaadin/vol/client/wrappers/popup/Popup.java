package org.vaadin.vol.client.wrappers.popup;

import org.vaadin.vol.client.wrappers.AbstractOpenLayersWrapper;
import org.vaadin.vol.client.wrappers.GwtOlHandler;
import org.vaadin.vol.client.wrappers.LonLat;
import org.vaadin.vol.client.wrappers.Size;

public class Popup extends AbstractOpenLayersWrapper {

    protected Popup() {
    };

    public native static Popup create(String id, LonLat lonlat, Size size,
            String contenHtml, boolean closable, GwtOlHandler onclosehandler)
    /*-{

    	if(onclosehandler) {
    		var f = function(evt) {
    			// TODO add event ??
    			onclosehandler.@org.vaadin.vol.client.wrappers.GwtOlHandler::onEvent(Lcom/google/gwt/core/client/JsArray;)(arguments);
    		};
    	}
    	return new $wnd.OpenLayers.Popup(id, lonlat, size, contenHtml, closable, f);
    }-*/;

    public final native void hide()
    /*-{
    	this.hide();
    }-*/;

    public final native void setSize(Size size) 
    /*-{
        this.setSize(size);
    }-*/;

}
