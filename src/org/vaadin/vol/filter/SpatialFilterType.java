package org.vaadin.vol.filter;

public enum SpatialFilterType {

    BBOX("BBOX"), INTERSECTS("INTERSECTS"), DWITHIN("DWITHIN"), WITHIN("WITHIN"), CONTAINS(
            "CONTAINS");

    private String value;

    /**
     * Constructor
     * 
     * @param value
     *            The value to attribute to this enum constant.
     * */
    private SpatialFilterType(String value) {
        this.value = value;
    }

    /**
     * Gets the value property:
     * */
    public String getValue() {
        return value;
    }

}
