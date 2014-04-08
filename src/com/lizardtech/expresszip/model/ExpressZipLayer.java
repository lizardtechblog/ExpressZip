/*******************************************************************************
 * Copyright 2014 Celartem, Inc., dba LizardTech
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.lizardtech.expresszip.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.vaadin.vol.AbstractLayerBase;
import org.vaadin.vol.Bounds;

// base class for raster/vector layers
public abstract class ExpressZipLayer {

	private boolean chosen;
	private boolean rendered;
	private int index;
	private boolean filterAllows;
	protected org.geotools.data.ows.Layer geoToolsLayer;
	private String name;
	protected List<ExpressZipLayer> children;
	protected ExpressZipLayer parent;

	public ExpressZipLayer(String name, org.geotools.data.ows.Layer gtl) {
		super();
		geoToolsLayer = gtl;
		this.name = name;
		chosen = false;
		filterAllows = true;
		parent = null;
		index = -1;
		
		children = new ArrayList<ExpressZipLayer>();
		if (geoToolsLayer != null) {
			geoToolsLayer.setTitle(name);
			for (org.geotools.data.ows.Layer l : geoToolsLayer.getLayerChildren()) {
				addChild(l);
			}
		}
	}

	abstract protected void addChild(org.geotools.data.ows.Layer child);

	public void getFilteredLayers(List<ExpressZipLayer> list) {
		if (filterAllows()) {
			list.add(this);
		}
		for (ExpressZipLayer child : getChildren()) {
			child.getFilteredLayers(list);
		}
	}

	public Set<String> getSupportedProjections() {
		if (geoToolsLayer != null) {
			return geoToolsLayer.getSrs();
		} else {
			return new HashSet<String>();
		}
	}

	public boolean projectionIsSupported(String epsg) {
		Set<String> supportedProjections = getSupportedProjections();
		for (String projection : supportedProjections)
			if (projection.equals(epsg))
				return true;
		return false;
	}

	private HashMap<String, org.vaadin.vol.AbstractLayerBase> mapProjectedVaadinLayers = new HashMap<String, org.vaadin.vol.AbstractLayerBase>();

	public org.vaadin.vol.AbstractLayerBase getVaadinLayer(String projection) {
		org.vaadin.vol.AbstractLayerBase vaadinLayer = mapProjectedVaadinLayers.get(projection);

		if (null == vaadinLayer) {
			vaadinLayer = createVaadinLayer(projection);
			mapProjectedVaadinLayers.put(projection, vaadinLayer);
		}
		return vaadinLayer;
	}

	abstract protected AbstractLayerBase createVaadinLayer(String epsgCode);

	abstract public Bounds getBoundsForLayer(String proj);

	public boolean isChosen() {
		return chosen;
	}

	public void setChosen(boolean chosen) {
		this.chosen = chosen;

		if (chosen) {
			for (AbstractLayerBase vaadinLayer : mapProjectedVaadinLayers.values()) {
				vaadinLayer.setOLVisibility(true);
			}
		}
	}

	public boolean isRendered() {
		return rendered;
	}

	public void setRendered(boolean rendered) {
		this.rendered = rendered;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void getLayerList(List<ExpressZipLayer> list) {
		for (ExpressZipLayer child : getChildren()) {
			list.add(child);
			child.getLayerList(list);
		}
	}

	public boolean filterAllows() {
		return filterAllows;
	}

	public void setFilterAllows(boolean filterAllows) {
		this.filterAllows = filterAllows;
	}

	public org.geotools.data.ows.Layer getGeoToolsLayer() {
		return geoToolsLayer;
	}

	public List<ExpressZipLayer> getChildren() {
		return children;
	}

	public ExpressZipLayer getParent() {
		return parent;
	}

	@Override
	public String toString() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}


}
