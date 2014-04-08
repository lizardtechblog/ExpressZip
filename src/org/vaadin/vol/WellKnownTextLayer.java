/**
 * 
 */
package org.vaadin.vol;

import org.vaadin.vol.client.ui.VWellKnownTextLayer;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.ClientWidget;

@ClientWidget(VWellKnownTextLayer.class)
public class WellKnownTextLayer extends AbstractAutoPopulatedVectorLayer implements Layer {
    private String wkt = "";


    public WellKnownTextLayer() {
        setDisplayName("WKT");
    }
    
    public WellKnownTextLayer(String wkt) {
        this();
        setWellKnownText(wkt);
    }
    
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.addAttribute("wkt", wkt);
    }
    
    public void setWellKnownText(String wkt) {
        this.wkt = wkt;
        requestRepaint();
    }

    public String getWellKnownText() {
        return wkt;
    }

}