/**
 * 
 */
package org.vaadin.vol;

import org.vaadin.vol.client.ui.VOpenStreetMapLayer;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

/**
 * OpenStreeMap layer that can be added to {@link OpenLayersMap}.
 */
@ClientWidget(VOpenStreetMapLayer.class)
public class OpenStreetMapLayer extends AbstractLayerBase implements ProjectedLayer {

    private String displayName;
    private String projection;
    private String url;

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        if (projection != null) {
            target.addAttribute("projection", projection);
        }
        if (displayName != null) {
            target.addAttribute("name", displayName);
        }
        if (url != null) {
            target.addAttribute("url", url);
        }

    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

    public String getProjection() {
        return projection;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
    	return getDisplayName();
    }
}