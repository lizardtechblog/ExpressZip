package org.vaadin.vol;

import org.vaadin.vol.client.ui.VVirtualEarthMapLayer;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

/**
 * Virtual Earth layer that can be added to {@link OpenLayersMap}.
 * <p>
  */
@ClientWidget(VVirtualEarthMapLayer.class)
public class VirtualEarthMapLayer extends AbstractComponent implements Layer {
	private String displayName;
	
	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

		if(displayName != null) {
			target.addAttribute("displayName", displayName);
		}
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

}