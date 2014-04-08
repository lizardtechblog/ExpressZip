package org.vaadin.vol.client.ui;

import org.vaadin.vol.client.wrappers.layer.GoogleSatelliteLayer;

public class VGoogleSatelliteMapLayer extends VAbstracMapLayer<GoogleSatelliteLayer> {

	@Override
	GoogleSatelliteLayer createLayer() {
		return GoogleSatelliteLayer.create(getDisplayName(), getProjection());
	}

}
