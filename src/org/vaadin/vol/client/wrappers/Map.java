package org.vaadin.vol.client.wrappers;

import org.vaadin.vol.client.wrappers.control.Control;
import org.vaadin.vol.client.wrappers.layer.Layer;
import org.vaadin.vol.client.wrappers.layer.VectorLayer;
import org.vaadin.vol.client.wrappers.popup.Popup;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget that contains open layers map. Proxys all relevant OpenLayers map
 * methods to the contained map.
 */
public class Map extends Widget {

    private static int sequense = 0;

    private MapOverlay jsoverlay;

    private final DivElement mapElement;

    private String mapInitOptions;

    @SuppressWarnings("unchecked")
    private JsArray<Control> initialControls = (JsArray<Control>) JsArray
            .createArray();

    public Map() {
        setElement(Document.get().createDivElement());
        mapElement = Document.get().createDivElement();
        Style style = mapElement.getStyle();
        style.setWidth(100, Unit.PCT);
        style.setHeight(100, Unit.PCT);
        setWidth("100%");
        setHeight("100%");
        getElement().appendChild(mapElement);
        String id = "VOLMAP_" + sequense++;
        mapElement.setId(id);
    }

    /**
     * @param mapInitOptions
     *            the js string that will be evaluated as options for the map.
     */
    public void setMapInitOptions(String mapInitOptions) {
        this.mapInitOptions = mapInitOptions;
    }

    public String getMapInitOptions() {
        return mapInitOptions;
    }

    private MapOverlay getMap() {
        if (jsoverlay == null) {
            jsoverlay = MapOverlay.get(mapElement.getId(), mapInitOptions,
                    initialControls);
        }
        return jsoverlay;
    }
    
    public Control addMaxZoomPanel(){
       return getMap().addMaxZoomPanel();
    }
    public Control addControlsInPanel(Control c1, Control c2) {
       return getMap().addControlsInPanel(c1, c2);
    }

    public void addControl(Control control) {
        if (jsoverlay == null) {
            initialControls.push(control);
        } else {
            getMap().addControl(control);
        }
    }

    public void resetLayersZIndex() {
    	getMap().resetLayersZIndex();
    }

    public void setLayerIndex(Layer layer, int index) {
    	getMap().setLayerIndex(layer, index);
    }
    
    public int getLayerIndex(Layer layer) {
    	return getMap().getLayerIndex(layer);
    }
    
    public void addLayer(Layer layer) {
        getMap().addLayer(layer);
    }

    public void removeLayer(Layer remove) {
        getMap().removeLayer(remove);
    }

    public void setCenter(LonLat lonLat, int zoom) {
        getMap().setCenter(lonLat, zoom);

    }

    public Projection getProjection() {
        return getMap().getProjection();
    }

    public void addPopup(Popup popup) {
        getMap().addPopup(popup);
    }

    public void removePopup(Popup popup) {
        getMap().removePopup(popup);
    }

    public Layer getLayer(String id) {
        return getMap().getLayer(id);
    }

    public void removeControl(Control control) {
        getMap().removeContol(control);
    }

    public Bounds getMaxExtent() {
        return getMap().getMaxExtent();
    }

    public int getZoom() {
        return getMap().getZoom();
    }

    public void setZoom(int zoom) {
        getMap().zoomTo(zoom);
    }

    public void registerEventHandler(String evtName, GwtOlHandler handler) {
        getMap().registerHandler(evtName, handler);
    }

    public Bounds getExtent() {
        return getMap().getExtent();
    }

    public void zoomToExtent(Bounds bounds) {
        getMap().zoomToExtent(bounds);
    }

    public void setRestrictedExtent(Bounds bounds) {
        getMap().setRestrictedExtent(bounds);
    }

    public Layer getBaseLayer() {
        return getMap().getBaseLayer();
    }

    public void setBaseLayer(Layer layer) {
        getMap().setBaseLayer(layer);
    }
        
    public JsArray<Control> getControls() {
        return getMap().getControls();
    }

    public LonLat getLonLatFromPixel(Pixel pixel) {
        return getMap().getLonLatFromPixel(pixel);
    }

    public void updateSize() {
        getMap().updateSize();
    }


}
