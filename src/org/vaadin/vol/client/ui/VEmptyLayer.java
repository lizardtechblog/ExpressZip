
package org.vaadin.vol.client.ui;

import org.vaadin.vol.client.wrappers.EmptyLayer;

public class VEmptyLayer extends VWebMapServiceLayer {

    private EmptyLayer layer;

    @Override
    public EmptyLayer getLayer() {
        if (this.layer == null) {
            this.layer = createLayer();
        }
        return this.layer;
    }

    EmptyLayer createLayer() {
        return EmptyLayer.createLayer(this.isBaseLayer());
    }
}
