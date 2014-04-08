package org.vaadin.vol.client.wrappers;

import org.vaadin.vol.client.wrappers.layer.VectorLayer;

import com.google.gwt.core.client.JsArray;
import com.vaadin.terminal.gwt.client.ValueMap;

public abstract class Cluster extends AbstractOpenLayersWrapper {

    protected Cluster() {
    };

    public native final static Cluster create(VectorLayer vectorLayer)
    /*-{
	    var cluster = new $wnd.OpenLayers.Strategy.AnimatedCluster({
			distance: 45, 
			animationMethod: $wnd.OpenLayers.Easing.Expo.easeOut, 
			animationDuration: 10
		});
		cluster.setLayer(vectorLayer);
		return cluster;
	}-*/;

    public native final JsArray<Vector> getFeatures() 
    /*-{
		return this.cluster
    }-*/;


    public native final ValueMap getAttributes() 
    /*-{
        return this.attributes;
    }-*/;
    
    public native final void setAttributes(ValueMap attrs) 
    /*-{
        this.attributes = attrs;
    }-*/;

    public native final void setLayer(VectorLayer layer)
    /*-{
		this.setLayer(layer);
	}-*/;

    public native final void setFeatures(JsArray<Vector> featureArray)
    /*-{
		this.features = featureArray.slice(); 
	}-*/;

    public native final void activate()
    /*-{
		this.activate();
	}-*/;

    public native final void cluster() 
    /*-{
        this.cluster();
    }-*/;

    public native final void deactivate()
    /*-{
		this.deactivate();
	}-*/;
    
}
