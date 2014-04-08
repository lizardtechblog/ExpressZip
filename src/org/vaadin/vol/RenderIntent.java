package org.vaadin.vol;

import java.io.Serializable;

public class RenderIntent implements Serializable {
    public static final RenderIntent DEFAULT = new RenderIntent("default");
    public static final RenderIntent SELECT = new RenderIntent("select");
    public static final RenderIntent TEMPORARY = new RenderIntent("temporary");
    public static final RenderIntent DELETE = new RenderIntent("delete");

    /**
     * The intent string that OpenLayers JavaScript uses
     * */
    private String value;

    /**
     * Constructor
     * 
     * @param value
     *            The value to attribute to this enum constant.
     * */
    public RenderIntent(String value) {
        this.value = value;
    }

    /**
     * Gets the value property:
     * */
    public String getValue() {
        return value;
    }

}