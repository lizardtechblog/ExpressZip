package org.vaadin.vol;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;

//	Style Properties
//	
//	The properties that you can use for styling are:
//	 -- fillColor 
//	Default is #ee9900. This is the color used for filling in Polygons. It is also used in the center of marks for points: the interior color of circles or other shapes. It is not used if an externalGraphic is applied to a point.
//	 
//	-- fillOpacity: 
//	Default is 0.4. This is the opacity used for filling in Polygons. It is also used in the center of marks for points: the interior color of circles or other shapes. It is not used if an externalGraphic is applied to a point.
//	 
//	-- strokeColor 
//	Default is #ee9900. This is color of the line on features. On polygons and point marks, it is used as an outline to the feature. On lines, this is the representation of the feature.
//	 
//	-- strokeOpacity 
//	Default is 1 This is opacity of the line on features. On polygons and point marks, it is used as an outline to the feature. On lines, this is the representation of the feature.
//	 
//	-- strokeWidth 
//	Default is 1 This is width of the line on features. On polygons and point marks, it is used as an outline to the feature. On lines, this is the representation of the feature.
//	 
//	-- strokeLinecap 
//	Default is round. Options are butt, round, square. This property is similar to the SVG stroke-linecap property. It determines what the end of lines should look like. See the SVG link for image examples.
//	 
//	-- strokeDashstyle 
//	Default is solid. Options are:
//	 -- dot
//	 -- dash
//	 -- dashdot
//	 -- longdash
//	 -- longdashdot
//	 -- solid
//	 
//	-- pointRadius 
//	Default is 6.
//	 
//	-- pointerEvents: 
//	Default is visiblePainted. Only used by the SVG Renderer. See SVG pointer-events definition for more.
//	 
//	-- cursor 
//	Cursor used when mouse is over the feature. Default is an empty string, which inherits from parent elements.
//	 
//	-- externalGraphic 
//	An external image to be used to represent a point.
//	 
//	-- graphicWidth, graphicHeight 
//	These properties define the height and width of an externalGraphic. This is an alternative to the pointRadius symbolizer property to be used when your graphic has different sizes in the X and Y direction.
//	 
//	-- graphicOpacity 
//	Opacity of an external graphic.
//	 
//	-- graphicXOffset, graphicYOffset 
//	Where the center of an externalGraphic should be.
//	 
//	-- rotation 
//	The rotation angle in degrees clockwise for a point symbolizer.
//	 
//	-- graphicName 
//	Name of a type of symbol to be used for a point mark.
//	 
//	-- display 
//	Can be set to none to hide features from rendering.

public class Style {

    public static final String STROKE_DASHSTYLE_SOLID = "solid";
    public static final String STROKE_DASHSTYLE_DASHDOT = "dashdot";
    public static final String STROKE_DASHSTYLE_DOT = "dot";
    public static final String STROKE_DASHSTYLE_DASH = "dash";
    public static final String STROKE_DASHSTYLE_LONGDASH = "longdash";
    public static final String STROKE_DASHSTYLE_LONGDASHDOT = "longdashdor";

    public static final String STROKE_LINECAP_BUTT = "butt";
    public static final String STROKE_LINECAP_ROUND = "round";
    public static final String STROKE_LINECAP_SQUARE = "square";

    private static long idx = 0;
    private String name;
    private JsObject styleProperty;

    public Style() {
        name = "Style" + String.valueOf(++idx);
        styleProperty = new JsObject();
        init();
    }

    public Style(String string) {
        name = string;
        styleProperty = new JsObject();
        init();
    }

    public String getName() {
        return name;
    }

    public void setProperty(String key, Object value) {
        styleProperty.setProperty(key, value);
    }

    private Object getProperty(String key) {
        return styleProperty.getProperty(key);
    }

    private void setPropertyByAttribute(String key, String value) {
        styleProperty.setProperty(key, "${" + value + "}");
    }

    /**
     * Hex fill color. Default is '#ee9900'.
     * 
     * @param c
     *            - hexidecimal color code or a W3C standard color name
     */
    public void setFillColor(String c) {
        setProperty("fillColor", c);
    }

    public void setFillColorByAttribute(String c) {
        setPropertyByAttribute("fillColor", c);
    }

    /** Hex fill color. */
    public String getFillColor() {
        return (String) getProperty("fillColor");
    }

    /** Fill opacity (0-1). Default is 0.4 */
    public void setFillOpacity(double o) {
        setProperty("fillOpacity", o);
    }

    public void setFillOpacityByAttribute(String o) {
        setPropertyByAttribute("fillOpacity", o);
    }

