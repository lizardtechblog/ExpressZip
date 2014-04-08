package org.vaadin.vol.client.ui;

import org.vaadin.vol.client.wrappers.layer.VirtualEarthLayer;

public class VVirtualEarthMapLayer extends VAbstracMapLayer<VirtualEarthLayer> {

	@Override
	VirtualEarthLayer createLayer() {
		return VirtualEarthLayer.create(getDisplayName());
	}

}
