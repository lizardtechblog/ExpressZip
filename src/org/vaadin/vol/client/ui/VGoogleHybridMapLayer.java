package org.vaadin.vol.client.ui;

import org.vaadin.vol.client.wrappers.layer.GoogleHybridLayer;

public class VGoogleHybridMapLayer extends VAbstracMapLayer<GoogleHybridLayer> {

	@Override
	GoogleHybridLayer createLayer() {
		return GoogleHybridLayer.create(getDisplayName(), getProjection());
	}

}
