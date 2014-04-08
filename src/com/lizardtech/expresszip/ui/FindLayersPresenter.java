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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.vaadin.vol.AbstractLayerBase;
import org.vaadin.vol.Bounds;
import org.vaadin.vol.OpenLayersMap;
import org.vaadin.vol.OpenLayersMap.ExtentChangeEvent;
import org.vaadin.vol.OpenLayersMap.ExtentChangeListener;
import org.vaadin.vol.PointVector;
import org.vaadin.vol.Popup;
import org.vaadin.vol.ProjectedLayer;
import org.vaadin.vol.Vector;
import org.vaadin.vol.VectorLayer;
import org.vaadin.vol.VectorLayer.SelectionMode;
import org.vaadin.vol.VectorLayer.VectorUnSelectedEvent;
import org.vaadin.vol.WebMapServiceLayer;

import com.lizardtech.expresszip.model.ExpressZipLayer;
import com.lizardtech.expresszip.model.MapModel;
import com.lizardtech.expresszip.vaadin.FindLayersViewComponent;
import com.lizardtech.expresszip.vaadin.OpenLayersMapViewComponent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

public class FindLayersPresenter implements FindLayersView.FindLayersViewListener, ExtentChangeListener, Serializable {
	private static final long serialVersionUID = 9186611991637464890L;

	OpenLayersMapViewComponent mapView;
	FindLayersViewComponent findLayersView;
	private OpenLayersMap findLayersMap;
	private MapModel mapModel;
	private Bounds bounds;
	private Popup popup;
	private VectorLayer clusterLayer;
	private BoundingBoxLayer boundingBoxLayer;
	private List<ExpressZipLayer> renderedLayers;
	private int zoomLevel;
	private ProjectedLayer baseLayer;

	public FindLayersPresenter(FindLayersViewComponent view, OpenLayersMapViewComponent mapView, MapModel mapModel) {
		this.mapModel = mapModel;
		this.findLayersView = view;
		this.mapView = mapView;
		renderedLayers = new ArrayList<ExpressZipLayer>();
		zoomLevel = 2;

		baseLayer = mapModel.getBaseLayers().get(0);

		findLayersView.setupMap(mapModel);
		findLayersView.addListener(this);
	}

	@Override
	public void baseMapChanged(ProjectedLayer newBaseLayer) {
		findLayersMap = null;

		if (mapModel.getBaseLayers().contains(newBaseLayer)) {
			if (newBaseLayer instanceof WebMapServiceLayer) {
				String baseLayerName = ((WebMapServiceLayer) newBaseLayer).getDisplayName();
				if (baseLayerName != null) {
					for (ExpressZipLayer l : mapModel.getLocalBaseLayers()) {
						if (l.getName().equals(baseLayerName)) {
							findLayersMap = MapModel.createOpenLayersMap(l, newBaseLayer.getProjection(), mapModel.getLayers(),
									this);
							break;
						}
					}
				}
			}
		}
		
		if (null == findLayersMap) {
			findLayersMap = MapModel.createOpenLayersMap((AbstractLayerBase) newBaseLayer, newBaseLayer.getProjection(),
					mapModel.getLayers(), this);
		}

		mapModel.setMap(findLayersMap, newBaseLayer.getProjection());

		findLayersMap.addLayer(newBaseLayer);
		findLayersMap.setBaseLayer(newBaseLayer);

		for (ExpressZipLayer rendered : mapModel.getRenderedLayers()) {
			mapModel.addLayerToMap(rendered);
		}

		if (bounds != null) {
			if (baseLayer != newBaseLayer) {
				bounds = MapModel.reprojectBounds(bounds, baseLayer.getProjection(), newBaseLayer.getProjection());
				if (null != bounds) {// projection may have failed
					bounds = MapModel.getBoundsForLayers(mapModel.getFilteredLayers(), newBaseLayer.getProjection());
					findLayersMap.zoomToExtent(bounds);
				} else
					findLayersMap.setZoom(zoomLevel);
			} else {
				findLayersMap.setCenter(bounds);
				findLayersMap.setZoom(zoomLevel);
			}
		} else {
			bounds = MapModel.getBoundsForLayers(mapModel.getFilteredLayers(), newBaseLayer.getProjection());
			findLayersMap.zoomToExtent(bounds);
		}

		baseLayer = newBaseLayer;

		addBoundingBoxLayer();
		addClusteredVectorLayer();

		mapView.displayMap(findLayersMap);

		if (bounds != null) {
			findLayersMap.zoomToExtent(bounds);
		}
	}

	public void paneEntered() {
		mapModel.removeVectorLayer();
		baseMapChanged(baseLayer);
	}

	public void paneExited() {
		if (null != popup) {
			findLayersMap.removeComponent(popup);
		}
		bounds = findLayersMap.getExtend();
		zoomLevel = findLayersMap.getZoom();
		findLayersMap.removeAllComponents();
		findLayersMap = null;
	}

	// FindLayersViewListener implementations

	@Override
	public void filtersChangedEvent() {
		if (clusterLayer != null) {
			findLayersMap.removeLayer(clusterLayer);
			clusterLayer.recluster(false);
			clusterLayer.removeAllComponents();

			for (ExpressZipLayer layer : mapModel.getFilteredLayers()) {
				Bounds bounds = layer.getBoundsForLayer(findLayersMap.getApiProjection());
				if (null != bounds) {
					double x = (bounds.getLeft() + bounds.getRight()) / 2.0d;
					double y = (bounds.getTop() + bounds.getBottom()) / 2.0d;
					PointVector point = new PointVector(x, y);
					point.setData(layer); // store layer for retrieval on click
					clusterLayer.addVector(point);
				}
			}
			findLayersMap.addLayer(clusterLayer);
			clusterLayer.recluster(true);
		}
	}

