package org.vaadin.vol.client.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.vaadin.vol.client.Costants;
import org.vaadin.vol.client.wrappers.Bounds;
import org.vaadin.vol.client.wrappers.Cluster;
import org.vaadin.vol.client.wrappers.GwtOlHandler;
import org.vaadin.vol.client.wrappers.JsObject;
import org.vaadin.vol.client.wrappers.Map;
import org.vaadin.vol.client.wrappers.Projection;
import org.vaadin.vol.client.wrappers.SelectFeatureFactory;
import org.vaadin.vol.client.wrappers.Style;
import org.vaadin.vol.client.wrappers.StyleMap;
import org.vaadin.vol.client.wrappers.Vector;
import org.vaadin.vol.client.wrappers.control.Control;
import org.vaadin.vol.client.wrappers.control.DrawFeature;
import org.vaadin.vol.client.wrappers.control.ModifyFeature;
import org.vaadin.vol.client.wrappers.control.SelectFeature;
import org.vaadin.vol.client.wrappers.control.TransformFeature;
import org.vaadin.vol.client.wrappers.geometry.Geometry;
import org.vaadin.vol.client.wrappers.geometry.LineString;
import org.vaadin.vol.client.wrappers.geometry.Point;
import org.vaadin.vol.client.wrappers.handler.PathHandler;
import org.vaadin.vol.client.wrappers.handler.PointHandler;
import org.vaadin.vol.client.wrappers.handler.PolygonHandler;
import org.vaadin.vol.client.wrappers.handler.RegularPolygonHandler;
import org.vaadin.vol.client.wrappers.layer.VectorLayer;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ValueMap;

public class VVectorLayer extends FlowPanel implements VLayer, Container {

    private VectorLayer vectors;
    private String drawingMode = "NONE";
    private Control df;
    private TransformFeature transformFeature;
    private GwtOlHandler _fAddedListener;
    private boolean updating;
    private ApplicationConnection client;
    private String displayName;
    private GwtOlHandler _fModifiedListener;

    private boolean immediate;
	private String projection;
	private Cluster cluster;

    public VectorLayer getLayer() {
        if (vectors == null) {
            vectors = VectorLayer.create(displayName, projection);
           // vectors.registerHandler("featureadded", getFeatureAddedListener());
            vectors.registerHandler("featuremodified",
                    getFeatureModifiedListener());
            vectors.registerHandler("afterfeaturemodified", new GwtOlHandler() {
                @SuppressWarnings("rawtypes")
                public void onEvent(JsArray arguments) {
                    if (updating) {
                        // ignore selections that happend during update, those
                        // should be already known and notified by the server
                        // side
                        return;
                    }
                    client.sendPendingVariableChanges();
                }
            });
            vectors.registerHandler("featureselected", new GwtOlHandler() {
                @SuppressWarnings("rawtypes")
                public void onEvent(JsArray arguments) {
                    if (updating) {
                        // ignore selections that happend during update, those
                        // should be already known and notified by the server
                        // side
                        return;
                    }
                    if (client.hasEventListeners(VVectorLayer.this, "vsel")) {
                        ValueMap javaScriptObject = arguments.get(0).cast();
                        Vector vector = javaScriptObject.getValueMap("feature").cast();
                        
						if (cluster != null) {
							JsArray<Vector> clusters = getLayer().getClusters();
							// first, find cluster that was selected
							for (int i = 0; i < clusters.length(); i++) {
								if (vector == clusters.get(i)) {
									Cluster cluster = vector.cast();
									
									// package up all features belonging to cluster
									JsArray<Vector> features = cluster.getFeatures();
									List<VAbstractVector> serverVectors = new ArrayList<VAbstractVector>();
									for (int j = 0; j < features.length(); j++) {
										for (Widget w : getChildren()) {
											VAbstractVector v = (VAbstractVector) w;
											if (v.getVector() == features.get(j)) {
												serverVectors.add(v);
												break; // found our match
											}
										}
									}
									client.updateVariable(paintableId, "vsel", serverVectors.toArray(), true);
									break;
								}
							}
						} else {
							for (Widget w : getChildren()) {
								VAbstractVector v = (VAbstractVector) w;
								if (v.getVector() == vector) {
									client.updateVariable(paintableId, "vsel", new Vector[]{vector}, true);
									//break; // only one hit per click
								}
							}
						}
                    }
                }
            });

            vectors.registerHandler("featureunselected", new GwtOlHandler() {
                @SuppressWarnings("rawtypes")
                public void onEvent(JsArray arguments) {
                    ValueMap javaScriptObject = arguments.get(0).cast();
                    Vector vector = javaScriptObject.getValueMap("feature")
                            .cast();
                    for (Widget w : getChildren()) {
                        VAbstractVector v = (VAbstractVector) w;
                        if (v.getVector() == vector) {
                            v.revertDefaultIntent();
                            // ignore selections that happend during update, those
                            // should be already known and notified by the server
                            // side
                            if (!updating && client.hasEventListeners(VVectorLayer.this,
                                    "vusel")) {
                                client.updateVariable(paintableId, "vusel", v,
                                        true);
                                break;
                            }
                        }
                    }
                }
            });

        }
        return vectors;
    }

