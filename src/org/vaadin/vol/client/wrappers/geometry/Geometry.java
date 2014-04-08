package org.vaadin.vol.client.wrappers.geometry;

import org.vaadin.vol.client.wrappers.Bounds;

import com.google.gwt.core.client.JavaScriptObject;


public abstract class Geometry extends JavaScriptObject {

	protected Geometry(){};
	
	
	public native final Bounds getBounds() 
	/*-{
		return this.getBounds();
	}-*/;
	
	public native final Geometry clone()
   /*-{
      console.log("clone");
      return this.clone();
   }-*/;
	   
	public native final void setBounds(double left, double bottom, double right, double top)
   /*-{
      console.log("setBounds");
      this.setBounds(new $wnd.OpenLayers.Bounds(left,bottom, right, top));
   }-*/;
}
