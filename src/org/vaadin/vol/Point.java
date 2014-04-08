package org.vaadin.vol;

import java.io.Serializable;

public class Point implements Serializable {
	private double lon;
	private double lat;
	
	public Point(double lon, double lat) {
		this.lon = lon;
		this.lat = lat;
	}
	
	public void setLon(double lon) {
		this.lon = lon;
	}
	public double getLon() {
		return lon;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLat() {
		return lat;
	}
	
	@Override
	public String toString() {
		return lon + ":" + lat;
	}

	public static Point valueOf(String string) {
		double lon;
		double lat;
		String substring = string.substring(6, string.length() - 1);
		String[] split = substring.split(" ");
		lon = Double.parseDouble(split[0]);
		lat = Double.parseDouble(split[1]);
		return new Point(lon, lat);
	}

}
