/**
 * 
 */
package org.vaadin.vol;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.tools.ReflectTools;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Component;

@ClientWidget(org.vaadin.vol.client.ui.VVectorLayer.class)
public class VectorLayer extends AbstractComponentContainer implements ProjectedLayer {

    private StyleMap stylemap;

    public enum SelectionMode {
        NONE, SIMPLE
        // MULTI, MULTI_WITH_AREA_SELECTION etc
    }

    private Vector selectedVector;

    private String displayName = "Vector layer";

    private List<Vector> vectors = new LinkedList<Vector>();

    private SelectionMode selectionMode = SelectionMode.NONE;

    public enum DrawingMode {
        NONE, LINE, AREA, RECTANGLE, CIRCLE, POINT, MODIFY
    }

    private DrawingMode drawingMode = DrawingMode.NONE;
    
    private String selectionCtrlId;             // Common SelectFeature control identifier

    private boolean recluster = false;
    private boolean redrawTransform = false;
    private double minx = 0.0f;
    private double maxx = 0.0f;
    private double miny = 0.0f;
    private double maxy = 0.0f;
    
	private String projection;

	public void addVector(Vector m) {
        addComponent(m);
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        target.addAttribute("name", displayName);
        target.addAttribute("dmode", drawingMode.toString());
        target.addAttribute("smode", selectionMode.toString());
        target.addAttribute("recluster", recluster);
        target.addAttribute("redrawTransform", redrawTransform);
        target.addAttribute("minx", minx);
        target.addAttribute("maxx", maxx);
        target.addAttribute("miny", miny);
        target.addAttribute("maxy", maxy);
        redrawTransform = false;

		if (projection != null) {
		    target.addAttribute("projection", projection);
		}

		if (selectionCtrlId != null) {
            target.addAttribute("selectionCtrlId", selectionCtrlId);
        }
        
        if (selectedVector != null) {
            target.addAttribute("svector", selectedVector);
        }

        if (stylemap != null) {
            stylemap.paint(target);
        }

        for (Vector m : vectors) {
            m.paint(target);
        }

    }

    public void replaceComponent(Component oldComponent, Component newComponent) {
        throw new UnsupportedOperationException();
    }

    public Iterator<Component> getComponentIterator() {
        LinkedList<Component> list = new LinkedList<Component>(vectors);
        return list.iterator();
    }

    @Override
    public void addComponent(Component c) {
        if (c instanceof Vector) {
            vectors.add((Vector) c);
            super.addComponent(c);
        } else {
            throw new IllegalArgumentException(
                    "VectorLayer supports only Vectors");
        }
    }

    @Override
    public void removeComponent(Component c) {
        vectors.remove(c);
        super.removeComponent(c);
        if (selectedVector == c) {
            selectedVector = null;
            fireEvent(new VectorUnSelectedEvent(this, (Vector) c));
        }
        requestRepaint();
    }

    public void setDrawingMode(DrawingMode drawingMode) {
        this.drawingMode = drawingMode;
        requestRepaint();
    }

    public DrawingMode getDrawingMode() {
        return drawingMode;
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);


        if (variables.containsKey("extentSelected")) {
            String[] object = (String[]) variables.get("extentSelected");
            Point[] points = new Point[object.length];
            for (int i = 0; i < points.length; i++) {
                points[i] = Point.valueOf(object[i]);
            }
            for(Vector v : vectors) {
            	v.setPointsWithoutRepaint(points);
            	vectorModified(v);
            }
        }

