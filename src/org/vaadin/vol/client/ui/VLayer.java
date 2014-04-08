package org.vaadin.vol.client.ui;

import org.vaadin.vol.client.wrappers.layer.Layer;

import com.vaadin.terminal.gwt.client.Paintable;

public interface VLayer extends Paintable {
	
	Layer getLayer();

}
