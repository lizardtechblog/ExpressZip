package org.vaadin.vol.client.ui;


import org.vaadin.vol.client.wrappers.layer.WebFeatureServiceLayer;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;

public class VWebFeatureServiceLayer extends
		VAbstractAutopopulatedVectorLayer<WebFeatureServiceLayer> {

	private String uri;
	private String featureType;
	private String ns;

	@Override
	WebFeatureServiceLayer createLayer() {
		if (layer == null) {
			layer = WebFeatureServiceLayer.create(getDisplay(), uri, featureType,
				ns, getProjection(), getStyleMap());
		}
		return (WebFeatureServiceLayer) layer;
	}

	@Override
	public void updateFromUIDL(UIDL uidl, final ApplicationConnection client) {
		updateGenericVectorLayersAttributes(uidl);
		if (!uidl.hasAttribute("cached")) {
			uri = uidl.getStringAttribute("uri");
			featureType = uidl.getStringAttribute("featureType");
			ns = uidl.getStringAttribute("featureNS");
		}
		
		super.updateFromUIDL(uidl, client);

        if (uidl.hasAttribute("filterType") && layer!=null) {
        	String filterType=uidl.getStringAttribute("filterType");
        	String filterProp=uidl.getStringAttribute("filterProp");
        	String filterValue=uidl.getStringAttribute("filterValue");
    		layer.setFilter(filterType,filterProp,filterValue);                	
        	if(uidl.hasAttribute("filterRefresh"))
        		layer.refresh();
        }
		
        if (uidl.hasAttribute("visibility") && layer!=null)
        	layer.setOLVisibility(uidl.getBooleanAttribute("visibility"));        

        updateSelectionControl(client);        
	}

	public String getUri() {
		return uri;
	}
}
