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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.geotools.data.ows.CRSEnvelope;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WebMapServer;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.ows.ServiceException;
import org.geotools.referencing.CRS;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.vaadin.vol.AbstractLayerBase;
import org.vaadin.vol.Bounds;
import org.vaadin.vol.Control;
import org.vaadin.vol.GoogleHybridMapLayer;
import org.vaadin.vol.OpenLayersMap;
import org.vaadin.vol.OpenLayersMap.ExtentChangeEvent;
import org.vaadin.vol.OpenLayersMap.ExtentChangeListener;
import org.vaadin.vol.OpenStreetMapLayer;
import org.vaadin.vol.ProjectedLayer;
import org.vaadin.vol.WebMapServiceLayer;

import com.lizardtech.expresszip.vaadin.ExpressZipApplication;

public class MapModel implements ExtentChangeListener {

	static private String DEFAULT_WMS_URL = "http://localhost/lizardtech/iserv/ows";
	static private URL wmsPrivateUrl;

	static private void setServerURL(String host) throws MalformedURLException {
		wmsPrivateUrl = new URL(host);
	}

	static public URL getPrivateServerURL() {
		return wmsPrivateUrl;
	}

	static public URL getPublicServerURL() {
		try {
			// if configured WMS is localhost (the default), then use app hostname in browser URLs
			if (wmsPrivateUrl.getHost().equals("localhost") || wmsPrivateUrl.getHost().equals("127.0.0.1")) {
				URL applUrl = new URL(ExpressZipApplication.Configuration.getProperty("applicationURL"));
				return new URL(wmsPrivateUrl.getProtocol(), applUrl.getHost(), wmsPrivateUrl.getPort(), wmsPrivateUrl.getFile());
			}
		} catch (MalformedURLException e) {
			// shouldn't be possible since we are creating it from another URL...
		}
		return wmsPrivateUrl;
	}

	static public final String EPSG4326 = "EPSG:4326";
	static public final String EPSG3857 = "EPSG:3857";

	private List<ExpressZipLayer> layerList = null; // All the layers accessible to the map
	private ArrayList<ProjectedLayer> baseLayers;
	private List<ExpressZipLayer> localBaseLayers;

	public ArrayList<ProjectedLayer> getBaseLayers() {
		return baseLayers;
	}

	public List<ExpressZipLayer> getLocalBaseLayers() {
		return localBaseLayers;
	}

	private RasterLayer rootRasterLayer;
	private VectorLayer rootVectorLayer;
	private String currentProjection;
	private String includedLocalBaseMap;

	static final Logger logger = Logger.getLogger(MapModel.class);
	private static final String googleTermsMessage = "<p>Please note that by using the Google Maps base layer you are bound by the <a href=\"https://developers.google.com/maps/terms\" target=\"_blank\">Google Terms of Use</a> and subect to the <a href=\"http://www.google.com/privacy.html\" target=\"_blank\">Google Privacy Policy</a>.</p>";
	private static final double ICON_SIZE = 200;

	public MapModel() {
		try {
			String configuredWMSHostURL = ExpressZipApplication.Configuration.getProperty("wmshost");
			if (configuredWMSHostURL != null)
				setServerURL(configuredWMSHostURL);
			else
				setServerURL(DEFAULT_WMS_URL);
		} catch (MalformedURLException e) {
			return;
		}
		getWMSLayers();
		initBaseLayers();

	}

	public static double getMinResForLayers(List<ExpressZipLayer> list, String projection) {
		double minRes = Double.MAX_VALUE;
		for (ExpressZipLayer l : list) {
			CRSEnvelope envelope = l.getGeoToolsLayer().getBoundingBoxes().get(projection);
			if (envelope == null)
				continue;
			if (Math.abs(envelope.getResX()) < minRes)
				minRes = Math.abs(envelope.getResX());
			if (Math.abs(envelope.getResY()) < minRes)
				minRes = Math.abs(envelope.getResY());
		}
		return minRes;
	}

	private void getWMSLayers() {
		WebMapServer expressServer = null;
		try {
			expressServer = new WebMapServer(getPrivateServerURL());
		} catch (ServiceException e) {
			logger.error("WebMapServer ctor", e);
		} catch (IOException e) {
			logger.error("WebMapServer ctor", e);
		}

		layerList = new LinkedList<ExpressZipLayer>();
		if (expressServer != null) {
			WMSCapabilities capabilities = expressServer.getCapabilities();
			rootRasterLayer = new RasterLayer("Layers", capabilities.getLayer(), null);
			rootRasterLayer.getLayerList(layerList);
		}
	}