        // support other drawing modes than area
        // TODO make events fired when new object is drawn/edited
        if (variables.containsKey("vertices")) {
            String[] object = (String[]) variables.get("vertices");
            Point[] points = new Point[object.length];
            for (int i = 0; i < points.length; i++) {
                points[i] = Point.valueOf(object[i]);
            }

            if (drawingMode == DrawingMode.LINE) {
                PolyLine polyline = new PolyLine();
                polyline.setPoints(points);
                newVectorPainted(polyline);
            } else if (drawingMode == DrawingMode.AREA 
            		|| drawingMode == DrawingMode.RECTANGLE 
            		|| drawingMode == DrawingMode.CIRCLE) {
                Area area = new Area();
                area.setPoints(points);
                newVectorPainted(area);
            } else if (drawingMode == DrawingMode.MODIFY) {
                Vector vector = (Vector) variables.get("modifiedVector");
                if (vector != null) {
                    vector.setPointsWithoutRepaint(points);
                    vectorModified(vector);
                } else {
                    Logger.getLogger(getClass().getName())
                            .severe("Vector modified event didn't provide related vector!?");
                }
            }
        }
        if (drawingMode == DrawingMode.POINT && variables.containsKey("x")) {
            Double x = (Double) variables.get("x");
            Double y = (Double) variables.get("y");
            PointVector point = new PointVector(x, y);
            newVectorPainted(point);
        }
        if (variables.containsKey("vusel")) {
            Vector object = (Vector) variables.get("vusel");
            if (selectedVector == object) {
                selectedVector = null;
            }
            VectorUnSelectedEvent vectorSelectedEvent = new VectorUnSelectedEvent(
                    this, object);
            fireEvent(vectorSelectedEvent);
        }
		if (variables.containsKey("vsel")) {
			Object[] objects = (Object[]) variables.get("vsel");
			Vector[] features = Arrays.asList(objects).toArray(new Vector[0]);
			VectorSelectedEvent vectorSelectedEvent = new VectorSelectedEvent(this, features);
			fireEvent(vectorSelectedEvent);
		}
    }
    
    private void vectorModified(Vector object2) {
        VectorModifiedEvent vectorModifiedEvent = new VectorModifiedEvent(this,
                object2);
        fireEvent(vectorModifiedEvent);
    }

    protected void newVectorPainted(Vector vector) {
        VectorDrawnEvent vectorDrawnEvent = new VectorDrawnEvent(this, vector);
        fireEvent(vectorDrawnEvent);
        requestRepaint();
    }

    public interface VectorDrawnListener {

        public final Method method = ReflectTools.findMethod(
                VectorDrawnListener.class, "vectorDrawn",
                VectorDrawnEvent.class);

        public void vectorDrawn(VectorDrawnEvent event);

    }

    public void addListener(VectorDrawnListener listener) {
        addListener(VectorDrawnEvent.class, listener,
                VectorDrawnListener.method);
    }

    public void removeListener(VectorDrawnListener listener) {
        removeListener(VectorDrawnEvent.class, listener,
                VectorDrawnListener.method);
    }

    public interface VectorModifiedListener {

        public final Method method = ReflectTools.findMethod(
                VectorModifiedListener.class, "vectorModified",
                VectorModifiedEvent.class);

        public void vectorModified(VectorModifiedEvent event);

    }

    public void addListener(VectorModifiedListener listener) {
        addListener(VectorModifiedEvent.class, listener,
                VectorModifiedListener.method);
    }

    public void removeListener(VectorModifiedListener listener) {
        removeListener(VectorModifiedEvent.class, listener,
                VectorModifiedListener.method);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return the stylemap
     */
    public StyleMap getStyleMap() {
        return stylemap;
    }

    /**
     * @param stylemap
     *            the stylemap to set
     */
    public void setStyleMap(StyleMap stylemap) {
        this.stylemap = stylemap;
        requestRepaint();
    }

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

    public String getSelectionCtrlId() {
        return selectionCtrlId;
    }

    public void setSelectionCtrlId(String selectionCtrlId) {
        this.selectionCtrlId = selectionCtrlId;
    }

    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
        requestRepaint();
    }

    public SelectionMode getSelectionMode() {
        return selectionMode;
    }

    public class VectorDrawnEvent extends Event {

        private Vector vector;

        public VectorDrawnEvent(Component source, Vector vector) {
            super(source);
            setVector(vector);
        }

        private void setVector(Vector vector) {
            this.vector = vector;
        }

        public Vector getVector() {
            return vector;
        }

    }

    public class VectorModifiedEvent extends Event {

        private Vector vector;

        public VectorModifiedEvent(Component source, Vector vector) {
            super(source);
            setVector(vector);
        }

        private void setVector(Vector vector) {
            this.vector = vector;
        }

        public Vector getVector() {
            return vector;
        }

    }

    public interface VectorSelectedListener {

        public final String EVENT_ID = "vsel";

        public final Method method = ReflectTools.findMethod(
                VectorSelectedListener.class, "vectorSelected",
                VectorSelectedEvent.class);

        public void vectorSelected(VectorSelectedEvent event);

    }

    public void addListener(VectorSelectedListener listener) {
        addListener(VectorSelectedListener.EVENT_ID, VectorSelectedEvent.class,
                listener, VectorSelectedListener.method);
    }

    public void removeListener(VectorSelectedListener listener) {
        removeListener(VectorSelectedListener.EVENT_ID,
                VectorSelectedEvent.class, listener);
    }

    public class VectorSelectedEvent extends Event {

        private Vector[] vectors;

        public VectorSelectedEvent(Component source, Vector[] vectors) {
            super(source);
            setVectors(vectors);
        }

        private void setVectors(Vector[] vector) {
            this.vectors = vector;
        }

        public Vector[] getVectors() {
            return vectors;
        }

    }

    public interface VectorUnSelectedListener {

        public final String EVENT_ID = "vusel";

        public final Method method = ReflectTools.findMethod(
                VectorUnSelectedListener.class, "vectorUnSelected",
                VectorUnSelectedEvent.class);

        public void vectorUnSelected(VectorUnSelectedEvent event);

    }
    
    public void addListener(VectorUnSelectedListener listener) {
        addListener(VectorUnSelectedListener.EVENT_ID,
                VectorUnSelectedEvent.class, listener,
                VectorUnSelectedListener.method);
    }

    public void removeListener(VectorUnSelectedListener listener) {
        removeListener(VectorUnSelectedListener.EVENT_ID,
                VectorUnSelectedEvent.class, listener);
    }

    public Vector getSelectedVector() {
        return selectedVector;
    }

    public void setSelectedVector(Vector selectedVector) {
        if (this.selectedVector != selectedVector) {
            if (this.selectedVector != null) {
                fireEvent(new VectorUnSelectedEvent(this, this.selectedVector));
            }
            this.selectedVector = selectedVector;
            if (selectedVector != null) {
                fireEvent(new VectorSelectedEvent(this, new Vector[]{selectedVector}));
            }
            requestRepaint();
        }
    }

	public class VectorUnSelectedEvent extends Event {

        private Vector vector;

        public VectorUnSelectedEvent(Component source, Vector vector) {
            super(source);
            setVector(vector);
        }

        private void setVector(Vector vector) {
            this.vector = vector;
        }

        public Vector getVector() {
            return vector;
        }

    }

	public void recluster(boolean on) {
		recluster = on;
	}
	public void redrawTransform(Bounds bounds){
	   redrawTransform = true;

	   minx = bounds.getMinLon();
	   maxx = bounds.getMaxLon();
      miny = bounds.getMinLat();
	   maxy = bounds.getMaxLat();
      requestRepaint();
	   
	}

}