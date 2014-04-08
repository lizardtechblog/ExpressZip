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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.vaadin.vol.Area;
import org.vaadin.vol.Bounds;
import org.vaadin.vol.Point;
import org.vaadin.vol.Style;
import org.vaadin.vol.Vector;
import org.vaadin.vol.VectorLayer;

import com.lizardtech.expresszip.model.ExpressZipLayer;

public class BoundingBoxLayer extends VectorLayer {
	private static final long serialVersionUID = -8468810399148265741L;

	private HashMap<ExpressZipLayer, Vector> vectorHashMap;

	public BoundingBoxLayer() {
		vectorHashMap = new HashMap<ExpressZipLayer, Vector>();
		setSelectionMode(SelectionMode.NONE);
	}

	public void addBoundingBox(ExpressZipLayer layer, String mapcrs) {
		if (!vectorHashMap.containsKey(layer)) {
			Bounds bounds = layer.getBoundsForLayer(mapcrs);

			if (bounds != null) {

				double ulx = bounds.getLeft();
				double uly = bounds.getTop();
				double lrx = bounds.getRight();
				double lry = bounds.getBottom();

				// add bounding boxes as vectors
				Area box = new Area();
				Point[] coordinateList = new Point[] { new Point(ulx, uly), new Point(lrx, uly), new Point(lrx, lry),
						new Point(ulx, lry) };
				box.setPoints(coordinateList);
				box.setData(layer);

				// use different style for layers to export
				Style mystyle = new Style();
				mystyle.extendCoreStyle("selected");
				mystyle.setFillColor("#7640BB");
				if (layer.isChosen()) {
					mystyle.setStrokeColor("#D4BFF2");
					mystyle.setFillOpacity(0.5);
				} else {
					mystyle.setStrokeColor("#CECECE");
					mystyle.setFillOpacity(0);
				}
				box.setCustomStyle(mystyle);
				addVector(box);
				vectorHashMap.put(layer, box);
			}
		}
	}

	public void removeBoundingBox(ExpressZipLayer layer) {
		Vector box = vectorHashMap.get(layer);
		if (box != null) {
			removeComponent(box);
			vectorHashMap.remove(layer);
		}
	}

	public void removeAll(boolean ignoreChosenFlag) {
		List<ExpressZipLayer> layers = new ArrayList<ExpressZipLayer>(vectorHashMap.keySet());
		for (ExpressZipLayer l : layers) {
			if (ignoreChosenFlag || !l.isChosen()) {
				removeBoundingBox(l);
			}
		}
	}

}
