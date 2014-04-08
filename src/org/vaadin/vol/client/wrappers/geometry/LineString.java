package org.vaadin.vol.client.wrappers.geometry;


import com.google.gwt.core.client.JsArray;



public class LineString extends Curve {

	protected LineString(){};

	public native static LineString create(JsArray<Point> points) 
	/*-{
		return new $wnd.OpenLayers.Geometry.LineString($wnd.toOlArray(points));
	}-*/;

	public native final JsArray<Point> getAllVertices() 
	/*-{
		return this.getVertices();
	}-*/;

	public native final JsArray<Point> getEndPoints() 
	/*-{
		return this.getVertices(true);
	}-*/;

	public native final JsArray<Point> getMiddlePoints() 
	/*-{
		return this.getVertices(false);
	}-*/;
}
