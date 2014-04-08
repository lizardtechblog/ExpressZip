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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.vaadin.vol.Area;
import org.vaadin.vol.Bounds;
import org.vaadin.vol.OpenLayersMap;
import org.vaadin.vol.Point;
import org.vaadin.vol.PolyLine;
import org.vaadin.vol.Style;
import org.vaadin.vol.StyleMap;
import org.vaadin.vol.VectorLayer;

import com.lizardtech.expresszip.model.ExportProps;
import com.lizardtech.expresszip.model.ExpressZipLayer;
import com.lizardtech.expresszip.model.GriddingOptions;
import com.lizardtech.expresszip.model.GriddingOptions.GridMode;
import com.lizardtech.expresszip.ui.SetupMapView.SetupMapViewListener;
import com.vaadin.ui.Component;

public class ExtentSelectorLayer extends VectorLayer implements SetupMapView.SetupMapViewListener {
	private static final long serialVersionUID = 1L;

	private OpenLayersMap map;
	public GriddingLayer grid;
	private GriddingOptions options;
	public Bounds bounds;
	List<SetupMapViewListener> listeners;
	private Area area; // feature to hold coordinates
	private double ulx, uly, lrx, lry;

	public ExtentSelectorLayer(OpenLayersMap map, ExportProps props) {
		this.options = props.getGriddingOptions();
		this.map = map;
		assert (this.map != null);
		this.bounds = props.getExportRegion();

		listeners = new ArrayList<SetupMapViewListener>();

		Style mystyle = new Style();
		mystyle.extendCoreStyle("default");
		mystyle.setStrokeColor("#8AC840");
		mystyle.setStrokeWidth(4);
		mystyle.setFillOpacity(0);

		Style selectStyle = new Style();
		selectStyle.setStrokeWidth(5);
		selectStyle.setStrokeColor("#8AC840");

		StyleMap stylemap = new StyleMap(mystyle, selectStyle, selectStyle);
		stylemap.setExtendDefault(true);
		setStyleMap(stylemap);

		// add area feature and add it to the layer
		area = new Area();
		addComponent(area);

		setSelectionMode(SelectionMode.NONE);
		setDrawingMode(DrawingMode.RECTANGLE);

		addListener(new VectorModifiedListener() {
			@Override
			public void vectorModified(VectorModifiedEvent event) {

				Bounds bounds = new Bounds(event.getVector().getPoints());

				// TODO - only works in latlon
				refresh(bounds, false);
				refreshGridding();
				for (SetupMapViewListener listener : listeners)
					listener.exportBoundsChanged(bounds);
			};
		});

		refresh(bounds, true);
	}

	public void clear() {
		if (bounds != null) // if bounds is null, nothing is in the map
		{
			map.removeLayer(this);

			if (options != null && options.isGridding()) {
				grid.clear();
				map.removeLayer(grid);
			}
		}
	}

	public void addListener(SetupMapViewListener listener) {
		listeners.add(listener);
	}

	public void refresh(Bounds newBounds, boolean paint) {
		bounds = newBounds;

		ulx = newBounds.getMinLon();
		uly = newBounds.getMaxLat();
		lrx = newBounds.getMaxLon();
		lry = newBounds.getMinLat();

		Point[] coordinateList = new Point[] { new Point(ulx, uly), new Point(lrx, uly), new Point(lrx, lry), new Point(ulx, lry) };
		if (paint)
			area.setPoints(coordinateList);
		else
			area.setPointsWithoutRepaint(coordinateList);

	}

	public void refreshGridding(ExportProps props) {
		options = props.getGriddingOptions();
		refreshGridding();
	}

	private void refreshGridding() {
		Iterator<Component> iter = map.getComponentIterator();

		while (iter.hasNext()) {
			Component layer = (Component) iter.next();
			if (layer == grid) {
				grid.clear();
				map.removeLayer(grid);
				break;
			}
		}

		if (options != null && options.isGridding()) {

			grid = new GriddingLayer(bounds, options);
			map.addLayer(grid);
		}
	}

	@Override
	public void shapeFileUploadedEvent(String filename, ByteArrayInputStream input) {
	}

	@Override
	public void projectionChangedEvent(String newProjection) {

	}

	@Override
	public void layerMovedEvent(Collection<ExpressZipLayer> layerNames) {

	}

	@Override
	public void layerClickedEvent(ExpressZipLayer layer) {

	}

