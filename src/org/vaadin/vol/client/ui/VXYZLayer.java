package org.vaadin.vol.client.ui;

import org.vaadin.vol.client.wrappers.layer.XYZLayer;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;

public class VXYZLayer extends VAbstracMapLayer<XYZLayer> {

    private String uri;
    private boolean spheriacalMercator;

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        if (!uidl.hasAttribute("cached")) {
            uri = uidl.getStringAttribute("uri");
            spheriacalMercator = uidl.getBooleanAttribute("spheriacalMercator");
        }
        super.updateFromUIDL(uidl, client);
    }

    @Override
    XYZLayer createLayer() {
        return XYZLayer.create(getDisplayName(), uri, spheriacalMercator, getAttribution());
    }

}
