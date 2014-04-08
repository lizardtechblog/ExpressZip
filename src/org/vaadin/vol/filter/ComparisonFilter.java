package org.vaadin.vol.filter;

public class ComparisonFilter extends Filter {

    /**
     * @return the property
     */
    public String getProperty() {
        return filter.getPropertyAsString("property");
    }

    /**
     * @param property
     *            the property to set
     */
    public void setProperty(String property) {
        filter.setProperty("property", property);
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return filter.getProperty("value");
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        filter.setProperty("value", value);
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(Double value) {
        filter.setProperty("value", value);
    }

    /**
     * @return the lowerBoundary
     */
    public Object getLowerBoundary() {
        return filter.getProperty("lowerBoundary");
    }

    /**
     * @param lowerBoundary
     *            the lowerBoundary to set
     */
    public void setLowerBoundary(String lowerBoundary) {
        filter.setProperty("lowerBoundary", lowerBoundary);
    }

    /**
     * @param lowerBoundary
     *            the lowerBoundary to set
     */
    public void setLowerBoundary(Double lowerBoundary) {
        filter.setProperty("lowerBoundary", lowerBoundary);
    }

    /**
     * @return the upperBoundary
     */
    public Object getUpperBoundary() {
        return filter.getProperty("upperBoundary");
    }

    /**
     * @param upperBoundary
     *            the upperBoundary to set
     */
    public void setUpperBoundary(String upperBoundary) {
        filter.setProperty("upperBoundary", upperBoundary);
    }

    /**
     * @param upperBoundary
     *            the upperBoundary to set
     */
    public void setUpperBoundary(Double upperBoundary) {
        filter.setProperty("upperBoundary", upperBoundary);
    }

    /**
     * @return the matchCase
     */
    public boolean getMatchCase() {
        return filter.getPropertyAsBoolean("matchCase");
    }

    /**
     * @param matchCase
     *            the matchCase to set
     */
    public void setMatchCase(Boolean matchCase) {
        filter.setProperty("matchCase", matchCase);
    }

}
