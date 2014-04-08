package org.vaadin.vol.client.ui;

import org.vaadin.vol.client.wrappers.Bounds;
import org.vaadin.vol.client.wrappers.Projection;
import org.vaadin.vol.client.wrappers.layer.TileMapServiceLayer;

import com.google.gwt.core.client.JavaScriptObject;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;

public class VMapTilerLayer extends VAbstracMapLayer<TileMapServiceLayer> {

    private String uri;
    private String layers;
    private String display;
    private Boolean isBaseLayer;
    private Double opacity;
    private String format;
    private boolean transparent;
    private Bounds mapBounds;
    private int minZoomLevel = 13;
    private int maxZoomLevel = 16;

    @Override
    TileMapServiceLayer createLayer() {
        return TileMapServiceLayer.create(display, uri, isBaseLayer,
                getGetUrlMethod(mapBounds, minZoomLevel, maxZoomLevel));
    }

    private native JavaScriptObject getGetUrlMethod(Bounds mapBounds,
            int minZoomLevel, int maxZoomLevel)
    /*-{
    	function func(bounds) {
            var res = this.map.getResolution();
            var x = Math.round((bounds.left - -20037508) / (res * this.tileSize.w));
            var y = Math.round((bounds.bottom - -20037508) / (res * this.tileSize.h));
            var z = this.map.getZoom();
            if ((mapBounds.intersectsBounds( bounds ) || mapBounds.contains(bounds.getCenterLonLat())) && z >= minZoomLevel && z <= maxZoomLevel ) {
               return this.url + z + "/" + x + "/" + y + "." + this.type;
            } else {
               console.log("not in area??");
               return "http://www.maptiler.org/img/none.png";
            }
    	};
    	return func;
     }-*/;

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        if (!uidl.hasAttribute("cached")) {
            uri = uidl.getStringAttribute("uri");
            display = uidl.getStringAttribute("display");
            isBaseLayer = uidl.getBooleanAttribute("isBaseLayer");

            minZoomLevel = uidl.getIntAttribute("zoomMin");
            maxZoomLevel = uidl.getIntAttribute("zoomMax");
            String[] stringArrayAttribute = uidl
                    .getStringArrayAttribute("bounds");
            double[] b = new double[stringArrayAttribute.length];
            for (int i = 0; i < b.length; i++) {
                b[i] = Double.parseDouble(stringArrayAttribute[i]);
            }
            Bounds bounds = Bounds.create(b[0], b[1], b[2], b[3]);
            bounds.transform(Projection.get("EPSG:4326"), getMap()
                    .getProjection());
            mapBounds = bounds;

            // transparent = uidl.getBooleanAttribute("transparent");
            // opacity = uidl.getDoubleAttribute("opacity");
            // format = uidl.getStringAttribute("format");
        }
        super.updateFromUIDL(uidl, client);
    }

    @Override
    protected void attachLayerToMap() {
        TileMapServiceLayer layer2 = getLayer();
        Bounds maxExtent = layer2.getMaxExtent();
        if (maxExtent == null) {
            /*
             * TMS in openlaeyers is coded with bad habbits. They don't use
             * getters and max extent will inherit extent + tile origin from bad
             * projection.
             */
            Bounds maxExtent3 = getMap().getMaxExtent();
            layer2.setMaxExtent(maxExtent3);
        }
        super.attachLayerToMap();

    }

}
