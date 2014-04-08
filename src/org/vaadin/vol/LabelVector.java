package org.vaadin.vol;

public class LabelVector extends PointVector {
    
    
    @Override
    public void setCaption(String caption) {
        super.setCaption(caption);
        setTextToSymbolizerOrAsLabelAttribute(caption);
   }

    private void setTextToSymbolizerOrAsLabelAttribute(String caption) {
        if(getStyleName().isEmpty()) {
            Style customStyle = getCustomStyle();
            if(customStyle == null) {
                customStyle = new Style();
            }
            setCustomStyle(customStyle);
            customStyle.setProperty("label", caption);
        } else {
            Attributes attributes = getAttributes();
            if(attributes == null) {
                attributes = new Attributes();
                setAttributes(attributes);
            }
            attributes.setProperty("label", caption);
            setCustomStyle(null);
        }
    }
    
    public LabelVector() {
    }
    
    public LabelVector(String label) {
        setCaption(label);
    }
    
    @Override
    public void setStyleName(String style) {
        boolean wasEmpty = getStyleName().isEmpty();
        super.setStyleName(style);
        if(wasEmpty != (style == null)) {
            setTextToSymbolizerOrAsLabelAttribute(getCaption());
        }
    }
    
    
}