	public void setMap(OpenLayersMap map, String proj) {
		this.map = map;
		this.currentProjection = proj;
		map.addListener(this);
	}

	public String getIncludedLocalBaseMap() {
		return includedLocalBaseMap;
	}

	public void setIncludedLocalBaseMap(String includedBase) {
		includedLocalBaseMap = includedBase;
	}

	public static OpenLayersMap createOpenLayersMap(AbstractLayerBase baseLayer, String projection,
			List<ExpressZipLayer> layersToScaleBy, ExtentChangeListener extentChangeListener) {
		BaseLayer czsBaseLayer = new BaseLayer(baseLayer.toString(), baseLayer, projection);
		return createOpenLayersMap(czsBaseLayer, projection, layersToScaleBy, extentChangeListener);
	}

	public static OpenLayersMap createOpenLayersMap(ExpressZipLayer baseLayer, String projection,
			List<ExpressZipLayer> layersToScaleBy, ExtentChangeListener extentChangeListener) {
		String mapUnits = null;
		if (projection == null)
			throw new IllegalArgumentException("no projection specified for requested map");

		List<ExpressZipLayer> allIntendedLayers = new ArrayList<ExpressZipLayer>();
		if (layersToScaleBy != null)
			allIntendedLayers.addAll(layersToScaleBy);
		if (baseLayer != null)
			allIntendedLayers.add(baseLayer);
		if (allIntendedLayers.isEmpty())
			throw new IllegalArgumentException("no layers specified for requested map");

		Bounds maxExtent = getBoundsForLayers(allIntendedLayers, projection);
		if (projection.equals(MapModel.EPSG3857)) {
			// working around an error in MODIS' 3857 extent
			if (maxExtent.getBottom() < -20037508.34)
				maxExtent.setBottom(-20037508.34);
			if (maxExtent.getLeft() < -20037508.34)
				maxExtent.setLeft(-20037508.34);
			if (maxExtent.getTop() > 20037508.34)
				maxExtent.setTop(20037508.34);
			if (maxExtent.getRight() > 20037508.34)
				maxExtent.setRight(20037508.34);
			mapUnits = new String("m");
		}

		double minRes = getMinResForLayers(allIntendedLayers, projection);
		double maxRes = Math.max(Math.abs(maxExtent.getTop() - maxExtent.getBottom()),
				Math.abs(maxExtent.getRight() - maxExtent.getLeft()))
				/ ICON_SIZE;
		int numZoomLevels = (int) Math.ceil(Math.log(maxRes / minRes) / Math.log(2)) + 1;

		if (projection.equals(EPSG3857) && maxRes > 156543.0339)
			maxRes = 156543.0339;

		StringBuilder jsOptions = new StringBuilder("{");
		jsOptions.append("projection: new $wnd.OpenLayers.Projection(\"");
		jsOptions.append(projection);
		jsOptions.append("\")");

		jsOptions.append(",displayProjection: new $wnd.OpenLayers.Projection(\"");
		jsOptions.append(MapModel.EPSG4326);
		jsOptions.append("\")");

		jsOptions.append(",maxExtent:new $wnd.OpenLayers.Bounds(");
		jsOptions.append(maxExtent.getLeft());
		jsOptions.append(",");
		jsOptions.append(maxExtent.getBottom());
		jsOptions.append(",");
		jsOptions.append(maxExtent.getRight());
		jsOptions.append(",");
		jsOptions.append(maxExtent.getTop());
		jsOptions.append(")");

		if (mapUnits != null) {
			jsOptions.append(",units:\"");
			jsOptions.append(mapUnits);
			jsOptions.append("\"");
		}

		jsOptions.append(",maxResolution:");
		jsOptions.append(maxRes);

		jsOptions.append(",numZoomLevels:");
		jsOptions.append(numZoomLevels);

		if (baseLayer == null) {
			jsOptions.append(",allOverlays:true");
		}

		jsOptions.append("}");

		OpenLayersMap map = new OpenLayersMap();
		map.setImmediate(true);
		map.setApiProjection(projection);
		map.getControls().clear();
		// add this as debugging aid
		// map.getControls().add(Control.LayerSwitcher);
		map.getControls().add(Control.MousePosition);
		map.getControls().add(Control.PanZoomBar);
		map.getControls().add(Control.NavToolbar);
		map.setVisible(true);
		map.setSizeFull();
		if (null != jsOptions)
			map.setJsMapOptions(jsOptions.toString());
		if (extentChangeListener != null)
			map.addListener(extentChangeListener);
		return map;
	}

