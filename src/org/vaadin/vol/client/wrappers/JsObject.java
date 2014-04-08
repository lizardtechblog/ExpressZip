package org.vaadin.vol.client.wrappers;

import com.google.gwt.core.client.JavaScriptObject;
import com.vaadin.terminal.gwt.client.ValueMap;

public class JsObject extends JavaScriptObject {
    protected JsObject() {
    }

    /**
     * Returns a new object.
     */
    public static native JsObject createObject()
    /*-{
    	return {};
    }-*/;

    public final native JsObject getFieldByName(String name)
    /*-{
    	return this[name];
    }-*/;

    public final native void setProperty(String name, int value)
    /*-{
    	this[name] = value;
    }-*/;

    public final native int getPropertyAsInt(String name)
    /*-{
    	var ret = this[name];
    	return (ret === undefined) ? 0 : ret;
    }-*/;

    public final native void setProperty(String name, String value)
    /*-{
    	this[name] = value;
    }-*/;

    public final native String getPropertyAsString(String name)
    /*-{
    	var ret = this[name];
    	return (ret === undefined) ? null : ret;
    }-*/;

    public final native void setProperty(String name, boolean value)
    /*-{
    	this[name] = value;
    }-*/;

    public final native boolean getPropertyAsBoolean(String name)
    /*-{
    	var ret = this[name];
    	return (ret === undefined) ? 0 : ret;
    }-*/;

    public final native void setProperty(String name, float value)
    /*-{
    	this[name] = value;
    }-*/;

    public final native float getPropertyAsFloat(String name)
    /*-{
    	var ret = this[name];
    	return (ret === undefined) ? 0 : ret;
    }-*/;

    public final native void setProperty(String name, double value)
    /*-{
    	this[name] = value;
    }-*/;

    public final native double getPropertyAsDouble(String name)
    /*-{
    	var ret = this[name];
    	return (ret === undefined) ? 0 : ret;
    }-*/;

    public final native void setProperty(String name, JsObject value)
    /*-{
    	this[name] = value;
    }-*/;

    public final native void setProperty(String name, JavaScriptObject value)
    /*-{
    	this[name] = value;
    }-*/;

    public final native void setProperty(String name, ValueMap value)
    /*-{
    	this[name] = value;
    }-*/;

    public final native JsObject getProperty(String name)
    /*-{
    	var ret = this[name];
    	return (ret === undefined) ? null : ret;
    }-*/;

    public final native String getPropertyNames()
    /*-{
    	var ret = "";
    	for (var i in this){
    		if(ret == ""){
    			ret = ret + i
    		} else {
    			ret = ret + "," + i;
    		}
    	}
    	return ret;
    }-*/;

    public final native boolean hasProperty(String name)
    /*-{
    	if(this[name] != undefined){return true}else{return false};
    }-*/;

    /**
     * Unset/Clear the property with the given name. Uses the javascript
     * operator delete
     * 
     * @param object
     *            The object in which the property exists
     * @param name
     *            The name of the property
     */
    public final native void unsetProperty(String name)
    /*-{
    	delete this[name];
    }-*/;

}
