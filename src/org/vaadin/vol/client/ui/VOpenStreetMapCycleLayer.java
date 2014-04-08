package org.vaadin.vol.client.ui;

import org.vaadin.vol.client.wrappers.layer.OpenStreetMapLayer;

public class VOpenStreetMapCycleLayer extends
        VAbstracMapLayer<OpenStreetMapLayer> {

    @Override
    OpenStreetMapLayer createLayer() {
        return OpenStreetMapLayer.createCycleMap(getDisplayName(),
                getProjection());
    }

}
