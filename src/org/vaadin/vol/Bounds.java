package org.vaadin.vol;

import java.io.Serializable;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;

public class Bounds implements Serializable {

    private double top;
    private double bottom;
    private double left;
    private double right;
    
    private boolean empty = true;
    public boolean isEmpty() {return empty;}

    public Bounds(Point... points) {
    	if(points.length == 0) {
    		return;
    	}
        
        Point first = points[0];
		init(first);

        for (Point point : points) {
        	extend(point);
        }
    }

	private void init(Point first) {
		top = first.getLat();
        bottom = first.getLat();
        right = first.getLon();
        left = first.getLon();
        empty = false;
	}


    /**
     * extend(Point... points) will be useful in case of multiple vector on the
     * same map to compute the bounds that surround all the vectors
     * 
     * Notes : there is no check of the starting bounds values the method will
     * fail if bounds values are not correctly initialized
     */
    public void extend(Point... points) {
        for (int i = 0; i < points.length; i++) {
            Point p = points[i];
            extend(p);
        }
    }

    public void extend(Point p) {
    	if(empty) {
    		init(p);
    	} else {
    		double lon = p.getLon();
    		if (lon < left) {
    			left = lon;
    		}
    		if (lon > right) {
    			right = lon;
    		}
    		double lat = p.getLat();
    		if (lat < bottom) {
    			bottom = lat;
    		}
    		if (lat > top) {
    			top = lat;
    		}
    	}
        // TODO figure out how to behave on poles and in date line
    }

    public void setTop(double top) {
        this.top = top;
    }

    public double getTop() {
        return top;
    }

    /**
     * Alias for {@link #setTop(double)}
     * 
     * @param lat
     */
    public void setMaxLat(double lat) {
        setTop(lat);
    }

    /**
     * Alias for {@link #getTop()}
     * 
     * @return
     */
    public double getMaxLat() {
        return getTop();
    }

    public void setBottom(double bottom) {
        this.bottom = bottom;
    }

    public double getBottom() {
        return bottom;
    }

    /**
     * Alias for {@link #setBottom(double)}
     * 
     * @param lat
     */
    public void setMinLat(double lat) {
        setBottom(lat);
    }

    /**
     * Alias for {@link #getBottom()}
     * 
     * @return
     */
    public double getMinLat() {
        return getBottom();
    }

    public void setLeft(double left) {
        this.left = left;
    }

    public double getLeft() {
        return left;
    }

    /**
     * Alias for {@link #setLeft(double)}
     * 
     * @param lon
     */
    public void setMinLon(double lon) {
        setLeft(lon);
    }

    /**
     * Alias for {@link #getLeft()}
     * 
     * @return
     */
    public double getMinLon() {
        return getLeft();
    }

    public void setRight(double right) {
        this.right = right;
    }

    public double getRight() {
        return right;
    }

    /**
     * Alias for {@link #setRight(double)}
     * 
     * @param lon
     */
    public void setMaxLon(double lon) {
        setRight(lon);
    }

    /**
     * Alias for {@link #getRight()}
     * 
     * @return
     */
    public double getMaxLon() {
        return getRight();
    }

    public void paint(String string, PaintTarget target) throws PaintException {
        target.addAttribute(string + "_top", top);
        target.addAttribute(string + "_right", right);
        target.addAttribute(string + "_bottom", bottom);
        target.addAttribute(string + "_left", left);
    }

    @Override
    public String toString() {
        return "t" + top + ",l" + left + ",b" + bottom + ",r" + right;
    }

}
