package org.vaadin.vol.client.wrappers;

import com.google.gwt.core.client.JavaScriptObject;

public class Bounds extends JavaScriptObject {
	protected Bounds(){};
	
	public static native final Bounds create(double left, double bottom, double right, double top)
	/*-{
		return new $wnd.OpenLayers.Bounds(left,bottom, right, top);
	}-*/;

	public native final void transform(Projection from, Projection to) 
	/*-{
		this.transform(from, to);
	}-*/;

	public native final double getBottom() 
	/*-{
		return this.bottom;
	}-*/;

	public native final double getLeft() 
	/*-{
		return this.left;
	}-*/;

	public native final double getTop() 
	/*-{
		return this.top;
	}-*/;

	public native final double getRight() 
	/*-{
		return this.right;
	}-*/;
	
	public native final Bounds clone()
   /*-{
      return this.clone();
   }-*/;

	

}
