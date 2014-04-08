package org.vaadin.vol.client.wrappers.layer;

import org.vaadin.vol.client.wrappers.Cluster;
import org.vaadin.vol.client.wrappers.GwtOlHandler;
import org.vaadin.vol.client.wrappers.StyleMap;
import org.vaadin.vol.client.wrappers.Vector;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class VectorLayer extends Layer {

    protected VectorLayer() {
    };

    /**
     * TODO name and styles
     * 
     * @return
     * 
     */
    public native final static VectorLayer create(String displayName, String projection)
    /*-{
        var layer_style = $wnd.OpenLayers.Util.extend({}, $wnd.OpenLayers.Feature.Vector.style['default']);
        layer_style.fillOpacity = 0.2;
        layer_style.graphicOpacity = 1;

        var options = {};
        options.styles = layer_style;
        if(projection) options.projection = projection;

        return new $wnd.OpenLayers.Layer.Vector(displayName, options);
    }-*/;

    public native final static VectorLayer create(String displayName, String projection,
            JavaScriptObject stylemap)
    /*-{
    	_myvector_layer = new $wnd.OpenLayers.Layer.Vector(displayName, projection);
    	if(stylemap) 
    		_myvector_layer.styleMap = stylemap;
    	return _myvector_layer;
    	
    }-*/;

    public native final void removeFeature(Vector vector)
    /*-{
    	this.removeFeatures(vector);
    }-*/;
    
    public native final void eraseFeature(Vector vector)
    /*-{
    	this.eraseFeatures(vector);
    }-*/;

    public native final void addFeature(Vector vector)
    /*-{
    	this.addFeatures(vector);
    }-*/;

/**
 * Add array of vectors and turn on clustering for the layer
 * @param vectors
 */
    public native final void addFeatures(JsArray<Vector> vectors)
    /*-{
		this.addFeatures(vectors);
    }-*/;

    public native final void drawFeature(Vector vector)
    /*-{
    	this.drawFeature(vector);
    }-*/;
    
    public native final void redraw()
    /*-{
        this.redraw();
    }-*/;

    public native final void setStyleMap(StyleMap style)
    /*-{
    	 this.styleMap = style;
    }-*/;

    public native final StyleMap getStyleMap(StyleMap style)
    /*-{
    	 return this.styleMap;
    }-*/;

    public native final void removeAllFeatures() 
    /*-{
        this.removeAllFeatures();
    }-*/;

    public native final void addClustering(Cluster cluster) 
    /*-{
		    // Define three colors that will be used to style the cluster features
            // depending on the number of features they contain.
            var colors = {
                one: "rgb(138, 200, 64)",
                low: "rgb(212, 191, 242)", 
                middle: "rgb(137, 91, 196)", 
                high: "rgb(118, 64, 187)"
            };
            
            // Define three rules to style the cluster features.
            var oneRule = new $wnd.OpenLayers.Rule({
                filter: new $wnd.OpenLayers.Filter.Comparison({
                    type: $wnd.OpenLayers.Filter.Comparison.LESS_THAN,
                    property: "count",
                    value: 2
                }),
                symbolizer: {
                    fillColor: colors.one,
                    fillOpacity: 0.9, 
                    strokeColor: colors.one,
                    strokeOpacity: 0.5,
                    strokeWidth: 12,
                    pointRadius: 7,
                }
            });
            var lowRule = new $wnd.OpenLayers.Rule({
                filter: new $wnd.OpenLayers.Filter.Comparison({
                    type: $wnd.OpenLayers.Filter.Comparison.BETWEEN,
                    property: "count",
                    lowerBoundary: 2,
                    upperBoundary: 50
                }),
                symbolizer: {
                    fillColor: colors.low,
                    fillOpacity: 0.9, 
                    strokeColor: colors.low,
                    strokeOpacity: 0.5,
                    strokeWidth: 12,
                    pointRadius: 10,
                    label: "${count}",
                    labelOutlineWidth: 1,
                    fontColor: "#ffffff",
                    fontOpacity: 0.8,
                    fontSize: "12px"
                }
            });
            var middleRule = new $wnd.OpenLayers.Rule({
                filter: new $wnd.OpenLayers.Filter.Comparison({
                    type: $wnd.OpenLayers.Filter.Comparison.BETWEEN,
                    property: "count",
                    lowerBoundary: 15,
                    upperBoundary: 50
                }),
                symbolizer: {
                    fillColor: colors.middle,
                    fillOpacity: 0.9, 
                    strokeColor: colors.middle,
                    strokeOpacity: 0.5,
                    strokeWidth: 12,
                    pointRadius: 15,
                    label: "${count}",
                    labelOutlineWidth: 1,
                    fontColor: "#ffffff",
                    fontOpacity: 0.8,
                    fontSize: "12px"
                }
            });
            var highRule = new $wnd.OpenLayers.Rule({
                filter: new $wnd.OpenLayers.Filter.Comparison({
                    type: $wnd.OpenLayers.Filter.Comparison.GREATER_THAN,
                    property: "count",
                    value: 50
                }),
                symbolizer: {
                    fillColor: colors.high,
                    fillOpacity: 0.9, 
                    strokeColor: colors.high,
                    strokeOpacity: 0.5,
                    strokeWidth: 12,
                    pointRadius: 20,
                    label: "${count}",
                    labelOutlineWidth: 1,
                    fontColor: "#ffffff",
                    fontOpacity: 0.8,
                    fontSize: "12px"
                }
            });
            
            // Create a Style that uses the three previous rules
            var style = new $wnd.OpenLayers.Style(null, {
                rules: [oneRule, lowRule, middleRule, highRule]
            });            

		this.addOptions({ 'renderers': ['Canvas','SVG']});
		this.addOptions({ 'strategies': [cluster] });
		this.addOptions({ 'styleMap': new $wnd.OpenLayers.StyleMap(style) });
	}-*/;

    public native final JsArray<Vector> getClusters() 
    /*-{
	    return this.strategies[0].clusters
    }-*/;

    public native final Cluster getCluster() 
    /*-{
	    return this.strategies[0];
    }-*/;

    /**
     * it's maybe useful for blocking beforefeatureselected events
     * @param eventName
     * @param handler
     */
	public native final void registerReturnFalseHandler(String eventName, GwtOlHandler handler) 
	/*-{
		var f = function() {
			$entry(handler.@org.vaadin.vol.client.wrappers.GwtOlHandler::onEvent(Lcom/google/gwt/core/client/JsArray;)(arguments));
			return false;
		};
		this.events.addEventType(eventName);
		this.events.register(eventName,this,f);
		
	}-*/;
	
	/**
	 * set to restrict content for the layer
	 * @param filterType kind of filter (==,!=,<,<=,>,>=,..,~)
	 * @param filterProp filtered property
	 * @param filterValue value for filter
	 */
	public native final void setFilter(String filterType,String filterProp,
			String filterValue)
	/*-{
	 if (filterValue) {
		 this.filter=new $wnd.OpenLayers.Filter.Comparison({
	                            type: filterType,
	                            property: filterProp,
	                            value: filterValue
	                        });
     }
     else
     	 this.filter=null;
	}-*/;

	public native final void refresh()
	/*-{
 		this.refresh({force: true});
	}-*/;

}
