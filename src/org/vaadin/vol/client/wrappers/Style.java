package org.vaadin.vol.client.wrappers;

import com.google.gwt.core.client.JavaScriptObject;

public class Style extends JsObject {

    protected Style() {
    };

    public static native Style create()
    /*-{
         var _style = $wnd.OpenLayers.Util.extend({}, $wnd.OpenLayers.Feature.Vector.style['default']);
         return  _style
    }-*/;

    public static native Style create(String string)
    /*-{
         var _style = new $wnd.OpenLayers.Style(string);
         return  _style
    }-*/;

    public static native Style create(JavaScriptObject symbolizer)
    /*-{
        if(symbolizer['__VOL_INHERIT']) {
            var parent = $wnd.OpenLayers.Feature.Vector.style[symbolizer['__VOL_INHERIT']];
            delete symbolizer['__VOL_INHERIT'];
            $wnd.OpenLayers.Util.applyDefaults(symbolizer, parent);
        }
        var options;
        if(symbolizer['__VOL_CONTEXT']) {
            var contextstr = symbolizer['__VOL_CONTEXT'];
            delete symbolizer['__VOL_CONTEXT'];
            context = $wnd.eval('(' + contextstr + ')');
            options = {};
            options.context = context;
        }
         var _style = new $wnd.OpenLayers.Style(symbolizer,options);
         return  _style
    }-*/;

    public native final String getId()
    /*-{
    	return this.id;
    }-*/;

    public native final String getName()
    /*-{
    	return this.name;
    }-*/;

    public native final void setFillOpacity(double o)
    /*-{
    	this.fillOpacity = o;
    }-*/;

    public native final double getFillOpacity()
    /*-{
    	return this.fillOpacity;
    }-*/;

    public native final void setPointRadius(double o)
    /*-{
    	this.pointRadius = o;
    }-*/;

    public native final double getPointRadiusAsDouble()
    /*-{
    	return this.pointRadius;
    }-*/;

    public native final void setPointRadius(String o)
    /*-{
    	this.pointRadius = o;
    }-*/;

    public native final String getPointRadiusAsString()
    /*-{
    	return this.pointRadius;
    }-*/;

    public native final void setStrokeColor(String s)
    /*-{
    	this.strokeColor = s;
    }-*/;

    public native final String getStrokeColor()
    /*-{
    	return this.strokeColor;
    }-*/;

    public native final void setStrokeWidth(double w)
    /*-{
    	this.strokeWidth = w;
    }-*/;

    public native final double getStrokeWidth()
    /*-{
    	return this.strokeWidth;
    }-*/;

    public native final void setExternalGraphic(String graphicURL)
    /*-{
    	this.externalGraphic = graphicURL;
    }-*/;

    public native final String getExternalGraphic()
    /*-{
    	return this.externalGraphic;
    }-*/;

    public native final void setGraphicSize(int width, int height)
    /*-{
    	this.graphicWidth = width;
    	this.graphicHeight = height;
    }-*/;

    public native final void setGraphicWidth(int width)
    /*-{
    	this.graphicWidth = width;
    }-*/;

    public native final void setGraphicHeight(int height)
    /*-{
    	this.graphicHeight = height;
    }-*/;

    public native final int getGraphicWidth()
    /*-{
    	return this.graphicWidth;
    }-*/;

    public native final int getGraphicHeight()
    /*-{
    	return this.graphicHeight;
    }-*/;

    public native final void setGraphicOffset(int xOffset, int yOffset)
    /*-{
    	this.graphicXOffset = xOffset;
    	this.graphicYOffset = yOffset;
    }-*/;

    public native final void setBackgroundGraphic(String backgroundGraphic)
    /*-{
    	this.backgroundGraphic = backgroundGraphic;
    }-*/;

    public native final String getBackgroundGraphic()
    /*-{
    	return this.backgroundGraphic;
    }-*/;

    public native final void setGraphicZIndex(int graphicZIndex)
    /*-{
    	this.graphicZIndex = graphicZIndex;
    }-*/;

    public native final int getGraphicZIndex()
    /*-{
    	return this.graphicZIndex;
    }-*/;

    public native final void setBackgroundGraphicZIndex(
            int backgroundGraphicZIndex)
    /*-{
    	this.backgroundGraphicZIndex = backgroundGraphicZIndex;
    }-*/;

    public native final int getBackgroundGraphicZIndex()
    /*-{
    	return this.backgroundGraphicZIndex;
    }-*/;

    public native final void setBackgroundOffset(int backgroundXOffset,
            int backgroundYOffset)
    /*-{
    	this.backgroundXOffset = backgroundXOffset;
    	this.backgroundYOffset = backgroundYOffset;
    }-*/;