	// openlayersmap -- the model for OpenLayers/browser rendering of the map
	private OpenLayersMap map;
	private Bounds visibleExtent;
	private boolean googleTermsAccepted = false;

	public OpenLayersMap getMap() {
		return map;
	}

	// only one vector layer
	public void addVectorLayer(VectorLayer layer) {
		removeVectorLayer(); // remove existing
		
		rootVectorLayer = layer;
		getMap().addLayer(rootVectorLayer.getVaadinLayer(currentProjection));
	}

	public void removeVectorLayer() {
		if (rootVectorLayer != null) {
			getMap().removeLayer(rootVectorLayer.getVaadinLayer(currentProjection));
			rootVectorLayer = null;
		}
	}

	public VectorLayer getVectorLayer() {
		return rootVectorLayer;
	}
	
	public List<ExpressZipLayer> getLayers() {
		return layerList;
	}

	// return layers in PAINTER'S order
	public List<ExpressZipLayer> getChosenLayers() {

		ArrayList<ExpressZipLayer> chosenList = new ArrayList<ExpressZipLayer>();
		for (ExpressZipLayer layer : getLayers()) {
			if (layer.isChosen())
				chosenList.add(layer);
		}
		Collections.sort(chosenList, new Comparator<ExpressZipLayer>() {
			@Override
			public int compare(ExpressZipLayer layer0, ExpressZipLayer layer1) {
				return layer0.getIndex() - layer1.getIndex();
			}
		});
		return chosenList;
	}

	public List<ExpressZipLayer> getFilteredLayers() {
		List<ExpressZipLayer> visibleList = new LinkedList<ExpressZipLayer>();

		// always filter local base layers
		List<ExpressZipLayer> nonBaseLayers = new ArrayList<ExpressZipLayer>(getLayers());
		nonBaseLayers.removeAll(localBaseLayers);

		for (ExpressZipLayer layer : nonBaseLayers) {
			if (layer.filterAllows())
				visibleList.add(layer);
		}
		return visibleList;
	}

	public void setAllLayersFilterAllows() {
		for (ExpressZipLayer layer : getLayers()) {
			layer.setFilterAllows(true);
		}
	}

	public void setChosenLayersVisible() {
		for (ExpressZipLayer layer : getLayers()) {
			if (layer.isChosen()) {
				layer.getVaadinLayer(getCurrentProjection()).setOLVisibility((true));
			}
		}
	}

	public List<ExpressZipLayer> getRenderedLayers() {
		List<ExpressZipLayer> visibleList = new LinkedList<ExpressZipLayer>();
		for (ExpressZipLayer layer : getLayers()) {
			if (layer.isRendered()) {
				visibleList.add(layer);
			}
		}
		return visibleList;
	}

	/*
	 * Disable all of the layers because we are going to go through each and re-enable what we have selected.
	 */
	public void disableAllLayers() {
		for (ExpressZipLayer layer : getLayers()) {
			layer.setChosen(false);
		}
	}

	/*
	 * Zooms to a layer given, finds the bounds for it and tells map to zoom to those given points if OpenLayersMap is null, use
	 * MapModel.map
	 */
	public void zoomToLayer(ExpressZipLayer layer, OpenLayersMap map, String projection) {

		CRSEnvelope envelope = layer.getGeoToolsLayer().getBoundingBoxes().get(projection);

		double minx = envelope.getMinX();
		double miny = envelope.getMinY();
		double maxx = envelope.getMaxX();
		double maxy = envelope.getMaxY();
		Bounds bounds = new Bounds();

		bounds.setTop(miny);
		bounds.setBottom(maxy);
		bounds.setLeft(minx);
		bounds.setRight(maxx);
		(map == null ? getMap() : map).zoomToExtent(bounds);
	}

	public static Bounds getBoundsForLayer(ExpressZipLayer layer, String projection) {
		if (layer == null)
			return null;

		return layer.getBoundsForLayer(projection);
	}

	public void zoomToExtent(Bounds bounds) {
		map.zoomToExtent(bounds);
	}

	public static Bounds getBoundsForLayers(List<ExpressZipLayer> list, String projection) {
		List<Bounds> bList = new ArrayList<Bounds>();

		for (ExpressZipLayer l : list) {
			Bounds bounds = getBoundsForLayer(l, projection);
			bList.add(bounds);
		}
		return getBoundingBox(bList);
	}

