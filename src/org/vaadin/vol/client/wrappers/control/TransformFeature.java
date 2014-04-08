package org.vaadin.vol.client.wrappers.control;

import org.vaadin.vol.client.wrappers.Vector;
import org.vaadin.vol.client.wrappers.geometry.Geometry;
import org.vaadin.vol.client.wrappers.handler.Handler;
import org.vaadin.vol.client.wrappers.layer.VectorLayer;

import com.google.gwt.core.client.JavaScriptObject;

public class TransformFeature extends Control {
    protected TransformFeature() {
    }

    public native static TransformFeature create(VectorLayer targetLayer)
    /*-{
    	 return new $wnd.OpenLayers.Control.TransformFeature(targetLayer, {rotate: false, irregular: true});
    }-*/;

    public native static TransformFeature create(VectorLayer targetLayer,
            JavaScriptObject options)
    /*-{
    	return new $wnd.OpenLayers.Control.TransformFeature(targetLayer, options);
    }-*/;

	public native final void setFeature(Vector feature)
	/*-{
        this.setFeature(feature);
	}-*/;

   public native final void destroy() 
   /*-{
      this.destroy();
   }-*/;
	
}
