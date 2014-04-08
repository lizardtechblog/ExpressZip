package org.vaadin.vol.filter;

import org.vaadin.vol.Bounds;

public class SpatialFilter extends Filter {

    /**
     * @return the property {String} Name of the context property to compare.
     * 
     */
    public String getProperty() {
        return filter.getPropertyAsString("property");
    }

    /**
     * @param property
     *            the property to set {String} Name of the context property to
     *            compare.
     * 
     */
    public void setProperty(String property) {
        filter.setProperty("property", property);
    }

    /**
     * @return the value {OpenLayers.Bounds || OpenLayers.Geometry} The bounds
     *         or geometry to be used by the filter.
     */
    public Object getValue() {
        return filter.getProperty("value");
    }

    /**
     * @param value
     *            the value to set {OpenLayers.Bounds} The bounds or geometry to
     *            be used by the filter.
     */
    public void setValue(Bounds value) {
        filter.setProperty("value", value);
    }

    /**
     * @param value
     *            the value to set {OpenLayers.Geometry} The bounds or geometry
     *            to be used by the filter.
     */
    // TODO : missing server side Geometry class
    //
    // public void setValue(Geometry value) {
    // filter.setProperty("value", value);
    // }
    /**
     * @return the distance {Number} The distance to use in a DWithin spatial
     *         filter.
     */
    public Double getDistance() {
        return filter.getPropertyAsDouble("distance");
    }

    /**
     * @param distance
     *            the distance to set {Number} The distance to use in a DWithin
     *            spatial filter.
     */
    public void setDistance(Double distance) {
        filter.setProperty("distance", distance);
    }

    /**
     * @return the distanceUnits {String} The units to use for the distance,
     *         e.g. 'm'
     */
    public String getDistanceUnits() {
        return filter.getPropertyAsString("distanceUnits");
    }

    /**
     * @param distanceUnits
     *            the distanceUnits to set {String} The units to use for the
     *            distance, e.g. 'm'
     */
    public void setDistanceUnits(String distanceUnits) {
        filter.setProperty("distanceUnits", distanceUnits);
    }

}