    /** Fill opacity (0-1). */
    public double getFillOpacity() {
        double o = (Double) getProperty("fillOpacity");
        return o;
    }

    /** Pixel point radius. Default is 6. */
    public void setPointRadius(double r) {
        setProperty("pointRadius", r);
    }

    public void setPointRadiusByAttribute(String r) {
        setPropertyByAttribute("pointRadius", r);
    }

    /** Pixel point radius. */
    public double getPointRadius() {
        return (Double) getProperty("pointRadius");
    }

    /**
     * Hex stroke color. Default is '#ee9900'.
     * 
     * @param c
     *            - see setFillColor
     */
    public void setStrokeColor(String c) {
        setProperty("strokeColor", c);
    }

    public void setStrokeColorByAttribute(String c) {
        setPropertyByAttribute("strokeColor", c);
    }

    /** Hex stroke html color. */
    public String getStrokeColor() {
        return (String) getProperty("strokeColor");
    }

    /** Pixel stroke width. Default is 1. */
    public void setStrokeWidth(double w) {
        setProperty("strokeWidth", w);
    }

    public void setStrokeWidthByAttribute(String w) {
        setPropertyByAttribute("strokeWidth", w);
    }

    /** Pixel stroke width. */
    public double getStrokeWidth() {
        return (Double) getProperty("strokeWidth");
    }

    /** Url to an external graphic that will be used for rendering points. */
    public void setExternalGraphic(String graphicURL) {
        setProperty("externalGraphic", graphicURL);
    }

    public void setExternalGraphicByAttribute(String graphicURL) {
        setPropertyByAttribute("externalGraphic", graphicURL);
    }

    /** Url to an external graphic that will be used for rendering points. */
    public String getExternalGraphic() {
        return (String) getProperty("externalGraphic");
    }

    /**
     * Convenient method to set the pixel width and height for sizing an
     * external graphic.
     * 
     * @param width
     *            The width (in pixels) to set
     * @param height
     *            The height (in pixels) to set
     */
    public void setGraphicSize(int width, int height) {
        setGraphicWidth(width);
        setGraphicHeight(height);
    }

    public void setGraphicWidth(int width) {
        setProperty("graphicWidth", width);
    }

    public void setGraphicWidthByAttribute(String widthAttr) {
        setPropertyByAttribute("graphicWidth", widthAttr);
    }

    public void setGraphicHeight(int height) {
        setProperty("graphicHeight", height);
    }

    public void setGraphicHeightByAttribute(String heightAttr) {
        setPropertyByAttribute("graphicHeight", heightAttr);
    }

    /** Pixel width for sizing an external graphic. */
    public int getGraphicWidth() {
        return (Integer) getProperty("graphicWidth");
    }

    /** Pixel height for sizing an external graphic. */
    public int getGraphicHeight() {
        return (Integer) getProperty("graphicHeight");
    }

    /**
     * Sets the offset for the displacement of the external graphic. The offset
     * is from the top-lef of the image (which is considered the point 0,0).
     * 
     * @param xOffset
     *            Pixel offset along the positive x axis for displacing an
     *            external graphic.
     * @param yOffset
     *            Pixel offset along the positive y axis for displacing an
     *            external graphic.
     */
    public void setGraphicOffset(int xOffset, int yOffset) {
        setGraphicXOffset(xOffset);
        setGraphicYOffset(yOffset);
    }

    public void setGraphicOffsetByAttribute(String xOffsetAttr,
            String yOffsetAttr) {
        setGraphicXOffsetByAttribute(xOffsetAttr);
        setGraphicYOffsetByAttribute(yOffsetAttr);
    }

    public void setGraphicXOffset(int xOffset) {
        setProperty("graphicXOffset", xOffset);
    }

    public void setGraphicXOffsetByAttribute(String xOffsetAttr) {
        setPropertyByAttribute("graphicXOffset", xOffsetAttr);
    }

    public void setGraphicYOffset(int yOffset) {
        setProperty("graphicYOffset", yOffset);
    }

    public void setGraphicYOffsetByAttribute(String yOffsetAttr) {
        setPropertyByAttribute("graphicYOffset", yOffsetAttr);
    }

    /**
     * Sets the size of the background graphic. If none of the dimensions are
     * set, the external graphic size is used.
     * 
     * @param backgroundWidth
     *            The width of the background width.
     * @param backgroundHeight
     *            The height of the background graphic.
     */
    public void setBackgroundGraphicSize(int backgroundWidth,
            int backgroundHeight) {
        setBackgroundWidth(backgroundWidth);
        setBackgroundHeight(backgroundHeight);
    }

    /**
     * The height of the background graphic. If not provided, the graphicHeight
     * will be used.
     */
    public void setBackgroundHeight(int backgroundHeight) {
        setProperty("backgroundHeight", backgroundHeight);
    }

