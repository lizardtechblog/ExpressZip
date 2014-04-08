package org.vaadin.vol.client.wrappers.control;


public class NavToolbar extends Control {
	protected NavToolbar() {};
	
	public static native NavToolbar create() 
	/*-{
	   	console.log("NAVTOOLBAR CREATE()");
		var nav = new $wnd.OpenLayers.Control.NavToolbar();
		var zoom = new $wnd.OpenLayers.Control.ZoomToMaxExtent({title:"Zoom to the max extent"});
		nav.addControls([zoom]);
		return nav;
	}-*/;
	
	public static native void addControl(NavToolbar nav, Control c)
	/*-{
	   	console.log("NAVTOOLBAR addControl()");
		nav.addControls([c]);
	}-*/;

	public static native void activateControl(NavToolbar nav, Control c)
	/*-{
	   	console.log("NAVTOOLBAR activateControl()");
		nav.activateControl(c);
	}-*/;

	public static native void removeControl(NavToolbar nav, Control c)
	/*-{
	   	console.log("NAVTOOLBAR removeControl()");
		$wnd.OpenLayers.Util.removeItem(nav.controls, c);
		nav.redraw();
	}-*/;


}
