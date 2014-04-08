package org.vaadin.vol.client.wrappers;

import com.google.gwt.core.client.JavaScriptObject;

public abstract class AbstractOpenLayersWrapper extends JavaScriptObject {

	protected AbstractOpenLayersWrapper(){};
	

	public native final void registerHandler(String eventName, GwtOlHandler handler) 
	/*-{
		var f = function() {
			$entry(handler.@org.vaadin.vol.client.wrappers.GwtOlHandler::onEvent(Lcom/google/gwt/core/client/JsArray;)(arguments));
		};
		this.events.addEventType(eventName);
		this.events.register(eventName,this,f);
		
	}-*/;

}
