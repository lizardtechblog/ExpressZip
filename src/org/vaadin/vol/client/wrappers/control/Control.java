package org.vaadin.vol.client.wrappers.control;

import org.vaadin.vol.client.wrappers.AbstractOpenLayersWrapper;
import org.vaadin.vol.client.wrappers.Map;

import com.google.gwt.core.client.JavaScriptObject;

public abstract class Control extends AbstractOpenLayersWrapper {

	protected Control () {}
	
	public native final void activate() 
	/*-{
		this.activate();
	}-*/;

	public native final void deActivate() 
	/*-{
		this.deactivate();
	}-*/;

	/**
	 * Helper to create a control by its name.
	 * 
	 * @param name
	 * @param map
	 * @return
	 */
	public static Control getControlByName(String name, Map map) {
//		if("OverviewMap".equals(name)) {
//			return OverviewMap.create(map.getBaseLayer());
//		}
	   if(name.compareToIgnoreCase("ZoomToMaxExtent") == 0)
	      return ZoomToMaxExtent.create();
	   else if(name.compareToIgnoreCase("NavToolbar") == 0){
	      return NavToolbar.create();
	   }
	   else if(name.compareToIgnoreCase("PanZoomBar") == 0){
	      return PanZoomBar.create();
	   }
	   else
	      return getControlByName(name);
	}

	private native static final Control getControlByName(String name) 
	/*-{
	   var str = "getControlByName -";
	   //console.log(str.concat(name)); 
	   if($wnd.OpenLayers.Control[name]) {
			return new $wnd.OpenLayers.Control[name]();
		} else {
			return null;
		}
	}-*/;

	public native final String getId() 
	/*-{
		return this.id;
	}-*/;

}
