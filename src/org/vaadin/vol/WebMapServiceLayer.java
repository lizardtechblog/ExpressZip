/**
 * 
 */
package org.vaadin.vol;

import org.vaadin.vol.client.ui.VWebMapServiceLayer;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

@ClientWidget(VWebMapServiceLayer.class)
public class WebMapServiceLayer extends AbstractLayerBase implements ProjectedLayer {
	private String uri = "";
	private String type = "wms";
	private String layers = "basic";
	private String cqlFilter = null;
	private String display_name = "";
	private Boolean isBaseLayer = true;
	private Double opacity = 1.0;
	private Boolean transparent = true;
	private Boolean isSingleTile = false;
	private String feature_id = "";
	private String format = "image/jpeg";
	private String projection;
	private String styles;

	public WebMapServiceLayer() {

	}

	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		target.addAttribute("uri", uri);
		target.addAttribute("type", type);
		target.addAttribute("layers", layers);
		target.addAttribute("display", display_name);
		target.addAttribute("isBaseLayer", isBaseLayer);
		target.addAttribute("opacity", opacity);
		target.addAttribute("isSingleTile", isSingleTile);
		target.addAttribute("featureid", feature_id);
		target.addAttribute("format", format);
		target.addAttribute("transparent", transparent);
		if(projection != null) {
		    target.addAttribute("projection", projection);
		}
		if(cqlFilter != null) {
			target.addAttribute("cqlFilter", cqlFilter);
		}
		if(styles != null) {
			target.addAttribute("styles", styles);
		}
	}

		
	public String getStyles() {
		return styles;
	}

	public void setStyles(String styles) {
		this.styles = styles;
		requestRepaint();
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
		this.display_name = displayName;
		requestRepaint();

	}

	public String getUri() {
		return uri;
	}

	public void setServiceType(String name) {
		this.type = name;
		requestRepaint();
	}

	public String getServiceType() {
		return type;
	}

	public String getFeatureID() {
		return feature_id;
	}

	public void setLayers(String layers) {
		this.layers = layers;
		requestRepaint();
	}

	public void resetFeatures() {
		this.feature_id = "";
	}

	public void addFeatureID(String featureid) {
		if (feature_id.equals("")) {
			this.feature_id = featureid;
		} else {
			StringBuffer buf = new StringBuffer(feature_id);
			buf.append(",");
			buf.append(featureid);
			this.feature_id = null;
			this.feature_id = buf.toString();
		}
	}

	public String getLayer() {
		return layers;
	}

	public void setFormat(String format) {
		this.format = format;
		requestRepaint();
	}

	public String getFormat() {
		return format;
	}

	public void setTransparent(Boolean transparent) {
		this.transparent = transparent;
		requestRepaint();
	}

	public Boolean getTransparent() {
		return transparent;
	}

	public void setCqlFilter(String cqlFilter) {
		this.cqlFilter = cqlFilter;
		requestRepaint();
	}

	public String getCqlFilter() {
		return cqlFilter;
	}

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

    public Boolean isSingleTile() {
        return isSingleTile;
    }

    public void setSingleTile(Boolean isSingleTile) {
        this.isSingleTile = isSingleTile;
    }
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}