package org.vaadin.vol.client.wrappers.geometry;

import org.vaadin.vol.client.wrappers.Projection;



public class Curve extends MultiPoint {

	protected Curve(){};

	public native static Curve create(Point[] points) 
	/*-{
		return new $wnd.OpenLayers.Geometry.Curve(points);
	}-*/;

	public native final float getLength() 
	/*-{
		this.getLength();
	}-*/;
	
	public native final float getGeodesicLength(Projection projection) 
	/*-{
		this.getGeodesicLength(projection);
	}-*/;

}
