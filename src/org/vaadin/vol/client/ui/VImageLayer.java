package org.vaadin.vol.client.ui;

import org.vaadin.vol.client.wrappers.Bounds;
import org.vaadin.vol.client.wrappers.Size;
import org.vaadin.vol.client.wrappers.layer.ImageLayer;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;

public class VImageLayer extends VAbstracMapLayer<ImageLayer> {

    private String uri;
    private String displayName;
    private Boolean isBaseLayer;
    private Bounds mapBounds;
    private int width;
    private int height;

    @Override
    ImageLayer createLayer() {
        return ImageLayer.create(displayName, uri, getBounds(), getSize(),
                isBaseLayer);
    }

    private Size getSize() {
        return Size.create(width, height);
    }

    private Bounds getBounds() {
        return mapBounds;
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        if (!uidl.hasAttribute("cached")) {
            width = uidl.getIntAttribute("w");
            height = uidl.getIntAttribute("h");
            uri = uidl.getStringAttribute("uri");
            displayName = uidl.getStringAttribute("name");
            isBaseLayer = uidl.getBooleanAttribute("isBaseLayer");

            String[] stringArrayAttribute = uidl
                    .getStringArrayAttribute("bounds");
            double[] b = new double[stringArrayAttribute.length];
            for (int i = 0; i < b.length; i++) {
                b[i] = Double.parseDouble(stringArrayAttribute[i]);
            }
            Bounds bounds = Bounds.create(b[0], b[1], b[2], b[3]);
            if (!isBaseLayer) {
                bounds.transform(((VOpenLayersMap) getParent().getParent())
                        .getProjection(), getMap().getProjection());
            }
            mapBounds = bounds;

        }
        super.updateFromUIDL(uidl, client);
    }

}
