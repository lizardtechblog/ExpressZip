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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.vaadin.vol.Bounds;
import org.vaadin.vol.Layer;
import org.vaadin.vol.OpenLayersMap;
import org.vaadin.vol.OpenLayersMap.ExtentChangeEvent;
import org.vaadin.vol.OpenLayersMap.ExtentChangeListener;
import org.vaadin.vol.WebMapServiceLayer;

import com.lizardtech.expresszip.model.ExportProps;
import com.lizardtech.expresszip.model.ExpressZipLayer;
import com.lizardtech.expresszip.model.MapModel;
import com.lizardtech.expresszip.model.SetupMapModel;
import com.lizardtech.expresszip.model.VectorLayer;
import com.lizardtech.expresszip.ui.ExportOptionsView.ExportOptionsViewListener;
import com.lizardtech.expresszip.vaadin.ExpressZipWindow;
import com.lizardtech.expresszip.vaadin.OpenLayersMapViewComponent;
import com.lizardtech.expresszip.vaadin.SetupMapViewComponent;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

public class SetupMapPresenter implements SetupMapView.SetupMapViewListener, ExportOptionsViewListener, ExtentChangeListener {

	private SetupMapModel setupMapModel;
	private SetupMapViewComponent setupMapView;
	private OpenLayersMapViewComponent mapView;
	private MapModel mapModel;
	private ExtentSelectorLayer extentLayer;
	private OpenLayersMap map;
	private Bounds bounds;
	private VectorLayer shapeFileLayer;
	private BoundingBoxLayer boundingBoxLayer;
	private List<ExpressZipLayer> chosenLayers;
	private String includedLocalBase;
	private boolean firstEntry = true;
	private int zoomLevel;

	public SetupMapPresenter(SetupMapModel model, SetupMapViewComponent view, OpenLayersMapViewComponent mapView, MapModel mapModel) {
		this.setupMapModel = model;
		this.setupMapView = view;
		this.mapView = mapView;
		setupMapView.addListener(this);

		this.mapModel = mapModel;

		bounds = null;
		zoomLevel = 2;

		boundingBoxLayer = new BoundingBoxLayer();
	}

	public boolean paneEntered() {
		ExportProps props = ((ExpressZipWindow) mapView.getApplication().getMainWindow()).getExportProps();

		List<ExpressZipLayer> newChosenLayers = mapModel.getChosenLayers();
		Set<ExpressZipLayer> newChosenLayerSet = new HashSet<ExpressZipLayer>(newChosenLayers);
		boolean layersChanged = (chosenLayers == null || !newChosenLayerSet.equals(new HashSet<ExpressZipLayer>(chosenLayers)));
		boolean baseChanged = (mapModel.getIncludedLocalBaseMap() != includedLocalBase);
		if (baseChanged || layersChanged) {
			chosenLayers = newChosenLayers;
			includedLocalBase = mapModel.getIncludedLocalBaseMap();

			HashSet<String> supportedProjections = new HashSet<String>();
			if (includedLocalBase != null) {
				for (ExpressZipLayer l : mapModel.getLocalBaseLayers()) {
					if (l.getName().equals(includedLocalBase)) {
						supportedProjections.addAll(l.getSupportedProjections());
						break;
					}
				}
			} else {
				// get all unique projections for the layers
				for (ExpressZipLayer l : chosenLayers) {
					supportedProjections.addAll(l.getSupportedProjections());
				}
				// keep the intersection of projections
				for (ExpressZipLayer l : chosenLayers) {
					supportedProjections.retainAll(l.getSupportedProjections());
				}
			}
			supportedProjections.remove(""); // GeoTools call may return a null string

			if (supportedProjections.size() == 0) {
				mapView.getApplication()
						.getMainWindow()
						.showNotification("Incompatible layers",
								"The current layers have no Coordinate Reference System in common.",
								Notification.TYPE_ERROR_MESSAGE);
				chosenLayers = null;
				return false;
			}

			setupMapView.updateTree(chosenLayers, supportedProjections);
			bounds = MapModel.getBoundsForLayers(chosenLayers, props.getMapProjection());
			firstEntry = true;
		}

		if (firstEntry || baseChanged)
			projectionChangedEvent(props.getMapProjection());

		mapModel.setMap(map, props.getMapProjection());
		
		if(shapeFileLayer != null) {
			mapModel.addVectorLayer(shapeFileLayer);
		}
		mapModel.updateOpenLayersMap();
		mapModel.updateOrder(chosenLayers);

		if (baseChanged || layersChanged) {
			map.zoomToExtent(bounds);
			zoomLevel = map.getZoom();
		} else {
			map.setCenter(bounds);
			map.setZoom(zoomLevel);
		}

		if (extentLayer != null)
			map.removeLayer(extentLayer);

		if (firstEntry)
			props.setExportRegion(bounds);

		resetExtentLayer(props.getExportRegion(), this);

		map.addLayer(boundingBoxLayer);

		mapView.displayMap(map);

		if (firstEntry) {
			firstEntry = false;
			setupMapView.setExportBounds(bounds);
		}

		return true;
	}