    /** The height of the background graphic. */
    public int getBackgroundHeight() {
        return (Integer) getProperty("backgroundHeight");
    }

    /**
     * The width of the background width. If not provided, the graphicWidth will
     * be used.
     */
    public void setBackgroundWidth(int backgroundWidth) {
        setProperty("backgroundWidth", backgroundWidth);
    }

    /** The width of the background width. */
    public int getBackgroundWidth() {
        return (Integer) getProperty("backgroundWidth");
    }

    /** Url to a graphic to be used as the background under an externalGraphic. */
    public void setBackgroundGraphic(String graphicURL) {
        setProperty("backgroundGraphic", graphicURL);
    }

    /** Url to a graphic to be used as the background under an externalGraphic. */
    public String getBackgroundGraphic() {
        return (String) getProperty("backgroundGraphic");
    }

    /**
     * Sets the offset for the displacement of the background graphic. The
     * offset is from the top-left of the image (which is considered the point
     * 0,0).
     * 
     * @param backgroundXOffset
     *            Pixel offset along the positive x axis for displacing an
     *            background graphic.
     * @param backgroundYOffset
     *            Pixel offset along the positive y axis for displacing an
     *            background graphic.
     */
    public void setBackgroundOffset(int backgroundXOffset, int backgroundYOffset) {
        setBackgroundXOffset(backgroundXOffset);
        setBackgroundYOffset(backgroundYOffset);
    }

    public void setBackgroundXOffset(int backgroundXOffset) {
        setProperty("backgroundXOffset", backgroundXOffset);
    }

    public void setBackgroundYOffset(int backgroundYOffset) {
        setProperty("backgroundYOffset", backgroundYOffset);
    }

    /** The integer z-index value to use in rendering. */
    public void setGraphicZIndex(int graphicZIndex) {
        setProperty("graphicZIndex", graphicZIndex);
    }

    /** The integer z-index value to use in rendering. */
    public int getGraphicZIndex() {
        return (Integer) getProperty("graphicZIndex");
    }

    /**
     * The integer z-index value to use in rendering the background graphic.
     * Usually is a number smaller then the GraphicZIndex, so the background can
     * be behind the feature graphic.
     */
    public void setBackgroundGraphicZIndex(int backgroundGraphicZIndex) {
        setProperty("backgroundGraphicZIndex", backgroundGraphicZIndex);
    }

    /** The integer z-index value to use in rendering the background graphic. */
    public int getBackgroundGraphicZIndex() {
        return (Integer) getProperty("backgroundGraphicZIndex");
    }

    /** Stroke opacity (0-1). Default is 1. */
    public void setStrokeOpacity(double strokeOpacity) {
        setProperty("strokeOpacity", strokeOpacity);
    }

    public double getStrokeOpacity() {
        return (Double) getProperty("strokeOpacity");
    }

    /**
     * The text for an optional label. For browsers that use the canvas
     * renderer, this requires either fillText or mozDrawText to be available.
     * <p>
     * Note: you can set a custom label for each feature added to a layer by
     * using tags in the label, and setting attributes using
     * {@link org.gwtopenmaps.openlayers.client.util.Attributes}. For example,
     * set the style.label to "${customLabel}", then, for each feature added to
     * the layer, add an "customLabel" attribute with
     * <p>
     * <code>attributes.setAttribute("customLabel","myLabel for this specific feature")</code>
     * <p>
     * Note: this can also be used in any style field of type String, such as
     * fillColor, fontColor, etc
     * */
    public void setLabel(String label) {
        setProperty("label", label);
    }

    public void setLabelByAttribute(String label) {
        setPropertyByAttribute("label", label);
    }

    /** The font color for the label, to be provided like CSS. */
    public void setFontColor(String fontColor) {
        setProperty("fontColor", fontColor);
    }

    /** The font size for the label, to be provided like in CSS. */
    public void setFontSize(String fontSize) {
        setProperty("fontSize", fontSize);
    }

    /** The font family for the label, to be provided like in CSS. */
    public void setFontFamily(String fontFamily) {
        setProperty("fontFamily", fontFamily);
    }

    /** The font weight for the label, to be provided like in CSS. */
    public void setFontWeight(String fontWeight) {
        setProperty("fontWeight", fontWeight);
    }

