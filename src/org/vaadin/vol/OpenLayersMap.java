package org.vaadin.vol;

import com.vaadin.event.Action;
import com.vaadin.terminal.KeyMapper;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.tools.ReflectTools;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Component;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Server side component for the VOpenLayersMap widget.
 */

@SuppressWarnings("serial")
@com.vaadin.ui.ClientWidget(org.vaadin.vol.client.ui.VOpenLayersMap.class)
public class OpenLayersMap extends AbstractComponentContainer implements
        Action.Container {

    private final Set<Action.Handler> actionHandlers = new LinkedHashSet<Action.Handler>();
    private final KeyMapper actionMapper = new KeyMapper();
    private List<Component> layers = new LinkedList<Component>();
    private double centerLon = 0;
    private double centerLat = 0;
    private int zoom = 3;
    private boolean partialRepaint;
    private boolean activateExtent = false;
    
    private Layer baseLayer;
    private VectorLayer extentLayer;
    
    private HashSet<Control> controls = new HashSet<Control>(Arrays.asList(
            Control.ArgParser, Control.Navigation, Control.TouchNavigation,
            Control.Attribution));

    public OpenLayersMap() {
        this(false);
    }

    public OpenLayersMap(boolean skipControls) {
        setWidth("500px");
        setHeight("350px");
        if (!skipControls) {
            addControl(Control.PanZoom);
            addControl(Control.LayerSwitcher);
        }
    }
    public void addExtentPanel(VectorLayer extentLayer){
       setExtentLayer(extentLayer);
    }
    public void removeExtentPanel(){
       unsetExtentLayer();
    }

    public void addControl(Control control) {
        controls.add(control);
        setDirty("controls");
    }

    public void removeControl(Control control) {
        controls.remove(control);
        setDirty("controls");
    }

    /**
     * @return set of current controls used by this map. Note that returns
     *         reference to internal data structure. If you modify the set
     *         directly, call requestRepaint for the map to force repaint.
     */
    public Set<Control> getControls() {
        return controls;
    }

    /**
     * A typed alias for {@link #addComponent(Component)}.
     *
     * @param layer
     */
    public void addLayer(Layer layer) {
        addComponent(layer);
    }

    @Override
    public void removeComponent(Component c) {
        super.removeComponent(c);
        layers.remove(c);
        setDirty("components");
    }

    /**
     * Adds component into the OpenLayers Map. Note that the map only supports
     * certain types of Components.
     * <p>
     * Developers are encouraged to use better typed methods instead:
     *
     * @see #addLayer(Layer)
     * @see #addPopup(Popup)
     *
     * @see com.vaadin.ui.AbstractComponentContainer#addComponent(com.vaadin.ui.Component)
     */
    @Override
    public void addComponent(Component c) {
        setDirty("components");
        super.addComponent(c);
        layers.remove(c);
        layers.add(c);
    }

    
    /**
     * Change the base layer of the map.
     * <p>
     * Note that once the change has been made client-side, a BaseLayerChangeEvent will be fired server-side, and the name of
     * the new base layer will be available from getBaseLayerName().
     * @param newBaseLayer      the layer that will be the new base layer
     * @throws IllegalArgumentException If the layer is not already associated with the map and/or the layer is not a base layer.                  
     */
    public void setBaseLayer(Layer newBaseLayer) {
        // Need to add a check to make sure the newBaseLayer is in fact a base layer....API needs more work for that
        if (newBaseLayer != null && layers.contains(newBaseLayer)) {
            //System.out.println("...not eq, setting");
            baseLayer = newBaseLayer;
            setDirty("baseLayer");
       } else {
            throw new IllegalArgumentException("Only existing map layers can become the base layer");               
       }
    }
    public void setExtentLayer(VectorLayer extentLayer){
       this.extentLayer = extentLayer;
       setDirty("extentLayer");
    }
    public void unsetExtentLayer(){
       this.extentLayer = null;
       setDirty("extentLayer");
    }
    
    public void activateExtentTransform(){
       activateExtent = true;
       setDirty("activateExtent");
    } 
    /**
     * Get the name (display name) of the current base layer
     * 
     * @return Current base layer name.  
     */
    public Layer getBaseLayer() {
        return baseLayer;
    }
    public VectorLayer getExtentLayer(){
       return extentLayer;
    }
    
    public void setCenter(double lon, double lat) {  
        centerLat = lat;
        centerLon = lon;
        setDirty("clat");
    }

    /**
     * Set the center of map to the center of a bounds
     *
     */
    public void setCenter(Bounds bounds) {
        centerLat = (bounds.getBottom() + bounds.getTop()) / 2.0;
        centerLon = (bounds.getRight() + bounds.getLeft()) / 2.0;
        setDirty("clat");
    }

    public void setZoom(int zoomLevel) {
        zoom = zoomLevel;
        setDirty("zoom");
    }

    public int getZoom() {
        return zoom;
    }

    private HashSet<String> dirtyFields = new HashSet<String>();
    private boolean fullRepaint = true;
    private double top;
    private double right;
    private double bottom;
    private double left;

    private String jsMapOptions;
    private Bounds zoomToExtent;
    private Bounds restrictedExtend;
    private String projection;

    /**
     * Sets one or more fields as 'dirty' in order to do a partial repaint of just those fields
     * @param fieldNames String var-args array of field names to mark as 'dirty'
     */
    protected void setDirty(String... fieldNames) {
        if (!fullRepaint && fieldNames != null && fieldNames.length > 0) {
            boolean needsParitialPaint = false;
            for (String fieldName : fieldNames) {
                if (fieldName != null) {
                    dirtyFields.add(fieldName);
                    needsParitialPaint = true;
                }
            }
            if (needsParitialPaint)
                partialPaint();
        }
    }

    /**
     * Determines whether or not a field has been marked as 'dirty'.  Used during {@see #paintContent} to determine if
     * a field should be written to the {@see PaintTarget}.
     * @param fieldName name of field
     * @return true if field is 'dirty, false otherwise
     */
    protected boolean isDirty(String fieldName) {
        /*
         * If full repaint if request repaint called directly or painted without
         * repaint.
         */
        if (fullRepaint || dirtyFields.isEmpty()) {
            return true;
        } else {
            return dirtyFields.contains(fieldName);
        }
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        
        if (isDirty("projection") && projection != null) {
            target.addAttribute("projection", projection);
        }
        if (isDirty("jsMapOptions") && jsMapOptions != null) {
            target.addAttribute("jsMapOptions", jsMapOptions);
        }

        if (isDirty("restrictedExtend") && restrictedExtend != null) {
            restrictedExtend.paint("re", target);
        }

        if (isDirty("zoomToExtent") && zoomToExtent != null) {
            zoomToExtent.paint("ze", target);
            zoomToExtent = null;
        } else {
            if (isDirty("clat")) {
                target.addAttribute("clon", centerLon);
                target.addAttribute("clat", centerLat);
            }
            if (isDirty("zoom")) {
                target.addAttribute("zoom", zoom);
            }
        }
        if (isDirty("components")) {
            target.addAttribute("componentsPainted", true);
            for (Component component : layers) {
                component.paint(target);
            }
        }
        if (isDirty("controls")) {
            target.addAttribute("controls", controls.toArray());
        }
        
        if (isDirty("baseLayer") && baseLayer != null) {
            target.addAttribute("baseLayer", baseLayer);
        }
        
        if (isDirty("extentLayer")){
           if(extentLayer != null)
              target.addAttribute("extentLayer", extentLayer);
           else
              target.addAttribute("removeExtentLayer", true);
        }
        
        if( isDirty("activateExtent") && activateExtent) {
            target.addAttribute("activateExtentTransform", true);
            activateExtent = false;
         }
        
        paintActions(target, findAndPaintBodyActions(target));

        clearPartialPaintFlags();
        fullRepaint = false;
    }
    
    /**
     * Receive and handle events and other variable changes from the client.
     *
     * {@inheritDoc}
     */
    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        if (variables.containsKey("top")) {
            updateExtent(variables);
            fireEvent(new ExtentChangeEvent());
        }

        if (variables.containsKey("baseLayer")) {
        	
            updateBaseLayer((Layer) variables.get("baseLayer"));
        }
        if( variables.containsKey("extentLayer")){
           updateExtentLayer( (VectorLayer) variables.get("extentLayer"));
        }
        
        // Actions
        if (variables.containsKey("action")) {
            String string = (String) variables.get("action");
            final StringTokenizer st = new StringTokenizer(string, ",");
            if (st.countTokens() == 2) {
                final String coords = st.nextToken();
                String[] split = coords.split(":");
                Point point = new Point(Double.parseDouble(split[0]),
                        Double.parseDouble(split[1]));
                final Action action = (Action) actionMapper.get(st.nextToken());

                if (action != null && actionHandlers != null) {
                    for (Action.Handler ah : actionHandlers) {
                        ah.handleAction(action, this, point);
                    }
                }
            }
        }
        if (variables.containsKey("mousemove")) {
           double lon = (Double) variables.get("lon");
           double lat = (Double) variables.get("lat");
           MouseMoveInfo info = new MouseMoveInfo();
           info.setLon(lon);
           info.setLat(lat);
           mouseMoved(info);
       }

        if (variables.containsKey("clicked")) {
            double lon = (Double) variables.get("lon");
            double lat = (Double) variables.get("lat");
            int x = (Integer) variables.get("x");
            int y = (Integer) variables.get("y");
            int width = (Integer) variables.get("width");
            int height = (Integer) variables.get("height");
            PointInformation pointInformation = new PointInformation();
            pointInformation.setLon(lon);
            pointInformation.setLat(lat);
            pointInformation.setX(x);
            pointInformation.setY(y);
            pointInformation.setWidth(width);
            pointInformation.setHeight(height);
            pointInformation.setBounds(getExtend());
            mapClicked(pointInformation);
        }
    }

    protected void updateBaseLayer(Layer baseLayer) {
        this.baseLayer = baseLayer;
        fireEvent(new BaseLayerChangeEvent());
    }
    protected void updateExtentLayer(VectorLayer extentLayer){
        this.extentLayer = extentLayer;
    }
    
    protected void updateExtent(Map<String, Object> variables) {
        int zoom = (Integer) variables.get("zoom");
        this.zoom = zoom;
        top = (Double) variables.get("top");
        right = (Double) variables.get("right");
        bottom = (Double) variables.get("bottom");
        left = (Double) variables.get("left");
		centerLat = (top + bottom) / 2.0d;
		centerLon = (right + left) / 2.0d;
    }

    /**
     * Note, this does not work until the map is rendered.
     *
     * @return
     */
    public Bounds getExtend() {
        Bounds bounds = new Bounds();
        bounds.setTop(top);
        bounds.setLeft(left);
        bounds.setRight(right);
        bounds.setBottom(bottom);
        return bounds;
    }

    public void replaceComponent(Component oldComponent, Component newComponent) {
        throw new UnsupportedOperationException();
    }

    public Iterator<Component> getComponentIterator() {
        return new LinkedList<Component>(layers).iterator();
    }

    public void addPopup(Popup popup) {
        addComponent(popup);
    }

    @Override
    public void requestRepaint() {
        if (!partialRepaint) {
            clearPartialPaintFlags();
            fullRepaint = true;
        }
        super.requestRepaint();
    }

    private void partialPaint() {
        partialRepaint = true;
        try {
            requestRepaint();
        } finally {
            partialRepaint = false;
        }
    }

    private void clearPartialPaintFlags() {
        dirtyFields.clear();
    }

    /**
     * Sets the js snippet that will be evaluated as maps custom options. See
     * OpenLayers JS api for more details.
     * <p>
     * Note, that the string will be executed as javascript on the client side.
     * VALIDATE content in case you accept input from the client.
     * <p>
     * Also note that init options only take effect if they are set before the
     * map gets rendered.
     *
     * @param jsMapOptions
     */
    public void setJsMapOptions(String jsMapOptions) {
        this.jsMapOptions = jsMapOptions;
    }

    public String getJsMapOptions() {
        return jsMapOptions;
    }

    /**
     * Zooms the map to display given bounds.
     *
     * <p>
     * Note that this method overrides possibly set center and zoom levels.
     *
     * @param bounds
     */
    public void zoomToExtent(Bounds bounds) {
        if(bounds != null) {
	    	zoomToExtent = bounds;
	        // also save center point for refreshes
	        centerLon = (zoomToExtent.getMinLon() + zoomToExtent.getMaxLon()) / 2.0d;
	        centerLat = (zoomToExtent.getMinLat() + zoomToExtent.getMaxLat()) / 2.0d;
	        setDirty("zoomToExtent");
        }
    }

    /**
     * Sets the area within the panning and zooming is restricted. With this
     * method developer can "limit" the area that is shown for the end user.
     * <p>
     * Note, that due the fact that open layers supports just zoom levels, the
     * displayed area might be slightly larger if the size of the component
     * don't match with the size of restricted area on minimum zoom level. If
     * area outside restricted extent may not be displayed at all, one must
     * ensure about this by either using a base layer that only contains the
     * desired area or by "masking" out the undesired area with e.g. a vector
     * layer.
     *
     * @param bounds
     */
    public void setRestrictedExtent(Bounds bounds) {
        restrictedExtend = bounds;
        setDirty("restrictedExtend");
    }

    /**
     * Sets the projection which is used by the user of this map. E.g. values
     * passed to API like {@link #setCenter(double, double)} should be in the
     * same projection.
     * <p>
     * Note that resetting projection on already rendered map may cause
     * unexpected results.
     *
     * @param projection
     */
    public void setApiProjection(String projection) {
        this.projection = projection;
        setDirty("projection");
    }

    /**
     * Gets the projection which is used by the user of this map. E.g. values
     * passed to API like {@link #setCenter(double, double)} should be in the
     * same projection.
     *
     * @return the projection used, defaults to EPSG:4326
     */
    public String getApiProjection() {
        return projection;
    }

    /**
     * Calculates an array of resolutions for use in OpenLayers map creation
     */
    protected double[] calculateResolutions(Bounds bounds, int tileSize,
            int zoomLevels) {

        final double extentWidth = bounds.getRight() - bounds.getLeft();
        final double extentHeight = bounds.getTop() - bounds.getBottom();

        double resX = extentWidth / tileSize;
        double resY = extentHeight / tileSize;

        if (resX <= resY) {
            // use one tile wide by N tiles high
            int tilesHigh = (int) Math.round(resY / resX);
            // previous resY was assuming 1 tile high, recompute with the actual
            // number of tiles
            // high
            resY = resY / tilesHigh;
        } else {
            // use one tile high by N tiles wide
            int tilesWide = (int) Math.round(resX / resY);
            // previous resX was assuming 1 tile wide, recompute with the actual
            // number of tiles
            // wide
            resX = resX / tilesWide;
        }

        // the maximum of resX and resY is the one that adjusts better
        final double res = Math.max(resX, resY);

        double[] resolutions = new double[zoomLevels];
        resolutions[0] = res;

        for (int i = 1; i < zoomLevels; i++) {
            resolutions[i] = resolutions[i - 1] / 2;
        }

        return resolutions;
    }

    private void paintActions(PaintTarget target, final Set<Action> actionSet)
            throws PaintException {
        if (!actionSet.isEmpty()) {
            target.addVariable(this, "action", "");
            target.startTag("actions");
            for (Action a : actionSet) {
                target.startTag("action");
                if (a.getCaption() != null) {
                    target.addAttribute("caption", a.getCaption());
                }
                if (a.getIcon() != null) {
                    target.addAttribute("icon", a.getIcon());
                }
                target.addAttribute("key", actionMapper.key(a));
                target.endTag("action");
            }
            target.endTag("actions");
        }
    }

    private Set<Action> findAndPaintBodyActions(PaintTarget target) {
        Set<Action> actionSet = new LinkedHashSet<Action>();
        if (actionHandlers != null) {
            final ArrayList<String> keys = new ArrayList<String>();
            for (Action.Handler ah : actionHandlers) {
                // Getting actions for the null item, which in this case means
                // the body item
                final Action[] actions = ah.getActions(this, this);
                if (actions != null) {
                    for (Action action : actions) {
                        actionSet.add(action);
                        keys.add(actionMapper.key(action));
                    }
                }
            }
            target.addAttribute("alb", keys.toArray());
        }
        return actionSet;
    }

    /**
     * Registers a new action handler for this container
     *
     * @see com.vaadin.event.Action.Container#addActionHandler(Action.Handler)
     */
    public void addActionHandler(Action.Handler actionHandler) {
        if (actionHandler != null) {
            if (!actionHandlers.contains(actionHandler)) {
                actionHandlers.add(actionHandler);
                requestRepaint();
            }
        }
    }

    /**
     * Removes a previously registered action handler for the contents of this
     * container.
     *
     * @see com.vaadin.event.Action.Container#removeActionHandler(Action.Handler)
     */
    public void removeActionHandler(Action.Handler actionHandler) {
        if (actionHandlers != null && actionHandlers.contains(actionHandler)) {
            actionHandlers.remove(actionHandler);
            requestRepaint();
        }
    }

    public void removeLayer(Layer layer) {
        removeComponent(layer);
    }

    public class MouseMovedEvent extends Event {

        private MouseMoveInfo info;

        public MouseMovedEvent(Component source, MouseMoveInfo pointInfo) {
            super(source);
            setPointInfo(pointInfo);

        }

        private void setPointInfo(MouseMoveInfo info) {
            this.info = info;

        }

        public MouseMoveInfo getMoveInfo() {
            return info;
        }
    }

    private void mouseMoved(MouseMoveInfo pointInfo){
       MouseMovedEvent mouseMovedEvent = new MouseMovedEvent(this, pointInfo);
       fireEvent(mouseMovedEvent);
    }

    public interface MouseMoveListener {

        public static final Method method = ReflectTools.findMethod(
              MouseMoveListener.class, "mouseMoved", MouseMovedEvent.class);

        public void mouseMoved(MouseMovedEvent event);

    }
    public void addListener(MouseMoveListener listener) {
       addListener("mousemove", MouseMovedEvent.class, listener, MouseMoveListener.method);
    }
 
    private void mapClicked(PointInformation pointInfo) {
        MapClickEvent mapClickEvent = new MapClickEvent(this, pointInfo);
        fireEvent(mapClickEvent);
    }

    public interface MapClickListener {

        public static final Method method = ReflectTools.findMethod(
                MapClickListener.class, "mapClicked", MapClickEvent.class);

        public void mapClicked(MapClickEvent event);

    }

    public void addListener(MapClickListener listener) {
        addListener("click", MapClickEvent.class, listener,
                MapClickListener.method);
    }
    

    public void removeListener(MapClickListener listener) {
        removeListener("click", MapClickEvent.class, listener);
    }

    public class MapClickEvent extends Event {

        private PointInformation pointInfo;

        public MapClickEvent(Component source, PointInformation pointInfo) {
            super(source);
            setPointInfo(pointInfo);

        }

        private void setPointInfo(PointInformation pointInfo) {
            this.pointInfo = pointInfo;

        }

        public PointInformation getPointInfo() {
            return pointInfo;
        }
    }

    public class ExtentChangeEvent extends Event {
        public ExtentChangeEvent() {
            super(OpenLayersMap.this);
        }

        @Override
        public OpenLayersMap getComponent() {
            return (OpenLayersMap) super.getComponent();
        }
    }

    public interface ExtentChangeListener {

        public static final Method method = ReflectTools.findMethod(
                ExtentChangeListener.class, "extentChanged",
                ExtentChangeEvent.class);

        public void extentChanged(ExtentChangeEvent event);

    }

    public void addListener(ExtentChangeListener listener) {
        addListener(ExtentChangeEvent.class, listener,
                ExtentChangeListener.method);
    }

    public void removeListener(ExtentChangeListener listener) {
        removeListener(ExtentChangeEvent.class, listener);
    }

    public class BaseLayerChangeEvent extends Event {
        public BaseLayerChangeEvent() {
            super(OpenLayersMap.this);
        }

        @Override
        public OpenLayersMap getComponent() {
            return (OpenLayersMap) super.getComponent();
        }
    }

    public interface BaseLayerChangeListener {

        public static final Method method = ReflectTools.findMethod(
                BaseLayerChangeListener.class, "baseLayerChanged",
                BaseLayerChangeEvent.class);

        public void baseLayerChanged(BaseLayerChangeEvent event);
    }

    public void addListener(BaseLayerChangeListener listener) {
        addListener(BaseLayerChangeEvent.class, listener,
                BaseLayerChangeListener.method);
    }

    public void removeListener(BaseLayerChangeListener listener) {
        removeListener(BaseLayerChangeEvent.class, listener);
    }

}
