/**
 * 
 */
package org.vaadin.vol;

import org.vaadin.vol.client.ui.VOpenStreetMapCycleLayer;

import com.vaadin.ui.ClientWidget;

/**
 * OpenCycleMap (OpenStreetMaps sister project) layer that can be added to
 * {@link OpenLayersMap}.
 * <p>
 * <strong>Note</strong> that to use this layer, developer must ensure the host
 * page has open street map javascripts inlcuded. This can be achived for
 * example by adding a following script tag to widgetset (...gwt.xml).
 * 
 * <p>
 * <code>
 *  &lt;script src="http://www.openstreetmap.org/openlayers/OpenStreetMap.js"&gt;&lt;/script&gt;
 * </code>
 */
@ClientWidget(VOpenStreetMapCycleLayer.class)
public class OpenStreetMapCycleLayer extends OpenStreetMapLayer implements
        Layer {

    public OpenStreetMapCycleLayer() {
        setDisplayName("OSM Cycle Map");
    }

}