	public Bounds getBoundsForLayers(List<ExpressZipLayer> list) {
		return getBoundsForLayers(list, currentProjection);
	}

	private static Bounds getBoundingBox(List<Bounds> boundsIn) {
		Double top = Double.NEGATIVE_INFINITY, left = Double.POSITIVE_INFINITY, bottom = Double.POSITIVE_INFINITY, right = Double.NEGATIVE_INFINITY;
		Bounds newB = new Bounds();
		for (Bounds b : boundsIn) {
			if (b.getLeft() < left) {
				left = b.getLeft();
			}
			if (b.getTop() > top) {
				top = b.getTop();
			}
			if (b.getBottom() < bottom) {
				bottom = b.getBottom();
			}
			if (b.getRight() > right) {
				right = b.getRight();
			}

		}
		newB.setBottom(bottom);
		newB.setRight(right);
		newB.setTop(top);
		newB.setLeft(left);
		return newB;
	}

	public void addLayerToMap(ExpressZipLayer layer) {
		getMap().addLayer(layer.getVaadinLayer(getCurrentProjection()));
	}

	public void removeLayerFromMap(ExpressZipLayer layer) {
		getMap().removeLayer(layer.getVaadinLayer(getCurrentProjection()));
	}

	public void updateOpenLayersMap() {

		getMap().removeAllComponents();

		// add basemap
		if ((getMap().getJsMapOptions() == null || !getMap().getJsMapOptions().contains(("allOverlays:true")))
				&& map.getBaseLayer() != null && map.getBaseLayer() instanceof WebMapServiceLayer) {
			getMap().addLayer(map.getBaseLayer());
		}

		// add raster layers
		for (ExpressZipLayer layer : getChosenLayers()) {
			AbstractLayerBase vaadinLayer = layer.getVaadinLayer(getCurrentProjection());
			getMap().addLayer(vaadinLayer);
		}

		// add vector layer
		if (null != rootVectorLayer) {
			getMap().addLayer(rootVectorLayer.getVaadinLayer(getCurrentProjection()));
		}
	}

	public String getCurrentProjection() {
		return currentProjection;
	}

	public ExpressZipLayer getRootRasterLayer() {
		return rootRasterLayer;
	}

	// set indexes based on painter's order
	public void updateOrder(List<ExpressZipLayer> orderedLayers) {
		for (int i = 0; i < orderedLayers.size(); i++) {
			
			// start from the back
			ExpressZipLayer layer = orderedLayers.get(orderedLayers.size() - i - 1);
			
			AbstractLayerBase vaadinLayer = layer.getVaadinLayer(getCurrentProjection());
			if (vaadinLayer instanceof WebMapServiceLayer) {
				if (!((WebMapServiceLayer) vaadinLayer).isBaseLayer()) {
					layer.setIndex(i);
					vaadinLayer.setIndex(i);
				}
			}
		}
		getMap().requestRepaintAll();
	}

	public Bounds getVisibleExtent() {
		return visibleExtent;
	}

	public void setVisibleExtent(Bounds bounds) {
		visibleExtent = bounds;
	}

	@Override
	public void extentChanged(ExtentChangeEvent event) {
		if (event.getComponent() == map) {
			setVisibleExtent(map.getExtend());
		}
	}

	public static Bounds reprojectBounds(Bounds inBounds, String fromCRS, String toCRS) {
		if (fromCRS.equals(toCRS))
			return inBounds;

		try {
			double ulx = inBounds.getLeft();
			double uly = inBounds.getTop();
			double lrx = inBounds.getRight();
			double lry = inBounds.getBottom();

			CoordinateReferenceSystem sourceCRS = CRS.decode(fromCRS);
			CoordinateReferenceSystem targetCRS = CRS.decode(toCRS);
			if (CRS.equalsIgnoreMetadata(sourceCRS, targetCRS)) {
				return inBounds;
			} else {
				ReferencedEnvelope re = new ReferencedEnvelope(ulx, lrx, lry, uly, sourceCRS);
				BoundingBox b = re.toBounds(targetCRS);
				return getBounds(b);
			}
		} catch (Exception e) {
		}

		return null;
	}

