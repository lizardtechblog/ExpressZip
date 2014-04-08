package org.vaadin.vol.filter;

public enum ComparisonFilterType {

    EQUAL_TO("=="), NOT_EQUAL_TO("!="), LESS_THAN("<"), GREATER_THAN(">"), LESS_THAN_OR_EQUAL_TO(
            "<="), GREATER_THAN_OR_EQUAL_TO(">="), BETWEEN(".."), LIKE("~");

    private String value;

    /**
     * Constructor
     * 
     * @param value
     *            The value to attribute to this enum constant.
     * */
    private ComparisonFilterType(String value) {
        this.value = value;
    }

    /**
     * Gets the value property:
     * */
    public String getValue() {
        return value;
    }

}
