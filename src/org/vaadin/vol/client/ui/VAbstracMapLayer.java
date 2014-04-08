package org.vaadin.vol.client.ui;

import org.vaadin.vol.client.wrappers.GwtOlHandler;
import org.vaadin.vol.client.wrappers.Map;
import org.vaadin.vol.client.wrappers.layer.Layer;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;

public abstract class VAbstracMapLayer<T extends Layer> extends Widget
        implements VLayer {

    public VAbstracMapLayer() {
        setElement(Document.get().createDivElement());
    }

    private T layer;
    protected boolean layerAttached = false;
    private String displayName;
    private String projection;
    protected String paintableId;
    private int index; // z-order index
    
    private GwtOlHandler loadStartHandler;
    private GwtOlHandler loadEndHandler;
    private GwtOlHandler visChangedHandler;
    private String attribution;
    

    public T getLayer() {
        if (layer == null) {
            layer = createLayer();
        }
        return layer;
    }

    abstract T createLayer();

    public void updateFromUIDL(UIDL uidl, final ApplicationConnection client) {
        if (client.updateComponent(this, uidl, false)) {
            return;
        }

        if (!uidl.hasAttribute("cached")) {
            this.paintableId = uidl.getId();
        }
        
        attribution = uidl.hasAttribute("attribution") ? uidl
                .getStringAttribute("attribution") : null;
        displayName = uidl.hasAttribute("name") ? uidl
                .getStringAttribute("name") : null;
        projection = uidl.hasAttribute("projection") ? uidl
                .getStringAttribute("projection") : null;

        // we'll do this lazy, not in attach, so implementations can
        // customize parameters for layer constructors. Possible changes must be
        // dealt inimplementation.
        if (!layerAttached) {
            attachLayerToMap();
            layerAttached = true;
        }

		index = uidl.hasAttribute("index") ? uidl.getIntAttribute("index") : -1;
		if (index != -1) {
			getMap().setLayerIndex(layer, index);
		}

        if (uidl.hasAttribute("visibility") && layer!=null)
            layer.setOLVisibility(uidl.getBooleanAttribute("visibility"));        

    	if (loadStartHandler==null && client.hasEventListeners(this, "llstart")) {
            loadStartHandler = new GwtOlHandler() {
                @SuppressWarnings("rawtypes")
                public void onEvent(JsArray arguments) {
                	String layerName=displayName!=null?displayName:paintableId;
                    client.updateVariable(paintableId, "llstart",layerName,false);
                    client.sendPendingVariableChanges();
                }
            };
            layer.registerHandler("loadstart", loadStartHandler);
    		
    	}
    	if (loadEndHandler==null && client.hasEventListeners(this, "llend")) {
            loadEndHandler = new GwtOlHandler() {
                @SuppressWarnings("rawtypes")
                public void onEvent(JsArray arguments) {
                	String layerName=displayName!=null?displayName:paintableId;
                    client.updateVariable(paintableId, "llend",layerName,false);
                    client.sendPendingVariableChanges();
                }
            };
            layer.registerHandler("loadend", loadEndHandler);    		
    	}
    	if (visChangedHandler==null && client.hasEventListeners(this, "lvis")) {
            visChangedHandler = new GwtOlHandler() {
                @SuppressWarnings("rawtypes")
                public void onEvent(JsArray arguments) {
                	String layerName=displayName!=null?displayName:paintableId;
                    client.updateVariable(paintableId, "lvis",layerName,false);
                    client.updateVariable(paintableId, "lisvis",layer.isVisible(),false);
                    client.sendPendingVariableChanges();
                }
            };
            layer.registerHandler("visibilitychanged", visChangedHandler);    		    		
    	}
    }

    protected void attachLayerToMap() {
        getMap().addLayer(getLayer());
    }

    protected Map getMap() {
        return ((VOpenLayersMap) getParent().getParent()).getMap();
    }

    @Override
    protected void onDetach() {
        if(layerAttached) {
            getMap().removeLayer(layer);
            layerAttached = false;
        }
        super.onDetach();
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        if(!layerAttached && layer != null) {
            attachLayerToMap();
        }
    }

    protected String getProjection() {
        return projection;
    }

    protected String getDisplayName() {
        return displayName;
    }
    
    protected String getAttribution() {
        return attribution;
    }

}
