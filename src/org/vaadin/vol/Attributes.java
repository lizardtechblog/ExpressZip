package org.vaadin.vol;

import java.util.Map;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;

/**
 * Attibutes class wraps a JavaScript Object
 * 
 * 
 */
public class Attributes extends JsObject {

    /**
     * return Attributes as a Map arrays
     */
    public Map<String, Object> getAttributes() {
        return this.getKeyValueMap();
    }

    public void paint(String string, PaintTarget target) throws PaintException {
        target.addAttribute(string, this.getKeyValueMap());
    }

}
