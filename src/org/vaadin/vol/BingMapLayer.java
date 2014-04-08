package org.vaadin.vol;

import org.vaadin.vol.client.ui.VBingMapLayer;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

/**
 * BingMap layer that can be added to {@link OpenLayersMap}. Require API key
 * from bingmapportal.com
 * 
 * <p>
 * Note that no settings can be changed after the layer has been drawn for the
 * first time.
 */
@ClientWidget(VBingMapLayer.class)
public class BingMapLayer extends AbstractComponent implements Layer {
    public enum Type {
        Road, Aerial, AerialWithLabels
        // , Birdseye, BirdseyeWithLabels
    }

    private String apikey;
    private String displayName;

    private Type type = Type.Road;

    public BingMapLayer() {
    }

    public BingMapLayer(String apikey) {
        setApikey(apikey);
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        if (apikey == null) {
            throw new IllegalStateException("Bing maps API key must be set!");
        }
        super.paintContent(target);

        if (displayName != null) {
            target.addAttribute("name", displayName);
        }
        target.addAttribute("apikey", apikey);
        target.addAttribute("type", getType().toString());
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public String getApikey() {
        return apikey;
    }

    public void setType(Type t) {
        type = t;
    }

    public Type getType() {
        return type;
    }

}