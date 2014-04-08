package org.vaadin.vol.client.ui;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;

import org.vaadin.vol.client.wrappers.GwtOlHandler;
import org.vaadin.vol.client.wrappers.Icon;
import org.vaadin.vol.client.wrappers.LonLat;
import org.vaadin.vol.client.wrappers.Map;
import org.vaadin.vol.client.wrappers.Marker;
import org.vaadin.vol.client.wrappers.Pixel;
import org.vaadin.vol.client.wrappers.Projection;
import org.vaadin.vol.client.wrappers.Size;
import org.vaadin.vol.client.wrappers.layer.MarkerLayer;

public class VMarker extends Widget implements VMarkable {

    protected Marker marker;
    protected String paintableId;
    protected ApplicationConnection client;

    public VMarker() {
        setElement(Document.get().createDivElement());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.vaadin.terminal.gwt.client.Paintable#updateFromUIDL(com.vaadin.terminal
     * .gwt.client.UIDL, com.vaadin.terminal.gwt.client.ApplicationConnection)
     */
    public void updateFromUIDL(UIDL childUIDL,
            final ApplicationConnection client) {
        if (client.updateComponent(this, childUIDL, false)) {
            return;
        }

        this.client = client;
        this.paintableId = childUIDL.getStringAttribute("id");

        if (marker != null) {
            getLayer().removeMarker(marker);
        }

        this.beforeMarkerCreation(childUIDL);

        this.marker = this.createMarker(childUIDL);

        this.addEventListeners();

        this.afterMarkerCreation(childUIDL);

        getLayer().addMarker(marker);
    }

    protected Marker createMarker(UIDL childUIDL) {
        LonLat point = createPoint(childUIDL);

        Icon icon = getIcon(childUIDL);
        return Marker.create(point, icon);
    }

    protected LonLat createPoint(UIDL childUIDL) {
        double lon = childUIDL.getDoubleAttribute("lon");
        double lat = childUIDL.getDoubleAttribute("lat");
        LonLat point = LonLat.create(lon, lat);
        this.reproject(childUIDL, point);
        return point;
    }

    protected void reproject(UIDL childUIDL, LonLat point) {
        Projection projection2 = getMap().getProjection();
        final Projection projection = Projection.get(childUIDL
          .getStringAttribute("pr"));
        point.transform(projection, projection2);
    }

    protected void addEventListeners() {
        final VMarkable paintable = this;
        if (this.client.hasEventListeners(this, "click")) {
            this.marker.addClickHandler(new GwtOlHandler() {
                @SuppressWarnings("rawtypes")
                public void onEvent(JsArray arguments) {
                    client.updateVariable(client.getPid(paintable), "click",
                            "", true);
                }
            });
        }
    }

    protected Icon getIcon(UIDL childUIDL) {
        if (childUIDL.hasAttribute("icon")) {
            String url = this.client.translateVaadinUri(childUIDL
                    .getStringAttribute("icon"));
            int width = childUIDL.hasAttribute("icon_w") ? childUIDL
                    .getIntAttribute("icon_w") : 32;
            int height = childUIDL.hasAttribute("icon_h") ? childUIDL
                    .getIntAttribute("icon_h") : 32;
            if(childUIDL.hasAttribute("icon_ox") && childUIDL.hasAttribute("icon_oy")) {
            	return Icon.create(url, Size.create(width, height), 
            			Pixel.create(childUIDL.getIntAttribute("icon_ox"),
            					childUIDL.getIntAttribute("icon_oy")));
            }
            return Icon.create(url, Size.create(width, height));
        }
        return null;
    }

    protected void beforeMarkerCreation(UIDL uidl) {
    }

    protected void afterMarkerCreation(UIDL uidl) {
    }

    protected MarkerLayer getLayer() {
        return ((VMarkerLayer) getParent()).getLayer();
    }

    protected Map getMap() {
        return ((VOpenLayersMap) getParent().getParent().getParent()).getMap();
    }

    public Marker getMarker() {
        return marker;
    }

    @Override
    protected void onDetach() {
        getLayer().removeMarker(marker);
        super.onDetach();
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        if (marker != null) {
            getLayer().addMarker(marker);
        }
    }

}
