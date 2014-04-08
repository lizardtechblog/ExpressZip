package org.vaadin.vol.client.ui;

import org.vaadin.vol.client.wrappers.Map;
import org.vaadin.vol.client.wrappers.Projection;
import org.vaadin.vol.client.wrappers.Vector;
import org.vaadin.vol.client.wrappers.layer.VectorLayer;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ValueMap;

public abstract class VAbstractVector extends Widget implements Paintable {

    protected Vector vector;
    protected ValueMap vectAttributes = null;
    private Projection projection;
    private String intent;

    public VAbstractVector() {
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
        if (childUIDL.hasAttribute("projection")) {
            projection = Projection.get(childUIDL
                    .getStringAttribute("projection"));
        } else {
            projection = null;
        }

        boolean update = vector != null;
        if(update) {
            // temporary remove erase the vector 
            getLayer().eraseFeature(vector);
        }
        
        updateAttributes(childUIDL, client);

        createOrUpdateVector(childUIDL, client);
        if (childUIDL.hasAttribute("style")) {
            intent = childUIDL.getStringAttribute("style");
            getVector().setRenderIntent(intent);
        }
        updateStyle(childUIDL, client);

        if (update) {
            ((VVectorLayer) getParent()).vectorUpdated(this);
        } else {
            getLayer().addFeature(vector);
        }
        
    }

    private void updateAttributes(UIDL childUIDL, ApplicationConnection client) {
        if (childUIDL.hasAttribute("olVectAttributes")) {
            vectAttributes = childUIDL.getMapAttribute("olVectAttributes");
        }
    }

    protected ValueMap getAttributes() {
        return vectAttributes;
    }

    private void updateStyle(UIDL childUIDL, ApplicationConnection client) {
        if (childUIDL.hasAttribute("olStyle")) {
            vector.setStyle(childUIDL.getMapAttribute("olStyle"));
        }
    }

    protected Projection getProjection() {
        if (projection == null) {
            VVectorLayer parent2 = (VVectorLayer) getParent();
            return parent2.getProjection();
        }
        return projection;
    }

    protected abstract void createOrUpdateVector(UIDL childUIDL,
            ApplicationConnection client);

    protected VectorLayer getLayer() {
        return ((VVectorLayer) getParent()).getLayer();
    }

    protected Map getMap() {
        return ((VOpenLayersMap) getParent().getParent().getParent()).getMap();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        getLayer().removeFeature(vector);
    }

    public Vector getVector() {
        return vector;
    }

    public void revertDefaultIntent() {
        getVector().setRenderIntent(intent);
        getVector().redraw();
    }

}
