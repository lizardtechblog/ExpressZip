package org.vaadin.vol;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A helper class to reproduce Javascript Object on server side
 * 
 */
public class JsObject implements Serializable {
	private HashMap<String, Object> keyvalue;

	public JsObject() {
		keyvalue = new HashMap<String, Object>();
	}

	public void setProperty(String key, Object value) {
        keyvalue.put(key, value);
    }

	public String getPropertyAsString(String key) {
		return (String) keyvalue.get(key);
	}

	public int getPropertyAsInt(String key) {
		return (Integer) keyvalue.get(key);
	}

	public double getPropertyAsDouble(String key) {
		return (Double) keyvalue.get(key);
	}

	public float getPropertyAsFloat(String key) {
		return (Float) keyvalue.get(key);
	}

	public boolean getPropertyAsBoolean(String key) {
		return (Boolean) keyvalue.get(key);
	}

	public Object getProperty(String key) {
		return keyvalue.get(key);
	}

	public Map<String, Object> getKeyValueMap() {
		return keyvalue;
	}

	public String toStringJS() {
		String retValue = "{";

		for (String key : keyvalue.keySet()) {
			Object value = keyvalue.get(key);
			if (value instanceof JsObject) {
				retValue = retValue + key + " : "
						+ ((JsObject) value).toStringJS() + ",";
			} else if (value instanceof String) {
				retValue = retValue + key + " : " + value + ",";
			} else {
				retValue = retValue + key + " : " + value.toString() + ",";
			}
		}

		retValue = retValue.substring(0, retValue.length() - 1).concat("}");
		return retValue;
	}

	public int size() {
		return keyvalue.size();
	}

	/**
	 * @param key
	 * @return
	 * @see java.util.HashMap#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key) {
		return keyvalue.containsKey(key);
	}

	/**
	 * @return
	 * @see java.util.HashMap#keySet()
	 */
	public Set<String> keySet() {
		return keyvalue.keySet();
	}

	/**
	 * @return
	 * @see java.util.HashMap#keySet()
	 */
	public String[] keyArray() {
		int size = keyvalue.size();
		return (String[]) keyvalue.keySet().toArray(new String[size]);
	}
}
