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
import java.util.Collections;
import java.util.List;

import org.geotools.data.ows.CRSEnvelope;
import org.vaadin.vol.Bounds;
import org.vaadin.vol.VectorLayer.DrawingMode;

import com.lizardtech.expresszip.model.ExportProps;
import com.lizardtech.expresszip.model.ExpressZipLayer;
import com.lizardtech.expresszip.model.Job;
import com.lizardtech.expresszip.model.MapModel;
import com.lizardtech.expresszip.model.RasterLayer;
import com.lizardtech.expresszip.vaadin.ExportOptionsViewComponent;
import com.lizardtech.expresszip.vaadin.ExpressZipWindow;

public class ExportOptionsPresenter implements ExportOptionsView.ExportOptionsViewListener {
	private ExportOptionsViewComponent view;
	private ExtentSelectorLayer extentSelector;

	/**
	 * 
	 * @param dimension
	 * @return true if dimension is in the correct format, else returns false.
	 */
	private boolean isValidDimension(String dimension) {
		// Input validation
		if (dimension.isEmpty() || !dimension.matches("[1-9][0-9]*")) {
			return false;
		}
		return true;
	}

	/**
	 * @param view
	 */
	public ExportOptionsPresenter(ExportOptionsViewComponent view) {
		this.view = view;
		view.addListener(this);
	}

	/**
	 * 
	 * @return true if all params are correct.
	 */
	private boolean validateParams() {
		// Input validation
		if (!isValidDimension(Integer.toString(view.getDimensionHeight()))) {
			view.setErrorMessage("Incorrect value for dimension's height");
			return false;
		} else if (!isValidDimension(Integer.toString(view.getDimensionWidth()))) {
			view.setErrorMessage("Incorrect value for dimension's width");
			return false;
		}

		return true;
	}

	// ExportOptionsViewListener implementations
	@Override
	/**
	 * submit job to server to save image.
	 */
	public void submitJobEvent(ExportProps param) {
		if (!validateParams()) {
			return;
		}
		// Set param to match field in ExportView
		ExportProps props = new ExportProps(param);
		props.setJobName(view.getJobName());
		props.setEmail(view.getEmail());
		props.setUserNotation(view.getUserNotation());
		props.setFormat(view.getImageFormat());
		props.setPackageFormat(view.getPackageFormat());
		props.setHeight(view.getDimensionHeight());
		props.setWidth(view.getDimensionWidth());
		MapModel mm = ((ExpressZipWindow) view.getApplication().getMainWindow()).getMapModel();
		ArrayList<ExpressZipLayer> chosenLayers = new ArrayList<ExpressZipLayer>();
		if (mm.getIncludedLocalBaseMap() != null) {
			for (ExpressZipLayer l : mm.getLayers()) {
				if (l.getName().equals(mm.getIncludedLocalBaseMap())) {
					chosenLayers.add(l);
					break;
				}
			}
		}
		chosenLayers.addAll(mm.getChosenLayers()); // these will be in painter's order
		
		// add cropping layers
		props.setCroppingLayer(mm.getVectorLayer());
		
		props.setEnabledLayers(chosenLayers);
		try {
			Job newJob = new Job(props, view);
			newJob.submit(view);
		} catch (Exception e) {
			view.setErrorMessage("Unable to save image");
			e.printStackTrace();
		}
	}

	private void updateGroundResolution(double width, double height) {
		Bounds bound = view.getExportProps().getExportRegion();
		view.setGroundResolution(Math.abs(bound.getRight() - bound.getLeft()) / width);
	}

	@Override
	public void updateWidthAndHeightFromRes(Double groundResolution) {
		assert (groundResolution != null && groundResolution != 0.0f);
		Bounds bounds = view.getExportProps().getExportRegion();
		if (bounds != null) {
			view.setTxtDimensionWidth((int) Math.ceil(Math.abs(bounds.getRight() - bounds.getLeft()) / groundResolution));
			view.setTxtDimensionHeight((int) Math.ceil(Math.abs(bounds.getBottom() - bounds.getTop()) / groundResolution));
		}
		view.setGroundResolution(groundResolution);
	}

	@Override
	/**
	 * called when height is changed.  Updates width field to match aspect ratio in respect to height.
	 */
	public void updateWidthAndResFromHeight(int height) {

		view.getExportProps().setHeight(height);

		if (!isValidDimension(Integer.toString(view.getDimensionHeight()))) {
			view.setComponentErrorMessage(view.getTxtHeight(), "Invalid Height");
			return;
		}
		view.clearComponentErrorMessage(view.getTxtHeight());

		double aspectRatio = view.getExportProps().getAspectRatio();
		if (aspectRatio > 0) {
			view.setTxtDimensionWidth((int) Math.round(view.getDimensionHeight() / aspectRatio));
		}
		updateGroundResolution(view.getDimensionWidth(), view.getDimensionHeight());
	}

	@Override
	/**
	 * called when width is changed.  Updates height field to match aspect ratio in respect to width.
	 */
	public void updateHeightAndResFromWidth(int width) {
		view.getExportProps().setWidth(width);

		if (!isValidDimension(Integer.toString(view.getDimensionWidth()))) {
			view.setComponentErrorMessage(view.getTxtWidth(), "Invalid Width");
			return;
		}

		view.clearComponentErrorMessage(view.getTxtWidth());

		// Compute the correct aspect ratio for height
		int height = 0;
		double aspectRatio = view.getExportProps().getAspectRatio();
		if (aspectRatio > 0) {
			height = (int) Math.round(view.getDimensionWidth() * aspectRatio);
		}
		view.setTxtDimensionHeight(height);
		updateGroundResolution(view.getDimensionWidth(), view.getDimensionHeight());
	}

	@Override
	public void updateGridding(ExportProps props) {
		// TODO Auto-generated method stub

	}

	public void paneEntered() {
		// set ground resolution to min rex / resy of all included layers
		Double minResXY = Double.MAX_VALUE;
		MapModel mm = ((ExpressZipWindow) view.getApplication().getMainWindow()).getMapModel();
		List<ExpressZipLayer> chosenLayers = mm.getChosenLayers();

		for (ExpressZipLayer layer : chosenLayers) {
			if (layer instanceof RasterLayer) {
				String mapCRS = mm.getCurrentProjection();
				List<CRSEnvelope> layerCRSs = layer.getGeoToolsLayer().getLayerBoundingBoxes();
				if (layerCRSs != null) {
					System.out.println("Map: " + mapCRS);
					for (CRSEnvelope envelope : layerCRSs) {
						if (mapCRS.equals(envelope.getEPSGCode())) {
							// System.out.println(envelope.toString());
							double resx = Math.abs(envelope.getResX());
							double resy = Math.abs(envelope.getResY());
							minResXY = Math.min(minResXY, resx);
							minResXY = Math.min(minResXY, resy);
							// System.out.println("minResXY: " + minResXY);
						}
					}
				}
			}
		}
		mm.getMap().removeExtentPanel();

		view.setMaximumResolution(minResXY);
		updateWidthAndHeightFromRes(minResXY);

		view.paneEntered();
		mm.zoomToExtent(view.getExportProps().getExportRegion());

		// add a read-only extent selector to outline export region
		if (extentSelector != null) {
			mm.getMap().removeLayer(extentSelector);
		}
		extentSelector = new ExtentSelectorLayer(mm.getMap(), view.getExportProps());
		extentSelector.setDrawingMode(DrawingMode.NONE);
		mm.getMap().addLayer(extentSelector);
	}
}
