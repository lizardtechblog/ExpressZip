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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridCoverageWriter;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.processing.CoverageProcessor;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.wms.WebMapServer;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.gce.image.WorldImageFormat;
import org.geotools.gce.image.WorldImageWriter;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.map.WMSLayer;
import org.geotools.referencing.CRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.RenderListener;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.resources.coverage.IntersectUtils;
import org.opengis.coverage.Coverage;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.vaadin.vol.Bounds;

import com.lizardtech.expresszip.model.ExportProps.OutputPackageFormat;
import com.lizardtech.expresszip.vaadin.ExportOptionsViewComponent;
import com.lizardtech.expresszip.vaadin.ExpressZipApplication;
import com.lizardtech.expresszip.vaadin.ExpressZipWindow;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class Job extends Observable implements Runnable, ExecutorListener {
	private ExportProps jobParams = null;
	private Logger logger = null;
	private List<String> shapefiles = new ArrayList<String>();
	private File exportDir = null;
	private ExportOptionsViewComponent parent;
	private String jobStatus = "Submitted";
	private volatile boolean shutdown = false;
	private RunState runState = RunState.Uninitialized;
	private List<String> outputTiles;
	private String outputArchiveName;
	private final GeoTiffWriteParams geoTiffWPs;

	public enum RunState {
		Uninitialized, Queued, Running, FinishedOK, Failed
	};

	private File outputLogFile = null;
	private Appender outputLogAppender = null;
	private URL url;
	private static int WMSTimeOut = 0;

	public Job(ExportProps param, ExportOptionsViewComponent view) {
		jobParams = param;
		parent = view;

		url = view.getApplication().getMainWindow().getURL();
		if (exportDir == null) {
			exportDir = new File(ExpressZipApplication.Configuration.getProperty("exportdirectory"), jobParams.getJobName());
			exportDir.mkdir();
		}
		String JobRunID = String.format("%d", System.identityHashCode(this));
		logger = Logger.getLogger(JobRunID);
		logger.setAdditivity(false);
		try {
			outputLogFile = new File(exportDir, getLogName());
			outputLogAppender = new FileAppender(new HTMLLayout(), outputLogFile.getPath());
			logger.addAppender(outputLogAppender);
		} catch (IOException e) {
			BasicConfigurator.configure();
		}

		// Set up GeoTIFF writer compression and tiling
		geoTiffWPs = new GeoTiffWriteParams();
		geoTiffWPs.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
		geoTiffWPs.setCompressionType("LZW");
		geoTiffWPs.setCompressionQuality(0.75F);
		geoTiffWPs.setTilingMode(GeoTiffWriteParams.MODE_EXPLICIT);
		geoTiffWPs.setTiling(256, 256);
	}

	public URL getUrl() {
		return url;
	}

	public void setStatus(String s) {
		jobStatus = s;
	}

	// this is called by the databinding in JobStatus view component
	public String getStatus() {
		switch (getRunState()) {
		case Uninitialized:
			return "Submitted";
		case Queued:
			return "Queued";
		case Running:
			return jobStatus;
		case Failed:
			return "Failed";
		case FinishedOK:
			return "Completed OK";
		default:
			return "Unknown status";
		}
	}

	private String _archiveName = "Uninitialized_OutputArchive.zip";

	public String getDL_URL() {
		String url = "";
		if (getRunState() == RunState.FinishedOK) {
			URL u = getUrl();
			String containerURL = u.getProtocol() + "://" + u.getAuthority();
			url = containerURL + "/exportdir/" + jobParams.getJobName() + "/" + _archiveName;
		}
		return url;
	}

	private File cropFile;

	private String getLogName() {
		return jobParams.getJobName() + "_Log.html";
	}

	public String getlog_URL() {
		String url = "";
		if (getRunState() == RunState.FinishedOK || getRunState() == RunState.Failed) {
			URL u = getUrl();
			String containerURL = u.getProtocol() + "://" + u.getAuthority();
			url = containerURL + "/exportdir/" + jobParams.getJobName() + "/" + getLogName();
		}
		return url;
	}

	public ExportProps getExporProps() {
		return jobParams;
	}

	public String getJobName() {
		return jobParams.getJobName();
	}

	public void cancel() {
		logger.info("Cancelling job " + jobParams.getJobName());
		shutdown = true;
	}

	public void run() {
		try {
			addshpFileLayers();
			logger.info("Saving the output image ...");
			saveImage(jobParams.getJobName(), jobParams.getWidth(), jobParams.getHeight(), jobParams.getFormat());
			logger.info("Done with job " + jobParams.getJobName());

		} catch (Exception e) {

			try {
				setRunState(RunState.Failed);
				logger.error("Job failed (" + e.getMessage() + ")", e);
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	private void saveImage(final String jobname, final int imageWidth, final int imageHeight, final String format) throws Exception {
		_archiveName = exportTiles(jobParams, jobname, format);
	}

	private int getWMSTimeOut() {
		if (WMSTimeOut == 0) {
			String strTimeOut = ExpressZipApplication.Configuration.getProperty("wmstimeout");
			if (strTimeOut != null) {
				try {
					WMSTimeOut = Integer.parseInt(strTimeOut);
				} catch (NumberFormatException e) {
					// empty
				}
			}
		}
		if (WMSTimeOut == 0)
			WMSTimeOut = 7 * 1000;
		return WMSTimeOut;
	}

	/**
	 * Add enable shapefiles to map to be saved as an image
	 * 
	 * @throws IOException
	 */
	private void addshpFileLayers() throws IOException {
		VectorLayer cropLayer = jobParams.getCroppingLayer();
		if (null != cropLayer) {
			logger.info("Adding VectorLayer " + cropLayer.getName());
			shapefiles.add(cropLayer.getName());
			cropFile = cropLayer.buildShpFilePath();
		}
		incrementShapefileRefCounts();
	}

	private void writeTarFile(File baseDir, File archive, List<String> files) throws IOException {
		FileOutputStream fOut = null;
		BufferedOutputStream bOut = null;
		GzipCompressorOutputStream gzOut = null;
		TarArchiveOutputStream tOut = null;
		try {
			fOut = new FileOutputStream(archive);
			bOut = new BufferedOutputStream(fOut);
			gzOut = new GzipCompressorOutputStream(bOut);
			tOut = new TarArchiveOutputStream(gzOut);

			for (String f : files) {
				File myfile = new File(baseDir, f);
				String entryName = myfile.getName();
				logger.info(String.format("Writing %s to TAR archive %s", f, archive));

				TarArchiveEntry tarEntry = new TarArchiveEntry(myfile, entryName);
				tOut.putArchiveEntry(tarEntry);

				FileInputStream fis = new FileInputStream(myfile);
				IOUtils.copy(fis, tOut);
				fis.close();
				tOut.closeArchiveEntry();
			}
		} finally {
			tOut.finish();
			tOut.close();
			gzOut.close();
			bOut.close();
			fOut.close();
		}
	}

	private void writeZipFile(File baseDir, File archive, List<String> files) throws FileNotFoundException, IOException {
		FilterOutputStream out = null;
		ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(archive));
		stream.setLevel(Deflater.DEFAULT_COMPRESSION);
		out = stream;

		byte data[] = new byte[18024];

		for (String f : files) {
			logger.info(String.format("Writing %s to ZIP archive %s", f, archive));
			((ZipOutputStream) out).putNextEntry(new ZipEntry(f));

			BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(baseDir, f)));

			int len = 0;
			while ((len = in.read(data)) > 0) {
				out.write(data, 0, len);
			}

			out.flush();
			in.close();
		}

		out.close();
	}

	private String zipFiles(List<String> files, String jobName, File baseDir) throws IOException {

		String suffix = "";
		String basename = "";
		OutputPackageFormat fmt = jobParams.getOutputPackageFormat();
		String extension = fmt.toString().toLowerCase();
		if (fmt == OutputPackageFormat.TAR)
			extension += ".gz";

		boolean done = false;
		int count = 1;
		try {
			File file = null;
			while (!done) { // Generate a unique name for the output archive

				basename = jobName + suffix + "." + extension;
				file = new File(baseDir, basename);
				done = file.createNewFile(); // stake our claim to a unique
												// filename
				suffix = "_" + String.valueOf(count++);
			}
			// Add log to a copy of the list so we archive it but don't delete it on clean up.
			List<String> copyFiles = new ArrayList<String>(files);
			copyFiles.add(outputLogFile.getName());
			if (fmt == OutputPackageFormat.ZIP)
				writeZipFile(baseDir, file, copyFiles);
			else
				writeTarFile(baseDir, file, copyFiles);
		} catch (IllegalArgumentException e) {
			logger.error(e);
			return null;
		}
		return basename;
	}

	enum extensions {
		jpg, png, tif, gif, bmp
	};

	private String exportTiles(ExportProps props, final String jobName, String format) throws Exception {
		// Hack to get proper extension and WorldImageFormat
		if (format.equals("JPEG")) {
			format = extensions.jpg.toString();
		} else if (format.equals("PNG")) {
			format = extensions.png.toString();
		} else if (format.equals("TIFF")) {
			format = extensions.tif.toString();
		} else if (format.equals("GIF")) {
			format = extensions.gif.toString();
		} else if (format.equals("BMP")) {
			format = extensions.bmp.toString();
		} else {
			throw new RuntimeException("Unknown format: " + format);
		}

		outputTiles = new ArrayList<String>();

		GriddingOptions griddingOptions = props.getGriddingOptions();
		if (griddingOptions.isGridding()) {

			Bounds bounds = getExportProps().getExportRegion();

			double cellSpacingX = 0.0;
			double cellSpacingY = 0.0;
			int tilePixelWidth = 0;
			int tilePixelHeight = 0;

			if (griddingOptions.getGridMode() == GriddingOptions.GridMode.DIVISION) {

				double boundsMaxX = Math.max(bounds.getLeft(), bounds.getRight());
				double boundsMinX = Math.min(bounds.getLeft(), bounds.getRight());
				double boundsMaxY = Math.max(bounds.getTop(), bounds.getBottom());
				double boundsMinY = Math.min(bounds.getTop(), bounds.getBottom());

				cellSpacingX = (boundsMaxX - boundsMinX) / griddingOptions.getDivX();
				cellSpacingY = (boundsMaxY - boundsMinY) / griddingOptions.getDivY();
				tilePixelWidth = props.getWidth() / griddingOptions.getDivX();
				tilePixelHeight = props.getHeight() / griddingOptions.getDivY();
			} else { // GRID_TILE_DIMENSIONS or GRID_GROUND_DISTANCE

				griddingOptions.setExportWidth(props.getWidth());
				griddingOptions.setExportHeight(props.getHeight());

				double widthRatio = (double) props.getWidth() / (double) griddingOptions.getTileSizeX();
				double heightRatio = (double) props.getHeight() / (double) griddingOptions.getTileSizeY();

				double max = Math.max(bounds.getLeft(), bounds.getRight());
				double min = Math.min(bounds.getLeft(), bounds.getRight());
				cellSpacingX = (max - min) / widthRatio;
				tilePixelWidth = griddingOptions.getTileSizeX();

				max = Math.max(bounds.getTop(), bounds.getBottom());
				min = Math.min(bounds.getTop(), bounds.getBottom());
				cellSpacingY = (max - min) / heightRatio;
				tilePixelHeight = griddingOptions.getTileSizeY();
			}

			double ulx = bounds.getLeft();
			double lrx = bounds.getRight();
			double uly = bounds.getTop();
			double lry = bounds.getBottom();

			double t_ulx = ulx, t_uly = uly, t_lrx = 0.0, t_lry = 0.0;
			int maxRows = (int) Math.ceil((bounds.getTop() - bounds.getBottom()) / cellSpacingY);
			int maxCols = (int) Math.ceil((bounds.getRight() - bounds.getLeft()) / cellSpacingX);
			int xCounter = 0, yCounter = 0;
			boolean lastColumn = false;
			boolean meters = griddingOptions.getGridMode() == GriddingOptions.GridMode.METERS;
			int tileCounter = 1;
			while (!lastColumn) {

				int lastWidth = 0;
				int lastHeight = 0;
				t_lrx = t_ulx + cellSpacingX;
				t_uly = uly;
				yCounter = 0;

				if (Math.abs((t_ulx + cellSpacingX) - lrx) <= 0.00001f || (t_ulx + cellSpacingX) >= lrx) {
					lastColumn = true;
					if (meters) {
						lastWidth = props.getWidth() - (tilePixelWidth * xCounter);
					}
				}

				boolean lastRow = false;
				while (!lastRow) {

					// TODO: this is wrong when NOT lat-lon
					if (Math.abs((t_uly - cellSpacingY) - lry) <= 0.00001f || (t_uly - cellSpacingY) <= lry) {

						lastRow = true;
						if (meters) {
							lastHeight = props.getHeight() - (tilePixelHeight * yCounter);
						}
					}

					yCounter++;
					t_lry = t_uly - cellSpacingY; // TODO: this is wrong when NOT lat-lon

					Bounds tileBounds = new Bounds();
					tileBounds.setLeft(t_ulx);
					tileBounds.setRight(meters && lastColumn ? lrx : t_lrx);
					tileBounds.setTop(t_uly);
					tileBounds.setBottom(meters && lastRow ? lry : t_lry);
					TileExport tile = new TileExport(tileBounds, meters && lastColumn ? lastWidth : tilePixelWidth, meters
							&& lastRow ? lastHeight : tilePixelHeight);
					exportTile(tile, jobName, format, tileCounter++);
					setStatus("Exporting tile " + tileCounter + " of " + maxRows * maxCols);
					t_uly = t_lry;
				}

				xCounter++;
				t_ulx = t_lrx;
			}
		} else {
			TileExport singleTile = new TileExport(props.getExportRegion(), props.getWidth(), props.getHeight());
			exportTile(singleTile, jobName, format, 0);
		}

		setStatus("Packaging tiles....");
		outputArchiveName = zipFiles(outputTiles, jobName, exportDir);
		return outputArchiveName;
	}

	private void exportTile(TileExport t, String jobName, String format, int counter) throws Exception {

		if (shutdown)
			throw new RuntimeException("Cancelled");

		ReferencedEnvelope tileBounds = null;
		Rectangle imageBounds = null;

		double minx = Math.min(t.bounds.getLeft(), t.bounds.getRight());
		double maxx = Math.max(t.bounds.getLeft(), t.bounds.getRight());
		double miny = Math.min(t.bounds.getTop(), t.bounds.getBottom());
		double maxy = Math.max(t.bounds.getTop(), t.bounds.getBottom());
		logger.info(String.format("Adding tile %d x: %f - %f, y: %f - %f", counter, minx, maxx, miny, maxy));
		if (jobParams.isProjectedCRS(CRS.decode(jobParams.getMapProjection()))) {
			tileBounds = new ReferencedEnvelope(t.bounds.getLeft(), t.bounds.getRight(), t.bounds.getTop(), t.bounds.getBottom(),
					CRS.decode(jobParams.getMapProjection()));

		} else {
			tileBounds = new ReferencedEnvelope(minx, maxx, miny, maxy, CRS.decode(jobParams.getMapProjection()));
		}

		// Set view point on map
		MapViewport viewport = new MapViewport(tileBounds);
		viewport.setCoordinateReferenceSystem(CRS.decode(jobParams.getMapProjection()));

		// Create the bound for the image
		imageBounds = new Rectangle(0, 0, t.width, t.height);

		// Set up an ImageBuffer to write image to
		logger.info(String.format("Allocating BufferedImage of size %d x %d", imageBounds.width, imageBounds.height));
		int bufferType = BufferedImage.TYPE_INT_RGB;
		if (extensions.valueOf(format) == extensions.png)
			bufferType = BufferedImage.TYPE_INT_ARGB;

		BufferedImage image = new BufferedImage(imageBounds.width, imageBounds.height, bufferType);
		Graphics2D graphics = image.createGraphics();

		// Render the unclipped image
		MapContent map = new MapContent();
		map.setViewport(viewport);

		WebMapServer wms = new WebMapServer(MapModel.getPrivateServerURL(), getWMSTimeOut());
		WMSLayer wmsLayer = null;
		for (ExpressZipLayer l : jobParams.getEnabledLayers()) {
			if (l instanceof RasterLayer && l.projectionIsSupported(jobParams.getMapProjection())) {
				if (wmsLayer == null)
					wmsLayer = new WMSLayer(wms, l.getGeoToolsLayer());
				else
					wmsLayer.addLayer(l.getGeoToolsLayer());
			}
		}
		if (wmsLayer != null)
			map.addLayer(wmsLayer);

		GTRenderer renderer = new StreamingRenderer();
		renderer.setMapContent(map);
		renderer.addRenderListener(new RenderListener() {

			@Override
			public void featureRenderer(SimpleFeature feature) {
			}

			@Override
			public void errorOccurred(Exception e) {
				logger.error("Streaming Renderer for tile failed (" + e.getMessage() + ")");
				throw new RuntimeException(e);
			}
		});

		try {
			renderer.paint(graphics, imageBounds, tileBounds);
		} finally {

			try {
				// clean up
				graphics.dispose();
			} catch (Exception e) {
			}

			try {
				map.dispose();
			} catch (Exception e) {
			}
		}

		// Clip the image to the shapefile
		if (cropFile != null) {
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = getFeatureSource(cropFile);
			GridCoverage2D clippedCoverage = (GridCoverage2D) clipImageToFeatureSource(image, tileBounds, featureSource);
			if (null == clippedCoverage) {
				logger.info("Tile does not intersect shapefile. Skipping.");
				return;
			}
			BufferedImage matted = mattCroppedImage(image, clippedCoverage, bufferType);
			image = matted;
		}

		// Save the image
		String fileBase = "";
		File fileToSave = null;
		try {
			do {
				fileBase = jobName;
				if (counter > 0)
					fileBase = fileBase + "_" + counter;
				fileToSave = new File(exportDir, fileBase + "." + format);
				counter++;
			} while (!fileToSave.createNewFile());

			logger.info(String.format("Writing image %s", fileToSave.getAbsolutePath()));

			GridCoverageFactory factory = new GridCoverageFactory();
			GridCoverage2D coverage = factory.create("LT", image, tileBounds);
			AbstractGridCoverageWriter coverageWriter = null;
			GeneralParameterValue[] formatParam = null;
			if (format.equals(extensions.tif.toString())) {
				ParameterValueGroup params = new GeoTiffFormat().getWriteParameters();
				params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(geoTiffWPs);
				coverageWriter = new GeoTiffWriter(fileToSave);
				formatParam = (GeneralParameterValue[]) params.values().toArray(new GeneralParameterValue[1]);
			} else {
				ParameterValueGroup params = new WorldImageFormat().getWriteParameters();
				params.parameter(WorldImageFormat.FORMAT.getName().toString()).setValue(format);
				formatParam = new GeneralParameterValue[] { params.parameter(WorldImageFormat.FORMAT.getName().toString()) };
				coverageWriter = new WorldImageWriter(fileToSave);
			}
			try {
				coverageWriter.write(coverage, formatParam);
			} finally {
				try {
					coverageWriter.dispose();
				} catch (Exception e) {
				}

				try {
					coverage.dispose(true);
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			logger.error(String.format("FAILED writing image %s (%s)", fileToSave.getAbsolutePath(), e.getMessage()));
			throw new RuntimeException(e);
		}

		// Gather up fileBase.* (include world and prj filenames) for zip file
		for (File child : exportDir.listFiles()) {
			String childName = child.getName();
			String baseName = FilenameUtils.getBaseName(childName);
			if (child.isFile() && baseName.equals(fileBase)) {
				String extension = FilenameUtils.getExtension(childName);
				extension.toLowerCase();
				if (extension.length() == 4 && extension.endsWith("w")) {
					extension = extension.substring(0, 1) + extension.substring(2);
					File renamedChild = new File(exportDir, baseName + "." + extension);
					child.renameTo(renamedChild);
					child = renamedChild;
				}

				outputTiles.add(child.getName());
			}
		}
	}

	private BufferedImage mattCroppedImage(final BufferedImage source, GridCoverage2D cropped, int bufferType) {
		int height = source.getHeight();
		int width = source.getWidth();
		BufferedImage image = new BufferedImage(width, height, bufferType);
		Graphics2D gr = image.createGraphics(); // the way getRenderedImage works, this image should be drawn at 0, 0, not at based
												// on where the coverage's tile is
		AffineTransform at = AffineTransform.getTranslateInstance(0, 0);
		gr.drawRenderedImage(cropped.getRenderedImage(), at);
		return image;
	}

	public static FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource(File cropFile) throws MalformedURLException,
			IOException {
		Map<String, Serializable> connectParameters = new java.util.HashMap<String, Serializable>();
		connectParameters.put("url", cropFile.toURI().toURL());
		connectParameters.put("create spatial index", true);
		DataStore dataStore = DataStoreFinder.getDataStore(connectParameters);
		if (dataStore == null)
			throw new RuntimeException("No DataStore found to handle" + cropFile.getPath());
		String[] typeNames = dataStore.getTypeNames();
		String typeName = typeNames[0];
		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = (FeatureSource<SimpleFeatureType, SimpleFeature>) dataStore
				.getFeatureSource(typeName);
		return featureSource;
	}

	private void cleanUp() {
		try {
			// downloaded tiles
			for (String file : outputTiles)
				(new File(exportDir, file)).delete();
			for (String s : shapefiles)
				((ExpressZipWindow) parent.getApplication().getMainWindow()).getSetupMapModel().ShapeFileDecrement(s);
		} catch (Exception e) {
			logger.error(e);
		} finally {
			outputLogAppender.close();
			logger.removeAllAppenders();
		}
		BackgroundExecutor executor = new BackgroundExecutor.Factory().getBackgroundExecutor();
		executor.removeListener(this);
	}

	public void submit(ExecutorListener view) throws Exception {
		BackgroundExecutor executor = new BackgroundExecutor.Factory().getBackgroundExecutor();
		executor.addListener(this);
		executor.execute(this);
	}

	public ExportProps getExportProps() {
		return jobParams;
	}

	public void setExportProps(ExportProps p) {
		jobParams = p;
	}

	private static List<Job> jobQueue = new ArrayList<Job>();

	public static List<Job> getJobQueue() {
		return jobQueue;
	}

	public static void removeJobFromQueue(Job job) {
		synchronized (jobQueue) {
			jobQueue.remove(job);
			job.cleanupFiles();
		}
	}

	private void cleanupFiles() {
		try {
			File file = new File(exportDir, _archiveName);
			file.delete();
			outputLogFile.delete();
			exportDir.delete();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void incrementShapefileRefCounts() {
		for (String s : shapefiles) {
			((ExpressZipWindow) parent.getApplication().getMainWindow()).getSetupMapModel().ShapeFileIncrement(s);
		}
	}

	public void decrementShapefileRefCounts() {
		for (String s : shapefiles) {
			((ExpressZipWindow) parent.getApplication().getMainWindow()).getSetupMapModel().ShapeFileDecrement(s);
		}
	}

	static { // prevents exception due to missing library.
		System.setProperty("com.sun.media.jai.disableMediaLib", "true");
	}

	public RunState getRunState() {
		return runState;
	}

	private Coverage clipImageToFeatureSource(RenderedImage image, ReferencedEnvelope bounds,
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource) throws IOException, FactoryException,
			MismatchedDimensionException, TransformException {
		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureSource.getFeatures();

		CoordinateReferenceSystem crsFeatures = featureSource.getSchema().getCoordinateReferenceSystem();
		CoordinateReferenceSystem crsMap = bounds.getCoordinateReferenceSystem();
		boolean needsReproject = !CRS.equalsIgnoreMetadata(crsFeatures, crsMap);
		MathTransform transform = CRS.findMathTransform(crsFeatures, crsMap, true);

		FeatureIterator<SimpleFeature> iterator = collection.features();
		List<Geometry> all = new ArrayList<Geometry>();
		try {
			while (iterator.hasNext()) {
				SimpleFeature feature = iterator.next();
				Geometry geometry = (Geometry) feature.getDefaultGeometry();
				if (geometry == null)
					continue;
				if (!geometry.isSimple())
					continue;
				if (needsReproject) {
					geometry = JTS.transform(geometry, transform);
					System.out.println("Reprojected a geometry.  Result is " + geometry.toString());
				}
				Geometry intersection = geometry.intersection(JTS.toGeometry(bounds));
				if (intersection.isEmpty()) {
					continue;
				}
				// String name = (String) feature.getAttribute("NAME");
				// if (name == null)
				// name = (String) feature.getAttribute("CNTRY_NAME");
				if (intersection instanceof MultiPolygon) {
					MultiPolygon mp = (MultiPolygon) intersection;
					for (int i = 0; i < mp.getNumGeometries(); i++) {
						com.vividsolutions.jts.geom.Polygon g = (com.vividsolutions.jts.geom.Polygon) mp.getGeometryN(i);
						Geometry gIntersection = IntersectUtils.intersection(g, JTS.toGeometry(bounds));
						if (gIntersection.isEmpty()) {
							continue;
						}
						all.add(g);
					}
				} else if (intersection instanceof Polygon)
					all.add(intersection);
				else
					continue;
			}
		} finally {
			if (iterator != null) {
				iterator.close();
			}
		}
		GridCoverageFactory gridCoverageFactory = new GridCoverageFactory();
		Coverage coverage = gridCoverageFactory.create("Raster", image, bounds);
		Coverage clippedCoverage = null;
		if (all.size() > 0) {
			CoverageProcessor processor = new CoverageProcessor();
			ParameterValueGroup params = processor.getOperation("CoverageCrop").getParameters();
			params.parameter("Source").setValue(coverage);
			GeometryFactory factory = JTSFactoryFinder.getGeometryFactory(null);
			Geometry[] a = all.toArray(new Geometry[0]);
			GeometryCollection c = new GeometryCollection(a, factory);
			// params.parameter("ENVELOPE").setValue(bounds);
			params.parameter("ROI").setValue(c);
			params.parameter("ForceMosaic").setValue(true);
			clippedCoverage = processor.doOperation(params);
		}
		if (all.size() == 0) {
			logger.info("Crop by shapefile requested but no simple features matched extent!");
		}
		return clippedCoverage;
	}

	public void setRunState(RunState runState) {
		this.runState = runState;
	}

	@Override
	public void taskFinished(Runnable r) {
		Job j = (Job) r;
		if (j == this) {
			if (getRunState() == RunState.Failed) {
				logger.info("Job FAILED " + j.getExportProps().getJobName());
			} else {
				setRunState(RunState.FinishedOK);
				logger.info("Finished job " + j.getExportProps().getJobName());
			}
			emailUser();
			cleanUp();
		}
	}

	@Override
	public void taskRunning(Runnable r) {
		Job j = (Job) r;
		if (j == this) {
			setRunState(RunState.Running);
			logger.info("Running job " + j.getExportProps().getJobName());
		}
	}

	@Override
	public void taskQueued(Runnable r) {
		Job j = (Job) r;
		if (j == this) {
			setRunState(RunState.Queued);
			synchronized (jobQueue) {
				jobQueue.add(j);
			}
			logger.info("Queued job " + getExportProps().getJobName());
			((ExpressZipWindow) parent.getApplication().getMainWindow()).showNotification(String.format("Job \"%s\" Queued",
					getJobName()));
		}
	}

	@Override
	public void taskError(Runnable r, Throwable e) {
		Job j = (Job) r;
		if (j == this) {
			setRunState(RunState.Failed);
			emailUser();
			logger.error("Failed job " + j.getExportProps().getJobName() + " Cause: " + e.getMessage());
			cleanUp();
		}
	}

	private void emailUser() {
		ExportProps props = getExportProps();
		String email = props.getEmail();

		if (email == null || email.isEmpty() || email.equalsIgnoreCase(ExportProps.DefaultNotSupplied)) {
			logger.info("email not configured or address not given: not sending notification");
			return;
		}

		logger.info("Sending email notification to: " + email);
		StringBuilder msgBody = new StringBuilder();
		String statuslink = getDL_URL();
		if (getRunState() == RunState.Failed) {
			URL u = getUrl();
			statuslink = u.getProtocol() + "://" + u.getAuthority() + "/ExpressZip";
		}
		msgBody.append("<p>Your ExpressZip request, Job \"");
		msgBody.append(props.getJobName());
		msgBody.append("\" completed processing at ");
		DateFormat fmttime = new SimpleDateFormat("HH:mm:ss");
		DateFormat fmtdate = new SimpleDateFormat("MM/dd/yyyy");
		Date rightnow = Calendar.getInstance().getTime();
		msgBody.append(fmttime.format(rightnow) + " on ");
		msgBody.append(fmtdate.format(rightnow));
		msgBody.append(".  You may download your data at:</p>");
		msgBody.append("<a href=\"" + statuslink + "\">" + statuslink + "</a>");

		BackgroundExecutor executor = new BackgroundExecutor.Factory().getBackgroundExecutor();
		executor.getMailServices().sendEmail(email, "ExpressZip@" + getUrl().getHost(),
				"Job " + props.getJobName() + " has finished processing",
				msgBody.toString(), "text/html");
	}

	@Override
	public String toString() {
		return getJobName();
	}
}