	@Override
	public void exportFieldsChanged(Bounds curRegion) {

		assert (curRegion != null);
		refresh(curRegion, true);
		redrawTransform(curRegion);
	}

	@Override
	public void exportBoundsChanged(Bounds b) {

	}

	private class GriddingLayer extends VectorLayer {
		private static final long serialVersionUID = -6082240174434583616L;

		private GriddingOptions options = null;
		private Bounds bounds = null;
		private StyleMap stylemap = null;
		private List<PolyLine> lines = new ArrayList<PolyLine>();

		private double cellSpacingX = 0.0;
		private double cellSpacingY = 0.0;

		public GriddingLayer(Bounds bounds, GriddingOptions options) {

			assert (bounds != null);
			this.bounds = bounds;
			assert (options != null);
			this.options = options;

			Style mystyle = new Style();
			mystyle.extendCoreStyle("default");
			mystyle.setStrokeColor("#DD0000");
			mystyle.setStrokeWidth(1);
			mystyle.setFillColor("#8F8F00");
			mystyle.setFillOpacity(0.1);

			Style selectStyle = new Style();
			selectStyle.setStrokeWidth(1);

			stylemap = new StyleMap(mystyle, selectStyle, null);
			stylemap.setExtendDefault(true);

			if (options.getGridMode() == GridMode.DIVISION)
				initializeDivisionGrid();
			else
				initializeMeterGrid();
			constructGridLines();

			setStyleMap(stylemap);
			setSelectionMode(SelectionMode.NONE);
		}

		private void constructGridLines() {
			long numrows = (long) (Math.abs(bounds.getTop() - bounds.getBottom()) / cellSpacingY);
			long numcols = (long) (Math.abs(bounds.getRight() - bounds.getLeft()) / cellSpacingX);
			if (numrows * numcols > ExportProps.MAX_TILES_PER_JOB)
				return;
			double ulx = bounds.getLeft() + cellSpacingX;
			double lrx = bounds.getRight();
			double uly = bounds.getTop();
			double lry = bounds.getBottom();

			// vertical grid lines
			while (ulx < lrx) {
				PolyLine vector = new PolyLine();
				vector.setPoints(new Point[] { new Point(ulx, uly), new Point(ulx, lry) });
				lines.add(vector);
				addComponent(vector);
				ulx += cellSpacingX;
			}

			ulx = bounds.getLeft();
			uly = bounds.getTop() - cellSpacingY;

			// horizontal grid lines
			while (uly > lry) {
				PolyLine vector = new PolyLine();
				vector.setPoints(new Point[] { new Point(ulx, uly), new Point(lrx, uly) });
				lines.add(vector);
				addComponent(vector);
				uly -= cellSpacingY;
			}
		}

		private void initializeDivisionGrid() {
			assert (options.getDivX() > 0.0 && options.getDivY() > 0.0);

			double boundsMaxX = Math.max(bounds.getLeft(), bounds.getRight());
			double boundsMinX = Math.min(bounds.getLeft(), bounds.getRight());
			double boundsMaxY = Math.max(bounds.getTop(), bounds.getBottom());
			double boundsMinY = Math.min(bounds.getTop(), bounds.getBottom());

			cellSpacingX = (boundsMaxX - boundsMinX) / options.getDivX();
			cellSpacingY = (boundsMaxY - boundsMinY) / options.getDivY();
		}

		private void initializeMeterGrid() {
			assert (options.getTileSizeX() > 0.0 && options.getTileSizeY() > 0.0);

			double widthRatio = (double) options.getExportWidth() / (double) options.getTileSizeX();
			double heightRatio = (double) options.getExportHeight() / (double) options.getTileSizeY();

			double max = Math.max(bounds.getLeft(), bounds.getRight());
			double min = Math.min(bounds.getLeft(), bounds.getRight());
			cellSpacingX = (max - min) / widthRatio;

			max = Math.max(bounds.getTop(), bounds.getBottom());
			min = Math.min(bounds.getTop(), bounds.getBottom());
			cellSpacingY = (max - min) / heightRatio;
		}

		public void clear() {
			int numLines = lines.size();
			for (int i = 0; i < numLines; ++i)
				map.removeComponent(lines.get(i));
		}

	}

	@Override
	public void shapeFileRemovedEvent() {
		// Only used for the single cropping shapefile
	}

	@Override
	public void layersSelectedEvent(Set<ExpressZipLayer> layers) {
		// TODO Auto-generated method stub

	}

}
