/**
 * 
 */
package org.vaadin.vol;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.vaadin.vol.client.ui.VMapTilerLayer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

/**
 * see great tool: www.maptiler.org
 */
@ClientWidget(VMapTilerLayer.class)
public class MapTilerLayer extends AbstractComponent implements Layer {
	private static final double MARGIN = 0.0001;
	private String uri = "";
	private String layers = "basic";
	private String display_name = "";
	private Boolean isBaseLayer = true;
	private Double opacity = 1.0;
	private Boolean transparent = true;
	private Double[] bounds;
	private int minZoom;
	private int maxZoom = -1;

	public MapTilerLayer(Double[] bounds, int maxZoom, int minZoom) {
		this.setMaxZoom(maxZoom);
		this.setMinZoom(minZoom);
		this.setBounds(bounds);
	}

	public MapTilerLayer(String url) throws SAXException, IOException,
			ParserConfigurationException {
		setUri(url);
		URL url2 = new URL(url + "tilemapresource.xml");
		Document doc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().parse(url2.openStream());
		Node bbox = doc.getElementsByTagName("BoundingBox").item(0);
		
		// yes, these a fucked up, probably in maptiler xml file
		double bottom = Double.parseDouble(bbox.getAttributes()
				.getNamedItem("minx").getNodeValue());
		double left = Double.parseDouble(bbox.getAttributes()
				.getNamedItem("miny").getNodeValue());
		double top = Double.parseDouble(bbox.getAttributes()
				.getNamedItem("maxx").getNodeValue());
		double right = Double.parseDouble(bbox.getAttributes()
				.getNamedItem("maxy").getNodeValue());
		setBounds(new Double[] {left-MARGIN,bottom-MARGIN,right+MARGIN,top+MARGIN });

		Node tilesets = doc.getElementsByTagName("TileSets").item(0);

		for (int i = 0; i < tilesets.getChildNodes().getLength(); i++) {
			Node item = tilesets.getChildNodes().item(i);
			if(!item.getNodeName().equals("TileSet")) {
				continue;
			}
			int z = Integer.parseInt(item.getAttributes().getNamedItem("order")
					.getNodeValue());
			if (getMaxZoom() == -1) {
				setMaxZoom(setMinZoom(z));
			} else if (z > getMaxZoom()) {
				setMaxZoom(z);
			} else if (z < getMinZoom()) {
				setMinZoom(z);
			}
		}
	}

	public void paintContent(PaintTarget target) throws PaintException {
		target.addAttribute("uri", uri);
		target.addAttribute("layers", layers);
		target.addAttribute("display", display_name);
		target.addAttribute("isBaseLayer", isBaseLayer);
		target.addAttribute("opacity", opacity);
		target.addAttribute("transparent", transparent);
		target.addAttribute("zoomMax", getMaxZoom());
		target.addAttribute("zoomMin", getMinZoom());
		target.addAttribute("bounds", getBounds());
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

	public void setLayers(String layers) {
		this.layers = layers;
		requestRepaint();
	}

	public String getLayer() {
		return layers;
	}

	public void setTransparent(Boolean transparent) {
		this.transparent = transparent;
	}

	public Boolean getTransparent() {
		return transparent;
	}

    public void setBounds(Double[] bounds) {
        this.bounds = bounds;
    }

    public Double[] getBounds() {
        return bounds;
    }

    public int setMinZoom(int minZoom) {
        this.minZoom = minZoom;
        return minZoom;
    }

    public int getMinZoom() {
        return minZoom;
    }

    public void setMaxZoom(int maxZoom) {
        this.maxZoom = maxZoom;
    }

    public int getMaxZoom() {
        return maxZoom;
    }
}