    /**
     * Sets the Label alignment string directly. This specifies the insertion
     * point relative to the text. It is a string composed of two characters.
     * <p>
     * The first character is for the horizontal alignment, the second for the
     * vertical alignment.
     * <p>
     * Valid values for horizontal alignment: 'l'=left, 'c'=center, 'r'=right.
     * Valid values for vertical alignment: 't'=top, 'm'=middle, 'b'=bottom.
     * Example values: 'lt', 'cm', 'rb'. The canvas renderer does not support
     * vertical alignment, it will always use 'b'.
     */
    public void setLabelAlign(String align) {
        setProperty("labelAlign", align);
    }

    /**
     * Vertical Label alignment. This specifies the insertion point relative to
     * the text.
     */
    public String getLabelAlign() {
        return (String) getProperty("labelAlign");
    }

    public void setLabelXOffset(int offset) {
        setProperty("labelXOffset", offset);
    }

    public Integer getLabelXOffset() {
        return (Integer) getProperty("labelXOffset");
    }

    public void setLabelYOffset(int offset) {
        setProperty("labelYOffset", offset);
    }

    public Integer getLabelYOffset() {
        return (Integer) getProperty("labelYOffset");
    }

    public void setLabelOutlineColor(String color) {
        setProperty("labelOutlineColor", color);
    }

    public String getLabelOutlineColor() {
        return (String) getProperty("labelOutlineColor");
    }
    
    public void setLabelOutlineWidth(int width) {
        setProperty("labelOutlineWidth", width);
    }
    
    public Integer getLabelOutlineWidth(){
        return (Integer) getProperty("labelOutlineWidth");
    }

    /** Stroke linecap. */
    public String getStrokeLinecap() {
        return (String) getProperty("strokeLinecap");
    }

    /**
     * Directly sets the StrokeLineCap string. Default is 'round'. [butt | round
     * | square]
     */
    public void setStrokeLinecap(String strokeLinecap) {
        setProperty("strokeLinecap", strokeLinecap);
    }

    /**
     * Directly sets the stroke dash style string. Default is Default is
     * 'solid'. [dot | dash | dashdot | longdash | longdashdot | solid]
     */
    public void setStrokeDashstyle(String strokeDashstyle) {
        setProperty("strokeDashstyle", strokeDashstyle);
    }

    /**
     * Stroke dash style.
     */
    public String getStrokeDashstyle() {
        return (String) getProperty("strokeDashstyle");
    }

    /** Set to false if no fill is desired. */
    public void setFill(boolean fill) {
        setProperty("fill", fill);
    }

    /** Set to false if no fill is desired. */
    public boolean getFill() {
        return (Boolean) getProperty("fill");
    }

    /** Set to false if no stroke is desired. */
    public void setStroke(boolean stroke) {
        setProperty("stroke", stroke);
    }

    /** Set to false if no stroke is desired. */
    public boolean getStroke() {
        return (Boolean) getProperty("stroke");
    }

    /** Set to false if no graphic is desired. */
    public void setGraphic(boolean graphic) {
        setProperty("graphic", graphic);
    }

    /** Set to false if no graphic is desired. */
    public boolean getGraphic() {
        return (Boolean) getProperty("graphic");
    }

    /** Cursor. Default is ''. */
    public void setCursor(String cursor) {
        setProperty("cursor", cursor);
    }

    /** Cursor. */
    public String getCursor() {
        return (String) getProperty("cursor");
    }

    /**
     * Directly sets the named graphic to use when rendering points. Default is
     * 'circle'.
     * <p>
     * Supported values include 'circle' (default), 'square', 'star', 'x',
     * 'cross', 'triangle'.
     */
    public void setGraphicName(String graphicName) {
        setProperty("graphicName", graphicName);
    }

    /**
     * Named graphic to use when rendering points. Supported values include
     * 'circle' (default), 'square', 'star', 'x', 'cross', 'triangle'.
     */
    public String getGraphicName() {
        return (String) getProperty("graphicName");
    }

    /**
     * Tooltip for an external graphic. Only supported in Firefox and Internet
     * Explorer.
     */
    public void setGraphicTitle(String graphicTitle) {
        setProperty("graphicTitle", graphicTitle);
    }

    /**
     * Tooltip for an external graphic. Only supported in Firefox and Internet
     * Explorer.
     */
    public String getGraphicTitle() {
        return (String) getProperty("graphicTitle");
    }

    public void paint(String string, PaintTarget target) throws PaintException {
        target.addAttribute(string, styleProperty.getKeyValueMap());
    }

    /**
     * @param coreStyleName
     *            the core style name this Style should extend. E.g. 'default'
     *            or 'selected'
     */
    public void extendCoreStyle(String coreStyleName) {
        setProperty("__VOL_INHERIT", coreStyleName);
    }

    private void init() {
        extendCoreStyle("default");
    }

    public void setContextJs(String js) {
        setProperty("__VOL_CONTEXT", js);
    }

}