    private GwtOlHandler getFeatureModifiedListener() {
        if (_fModifiedListener == null) {
            _fModifiedListener = new GwtOlHandler() {

                @SuppressWarnings("rawtypes")
                public void onEvent(JsArray arguments) {
                    if (drawingMode != "NONE") {
                        // use vaadin js object helper to get the actual feature
                        // from ol event
                        // TODO improve typing of OL events
                        JsObject event = arguments.get(0).cast();
                        Vector feature = event.getFieldByName("feature").cast();
                        Geometry geometry = feature.getGeometry();

                        boolean isLineString = true; // TODO
                        if (isLineString) {
                            LineString ls = geometry.cast();
                            JsArray<Point> allVertices = ls.getAllVertices();
                            client.updateVariable(paintableId,
                                    "newVerticesProj", getMap().getProjection()
                                            .toString(), false);
                            String[] points = new String[allVertices.length()];
                            for (int i = 0; i < allVertices.length(); i++) {
                                Point point = allVertices.get(i);
                                point = point.nativeClone();
                                point.transform(getMap().getProjection(),
                                        getProjection());
                                points[i] = point.toString();
                            }
                            // VConsole.log("modified");
                            // communicate points to server and mark the
                            // new geometry to be removed on next update.
                            client.updateVariable(paintableId, "vertices",
                                    points, false);

                            Vector modifiedFeature = ((ModifyFeature) df.cast())
                                    .getModifiedFeature();
                            Iterator<Widget> iterator = iterator();
                            while (iterator.hasNext()) {
                                VAbstractVector next = (VAbstractVector) iterator
                                        .next();
                                Vector vector = next.getVector();
                                if (vector == modifiedFeature) {
                                    client.updateVariable(paintableId,
                                            "modifiedVector", next, immediate);
                                    break;
                                }
                            }

                            // client.sendPendingVariableChanges();
                            // lastNewDrawing = feature;
                        }
                    }
                }
            };
        }
        return _fModifiedListener;
    }

    private String paintableId;
    private Vector lastNewDrawing;
    private boolean added = false;
    private String currentSelectionMode;
    private SelectFeature selectFeature;
    private String selectionCtrlId;             // Common SelectFeature control identifier

