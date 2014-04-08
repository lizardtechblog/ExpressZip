package org.vaadin.vol.client.ui;

import org.vaadin.vol.client.wrappers.layer.BingMapLayer;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;

public class VBingMapLayer extends VAbstracMapLayer<BingMapLayer> {

    private String apiKey;
    private String type;

    @Override
    BingMapLayer createLayer() {
        return BingMapLayer.create(getDisplayName(), getApiKey(), getType());
    }

    private String getApiKey() {
        return apiKey;
    }

    private String getType() {
        return type;
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        if (!uidl.hasAttribute("cached")) {
            apiKey = uidl.getStringAttribute("apikey");
            type = uidl.getStringAttribute("type");
        }
        super.updateFromUIDL(uidl, client);
    }

}
