package org.vaadin.vol;

import org.vaadin.vol.client.ui.VGoogleHybridMapLayer;

import com.vaadin.ui.ClientWidget;

/**
 * Google hybrid (satellite+streets) layer that can be added to {@link OpenLayersMap}.
 * <p>
 * <strong>Note</strong> that to use this layer, developer must ensure the host
 * page has google map javascripts inlcuded. This can be achived for example by
 * adding a following script tag to widgetset (...gwt.xml).
 * 
 * <p>
 * <code>
 *  &lt;script src="http://maps.google.com/maps/api/js?v=3.2&amp;sensor=false"&gt;&lt;/script&gt;
 * </code>
 */
@ClientWidget(VGoogleHybridMapLayer.class)
public class GoogleHybridMapLayer extends GoogleStreetMapLayer implements
		ProjectedLayer {

}