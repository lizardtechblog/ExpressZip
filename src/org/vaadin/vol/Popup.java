/**
 * 
 */
package org.vaadin.vol;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.vaadin.vol.client.ui.VPopup;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.tools.ReflectTools;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

@SuppressWarnings("serial")
@ClientWidget(VPopup.class)
public class Popup extends AbstractComponentContainer {

    public class CloseEvent extends Event {
        public CloseEvent() {
            super(Popup.this);
        }
    }

    public interface CloseListener {
        public void onClose(CloseEvent event);

        final Method method = ReflectTools.findMethod(CloseListener.class,
                "onClose", CloseEvent.class);
        final String id = "close";
    }

    public enum PopupStyle {
        DEFAULT, ANCHORED, ANCHORED_BUBBLE, FRAMED, FRAMED_CLOUD
    }

    private String projection = "EPSG:4326";
    private PopupStyle popupstyle = PopupStyle.DEFAULT;
    private Component anchor;
    private Component content;
    private Point point = new Point(0, 0);
    private boolean closable = true;

    public Popup(double lon, double lat, String content) {
        point = new Point(lon, lat);
        setContent(content);
    }

    public Popup(String content) {
        setContent(content);
    }

    public Popup(Component content) {
        addComponent(content);
    }

    public Popup() {
        this("");
    }

    public double getLon() {
        return point.getLon();
    }

    public double getLat() {
        return point.getLat();
    }

    public void setLon(double lon) {
        point.setLon(lon);
        requestRepaint();
    }

    public void setLat(double lat) {
        point.setLat(lat);
        requestRepaint();
    }

    public void setContent(String content) {
        Label c = new Label(content, Label.CONTENT_XHTML);
        c.setSizeUndefined();
        addComponent(c);
        requestRepaint();
    }

    @Override
    public void addComponent(Component c) {
        if(c == null)  {
            setContent("");
        } else {
            if(content != null) {
                removeAllComponents();
            }
            super.addComponent(c);
            content = c;
        }
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.addAttribute("lon", point.getLon());
        target.addAttribute("lat", point.getLat());
        target.addAttribute("pr", projection);
        target.addAttribute("style", popupstyle.toString());
        target.addAttribute("closable", isClosable());
        if (anchor == null
                && (popupstyle == PopupStyle.FRAMED
                        || popupstyle == PopupStyle.ANCHORED || popupstyle == PopupStyle.ANCHORED_BUBBLE)) {
            throw new IllegalStateException(
                    "Anchor elemen hasn't been defined, but is required for this type of popup.");
        }
        if (anchor != null) {
            target.addAttribute("anchor", anchor);
        }
        content.paint(target);
    }

    public void addClickListener(ClickListener listener) {
        addListener("click", ClickEvent.class, listener,
                ClickListener.clickMethod);
    }

    public void removeClickListener(ClickListener listener) {
        removeListener(ClickEvent.class, listener);
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        if (variables.containsKey("close")) {
            if (hasListeners(CloseEvent.class)) {
                fireEvent(new CloseEvent());
            } else {
                Component parent2 = getParent();
                if (parent2 instanceof OpenLayersMap) {
                    OpenLayersMap olm = (OpenLayersMap) parent2;
                    olm.removeComponent(this);
                }
            }
        }
    }

    public void setPopupStyle(PopupStyle style) {
        popupstyle = style;
        requestRepaint();
    }

    public PopupStyle getPopupStyle() {
        return popupstyle;
    }

    public void setAnchor(Marker marker) {
        anchor = marker;
        requestRepaint();
    }

    public void addListener(CloseListener listener) {
        super.addListener(CloseListener.id, CloseEvent.class, listener,
                CloseListener.method);
    }

    public void removeListener(CloseListener listener) {
        super.removeListener(CloseListener.id, CloseEvent.class, listener);
    }

    public boolean isClosable() {
        return closable;
    }

    public void setClosable(boolean closable) {
        this.closable = closable;
    }

    public void replaceComponent(Component oldComponent, Component newComponent) {
        throw new UnsupportedOperationException();
    }

    public Iterator<Component> getComponentIterator() {
        return Collections.singleton(content).iterator();
    }

    @Override
    public void removeComponent(Component c) {
        if (c == content) {
            super.removeComponent(c);
            content = null;
        } else {
            throw new IllegalArgumentException(
                    "Given component is not in this popup");
        }
    }

    public void setContent(Table table) {
        addComponent(table);
    }
}