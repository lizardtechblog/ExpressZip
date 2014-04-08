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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;
import org.vaadin.vol.Bounds;

import com.lizardtech.expresszip.vaadin.ExpressZipApplication;

public class ExportProps {
	private Integer height = 0;
	private Integer width = 0;

	private String mapProjection = MapModel.EPSG4326;
	private org.vaadin.vol.Bounds exportRegion;
	private String email = null;
	private String userNotation = null;
	private List<ExpressZipLayer> enabledLayers;
	private List<TileExport> tiles = null;
	private VectorLayer croppingLayer;

	public static final String DefaultNotSupplied = "Not Supplied";
	public static final int MAX_TILE_WIDTH = 5000;
	public static final int MAX_TILE_HEIGHT = 5000;
	public static final long MAX_TILES_PER_JOB = 10000;
	public static final int DEFAULT_TILE_SIZE = 5000;

	public ExportProps() {
		width = 5000;
		email = new String(DefaultNotSupplied);
		userNotation = new String(DefaultNotSupplied);
		griddingOptions = new GriddingOptions();
	}

	public ExportProps(ExportProps props) {
		height = new Integer(props.height);
		width = new Integer(props.width);
		mapProjection = new String(props.mapProjection);
		exportRegion = new Bounds();
		exportRegion.setTop(props.getExportRegion().getTop());
		exportRegion.setLeft(props.getExportRegion().getLeft());
		exportRegion.setBottom(props.getExportRegion().getBottom());
		exportRegion.setRight(props.getExportRegion().getRight());

		assert (props.email != null);
		email = new String(props.email);
		jobName = new String(props.jobName == null ? "" : props.jobName);
		enabledLayers = new ArrayList<ExpressZipLayer>();
		if (props.enabledLayers != null)
			enabledLayers.addAll(props.enabledLayers);
		tiles = new ArrayList<TileExport>();
		if (props.tiles != null)
			tiles.addAll(props.tiles);
		outputImageFormat = props.outputImageFormat;
		outputPackageFormat = props.outputPackageFormat;
		setTileOutput(props.isTileOutput());
		griddingOptions = new GriddingOptions(props.griddingOptions);
		croppingLayer = props.croppingLayer;
	}

	public VectorLayer getCroppingLayer() {
		return croppingLayer;
	}

	public void setCroppingLayer(VectorLayer croppingFile) {
		this.croppingLayer = croppingFile;
	}

	private String jobName = null;

	public String getJobName() {
		return jobName;
	}

	public enum OutputImageFormat {
		PNG, JPEG, TIFF, GIF, BMP
	}

	private OutputImageFormat outputImageFormat = OutputImageFormat.JPEG;

	public static enum OutputPackageFormat {
		ZIP, TAR
	}

	private boolean tileOutput = false;

	private GriddingOptions griddingOptions;

	public void setGriddingOptions(GriddingOptions options) {
		griddingOptions = options;
	}

	public GriddingOptions getGriddingOptions() {
		return griddingOptions;
	}

	public void setFormat(String format) {
		if (format.equals("JPEG")) {
			outputImageFormat = OutputImageFormat.JPEG;
			return;
		} else if (format.equals("PNG")) {
			outputImageFormat = OutputImageFormat.PNG;
			return;
		} else if (format.equals("TIFF")) {
			outputImageFormat = OutputImageFormat.TIFF;
			return;
		} else if (format.equals("GIF")) {
			outputImageFormat = OutputImageFormat.GIF;
			return;
		} else if (format.equals("BMP")) {
			outputImageFormat = OutputImageFormat.BMP;
			return;
		}
	}

	private OutputPackageFormat outputPackageFormat = OutputPackageFormat.ZIP;

	public void setPackageFormat(OutputPackageFormat fmt) {
		this.outputPackageFormat = fmt;
	}

	public void setEmail(String email) {
		this.email = email;
		if (this.email == null)
			this.email = new String(DefaultNotSupplied);
	}

	public String getEmail() {
		return email;
	}

