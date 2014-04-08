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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.ows.Layer;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.vaadin.vol.AbstractLayerBase;
import org.vaadin.vol.Bounds;
import org.vaadin.vol.Style;
import org.vaadin.vol.StyleMap;
import org.vaadin.vol.WellKnownTextLayer;

import com.lizardtech.expresszip.vaadin.ExpressZipApplication;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

public class VectorLayer extends ExpressZipLayer {

	private File filepath;
	private String wkt = null;

	public VectorLayer(File filepath, // file path on disk
			String basename // base name of shapefile files
	) {
		super(basename, null);
		try {
			this.filepath = filepath;
			assert (this.filepath.exists() && this.filepath.isDirectory());
			assert (buildShpFilePath().exists());

			getWktString(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public File buildShpFilePath() {
		System.out.println(filepath.toString() + "/" + getName() + ".shp");
		return new File(filepath.toString() + "/" + getName() + ".shp");
	}

	private SimpleFeatureSource featureSource;
	private ReferencedEnvelope nativeBounds;

	private String getWktString(String projection) throws NoSuchAuthorityCodeException, FactoryException,
			MismatchedDimensionException, TransformException, IOException {
		String retval = null;

		// On construction, we set up with the native CRS of the underlying
		// shapefile. If that's what they asked for, send it back.
		if ((wkt != null)
				&& (projection != null && CRS.equalsIgnoreMetadata(nativeBounds.getCoordinateReferenceSystem(),
						CRS.decode(projection))))
			return wkt;

		SimpleFeatureCollection featureCollection = null;
		FileDataStore store;
		store = FileDataStoreFinder.getDataStore(buildShpFilePath());
		featureSource = store.getFeatureSource();
		featureCollection = featureSource.getFeatures();
		nativeBounds = featureCollection.getBounds();

		boolean needsReproject = false;
		MathTransform transform = null;
		if (projection != null) {
			CoordinateReferenceSystem crsFeatures = featureSource.getSchema().getCoordinateReferenceSystem();
			CoordinateReferenceSystem crsMap = (projection != null) ? CRS.decode(projection) : nativeBounds
					.getCoordinateReferenceSystem();
			needsReproject = !CRS.equalsIgnoreMetadata(crsFeatures, crsMap);
			transform = CRS.findMathTransform(crsFeatures, crsMap, true);
		}
		if (featureCollection != null) {
			String tmp = new String("GEOMETRYCOLLECTION ( ");
			SimpleFeatureIterator iterator = featureCollection.features();
			while (iterator.hasNext()) {
				SimpleFeature feature = iterator.next();
				Geometry geo = (Geometry) feature.getDefaultGeometry();

				if (geo.isValid() && geo.isSimple()) {
					if (needsReproject) {
						geo = JTS.transform(geo, transform);
					}
					WKTWriter ww = new WKTWriter();
					String multipolyStr = ww.writeFormatted(geo);
					tmp += multipolyStr;

					if (iterator.hasNext())
						tmp += ", ";
				}
			}
			iterator.close();
			tmp += " ) ";

			boolean dumpWKT = false;
			if (dumpWKT)
				dumpWktString(tmp);

			if (projection == null || CRS.equalsIgnoreMetadata(nativeBounds.getCoordinateReferenceSystem(), CRS.decode(projection))) {
				wkt = tmp;
			}
			retval = tmp;
		}
		return retval;
	}

	private void dumpWktString(String wkt) {
		Writer writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("wkt.txt"), "utf-8"));
			writer.write(wkt);
		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
			}
		}
	}

	@Override
	public AbstractLayerBase createVaadinLayer(String projection) {
		WellKnownTextLayer wktLayer = new WellKnownTextLayer();
		wktLayer.setDisplayName(getName());
		wktLayer.setProjection(projection);
		Style mystyle = new Style();
		mystyle.extendCoreStyle("default");
		mystyle.setStrokeColor("#D4BFF2");
		mystyle.setStrokeWidth(4);
		mystyle.setFillOpacity(0);

		Style selectStyle = new Style();
		selectStyle.setStrokeColor("#D4BFF2");
		selectStyle.setStrokeWidth(4);

		StyleMap stylemap = new StyleMap(mystyle, selectStyle, selectStyle);
		stylemap.setExtendDefault(true);
		wktLayer.setStyleMap(stylemap);
		try {
			wktLayer.setWellKnownText(getWktString(projection));
		} catch (Exception e) {
			ExpressZipApplication.logger.error("Unable to get WKTString for projection " + projection);
			wktLayer = null;
		}
		return wktLayer;
	}

	@Override
	protected void addChild(Layer child) {
		// TODO Auto-generated method stub

	}

	@Override
	public Bounds getBoundsForLayer(String proj) {
		BoundingBox env;
		try {
			env = nativeBounds.toBounds(CRS.decode(proj));
		} catch (Exception e) {
			return null;
		}
		Bounds b = new Bounds();
		b.setBottom(env.getMinY());
		b.setTop(env.getMaxY());
		b.setLeft(env.getMinX());
		b.setRight(env.getMaxX());
		return b;
	}

}