	public void paneExited() {
		if (map != null) {
			bounds = map.getExtend();
			zoomLevel = map.getZoom();

			if (boundingBoxLayer != null)
				map.removeLayer(boundingBoxLayer);
			map.removeLayer(extentLayer);
		}
	}

	// SetupMapViewListener implementations
	@Override
	public void shapeFileUploadedEvent(final String filename, final ByteArrayInputStream input) {
		final Window modal = new Window("Wait");
		final Window mainWindow = (ExpressZipWindow) setupMapView.getApplication().getMainWindow();
		final SetupMapPresenter presenter = this;

		Thread spinner = new Thread(new Runnable() {
			public void run() {
				ProgressIndicator pi = new ProgressIndicator();
				pi.setCaption("Processing Shapefile...");
				modal.setModal(true);
				modal.setClosable(false);
				modal.setResizable(false);
				modal.getContent().setSizeUndefined(); // trick to size around content
				modal.getContent().addComponent(pi);
				modal.setWidth(modal.getWidth(), modal.getWidthUnits());
				mainWindow.addWindow(modal);
				VectorLayer uploadedShapeFile = setupMapModel.shapeFileUploaded(filename, input);
				if (uploadedShapeFile != null) {
					shapeFileLayer = uploadedShapeFile;
					mapModel.addVectorLayer(shapeFileLayer);
					setupMapView.updateShapeLayer(shapeFileLayer);
					mapModel.updateOpenLayersMap();

					Bounds shpFileBounds = shapeFileLayer.getBoundsForLayer(mapModel.getCurrentProjection());
					resetExtentLayer(shpFileBounds, presenter);
					map.addLayer(boundingBoxLayer);
				}
				mainWindow.removeWindow(modal);
			}
		});
		spinner.start();
	}

	@Override
	public void shapeFileRemovedEvent() {
		shapeFileLayer = null;
		mapModel.removeVectorLayer();
		setupMapView.updateShapeLayer(shapeFileLayer);
	}

