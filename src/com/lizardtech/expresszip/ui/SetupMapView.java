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
package com.lizardtech.expresszip.ui;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.vaadin.vol.Bounds;

import com.lizardtech.expresszip.model.ExpressZipLayer;

public interface SetupMapView {
	public void updateTree(List<ExpressZipLayer> chosenLayers, Set<String> supportedProjections);

	public void updateShapeLayer(ExpressZipLayer shapeLayer);

	public void setProjection(String epsg);

	public void setExportBounds(Bounds bounds);

	interface SetupMapViewListener {
		void shapeFileUploadedEvent(String filename, ByteArrayInputStream input);

		void shapeFileRemovedEvent();

		void projectionChangedEvent(String newProjection);

		void layerMovedEvent(Collection<ExpressZipLayer> layerNames);

		void layersSelectedEvent(Set<ExpressZipLayer> layers);

		void layerClickedEvent(ExpressZipLayer layer);

		void exportFieldsChanged(Bounds curRegion);

		void exportBoundsChanged(Bounds bounds);

	}

	public void addListener(SetupMapViewListener listener);

	public void removeListener(SetupMapViewListener listener);
}
