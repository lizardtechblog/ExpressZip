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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import org.opengis.geometry.BoundingBox;
import org.vaadin.vol.Bounds;

import com.lizardtech.expresszip.vaadin.ExpressZipWindow;
import com.vaadin.ui.Component;

public class Filter implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Keep a list so that the vaadin app doesnt know about the filters, doesnt
	// need to
	public static AxisFilters[] axisArray = new AxisFilters[] { AxisFilters.LAYER_NAME, AxisFilters.KEYWORD, AxisFilters.LOCATION,
			AxisFilters.SRS };
	private ArrayList<FilterObject> filterList = new ArrayList<FilterObject>();
	Component parent;

	public Filter(Component parent) {
		this.parent = parent;
	}

	public enum AxisFilters {
		LAYER_NAME, KEYWORD, LOCATION, SRS
	}

	// This will return a list of filtered layers -- based on filters added
	public void filterLayers(MapModel mm) {

		mm.setAllLayersFilterAllows();

		// always filter on visible extent, then add filters
		filterOnExtent(mm);

		for (FilterObject f : filterList) {
			switch (f.filterAxis) {
			case KEYWORD:
				filterOnKeyword(f, mm);
				break;
			case LAYER_NAME:
				filterOnName(f, mm);
				break;
			case LOCATION:
				filterOnLocation(f, mm);
				break;
			case SRS:
				filterOnSRS(f, mm);
				break;
			}
		}

		return;
	}

	private void filterOnExtent(MapModel mm) {
		for (ExpressZipLayer l : mm.getLayers()) {
			boolean intersects = layerIntersectBounds(l, mm.getVisibleExtent(), mm.getCurrentProjection());
			l.setFilterAllows(l.isChosen() || intersects);
		}
	}

	private void filterOnLocation(FilterObject fo, MapModel mm) {
		String op = fo.getModifier();
		Bounds locationBounds = OSMGeocode.getInstance().getBounds(fo.getField(), mm.getCurrentProjection(), mm.getVisibleExtent());
		if (locationBounds == null) {
			// Couldn't find the location
			((ExpressZipWindow) parent.getApplication().getMainWindow()).showNotification("Could not find: " + fo.getField());
			return;
		}
		// System.out.println(locationBounds.toString());

		for (ExpressZipLayer l : mm.getLayers()) {
			if (op.equals("inside")) {
				if (!layerWithinBounds(l, locationBounds)) {
					l.setFilterAllows(false);
				}
			} else if (op.equals("outside")) {
				if (layerWithinBounds(l, locationBounds)) {
					l.setFilterAllows(false);
				}
			}
		}
	}

	/**
	 * @param layer
	 *            the layer we wish to see if it falls within bounds
	 * @param latlonBounds
	 *            bounds that is the superset we are checking if layer falls in
	 * @return true if yes, false otherwise
	 */
	private boolean layerWithinBounds(ExpressZipLayer l, Bounds latlonBounds) {
		BoundingBox layerEnv = MapModel.getBoundingBox(l.getBoundsForLayer(MapModel.EPSG4326), MapModel.EPSG4326);
		BoundingBox refEnv = MapModel.getBoundingBox(latlonBounds, MapModel.EPSG4326);
		return refEnv.contains(layerEnv);
	}

	/**
	 * 
	 * @param l
	 *            layer to intersect
	 * @param bounds
	 *            to intersect
	 * @param proj
	 *            projection system to use
	 * @return
	 */
	private boolean layerIntersectBounds(ExpressZipLayer l, Bounds bounds, String proj) {
		BoundingBox layerEnv = MapModel.getBoundingBox(l.getBoundsForLayer(proj), proj);
		BoundingBox refEnv = MapModel.getBoundingBox(bounds, proj);
		return refEnv.intersects(layerEnv);
	}

	private void filterOnSRS(FilterObject fo, MapModel mm) {
		String field = fo.getField();
		layerloop: for (ExpressZipLayer l : mm.getLayers()) {
			Set<String> srsSet;

			// If current SRS is null; get parent SRS instead (since that
			// encompasses this layer)
			if (l.getGeoToolsLayer().getSrs() == null) {
				srsSet = l.getGeoToolsLayer().getParent().getSrs();
			} else {
				srsSet = l.getGeoToolsLayer().getSrs();
			}

			if (srsSet == null) // TODO: do better job of finding the layer's supported SRSs
				continue;

			for (String srs : srsSet)
				if (srs.matches("(?i).*" + field + ".*"))
					continue layerloop;
			l.setFilterAllows(false);
		}
	}

	private void filterOnKeyword(FilterObject fo, MapModel mm) {
		String mod = fo.getModifier();
		String field = fo.getField();

		if (mod.equals("contains")) {
			layerloop: for (ExpressZipLayer l : mm.getLayers()) {
				// If enabled; do not filter on this layer and just continue.
				if (l.isChosen() || l == mm.getRootRasterLayer())
					continue;
				if (l.getGeoToolsLayer().getKeywords() != null)
					for (String keyword : l.getGeoToolsLayer().getKeywords())
						if (keyword.matches("(?i).*" + field + ".*"))
							continue layerloop;
				l.setFilterAllows(false);
			}
		} else if (mod.equals("starts with")) {
			layerloop: for (ExpressZipLayer l : mm.getLayers()) {

				// If enabled; do not filter on this layer and just continue.
				if (l.isChosen() || l == mm.getRootRasterLayer())
					continue;
				if (l.getGeoToolsLayer().getKeywords() != null)
					for (String keyword : l.getGeoToolsLayer().getKeywords())
						if (keyword.toLowerCase().startsWith(field.toLowerCase()))
							continue layerloop;
				l.setFilterAllows(false);
			}
		} else if (mod.equals("ends with")) {
			layerloop: for (ExpressZipLayer l : mm.getLayers()) {

				// If enabled; do not filter on this layer and just continue.
				if (l.isChosen() || l == mm.getRootRasterLayer())
					continue;
				if (l.getGeoToolsLayer().getKeywords() != null)
					for (String keyword : l.getGeoToolsLayer().getKeywords())
						if (keyword.toLowerCase().endsWith(field.toLowerCase()))
							continue layerloop;
				l.setFilterAllows(false);
			}
		} else {
			// Does not contain
			for (ExpressZipLayer l : mm.getLayers()) {

				// If enabled; do not filter on this layer and just continue.
				if (l.isChosen() || l == mm.getRootRasterLayer())
					continue;
				if (l.getGeoToolsLayer().getKeywords() == null)
					continue;
				for (String keyword : l.getGeoToolsLayer().getKeywords()) {
					if (keyword.matches("(?i).*" + field + ".*")) {
						l.setFilterAllows(false);
						break;
					}
				}
			}
		}
	}

	private void filterOnName(FilterObject fo, MapModel mm) {
		String mod = fo.getModifier();
		String field = fo.getField();
		if (mod.equals("contains")) {
			for (ExpressZipLayer l : mm.getLayers()) {
				// If enabled; do not filter on this layer and just continue.
				if (l.isChosen() || l == mm.getRootRasterLayer()) {
					continue;
				}

				if (!l.getName().matches("(?i).*" + field + ".*")) {
					l.setFilterAllows(false);
				}
			}
		} else if (mod.equals("starts with")) {
			for (ExpressZipLayer l : mm.getLayers()) {

				// If enabled; do not filter on this layer and just continue.
				if (l.isChosen() || l == mm.getRootRasterLayer()) {
					continue;
				}

				if (!l.getGeoToolsLayer().getName().toLowerCase().startsWith(field.toLowerCase())) {
					l.setFilterAllows(false);
				}
			}
		} else if (mod.equals("ends with")) {
			for (ExpressZipLayer l : mm.getLayers()) {

				// If enabled; do not filter on this layer and just continue.
				if (l.isChosen() || l == mm.getRootRasterLayer()) {
					continue;
				}

				if (!l.getGeoToolsLayer().getName().toLowerCase().endsWith(field.toLowerCase())) {
					l.setFilterAllows(false);
				}
			}
		} else {
			// Does not contain
			for (ExpressZipLayer l : mm.getLayers()) {

				// If enabled; do not filter on this layer and just continue.
				if (l.isChosen() || l == mm.getRootRasterLayer()) {
					continue;
				}

				if (l.getGeoToolsLayer().getName().matches("(?i).*" + field + ".*")) {
					l.setFilterAllows(false);
				}
			}
		}
	}

	public void addFilter(FilterObject in) {
		filterList.add(in);
	}

	public void delFilter(FilterObject in, MapModel mm) {
		filterList.remove(in);
		mm.setAllLayersFilterAllows();
	}

	public String getNameOfFilter(AxisFilters in) {
		switch (in) {
		case LAYER_NAME:
			return "Layer Name";
		case KEYWORD:
			return "Keyword";
		case LOCATION:
			return "Location";
		case SRS:
			return "SRS";
		default:
			return null;
		}
	}

	public AxisFilters getFilterByName(String in) {
		for (AxisFilters f : axisArray) {
			if (in.equals(getNameOfFilter(f)))
				return f;
		}
		return null;
	}

	/*
	 * This class will take care of handling the actual filter objects for us with all the required fields needed to filter (for
	 * right now)
	 */
	public static class FilterObject {
		private AxisFilters filterAxis; // LAYER_NAME, SRS, KEYWORD, ...
		private String modifier; // contains, does not contain, less than, ...
		private String field; // e.g. Washington

		public FilterObject(AxisFilters axis_in, String mod_in, String field_in) {
			filterAxis = axis_in;
			modifier = mod_in;
			field = field_in;
			System.out.printf("Debug: New FilterObject created! filterAxis = %s, modifer = %s, field = %s\n",
					filterAxis.toString(), modifier, field);
		}

		public FilterObject(AxisFilters axis_in, String field_in) {
			filterAxis = axis_in;
			modifier = null;
			field = field_in;
			System.out.printf("Debug: New FilterObject created! filterAxis = %s, modifer = null, field = %s\n",
					filterAxis.toString(), field);
		}

		public AxisFilters getFilterAxis() {
			return filterAxis;
		}

		public String getModifier() {
			return modifier;
		}

		public String getField() {
			return field;
		}
	}
}
