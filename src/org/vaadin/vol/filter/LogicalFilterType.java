package org.vaadin.vol.filter;

public enum LogicalFilterType {

    AND("&&"), OR("||"), NOT("!");

    private String value;

    /**
     * Constructor
     * 
     * @param value
     *            The value to attribute to this enum constant.
     * */
    private LogicalFilterType(String value) {
        this.value = value;
    }

    /**
     * Gets the value property:
     * */
    public String getValue() {
        return value;
    }

}
