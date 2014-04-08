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

import java.util.HashSet;
import java.util.Set;

import org.geotools.data.ows.CRSEnvelope;
import org.geotools.data.ows.Layer;
import org.vaadin.vol.AbstractLayerBase;
import org.vaadin.vol.Bounds;
import org.vaadin.vol.Point;

public class BaseLayer extends ExpressZipLayer {

	public BaseLayer(String name, AbstractLayerBase layer, String projection) {
		super(name, null);
		CRSEnvelope bbox = null;
		if (projection.equals(MapModel.EPSG3857)) {
			bbox = new CRSEnvelope(projection, -20037508.34, -20037508.34, 20037508.34, 20037508.34);
			bbox.setResX(2);
			bbox.setResY(2);
		} else if (projection.equals(MapModel.EPSG4326)) {
			bbox = new CRSEnvelope(projection, -180, -90, 180, 90);
			bbox.setResX(.008333333);
			bbox.setResY(.008333333);
		} else
			throw new IllegalArgumentException("unsupported projection in worldwide basemap");
		geoToolsLayer = new Layer();
		geoToolsLayer.setBoundingBoxes(bbox);
		onlyProjection = projection;
		vaadinLayer = layer;
	}

	@Override
	protected void addChild(Layer child) {
	}

	@Override
	protected AbstractLayerBase createVaadinLayer(String epsgCode) {
		if (epsgCode.equals(onlyProjection))
			return vaadinLayer;
		return null;
	}

	@Override
	public Bounds getBoundsForLayer(String proj) {
		if (proj.equals(onlyProjection)) {
			if (proj.equals(MapModel.EPSG3857))
				return new Bounds(new Point(-20037508.34, -20037508.34), new Point(20037508.34, 20037508.34));
			if (proj.equals(MapModel.EPSG4326))
				return new Bounds(new Point(-180, -90), new Point(180, 90));
		}
		return null;
	}

	@Override
	public Set<String> getSupportedProjections() {
		HashSet<String> supportedProjections = new HashSet<String>(1);
		supportedProjections.add(onlyProjection);
		return supportedProjections;
	}

	private String onlyProjection;
	private AbstractLayerBase vaadinLayer;
}