	@Override
	public void projectionChangedEvent(String newProjection) {

		// user switched the projection.
		ExportProps props = ((ExpressZipWindow) mapView.getApplication().getMainWindow()).getExportProps();

		// the old export region which we will need to reprojected
		Bounds newExportRegion = MapModel.reprojectBounds(props.getExportRegion(), props.getMapProjection(), newProjection);

		// reproject map bounds
		bounds = MapModel.reprojectBounds(bounds, props.getMapProjection(), newProjection);

		props.setMapProjection(newProjection);

		List<ExpressZipLayer> supportedLayers = new LinkedList<ExpressZipLayer>();
		List<ExpressZipLayer> unsupportedLayers = new LinkedList<ExpressZipLayer>();

		// determine which of the enabled layers support this new projection
		for (ExpressZipLayer layer : chosenLayers) {
			if (layer.projectionIsSupported(newProjection))
				supportedLayers.add(layer);
			else
				unsupportedLayers.add(layer);
		}

		if (!supportedLayers.isEmpty()) {
			Bounds maxBounds = MapModel.getBoundsForLayers(supportedLayers, newProjection);

			String includedBaseLayer = mapModel.getIncludedLocalBaseMap();
			ExpressZipLayer baseLayer = null;
			if (includedBaseLayer != null) {
				for (ExpressZipLayer l : mapModel.getLayers()) {
					if (l.getName().equals(includedBaseLayer)) {
						baseLayer = l;
						break;
					}
				}
			}

			map = MapModel.createOpenLayersMap(baseLayer, newProjection, supportedLayers, this);

			if (null == bounds) {
				bounds = maxBounds;
			}
			if (null == newExportRegion) {
				newExportRegion = maxBounds;
			}

			if (baseLayer != null) {
				Layer baseLayerV = baseLayer.getVaadinLayer(newProjection);
				if (baseLayerV instanceof WebMapServiceLayer)
					((WebMapServiceLayer) baseLayerV).setBaseLayer(true);
				map.addLayer(baseLayerV);
				map.setBaseLayer(baseLayerV);
			}
		}

		mapModel.setMap(map, props.getMapProjection());
		mapModel.updateOpenLayersMap();
		mapModel.updateOrder(chosenLayers);
		map.zoomToExtent(bounds);

		exportBoundsChanged(newExportRegion);
		resetExtentLayer(props.getExportRegion(), this);
		map.addLayer(boundingBoxLayer);

		mapView.displayMap(map);

	}

	@Override
	public void layerClickedEvent(ExpressZipLayer layer) {
		map.zoomToExtent(MapModel.getBoundsForLayer(layer, mapModel.getCurrentProjection()));
	}

	@Override
	public void layerMovedEvent(Collection<ExpressZipLayer> reorderedLayerNames) {
		chosenLayers = new ArrayList<ExpressZipLayer>(reorderedLayerNames);
		mapModel.updateOrder(chosenLayers);
	}

	@Override
	public void exportFieldsChanged(Bounds b) {
	}

	@Override
	public void exportBoundsChanged(Bounds b) {
		((ExpressZipWindow) mapView.getApplication().getMainWindow()).getExportProps().setExportRegion(b);
		setupMapView.setExportBounds(b);
	}

	@Override
	public void submitJobEvent(ExportProps param) {
	}

	@Override
	public void updateGridding(ExportProps props) {
		extentLayer.refreshGridding(props);
	}

	@Override
	public void updateWidthAndResFromHeight(int height) {
	}

	@Override
	public void updateHeightAndResFromWidth(int width) {
	}

	@Override
	public void updateWidthAndHeightFromRes(Double groundResolution) {
	}

	@Override
	public void layersSelectedEvent(Set<ExpressZipLayer> layers) {
		boundingBoxLayer.removeAll(true);
		for (ExpressZipLayer l : layers) {
			boundingBoxLayer.addBoundingBox(l, mapModel.getCurrentProjection());
		}

	}

	@Override
	public void extentChanged(ExtentChangeEvent event) {
		bounds = map.getExtend();
	}

	/**
	 * Removes, reconfigures and add the extent selector
	 * 
	 * @param bounds
	 *            requested bounds of the new extent. Needs to be within bounds of layers to be used.
	 * @param presenter
	 *            listener to pass to the extentSelector
	 */
	private void resetExtentLayer(Bounds bounds, SetupMapPresenter presenter) {
		if (extentLayer != null) {
			map.removeLayer(extentLayer);
			setupMapView.removeListener(extentLayer);
		}

		// ensure bounds are within our layers
		Bounds maxBounds = mapModel.getBoundsForLayers(mapModel.getChosenLayers());
		if (bounds.getLeft() > maxBounds.getLeft() && bounds.getRight() < maxBounds.getRight()
				&& bounds.getBottom() > maxBounds.getBottom() && bounds.getTop() < maxBounds.getTop()) {

			exportBoundsChanged(bounds);
		}

		ExportProps props = ((ExpressZipWindow) mapView.getApplication().getMainWindow()).getExportProps();

		extentLayer = new ExtentSelectorLayer(map, props);
		extentLayer.addListener(presenter);
		extentLayer.refreshGridding(props);
		setupMapView.addListener(extentLayer);

		map.addLayer(extentLayer);
		map.addExtentPanel(extentLayer);
		map.activateExtentTransform();
	}
}
