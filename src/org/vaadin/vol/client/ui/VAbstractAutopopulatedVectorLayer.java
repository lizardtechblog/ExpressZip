package org.vaadin.vol.client.ui;

import java.util.HashMap;
import java.util.Map;

import org.vaadin.vol.client.wrappers.GwtOlHandler;
import org.vaadin.vol.client.wrappers.Projection;
import org.vaadin.vol.client.wrappers.SelectFeatureFactory;
import org.vaadin.vol.client.wrappers.StyleMap;
import org.vaadin.vol.client.wrappers.Vector;
import org.vaadin.vol.client.wrappers.control.SelectFeature;
import org.vaadin.vol.client.wrappers.format.WKT;
import org.vaadin.vol.client.wrappers.layer.VectorLayer;

import com.google.gwt.core.client.JsArray;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ValueMap;

public abstract class VAbstractAutopopulatedVectorLayer<T> extends
        VAbstracMapLayer<VectorLayer> {

    private Boolean isBaseLayer;
    private Double opacity;
    private boolean transparent;
    private String display;
    protected VectorLayer layer;
    private SelectFeature control;
    private StyleMap styleMap;
    private GwtOlHandler selectedhandler;
    private GwtOlHandler beforeSelectedhandler;
    private GwtOlHandler unselectedhandler;
    private String selectionCtrlId;

    public VAbstractAutopopulatedVectorLayer() {
        super();
    }

    protected void updateGenericVectorLayersAttributes(UIDL uidl) {
        if (!uidl.hasAttribute("cached")) {
            display = uidl.getStringAttribute("display");
            selectionCtrlId = uidl.getStringAttribute("selectionCtrlId");
            setStyleMap(VVectorLayer.getStyleMap(uidl));
        }
    }

    protected void updateSelectionControl(final ApplicationConnection client) {
    	boolean hasSelListener=client.hasEventListeners(this, "vsel");
    	boolean hasUnselListener=client.hasEventListeners(this, "vunsel");
    	boolean hasBefSelListener=client.hasEventListeners(this, "vbefsel");
        if (hasSelListener || hasBefSelListener || hasUnselListener) {
            if (control == null) {
                if (hasSelListener && selectedhandler == null) {
                    selectedhandler = new GwtOlHandler() {
                        @SuppressWarnings("rawtypes")
                        public void onEvent(JsArray arguments) {
                            ValueMap javaScriptObject = arguments.get(0).cast();
                            Vector vector = javaScriptObject.getValueMap(
                                    "feature").cast();
                            String fid = vector.getFeatureId();
                            ValueMap attr = vector.getAttributes();
                            client.updateVariable(paintableId, "fid", fid,
                                    false);
                            Map<String, Object> hashMap = new HashMap<String, Object>();
                            for (String key : attr.getKeySet()) {
                                hashMap.put(key, attr.getString(key));
                            }
                            client.updateVariable(paintableId, "attr", hashMap,
                                    false);
                            Projection targetProjection = getMap()
                                    .getProjection();
                            String projection = getProjection();
                            if (projection == null) {
                                projection = "EPSG:4326";
                            }
                            Projection sourceProjection = Projection
                                    .get(projection);
                            WKT wktFormatter = WKT.create(sourceProjection,
                                    targetProjection);
                            String wkt = wktFormatter.write(vector);
                            client.updateVariable(paintableId, "wkt", wkt,
                                    false);
                            // todo - maybe there is some more important object than fid 
                            client.updateVariable(paintableId, "vsel", fid,
                                    false);
                            client.sendPendingVariableChanges();
                        }
                    };
                    layer.registerHandler("featureselected", selectedhandler);
                }
                if (hasUnselListener && unselectedhandler == null) {
                    unselectedhandler = new GwtOlHandler() {
                        @SuppressWarnings("rawtypes")
                        public void onEvent(JsArray arguments) {
                            ValueMap javaScriptObject = arguments.get(0).cast();
                            Vector vector = javaScriptObject.getValueMap(
                                    "feature").cast();
                            String fid = vector.getFeatureId();
                            ValueMap attr = vector.getAttributes();
                            client.updateVariable(paintableId, "fid", fid,
                                    false);
                            Map<String, Object> hashMap = new HashMap<String, Object>();
                            for (String key : attr.getKeySet()) {
                                hashMap.put(key, attr.getString(key));
                            }
                            client.updateVariable(paintableId, "attr", hashMap,
                                    false);
                            Projection targetProjection = getMap()
                                    .getProjection();
                            String projection = getProjection();
                            if (projection == null) {
                                projection = "EPSG:4326";
                            }
                            Projection sourceProjection = Projection
                                    .get(projection);
                            WKT wktFormatter = WKT.create(sourceProjection,
                                    targetProjection);
                            String wkt = wktFormatter.write(vector);
                            client.updateVariable(paintableId, "wkt", wkt,
                                    false);
                            // todo - maybe there is some more important object than fid 
                            client.updateVariable(paintableId, "vunsel", fid,
                                    false);
                            client.sendPendingVariableChanges();
                        }
                    };
                    layer.registerHandler("featureunselected", unselectedhandler);
                }
                if (hasBefSelListener && beforeSelectedhandler == null) {
                    beforeSelectedhandler = new GwtOlHandler() {
                        @SuppressWarnings("rawtypes")
                        public void onEvent(JsArray arguments) {
                            ValueMap javaScriptObject = arguments.get(0).cast();
                            Vector vector = javaScriptObject.getValueMap(
                                    "feature").cast();
                            String fid = vector.getFeatureId();
                            ValueMap attr = vector.getAttributes();
                            client.updateVariable(paintableId, "fid", fid,
                                    false);
                            Map<String, Object> hashMap = new HashMap<String, Object>();
                            for (String key : attr.getKeySet()) {
                                hashMap.put(key, attr.getString(key));
                            }
                            client.updateVariable(paintableId, "attr", hashMap,
                                    false);
                            Projection targetProjection = getMap()
                                    .getProjection();
                            String projection = getProjection();
                            if (projection == null) {
                                projection = "EPSG:4326";
                            }
                            Projection sourceProjection = Projection
                                    .get(projection);
                            WKT wktFormatter = WKT.create(sourceProjection,
                                    targetProjection);
                            String wkt = wktFormatter.write(vector);
                            client.updateVariable(paintableId, "wkt", wkt,
                                    false);
                            // todo - maybe there is some more important object than fid 
                            client.updateVariable(paintableId, "vbefsel", fid,
                                    false);
                            client.sendPendingVariableChanges();
                        }
                    };
                    layer.registerReturnFalseHandler("beforefeatureselected", 
                    		beforeSelectedhandler);
                }
                /*
                 * In openLayers the constructor for OpenLayers.Control.SelectFeature
                 * takes a array of layers. IMO all Instances of VAbstractAutopopulatedVectorLayer
                 * should share only one map owned select control.
                 */                
                control = SelectFeatureFactory.getInst().getOrCreate(selectionCtrlId,getMap(),layer);
            }
            control.activate();
        } else if (control != null) {
        	SelectFeatureFactory.getInst().removeLayer(control,selectionCtrlId,getMap(),layer);
            control = null;
        }
    }

    public String getDisplay() {
        return display;
    }

    public Boolean isBaseLayer() {
        return isBaseLayer;
    }

    public Double getOpacity() {
        return opacity;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public StyleMap getStyleMap() {
        return styleMap;
    }

    public void setStyleMap(StyleMap styleMap) {
        this.styleMap = styleMap;
    }
}