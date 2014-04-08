package org.vaadin.vol.client.ui;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.vaadin.vol.client.wrappers.Map;
import org.vaadin.vol.client.wrappers.layer.MarkerLayer;

public class VMarkerLayer extends FlowPanel implements VLayer, Container {

    private MarkerLayer markers;
    private String name;
    private boolean layerAdded;

    public MarkerLayer getLayer() {
        if (markers == null) {
            markers = MarkerLayer.create(name);
        }
        return markers;
    }

    public void updateFromUIDL(UIDL layer, ApplicationConnection client) {
        if (client.updateComponent(this, layer, false)) {
            return;
        }
        name = layer.getStringAttribute("name");
        if (!layerAdded) {
            getMap().addLayer(getLayer());
            layerAdded = true;
        }

        Iterator<Widget> iterator = getChildren().iterator();
        LinkedList<Widget> orphaned = new LinkedList<Widget>();
        while (iterator.hasNext()) {
            orphaned.add(iterator.next());
        }
        int childCount = layer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            UIDL childUIDL = layer.getChildUIDL(i);
            VMarkable paintable = (VMarkable)client.getPaintable(childUIDL);
            Widget marker = (Widget)paintable;
            boolean isNew = !hasChildComponent(marker);
            if (isNew) {
                add(marker);
            }
            paintable.updateFromUIDL(childUIDL, client);
            orphaned.remove(marker);
        }
        for (Widget widget : orphaned) {
            remove(widget);
        }

    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if (this.markers != null)
            getMap().removeLayer(this.markers);
    }

    protected Map getMap() {
        return ((VOpenLayersMap) getParent().getParent()).getMap();
    }

    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
        // TODO Auto-generated method stub

    }

    public boolean hasChildComponent(Widget component) {
        return getWidgetIndex(component) != -1;
    }

    public void updateCaption(Paintable component, UIDL uidl) {
        // TODO Auto-generated method stub

    }

    public boolean requestLayout(Set<Paintable> children) {
        // TODO Auto-generated method stub
        return false;
    }

    public RenderSpace getAllocatedSpace(Widget child) {
        // TODO Auto-generated method stub
        return null;
    }

}
