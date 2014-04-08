/**
 * 
 */
package org.vaadin.vol;

import java.util.Map;

import org.vaadin.vol.client.ui.VMarker;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

@SuppressWarnings("serial")
@ClientWidget(VMarker.class)
public class Marker extends AbstractComponent {
	private double lon;
	private double lat;
	private String projection = "EPSG:4326";
	private int icon_w;
	private int icon_h;
	private int icon_ox = Integer.MIN_VALUE; // Integer.MIN_VALUE means ignore explicit offset
	private int icon_oy = Integer.MIN_VALUE; // Integer.MIN_VALUE means ignore explicit offset

	public Marker(double lon, double lat) {
		this.lon = lon;
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public double getLat() {
		return lat;
	}

	public void setLon(double lon) {
		this.lon = lon;
		requestRepaint();
	}

	public void setLat(double lat) {
		this.lat = lat;
		requestRepaint();
	}

	public void setIcon(String url, int width, int height) {
		this.setIcon(new ExternalResource(url), width, height, Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	public void setIcon(String url, int width, int height, int xOffset, int yOffset) {
		this.setIcon(new ExternalResource(url), width, height, xOffset, yOffset);
	}
	
	public void setIcon(Resource icon, int width, int height) {
		this.setIcon(icon, width, height, Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	public void setIcon(Resource icon, int width, int height, int xOffset, int yOffset) {
		icon_w = width;
		icon_h = height;
		icon_ox = xOffset;
		icon_oy = yOffset;
		setIcon(icon); // also calls requestRepaint()		
	}
	
	public void paintContent(PaintTarget target) throws PaintException {
		target.addAttribute("lon", lon);
		target.addAttribute("lat", lat);
		target.addAttribute("pr", projection);
		if(getIcon() != null && icon_h != 0 && icon_w != 0) {
			target.addAttribute("icon_w", icon_w);
			target.addAttribute("icon_h", icon_h);
			if(icon_ox != Integer.MIN_VALUE && icon_oy != Integer.MIN_VALUE)
			{
				target.addAttribute("icon_ox", icon_ox);
				target.addAttribute("icon_oy", icon_oy);
			}
		}
	}

	public void addClickListener(ClickListener listener) {
		addListener("click", ClickEvent.class, listener,
				ClickListener.clickMethod);
		requestRepaint();
	}

	public void removeClickListener(ClickListener listener) {
		removeListener(ClickEvent.class, listener);
	}

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);
		if (variables.containsKey("click")) {
			fireEvent(new ClickEvent(this, null));
		}
	}

}