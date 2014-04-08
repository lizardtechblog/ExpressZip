package org.vaadin.vol.client.wrappers.control;

import org.vaadin.vol.client.wrappers.Vector;
import org.vaadin.vol.client.wrappers.layer.VectorLayer;

public class ModifyFeature extends Control {

    protected ModifyFeature() {
    }

    public native static ModifyFeature create(VectorLayer targetLayer)
    /*-{
        var o = {
           multiple: false,
           box: false,
           clickout: false,
           toggle: false,
           hover: false,
           createVertices: false
        }  
            
        var control = new $wnd.OpenLayers.Control.ModifyFeature(targetLayer, o);
                
        control.mode = $wnd.OpenLayers.Control.ModifyFeature.RESHAPE;         
        control.mode |= $wnd.OpenLayers.Control.ModifyFeature.RESIZE;         
        control.mode |= $wnd.OpenLayers.Control.ModifyFeature.DRAG;    
            
      return control;
    }-*/;
    
    public final native Vector getModifiedFeature()
    /*-{
    	return this.feature;
    }-*/;

    // TODO combine these various methods to a common super class. Most shared
    // at least with SelectFeature

    public final native void clickFeature(Vector vector)
    /*-{
        this.selectControl.clickFeature(vector);
    }-*/;

    public final native void select(Vector vector)
    /*-{
        this.selectControl.select(vector);
    }-*/;

    public final native void unselect(Vector vector)
    /*-{
        this.selectControl.unselect(vector);
    }-*/;

    public final native void highlight(Vector vector)
    /*-{
        this.selectControl.highlight(vector);
    }-*/;

    public final native void unhighlight(Vector vector)
    /*-{
        this.selectControl.unhighlight(vector);
    }-*/;

    public final native void resetVertices()
    /*-{
        this.resetVertices();
    }-*/;
}
