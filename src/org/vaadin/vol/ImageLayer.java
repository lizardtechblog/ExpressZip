/**
 * 
 */
package org.vaadin.vol;

import org.vaadin.vol.client.ui.VImageLayer;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

/**
 */
@ClientWidget(VImageLayer.class)
public class ImageLayer extends AbstractComponent implements Layer {
    private String uri = "";
    private String display_name = "";
    private Boolean isBaseLayer = true;
    private Double opacity = 1.0;
    private Boolean transparent = true;
    private Double[] bounds = new Double[] { -180d, -90d, 180d, 90d };
    private int height;
    private int width;

    public ImageLayer(String url, int width, int height) {
        setUri(url);
        this.width = width;
        this.height = height;
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        target.addAttribute("uri", uri);
        target.addAttribute("name", getDisplayName());
        target.addAttribute("isBaseLayer", isBaseLayer);
        target.addAttribute("opacity", opacity);
        target.addAttribute("transparent", transparent);
        target.addAttribute("bounds", getBounds());
        target.addAttribute("w", width);
        target.addAttribute("h", height);
    }

    public void setUri(String uri) {
        this.uri = uri;
        requestRepaint();
    }

    public void setBaseLayer(boolean isBaseLayer) {
        this.isBaseLayer = isBaseLayer;
        requestRepaint();
    }

    public boolean isBaseLayer() {
        return isBaseLayer;
    }

    public void setOpacity(Double opacity) {
        this.opacity = opacity;
        requestRepaint();
    }

    public Double getOpacity() {
        return opacity;
    }

    public String getDisplayName() {
        return display_name;
    }

    public void setDisplayName(String displayName) {
        display_name = displayName;
        requestRepaint();

    }

    public String getUri() {
        return uri;
    }

    public void setTransparent(Boolean transparent) {
        this.transparent = transparent;
    }

    public Boolean getTransparent() {
        return transparent;
    }

    public void setBounds(Double... bounds) {
        this.bounds = bounds;
    }

    public Double[] getBounds() {
        return bounds;
    }

}