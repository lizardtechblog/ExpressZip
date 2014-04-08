package org.vaadin.vol.client.wrappers.geometry;

import org.vaadin.vol.client.wrappers.Projection;



public class Point extends Geometry {

	protected Point(){};

	public native static Point create(double x, double y) 
	/*-{
		return new $wnd.OpenLayers.Geometry.Point(x,y);
	}-*/;

	public final native void transform(Projection fromProjection, Projection toProjection) 
	/*-{
		this.transform(fromProjection, toProjection);
	}-*/;

	public final native Point nativeClone() 
	/*-{
		return this.clone();
	}-*/;

	public final native double getX() 
	/*-{
		return this.x;
	}-*/;

	public final native double getY() 
	/*-{
		return this.y;
	}-*/;
	
	public final double getLongitude() {
		return getX();
	}
	
	public final double getLatitude() {
		return getY();
	}

	/**
	 * Creates a point from string representation where lon and lat are separated by ":".
	 * @param string
	 * @return
	 */
	public static final Point create(String string) {
		String[] split = string.split(":");
		float lon = Float.parseFloat(split[0]);
		float lat = Float.parseFloat(split[1]);
		return create(lon, lat);
	}

}