    public native final void setBackgroundXOffset(int backgroundXOffset)
    /*-{
    	this.backgroundXOffset = backgroundXOffset;
    }-*/;

    public native final void setBackgroundYOffset(int backgroundYOffset)
    /*-{
    	this.backgroundYOffset = backgroundYOffset;
    }-*/;

    public native final void setBackgroundWidth(int backgroundWidth)
    /*-{
    	this.backgroundWidth = backgroundWidth;
    }-*/;

    public native final int getBackgroundWidth()
    /*-{
    	return this.backgroundWidth;
    }-*/;

    public native final void setBackgroundHeight(int backgroundHeight)
    /*-{
    	this.backgroundHeight = backgroundHeight;
    }-*/;

    public native final int getBackgroundHeight()
    /*-{
    	return this.backgroundHeight;
    }-*/;

    public native final void setBackgroundGraphicSize(int backgroundWidth,
            int backgroundHeight)
    /*-{
    	this.backgroundWidth = backgroundWidth;
    	this.backgroundHeight = backgroundHeight;
    }-*/;

    public native final void setBackgroundGraphicWidth(int backgroundWidth)
    /*-{
    	this.backgroundWidth = backgroundWidth;
    }-*/;

    public native final void setBackgroundGraphicHeight(int backgroundHeight)
    /*-{
    	this.backgroundHeight = backgroundHeight;
    }-*/;

    public native final void setLabel(String label)
    /*-{
    	this.label = label;
    }-*/;

    public native final String getLabel()
    /*-{
    	return this.label;
    }-*/;

    public native final void setFontColor(String fontColor)
    /*-{
    	this.fontColor = fontColor;
    }-*/;

    public native final String getFontColor()
    /*-{
    	return this.fontColor;
    }-*/;

    public native final void setFontSize(String fontSize)
    /*-{
    	this.fontSize = fontSize;
    }-*/;

    public native final String getFontSize()
    /*-{
    	return this.fontSize;
    }-*/;

    public native final void setFontFamily(String fontFamily)
    /*-{
    	this.fontFamily = fontFamily;
    }-*/;

    public native final String getFontFamily()
    /*-{
    	return this.fontFamily;
    }-*/;

    public native final void setFontWeight(String fontWeight)
    /*-{
    	this.fontWeight = fontWeight;
    }-*/;

    public native final String getFontWeight()
    /*-{
    	return this.fontWeight;
    }-*/;

    public native final void setLabelAlign(String labelAlign)
    /*-{
    	this.labelAlign = labelAlign;
    }-*/;

    public native final String getLabelAlign()
    /*-{
    	return this.labelAlign;
    }-*/;

    public native final void setStrokeOpacity(double strokeOpacity)
    /*-{
    	this.strokeOpacity = strokeOpacity;
    }-*/;

    public native final double getStrokeOpacity()
    /*-{
    	return this.StrokeOpacity;
    }-*/;

    public native final void setStrokeLinecap(String strokeLinecap)
    /*-{
    this.strokeLinecap=strokeLinecap;
    }-*/;

    public native final String getStrokeLinecap()
    /*-{
    	return this.strokeLinecap;
    }-*/;

    public native final void setStrokeDashstyle(String strokeDashstyle)
    /*-{
    this.strokeDashstyle=strokeDashstyle;
    }-*/;

    public native final String getStrokeDashstyle()
    /*-{
    	return  this.strokeDashstyle;
    }-*/;

    public native final void setFill(boolean fill)
    /*-{
    	this.fill=fill;
    }-*/;

    public native final boolean getFill()
    /*-{
    	return this.fill;
    }-*/;

    public native final void setStroke(boolean stroke)
    /*-{
    	this.stroke=stroke;
    }-*/;

    public native final boolean getStroke()
    /*-{
    	return this.stroke;
    }-*/;

    public native final void setGraphic(boolean graphic)
    /*-{
    	this.graphic=graphic;
    }-*/;

    public native final boolean getGraphic()
    /*-{
    	return this.graphic;
    }-*/;

    public native final void setCursor(String cursor)
    /*-{
    	this.cursor=cursor;
    }-*/;

    public native final String getCursor()
    /*-{
    	return this.cursor;
    }-*/;

    public native final void setGraphicName(String graphicName)
    /*-{
    	this.graphicName=graphicName;
    }-*/;

    public native final String getGraphicName()
    /*-{
    	return this.graphicName;
    }-*/;

    public native final void setGraphicTitle(String graphicTitle)
    /*-{
    	this.graphicTitle=graphicTitle;
    }-*/;

    public native final String getGraphicTitle(JsObject object)
    /*-{
    	this.graphicTitle;
    }-*/;
}
