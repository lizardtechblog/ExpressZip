package org.vaadin.vol.client.wrappers.layer;

public class BingMapLayer extends Layer {

    protected BingMapLayer() {
    };

    public native final static BingMapLayer create(String displayName,
            String apikey, String type)
    /*-{
     	if(!displayName) {
     	    displayName = "Bing map";
     	}
     	var c ={};
     	c.name = displayName;
     	c.type = type;
     	c.key = apikey;
     	
    	return new $wnd.OpenLayers.Layer.Bing(c);
    }-*/;

}
