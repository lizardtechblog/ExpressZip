package org.vaadin.vol;

import org.vaadin.vol.client.ui.VPointVector;

import com.vaadin.ui.ClientWidget;

@ClientWidget(VPointVector.class)
public class PointVector extends Vector {
	
	public PointVector(double x, double y) {
		setPoints(new Point(x, y));
	}
	
	public PointVector() {
		setPoints(new Point(0, 0));
	}
	
	public Point getPoint() {
		return getPoints()[0];
	}
	
	/**
	 * Note that for PointVector only one points is needed and handled.
	 * 
	 * @see org.vaadin.vol.Vector#setPoints(org.vaadin.vol.Point[])
	 */
	@Override
	public void setPoints(Point... points) {
		super.setPoints(points);
	}
	

}
