/**
 * 
 */
package org.vaadin.vol;

import org.vaadin.vol.client.ui.VWebFeatureServiceLayer;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.ClientWidget;

@ClientWidget(VWebFeatureServiceLayer.class)
public class WebFeatureServiceLayer extends AbstractAutoPopulatedVectorLayer implements Layer {
    private String uri = "";
    private String featureType = "basic";
    private String featureNS;
    boolean unselectAll=false;


    public WebFeatureServiceLayer() {

    }
    
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.addAttribute("uri", uri);
        target.addAttribute("featureType", featureType);
        target.addAttribute("featureNS", featureNS);
    }
    
    public void setUri(String uri) {
        this.uri = uri;
        requestRepaint();
    }

    public String getUri() {
        return uri;
    }

    public void setFeatureType(String featureType) {
        this.featureType = featureType;
        requestRepaint();
    }

    public String getFeatureType() {
        return featureType;
    }

    public void setFeatureNS(String ns) {
        this.featureNS = ns;
    }

    public String getFeatureNS() {
        return featureNS;
    }    
}