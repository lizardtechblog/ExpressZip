package org.vaadin.vol.client.wrappers;

import com.google.gwt.core.client.JsArray;

/**
 * Untyped bare bones handler interface for quick prototyping.
 */
public interface GwtOlHandler {
	@SuppressWarnings("rawtypes")
    public void onEvent(JsArray arguments);
}
