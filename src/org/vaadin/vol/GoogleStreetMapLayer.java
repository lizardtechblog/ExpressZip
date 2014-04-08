/**
 * 
 */
package org.vaadin.vol;

import org.vaadin.vol.client.ui.VGoogleStreetMapLayer;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

/**
 * Google street layer that can be added to {@link OpenLayersMap}.
 * <p>
 * <strong>Note</strong> that to use this layer, developer must ensure the host
 * page has google map javascripts inlcuded. This can be achived for example by adding a following script tag to widgetset (...gwt.xml).
 * 
 * <p>
 * <code>
 *  &lt;script src="http://maps.google.com/maps/api/js?v=3.2&amp;sensor=false"&gt;&lt;/script&gt;
 * </code>
 */
@ClientWidget(VGoogleStreetMapLayer.class)
public class GoogleStreetMapLayer extends AbstractLayerBase implements ProjectedLayer {

	private String displayName;
	private String projection;
	
	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		if(projection != null) {
			target.addAttribute("projection", projection);
		}
		if(displayName != null) {
			target.addAttribute("name", displayName);
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

	@Override
    public String toString() {
    	return getDisplayName();
    }

}