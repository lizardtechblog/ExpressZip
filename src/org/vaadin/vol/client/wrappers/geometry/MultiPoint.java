package org.vaadin.vol.client.wrappers.geometry;




public class MultiPoint extends Geometry {

	protected MultiPoint(){};

	public native static MultiPoint create(Point[] points) 
	/*-{
		return new $wnd.OpenLayers.Geometry.MultiPoint(points);
	}-*/;
	
}
