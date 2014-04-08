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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.geotools.data.FeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.lizardtech.expresszip.vaadin.ExpressZipApplication;
import com.lizardtech.expresszip.vaadin.ExpressZipWindow;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

public class SetupMapModel {
	static public String basename(String filename) {
		return filename.substring(0, filename.lastIndexOf('.'));
	}

	private Window parent;

	public SetupMapModel(Window w) {
		parent = w;
	}

	private HashMap<String, Integer> ShapefileRefCountMap = new HashMap<String, Integer>();

	public void ShapeFileIncrement(String f) {
		if (!ShapefileRefCountMap.containsKey(f))
			throw new IllegalArgumentException("No such shapefile " + f);
		else {
			Integer v = ShapefileRefCountMap.get(f);
			v++;
			ShapefileRefCountMap.put(f, v);
		}
	}

	public void ShapeFileDecrement(String f) {
		if (!ShapefileRefCountMap.containsKey(f))
			throw new IllegalArgumentException("No such shapefile " + f);
		else {
			Integer v = ShapefileRefCountMap.get(f);
			v--;
			if (v < 0)
				throw new RuntimeException("Negative Reference Count!");
			else if (v == 0) {
				shapeFileDeleteWildCard(f);
				ShapefileRefCountMap.remove(f);
			} else
				// if (v>0)
				ShapefileRefCountMap.put(f, v);
		}
	}

	private void shapeFileDeleteWildCard(final String basename) {
		final File sessionShapeDir = getSessionShapeFileDir();
		final File folder_zipextract = new File(sessionShapeDir + "/" + basename);
		if (folder_zipextract.exists() && folder_zipextract.isDirectory()) {
			for (File file : folder_zipextract.listFiles())
				if (!file.delete())
					ExpressZipApplication.logger.error("Failed to delete file " + file.getAbsolutePath());
			if (folder_zipextract.listFiles().length == 0) {
				folder_zipextract.delete();
			}
		}
		final File[] files = sessionShapeDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return name.matches(basename + "*");
			}
		});
		for (final File file : files) {
			if (!file.delete()) {
				ExpressZipApplication.logger.error("Failed to delete file " + file.getAbsolutePath());
			}
		}
		if (sessionShapeDir.listFiles().length == 0) {
			sessionShapeDir.delete();
		}
	}

	private File getSessionShapeFileDir() {
		String exportdir = ExpressZipApplication.Configuration.getProperty("exportdirectory");
		return new File(exportdir, "shpFiles" + ((ExpressZipApplication) parent.getApplication()).getApplicationID());
	}

	public void ShapefileRefCount_DecrementAll() {
		ArrayList<String> keys = new ArrayList<String>();
		for (Map.Entry<String, Integer> entry : ShapefileRefCountMap.entrySet()) {
			String v = entry.getKey();
			keys.add(v);
		}

		for (String key : keys) {
			ShapeFileDecrement(key);
		}
	}

	// returns null if failed to extract zip
	public VectorLayer shapeFileUploaded(String filename, ByteArrayInputStream input) {
		VectorLayer retval = null;
		File sessionShapeDir = getSessionShapeFileDir();
		String basename = basename(filename);
		String extension = filename.substring(filename.lastIndexOf('.') + 1);
		String dstDir = sessionShapeDir + "/" + basename + "/";

		if (!(new File(dstDir)).exists()) {
			if (!(new File(dstDir)).mkdirs()) {
				// TODO: permissions error?
				return null;
			}
		}
		ShapefileRefCountMap.put(basename, 1); // First reference is this session.
		if (extension.equalsIgnoreCase("zip")) { // Additional reference for each Job.
			if (extractZip(dstDir, input)) {
				File cropFile = new File(dstDir, basename + ".shp");
				if (validateShapeFile(cropFile))
					retval = new VectorLayer(new File(dstDir), basename);
			} else {
				((ExpressZipWindow) parent.getApplication().getMainWindow()).showNotification("Not a valid ShapeFile",
						"Put at least a .shp, .shx, .dbf, and .prj files into a ZIP archive for upload.",
						Notification.TYPE_ERROR_MESSAGE);
			}
		} else if (extension.equalsIgnoreCase("shp")) {
			((ExpressZipWindow) parent.getApplication().getMainWindow()).showNotification("Shapefiles must zipped",
					"Put at least the .shp, .shx, .dbf, and .prj files into a ZIP archive for upload.",
					Notification.TYPE_ERROR_MESSAGE);
		} else {
			((ExpressZipWindow) parent.getApplication().getMainWindow()).showNotification("Only zipped shapefiles accepted",
					"Put at least a .shp, .shx, .dbf, and .prj files into a ZIP archive for upload.",
					Notification.TYPE_ERROR_MESSAGE);
		}

		return retval;
	}

	private boolean validateShapeFile(File cropFile) {
		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = null;
		boolean retval = true;
		String errString = null;
		try {
			featureSource = Job.getFeatureSource(cropFile);
		} catch (MalformedURLException e) {
			errString = "Failed to open FeatureSource from " + cropFile.getName();
		} catch (IOException e) {
			errString = "Failed to open file " + cropFile.getName();
		} catch (RuntimeException e) {
			errString = e.getMessage();
		}
		CoordinateReferenceSystem crsFeatures = featureSource.getSchema().getCoordinateReferenceSystem();
		if (crsFeatures == null) {
			errString = "Failed to find CRS (is there a .PRJ file?) in the archive for " + cropFile.getName();
		}
		if (errString != null) {
			ExpressZipApplication.logger.error(errString);
			((ExpressZipWindow) parent.getApplication().getMainWindow()).showNotification(
					"Failed to add invalid shapefile archive", "<br/>" + errString, Notification.TYPE_ERROR_MESSAGE);
			retval = false;
		}
		return retval;
	}

	private boolean validateUnzippedShapefile(String unzipDir) {
		// Make sure there is at a minimum a shapeName.shp file in dstDir
		File shapeDir = new File(unzipDir);
		if (shapeDir.isDirectory()) {
			String shapeName = shapeDir.getName();
			return new File(shapeDir, shapeName + ".shp").exists();
		} else {
			return false;
		}
	}

	private boolean extractZip(String dstDir, ByteArrayInputStream input) {
		try {
			byte[] buf = new byte[1024];
			ZipInputStream zipinputstream = null;
			ZipEntry zipentry;
			zipinputstream = new ZipInputStream(input);

			zipentry = zipinputstream.getNextEntry();
			while (zipentry != null) {
				// for each entry to be extracted
				String entryName = zipentry.getName();
				System.out.println("entryname " + entryName);
				int n;
				FileOutputStream fileoutputstream;
				File newFile = new File(entryName);
				String directory = newFile.getParent();

				if (directory == null)
					if (newFile.isDirectory())
						break;

				fileoutputstream = new FileOutputStream(dstDir + entryName);

				while ((n = zipinputstream.read(buf, 0, 1024)) > -1)
					fileoutputstream.write(buf, 0, n);

				fileoutputstream.close();
				zipinputstream.closeEntry();
				zipentry = zipinputstream.getNextEntry();
			}

			zipinputstream.close();

			if (!validateUnzippedShapefile(dstDir)) {
				new File(dstDir).delete();

				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