    private GwtOlHandler getFeatureAddedListener() {
        if (_fAddedListener == null) {
            _fAddedListener = new GwtOlHandler() {

                @SuppressWarnings("rawtypes")
                public void onEvent(JsArray arguments) {
                    if (!updating && drawingMode != "NONE") {
                        // use vaadin js object helper to get the actual feature
                        // from ol event
                        // TODO improve typing of OL events
                        JsObject event = arguments.get(0).cast();
                        Vector feature = event.getFieldByName("feature").cast();
                        Geometry geometry = feature.getGeometry();

                        if (drawingMode == "AREA" || drawingMode == "LINE" || drawingMode == "RECTANGLE" || drawingMode == "CIRCLE") {
                            LineString ls = geometry.cast();
                            JsArray<Point> allVertices = ls.getAllVertices();
                            // TODO this can be removed??
                            client.updateVariable(paintableId,
                                    "newVerticesProj", getMap().getProjection()
                                            .toString(), false);
                            String[] points = new String[allVertices.length()];
                            for (int i = 0; i < allVertices.length(); i++) {
                                Point point = allVertices.get(i);
                                point.transform(getMap().getProjection(),
                                        getProjection());
                                points[i] = point.toString();
                            }
                            client.updateVariable(paintableId, "vertices",
                                    points, false);
                        } else if (drawingMode == "POINT") {
                            // point
                            Point point = geometry.cast();
                            point.transform(getMap().getProjection(),
                                    getProjection());
                            double x = point.getX();
                            double y = point.getY();
                            client.updateVariable(paintableId, "x", x, false);
                            client.updateVariable(paintableId, "y", y, false);
                        }
                        // VConsole.log("drawing done");
                        // communicate points to server and mark the
                        // new geometry to be removed on next update.
                        client.sendPendingVariableChanges();
                        if (drawingMode != "MODIFY") {
                            lastNewDrawing = feature;
                        }
                    }
                }
            };
        }
        return _fAddedListener;
    }

    public void updateFromUIDL(UIDL layer, ApplicationConnection client) {
        if (client.updateComponent(this, layer, false)) {
            return;
        }
        this.client = client;
        paintableId = layer.getId();
        updating = true;
        displayName = layer.getStringAttribute("name");
        immediate = layer.getBooleanAttribute("immediate");
        projection = layer.hasAttribute("projection") ? layer.getStringAttribute("projection") : null;
        if (!added) {
            getMap().addLayer(getLayer());
            added = true;
        }

        // Last new drawing only visible to next update. If used by server side
        // handler, we probably have it as a child component
        if (lastNewDrawing != null) {
            getLayer().removeFeature(lastNewDrawing);
            lastNewDrawing = null;
        }

        updateStyleMap(layer);
        setDrawingMode(layer.getStringAttribute("dmode"));
        
        // Identifier for SelectFeature control to use ... layers specifying the
        // the same identifier can all listen for their own Select events on the map.
        selectionCtrlId = layer.getStringAttribute("selectionCtrlId");
        
        setSelectionMode(layer);

        HashSet<Widget> orphaned = new HashSet<Widget>();
        for (Iterator<Widget> iterator = iterator(); iterator.hasNext();) {
            orphaned.add(iterator.next());
        }

        if(cluster!=null) {
        	cluster.deactivate();
        }
        JsArray<Vector> features = (JsArray<Vector>) JsArray.createArray();
        int childCount = layer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            UIDL childUIDL = layer.getChildUIDL(i);
            VAbstractVector vector = (VAbstractVector) client
                    .getPaintable(childUIDL);
            boolean isNew = !hasChildComponent(vector);
            if (isNew) {
                add(vector);
            }
            vector.updateFromUIDL(childUIDL, client);
            features.push(vector.getVector());
            orphaned.remove(vector);
        }
        for (Widget widget : orphaned) {
            widget.removeFromParent();
        }
        boolean recluster = layer.getBooleanAttribute("recluster");
        if(recluster) {
           cluster = Cluster.create(getLayer());
           getLayer().addClustering(cluster);
           cluster.setFeatures(features);
           cluster.activate();
           cluster.cluster();
        }
        
