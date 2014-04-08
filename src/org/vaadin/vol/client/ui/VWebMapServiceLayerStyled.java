package org.vaadin.vol.client.ui;

import org.vaadin.vol.client.wrappers.layer.WebMapServiceLayerStyled;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;

public class VWebMapServiceLayerStyled extends VWebMapServiceLayer
{

    private String sld;
    private WebMapServiceLayerStyled wms;
    private double opacity;

    @Override
    WebMapServiceLayerStyled createLayer() {
     	wms = WebMapServiceLayerStyled.create(super.getDisplay(), super.getUri(), super.getLayers(), super.getFormat(), 
		super.isBaseLayer(), super.isTransparent(), super.getOpacity(), super.isSingleTile(), sld);
    	return wms;
    }
    
    @Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		if(!uidl.hasAttribute("cached")) {
			sld = uidl.getStringAttribute("sld");
			if(super.getOpacity() != null)
			{
				opacity = uidl.getDoubleAttribute("opacity");
				wms.setOpacity(opacity);
			}	
		}
		super.updateFromUIDL(uidl, client);
	}


}