	@Override
	public void zoomToLayersEvent(Set<ExpressZipLayer> layers) {
		mapModel.zoomToExtent(mapModel.getBoundsForLayers(new ArrayList<ExpressZipLayer>(layers)));
	}

	@Override
	public void layerHighlightedEvent(Set<ExpressZipLayer> toggledList) {
		boundingBoxLayer.removeAll(false);
		for (ExpressZipLayer layer : toggledList) {
			boundingBoxLayer.addBoundingBox(layer, mapModel.getCurrentProjection());
		}
	}

	@Override
	public void extentChanged(ExtentChangeEvent event) {
		if (event.getComponent() == findLayersMap) {
			if (popup != null)
				findLayersMap.removeComponent(popup);
			bounds = findLayersMap.getExtend();
			mapModel.setVisibleExtent(bounds);
			findLayersView.applyFilters();
			findLayersView.showRenderedLayers(renderedLayers);
		}
	}

	private void addClusteredVectorLayer() {
		if (clusterLayer != null) {
			findLayersMap.removeLayer(clusterLayer);
		}
		clusterLayer = new VectorLayer();
		clusterLayer.setSelectionMode(SelectionMode.SIMPLE);
		clusterLayer.addListener(new org.vaadin.vol.VectorLayer.VectorSelectedListener() {

			@Override
			public void vectorSelected(org.vaadin.vol.VectorLayer.VectorSelectedEvent event) {
				Vector[] vectors = event.getVectors();
				if (null == vectors)
					return;

				if (null != popup) {
					findLayersMap.removeComponent(popup);
				}
				popup = new Popup();
				findLayersMap.addPopup(popup);
				CssLayout cssLayout = new CssLayout();
				VerticalLayout verticalLayout = new VerticalLayout();
				Panel panel = new Panel();
				panel.setStyleName(Runo.PANEL_LIGHT);
				panel.setContent(verticalLayout);
				int height = Math.min(400, (vectors.length + 1) * 32 + 10);
				panel.setHeight(height + "px");
				cssLayout.addComponent(panel);
				popup.addComponent(cssLayout);

				TreeTable popupTable = findLayersView.getPopupTable();
				popupTable.removeAllItems();
				verticalLayout.addComponent(popupTable);

				List<ExpressZipLayer> layers = new ArrayList<ExpressZipLayer>();
				for (Vector selected : vectors) {
					final ExpressZipLayer layer = (ExpressZipLayer) selected.getData();
					findLayersView.addLayerItem(layer, popupTable);
					layers.add(layer);
				}
				Bounds b = MapModel.getBoundsForLayers(layers, MapModel.EPSG4326);
				popup.setLat((b.getMinLat() + b.getMaxLat()) / 2.0d);
				popup.setLon((b.getMaxLon() + b.getMinLon()) / 2.0d);
			}

		});

		clusterLayer.addListener(new org.vaadin.vol.VectorLayer.VectorUnSelectedListener() {

			@Override
			public void vectorUnSelected(VectorUnSelectedEvent event) {
				if (null != popup) {
					findLayersMap.removeComponent(popup);
					popup = null;
				}
			}
		});

		clusterLayer.setImmediate(true);
		findLayersMap.addLayer(clusterLayer);
	}

	private void addRenderedLayer(ExpressZipLayer layer) {
		if (!renderedLayers.contains(layer)) {
			removeRenderedLayer(layer);
			renderedLayers.add(layer);
			AbstractLayerBase wmsLayer = layer.getVaadinLayer(mapModel.getCurrentProjection());
			findLayersMap.addLayer(wmsLayer);
			wmsLayer.setIndex(renderedLayers.size() - 1);
		}
	}

	private void removeRenderedLayer(ExpressZipLayer layer) {
		if (renderedLayers.contains(layer)) {
			renderedLayers.remove(layer);
			findLayersMap.removeLayer(layer.getVaadinLayer(mapModel.getCurrentProjection()));
		}
	}

	private void addBoundingBoxLayer() {
		if (boundingBoxLayer != null) {
			findLayersMap.removeLayer(boundingBoxLayer);
		}
		boundingBoxLayer = new BoundingBoxLayer();
		findLayersMap.addLayer(boundingBoxLayer);
		
		for(ExpressZipLayer l : mapModel.getRenderedLayers()) {
			boundingBoxLayer.addBoundingBox(l, mapModel.getCurrentProjection());
		}
		for(ExpressZipLayer l : mapModel.getChosenLayers()) {
			boundingBoxLayer.addBoundingBox(l, mapModel.getCurrentProjection());
		}
	}

	@Override
	public void chooseLayerForExportEvent(ExpressZipLayer layer, boolean export, boolean highlighted) {
		layer.setChosen(export);
		boundingBoxLayer.removeBoundingBox(layer);

		if (export || highlighted) {
			boundingBoxLayer.addBoundingBox(layer, mapModel.getCurrentProjection());
		}
	}

	@Override
	public void renderLayerEvent(ExpressZipLayer layer, boolean render) {
		if (renderedLayers.contains(layer)) {
			removeRenderedLayer(layer);
		}

		if (render) {
			addRenderedLayer(layer);
		}
	}

	@Override
	public void setIncludedLocalBaseMap(String localBaseLayerName) {
		mapModel.setIncludedLocalBaseMap(localBaseLayerName);
	}
}