        boolean dirtyTransform = layer.getBooleanAttribute("redrawTransform");
        if(dirtyTransform){
           updateFeature(layer.getDoubleAttribute("minx"), 
                         layer.getDoubleAttribute("miny"), 
                         layer.getDoubleAttribute("maxx"), 
                         layer.getDoubleAttribute("maxy"));
        }
        updating = false;
    }
    
    private void updateFeature(double left, double bottom, double right, double top){
       
       Iterator<Widget> iterator = iterator();
       // assume first vector is our Area vector for ExtentSelector
       if (iterator.hasNext()) {
          VAbstractVector next = (VAbstractVector) iterator.next();
          Vector vector = next.getVector();    
          Geometry oldGeometry = vector.getGeometry();
          Geometry newGeometry = oldGeometry.clone();
          newGeometry.setBounds(left, bottom, right, top);

          getLayer().removeFeature(vector);   
          vector.setGeometry(newGeometry);      
          getLayer().addFeature(vector);
          
          transformFeature.setFeature(vector);
       }
    }

    private void setSelectionMode(final UIDL layer) {
        String newSelectionMode = layer.getStringAttribute("smode").intern();
        if (currentSelectionMode != newSelectionMode) {
            if (selectFeature != null) {
                /* remove this layer from the SelectFeature instead of removing the control
                selectFeature.deActivate();
                getMap().removeControl(selectFeature);
                */
                SelectFeatureFactory.getInst().removeLayer(selectFeature, selectionCtrlId, getMap(), vectors);
                selectFeature = null;
            }

            if (newSelectionMode != "NONE") {
                /* delegate responsibility for managing the SelectFeature to the factory;
                 * just let it know we want to register this vectorlayer in the SelectFeature.
                selectFeature = SelectFeature.create(vectors);
                getMap().addControl(selectFeature);
                */
                selectFeature = SelectFeatureFactory.getInst().getOrCreate(selectionCtrlId, getMap(), vectors);
                selectFeature.activate();
            }

            currentSelectionMode = newSelectionMode;
        }
        if (currentSelectionMode != "NONE" || drawingMode == "MODIFY") {
            if (layer.hasAttribute("svector")) {
            	Scheduler.get().scheduleFinally(new ScheduledCommand() {
					
					public void execute() {
						VAbstractVector selectedVector = (VAbstractVector) layer
								.getPaintableAttribute("svector", client);
						if (selectedVector != null) {
							updating = true;
							// ensure selection
							if (drawingMode == "MODIFY") {
								ModifyFeature mf = (ModifyFeature) df.cast();
								if (mf.getModifiedFeature() != null) {
									mf.unselect(mf.getModifiedFeature());
								}
								mf.select(selectedVector.getVector());
							} else {
								selectFeature.select(selectedVector.getVector());
							}
							updating = false;
						}
						
					}
				});
            } else {
                // remove selection
                if (drawingMode == "MODIFY") {
                    ModifyFeature modifyFeature = (ModifyFeature) df.cast();
                    if (modifyFeature.getModifiedFeature() != null) {
                        modifyFeature.unselect(modifyFeature
                                .getModifiedFeature());
                    }
                } else {
                    try {
                        selectFeature.unselectAll();
                        
                    } catch (Exception e) {
                        // NOP, may throw exception if selected vector gets
                        // deleted
                    }
                }
            }
        }
    }

    private void setDrawingMode(String newDrawingMode) {
    	newDrawingMode = newDrawingMode.intern();
        if (drawingMode != newDrawingMode) {
            if (drawingMode != "NONE") {
                // remove old drawing feature
                if (drawingMode == "MODIFY") {
                    ModifyFeature mf = df.cast();
                    if (mf.getModifiedFeature() != null) {
                        mf.unselect(mf.getModifiedFeature());
                }
                }
                df.deActivate();
                getMap().removeControl(df);
            }
            drawingMode = newDrawingMode;
            df = null;

            if (drawingMode == "AREA") {
                df = DrawFeature.create(getLayer(), PolygonHandler.get());
            } else if (drawingMode == "LINE") {
                df = DrawFeature.create(getLayer(), PathHandler.get());
            } else if (drawingMode == "MODIFY") {
                df = ModifyFeature.create(getLayer());
            } else if (drawingMode == "POINT") {
                df = DrawFeature.create(getLayer(), PointHandler.get());
            } else if (drawingMode == "RECTANGLE") {
                df = DrawFeature.create(getLayer(), RegularPolygonHandler.get(), RegularPolygonHandler.getRectangleOptions());
                DrawFeature.registerCallback(df,this);
                transformFeature = createTransformControl();
                getMap().addControl(transformFeature);
            } else if (drawingMode == "CIRCLE") {
                df = DrawFeature.create(getLayer(), RegularPolygonHandler.get(), RegularPolygonHandler.getCircleOptions());
            }
            if (df != null) {
                getMap().addControl(df);
                df.activate();
            }
        }
    }
    public TransformFeature createTransformControl(){

       TransformFeature tf = TransformFeature.create(getLayer());
       tf.deActivate();
       tf.registerHandler("transformcomplete", new GwtOlHandler() {
           @SuppressWarnings("rawtypes")
           public void onEvent(JsArray arguments) {
            ValueMap javaScriptObject = arguments.get(0).cast();
               Vector feature = javaScriptObject.getValueMap("feature").cast();
               onResize(feature.getGeometry());
           }
       });
       return tf;
    }
    
    
    public DrawFeature getDrawFeature(){ return (DrawFeature) df;}
    public TransformFeature getTransformFeature() {return transformFeature;}
        
    public void onEndDrag(Object box) {            
    	//updating = true; // avoid events firing during transform
    	Geometry geometry = ((JavaScriptObject) box).cast();
    	Iterator<Widget> iterator = iterator();
        // assume first vector is our Area vector for ExtentSelector
        if (iterator.hasNext()) {
            VAbstractVector next = (VAbstractVector) iterator.next();
            Vector vector = next.getVector();
            getLayer().removeFeature(vector);            
            vector.setGeometry(geometry);
            getLayer().addFeature(vector);
            transformFeature.setFeature(vector);
            df.deActivate();
        }
        //updating = false;
        onResize(geometry);
    }

    public void onResize(Geometry geometry) {
       LineString ls = geometry.cast();
       
        JsArray<Point> allVertices = ls.getAllVertices();
       
        client.updateVariable(paintableId,
                "newVerticesProj", getMap().getProjection().toString(), false);
        String[] points = new String[allVertices.length()];
        for (int i = 0; i < allVertices.length(); i++) {
            Point point = allVertices.get(i);
            point = point.nativeClone();
            point.transform(getMap().getProjection(),
                    getProjection());
            points[i] = point.toString();
        }
        client.updateVariable(paintableId, "extentSelected", points, true);
    }

    private void updateStyleMap(UIDL childUIDL) {
        StyleMap sm = getStyleMap(childUIDL);
        if (sm == null) {
            sm = StyleMap.create();
        }

        getLayer().setStyleMap(sm);
    }

    public static StyleMap getStyleMap(UIDL childUIDL) {
        if (!childUIDL.hasAttribute("olStyleMap")) {
            return null;
        }
        String[] renderIntents = childUIDL
                .getStringArrayAttribute("olStyleMap");
        StyleMap sm;
        if (renderIntents.length == 1 && renderIntents[0].equals("default")) {
            sm = StyleMap.create();
            sm.setStyle(
                    "default",
                    Style.create(childUIDL.getMapAttribute(
                            "olStyle_" + renderIntents[0]).cast()));
        } else {
            sm = StyleMap.create();
            for (String intent : renderIntents) {
                if (intent.startsWith("__")) {
                    String specialAttribute = intent.replaceAll("__", "");
                    if (specialAttribute.equals("extendDefault")) {
                        sm.setExtendDefault(true);
                    }
                } else {
                    Style style = Style.create(childUIDL
                            .getMapAttribute("olStyle_" + intent));
                    sm.setStyle(intent, style);
                }
            }
        }

        if (childUIDL.hasAttribute(Costants.STYLEMAP_UNIQUEVALUERULES)) {
            addUniqueValueRules(sm, childUIDL);
        }
        return sm;
    }

    private static void addUniqueValueRules(StyleMap sm, UIDL childUIDL) {

        if (childUIDL.hasAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_KEYS)) {
            String[] uvrKeysArray = childUIDL
                    .getStringArrayAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_KEYS);

            for (String uvrkey : uvrKeysArray) {

                String property = childUIDL
                        .getStringAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX
                                + uvrkey
                                + Costants.STYLEMAP_UNIQUEVALUERULES_PROPERTY_SUFFIX);
                String intent = childUIDL
                        .getStringAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX
                                + uvrkey
                                + Costants.STYLEMAP_UNIQUEVALUERULES_INTENT_SUFFIX);
                // Object context =
                // childUIDL.getStringAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX+uvrkey+Costants.STYLEMAP_UNIQUEVALUERULES_CONTEXT_SUFFIX);

                if (childUIDL
                        .hasAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX
                                + uvrkey + "_lookupkeys")) {
                    String[] lookup_keys = childUIDL
                            .getStringArrayAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX
                                    + uvrkey
                                    + Costants.STYLEMAP_UNIQUEVALUERULES_LOOKUPKEYS_SUFFIX);

                    JsObject symbolizer_lookup_js = JsObject.createObject();

                    for (String key : lookup_keys) {

                        ValueMap symbolizer = childUIDL
                                .getMapAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX
                                        + uvrkey
                                        + Costants.STYLEMAP_UNIQUEVALUERULES_LOOKUPITEM_SUFFIX
                                        + key);
                        symbolizer_lookup_js
                                .setProperty(key, symbolizer.cast());
                    }

                    sm.addUniqueValueRules(intent, property,
                            symbolizer_lookup_js.cast(), null);
                }
            }

        }

        return;
    }

    @Override
    protected void onDetach() {
        getMap().removeLayer(getLayer());
        added = false;
        super.onDetach();
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        if (!added && vectors != null) {
            getMap().addLayer(getLayer());
        }
    }

    private Map getMap() {
        return getVMap().getMap();
    }

    private VOpenLayersMap getVMap() {
        return ((VOpenLayersMap) getParent().getParent());
    }

    protected Projection getProjection() {
        return getVMap().getProjection();
    }

    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
        // TODO Auto-generated method stub

    }

    public boolean hasChildComponent(Widget component) {
        return getWidgetIndex(component) != -1;
    }

    public void updateCaption(Paintable component, UIDL uidl) {
        // TODO Auto-generated method stub

    }

    public boolean requestLayout(Set<Paintable> children) {
        // TODO Auto-generated method stub
        return false;
    }

    public RenderSpace getAllocatedSpace(Widget child) {
        // TODO Auto-generated method stub
        return null;
    }

    public void vectorUpdated(VAbstractVector vAbstractVector) {
        // redraw
        getLayer().drawFeature(vAbstractVector.getVector());
        if (df != null) {
            String id = df.getId();
            if (id.contains("ModifyFeature")) {
                ModifyFeature mf = df.cast();
                Vector modifiedFeature = mf.getModifiedFeature();
                if (modifiedFeature == vAbstractVector.getVector()) {
                    // VConsole.log("Whoops, modified on the server side " +
                    // "while currently being modified on the client side. " +
                    // "This may cause issues for OL unleass we notify " +
                    // "the ModifyFeature about the change.");
                    mf.resetVertices();
                }
            }
        }
    }

}
