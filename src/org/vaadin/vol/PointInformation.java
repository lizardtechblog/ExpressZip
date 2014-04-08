package org.vaadin.vol;

import java.io.Serializable;

public class PointInformation implements Serializable {

    private int x;
    private int y;
    private double lat;
    private double lon;
    // height & width of the map when the point clicked
    private int height;
    private int width;
    private Bounds bounds;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public Bounds getBounds() {
        return bounds;
    }

    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    public String getBoundingBoxString() {
        return bounds.getLeft() + "," + bounds.getBottom() + ","
                + bounds.getRight() + "," + bounds.getTop();
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append("x: ").append(x).append(" - ").append("y: ").append(y)
                .append("<br>");
        strBld.append("lat: ").append(lat).append(" - ").append("lon: ")
                .append(lon).append("<br>");
        strBld.append("width: ").append(width).append(" - ").append("height: ")
                .append(height).append("<br>");
        strBld.append("bbox: ").append(getBoundingBoxString());
        return strBld.toString();
    }
}