	/**
	 * Expands inBounds by multiplying min of inBounds.getWidth and inBounds.getHeight times percent
	 * 
	 * @param inBounds
	 * @param percent
	 * @return
	 */
	public static Bounds expandBounds(Bounds inBounds, String inCRS, double percent) {

		try {
			double ulx = inBounds.getLeft();
			double uly = inBounds.getTop();
			double lrx = inBounds.getRight();
			double lry = inBounds.getBottom();

			CoordinateReferenceSystem theCRS = CRS.decode(inCRS);
			ReferencedEnvelope re = new ReferencedEnvelope(ulx, lrx, lry, uly, theCRS);
			double expandBy = Math.min(re.getHeight(), re.getWidth()) * percent;
			re.expandBy(expandBy);
			return getBounds(re.toBounds(theCRS));

		} catch (Exception e) {
		}

		return null;
	}

	public static Bounds getBounds(BoundingBox bb) {
		Bounds bounds = new Bounds();
		bounds.setTop(bb.getMaxY());
		bounds.setBottom(bb.getMinY());
		bounds.setLeft(bb.getMinX());
		bounds.setRight(bb.getMaxX());
		return bounds;
	}

	public static BoundingBox getBoundingBox(Bounds b, String projection) {
		ReferencedEnvelope re = null;
		try {
			re = new ReferencedEnvelope(b.getLeft(), b.getRight(), b.getBottom(), b.getTop(), CRS.decode(projection));
		} catch (Exception e) {
		}
		return re;
	}

	private void initBaseLayers() {

		List<String> listLocalBaseLayers = new ArrayList<String>();
		try {
			String csvLocalBaseLayers = ExpressZipApplication.Configuration.getProperty("availableLocalBaseLayers");
			listLocalBaseLayers = Arrays.asList(csvLocalBaseLayers.split("\\s*,\\s*"));
		} catch (Exception e) {
		}

		String defaultBaseLayerName = "MODIS";
		try {
			defaultBaseLayerName = ExpressZipApplication.Configuration.getProperty("defaultBaseLayerName");

		} catch (Exception e) {
		}

		baseLayers = new ArrayList<ProjectedLayer>();
		localBaseLayers = new ArrayList<ExpressZipLayer>();

		for (ExpressZipLayer l : getLayers()) {
			if (l instanceof RasterLayer && listLocalBaseLayers.contains(l.getName())) {
				WebMapServiceLayer wms = null;
				if (l.getSupportedProjections().contains(MapModel.EPSG4326)) {
					wms = (WebMapServiceLayer) l.getVaadinLayer(MapModel.EPSG4326);
				} else if (l.getSupportedProjections().contains(MapModel.EPSG3857)) {
					wms = (WebMapServiceLayer) l.getVaadinLayer(MapModel.EPSG3857);
				} else
					break; // not using this one.
				wms.setBaseLayer(true);

				// if this is the default, put at beginning of list
				if (l.getName().equals(defaultBaseLayerName)) {
					baseLayers.add(0, wms);
				} else {
					baseLayers.add(wms);
				}
				localBaseLayers.add(l);
			}
		}

		OpenStreetMapLayer osm = new OpenStreetMapLayer();
		osm.setDisplayName("Open Street Maps");
		osm.setProjection(MapModel.EPSG3857);
		baseLayers.add(osm);

		GoogleHybridMapLayer googlebase = new GoogleHybridMapLayer();
		googlebase.setDisplayName("Google Maps");
		googlebase.setProjection(MapModel.EPSG3857);
		baseLayers.add(googlebase);

		WebMapServiceLayer wmsLayer = new WebMapServiceLayer();
		wmsLayer.setProjection(MapModel.EPSG4326);
		wmsLayer.setDisplayName("OpenLayers WMS");
		wmsLayer.setBaseLayer(true);
		wmsLayer.setUri("http://vmap0.tiles.osgeo.org/wms/vmap0");
		wmsLayer.setFormat("image/png");
		baseLayers.add(wmsLayer);
	}

	public String getBaseLayerTerms(ProjectedLayer baseLayer) {
		if (baseLayer instanceof GoogleHybridMapLayer)
			return googleTermsMessage;
		return null;
	}

	public boolean getBaseLayerTermsAccepted(ProjectedLayer baseLayer) {
		if (baseLayer instanceof GoogleHybridMapLayer)
			return this.googleTermsAccepted;
		return false;
	}

	public void setBaseLayerTermsAccepted(ProjectedLayer baseLayer) {
		if (baseLayer instanceof GoogleHybridMapLayer)
			googleTermsAccepted = true;
	}
}
