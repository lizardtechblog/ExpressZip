
package org.vaadin.vol.client.ui;

import com.vaadin.terminal.gwt.client.Paintable;

import org.vaadin.vol.client.wrappers.Marker;

public interface VMarkable extends Paintable {
    Marker getMarker();
}
