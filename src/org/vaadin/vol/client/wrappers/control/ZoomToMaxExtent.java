package org.vaadin.vol.client.wrappers.control;

public class ZoomToMaxExtent extends Control {
    protected ZoomToMaxExtent() {
    }
    public static ZoomToMaxExtent create(){
       return ZoomToMaxExtent.createNative();
    }

    public native static ZoomToMaxExtent createNative()
    /*-{
      console.log("ZoomToMaxExtent create");
      
      var maxExtentButton = 
         new $wnd.OpenLayers.Control.ZoomToMaxExtent({title:"Zoom to the max extent"});
      var panel = new $wnd.OpenLayers.Control.Panel({defaultControl: maxExtentButton});
      panel.addControls(maxExtentButton);
       
		return panel;
    }-*/;
}
