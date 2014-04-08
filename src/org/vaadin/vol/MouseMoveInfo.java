package org.vaadin.vol;

import java.io.Serializable;

public class MouseMoveInfo implements Serializable {

    private double lat;
    private double lon;

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
    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append("lat: ").append(lat).append(" - ").append("lon: ")
                .append(lon).append("<br>");
        return strBld.toString();
    }
}
