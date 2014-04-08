package org.vaadin.vol.client.wrappers.geometry;


import com.google.gwt.core.client.JsArray;

public class LinearRing extends LineString {

	protected LinearRing(){};

	public native static LinearRing create(JsArray<Point> points) 
	/*-{
		points.push(points[0]);
		
		var linearRing = new $wnd.OpenLayers.Geometry.LinearRing($wnd.toOlArray(points));
		
		return linearRing;
	}-*/;

}
