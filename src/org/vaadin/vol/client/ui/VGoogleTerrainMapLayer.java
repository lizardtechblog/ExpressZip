package org.vaadin.vol.client.ui;

import org.vaadin.vol.client.wrappers.layer.GoogleTerrainLayer;

public class VGoogleTerrainMapLayer extends VAbstracMapLayer<GoogleTerrainLayer> {

	@Override
	GoogleTerrainLayer createLayer() {
		return GoogleTerrainLayer.create(getDisplayName(), getProjection());
	}

}