	/**
	 * 
	 * @return width to height ratio of hte map
	 * @note may return -1 if there was a problem retrieving the bounds from OpenLayers
	 */
	public double getAspectRatio() {
		ReferencedEnvelope mapBounds = null;
		double heightToWidth = 0.0;

		org.vaadin.vol.Bounds bounds = getExportRegion();
		if (bounds != null) {
			try {
				if (isProjectedCRS(CRS.decode(mapProjection))) {
					mapBounds = new ReferencedEnvelope(bounds.getLeft(), bounds.getRight(), bounds.getTop(), bounds.getBottom(),
							CRS.decode(mapProjection));
					heightToWidth = Math.abs(mapBounds.getSpan(1)) / Math.abs(mapBounds.getSpan(0));

				} else {
					mapBounds = new ReferencedEnvelope(bounds.getTop(), bounds.getBottom(), bounds.getLeft(), bounds.getRight(),
							CRS.decode(mapProjection));
					heightToWidth = Math.abs(mapBounds.getSpan(0)) / Math.abs(mapBounds.getSpan(1));
				}

				return heightToWidth;
			} catch (MismatchedDimensionException e) {
				e.printStackTrace();
			} catch (NoSuchAuthorityCodeException e) {
				e.printStackTrace();
			} catch (FactoryException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	public boolean isProjectedCRS(CoordinateReferenceSystem crs) {
		return (crs instanceof ProjectedCRS);
	}

	public void setExportRegion(org.vaadin.vol.Bounds extent) {
		exportRegion = extent;
	}

	public org.vaadin.vol.Bounds getExportRegion() {
		if (exportRegion == null) {
			exportRegion = new Bounds();
			exportRegion.setTop(0);
			exportRegion.setLeft(0);
			exportRegion.setBottom(0);
			exportRegion.setRight(0);
		}
		return exportRegion;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * 
	 * @return the format of the file as a string. Returns null if format is not found.
	 */
	public String getFormat() {
		if (outputImageFormat == OutputImageFormat.JPEG) {
			return "JPEG";
		} else if (outputImageFormat == OutputImageFormat.GIF) {
			return "GIF";
		} else if (outputImageFormat == OutputImageFormat.BMP) {
			return "BMP";
		} else if (outputImageFormat == OutputImageFormat.TIFF) {
			return "TIFF";
		} else if (outputImageFormat == OutputImageFormat.PNG) {
			return "PNG";
		}

		return null;
	}

	public List<ExpressZipLayer> getEnabledLayers() {
		return enabledLayers;
	}

	public void setEnabledLayers(List<ExpressZipLayer> enabledLayers) {
		this.enabledLayers = enabledLayers;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getMapProjection() {
		return mapProjection;
	}

	public void setMapProjection(String mapProjection) {
		this.mapProjection = mapProjection;
	}

	public OutputPackageFormat getOutputPackageFormat() {
		return outputPackageFormat;
	}

	public void setOutputPackageFormat(OutputPackageFormat outputPackageFormat) {
		this.outputPackageFormat = outputPackageFormat;
	}

	public List<TileExport> getTiles() {
		if (tiles == null) {
			tiles = new ArrayList<TileExport>();
			tiles.add(new TileExport(getExportRegion(), getWidth(), getHeight()));
		}
		return tiles;
	}

	public void setTiles(List<TileExport> tiles) {
		this.tiles = tiles;
	}

	public String getUserNotation() {
		return userNotation;
	}

	public void setUserNotation(String userNotation) {
		this.userNotation = userNotation;
		if (this.userNotation == null || this.userNotation.isEmpty())
			this.userNotation = new String(DefaultNotSupplied);
	}

	public static boolean jobNameIsUnique(String name) {
		String exportDir = ExpressZipApplication.Configuration.getProperty("exportdirectory");
		File dir = new File(exportDir, name);
		if (dir.exists()) {
			return false;
		}
		return true;
	}

	public boolean isTileOutput() {
		return tileOutput;
	}

	public void setTileOutput(boolean tileOutput) {
		this.tileOutput = tileOutput;
	}

}
