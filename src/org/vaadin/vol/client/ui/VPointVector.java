package org.vaadin.vol.client.ui;

import org.vaadin.vol.client.wrappers.Projection;
import org.vaadin.vol.client.wrappers.Vector;
import org.vaadin.vol.client.wrappers.geometry.Point;

import com.google.gwt.core.client.JavaScriptObject;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ValueMap;

public class VPointVector extends VAbstractVector {

    @Override
    protected void createOrUpdateVector(UIDL childUIDL, ApplicationConnection client) {
        Projection mapProjection = getMap().getProjection();

        Point p = Point.create(childUIDL.getStringArrayAttribute("points")[0]);
        p.transform(getProjection(), mapProjection);

        JavaScriptObject style = null;
        ValueMap attributes = getAttributes();
        if (vector == null) {
        	vector = Vector.create(p, attributes, style);
        } else {
        	vector.setGeometry(p);
        	vector.setStyle(style);
        	vector.setAttributes(attributes);
        }

    }

}
