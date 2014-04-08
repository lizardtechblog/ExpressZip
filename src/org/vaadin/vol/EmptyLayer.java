
package org.vaadin.vol;

import com.vaadin.ui.ClientWidget;

import org.vaadin.vol.client.ui.VEmptyLayer;

@ClientWidget(VEmptyLayer.class)
public class EmptyLayer extends WebMapServiceLayer {

    public EmptyLayer() {
        this.setBaseLayer(true);
    }
}
