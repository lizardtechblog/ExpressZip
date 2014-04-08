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

import org.geotools.data.ows.CRSEnvelope;
import org.geotools.data.ows.Layer;
import org.vaadin.vol.AbstractLayerBase;
import org.vaadin.vol.Bounds;
import org.vaadin.vol.WebMapServiceLayer;

public class RasterLayer extends ExpressZipLayer {
	public RasterLayer(String name, org.geotools.data.ows.Layer layer, RasterLayer parent) {
		super(name, layer);
		this.parent = parent;
	}

	@Override
	public AbstractLayerBase createVaadinLayer(String projection) {

		WebMapServiceLayer wmsLayer = new WebMapServiceLayer();
		wmsLayer.setDisplayName(getName());
		wmsLayer.setBaseLayer(false);
		wmsLayer.setUri(MapModel.getPublicServerURL().toString());
		wmsLayer.setProjection(projection);
		wmsLayer.setLayers(getName());
		wmsLayer.setFormat("image/png");
		wmsLayer.setImmediate(true);
		return wmsLayer;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	protected void addChild(Layer child) {
		RasterLayer czsChild = new RasterLayer(child.getName(), child, this);
		children.add(czsChild);
	}

	@Override
	public Bounds getBoundsForLayer(String proj) {
		Bounds bounds = new Bounds();

		CRSEnvelope envelope = getGeoToolsLayer().getBoundingBoxes().get(proj);
		if (envelope == null) {
			bounds = getProjectedBoundsForLayer(proj);
		} else {
			bounds.setTop(envelope.getMaxY());
			bounds.setBottom(envelope.getMinY());
			bounds.setLeft(envelope.getMinX());
			bounds.setRight(envelope.getMaxX());
		}
		return bounds;
	}

	public Bounds getProjectedBoundsForLayer(String projection) {
		CRSEnvelope latLonEnvelope = getGeoToolsLayer().getLatLonBoundingBox();
		if (latLonEnvelope != null) {
			Bounds inBounds = new Bounds();
			inBounds.setTop(latLonEnvelope.getMinY());
			inBounds.setBottom(latLonEnvelope.getMaxY());
			inBounds.setLeft(latLonEnvelope.getMinX());
			inBounds.setRight(latLonEnvelope.getMaxX());
			return MapModel.reprojectBounds(inBounds, MapModel.EPSG4326, projection);
		}
		return null;
	}

}
