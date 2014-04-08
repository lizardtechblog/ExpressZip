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

import net.sf.json.JSONArray;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.vaadin.vol.Bounds;
import org.vaadin.vol.Point;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public abstract class Geocode {
	/**
	 * Constructor
	 */
	public Geocode() {
	}

	public abstract String getAddress(Point coordinates);

	/**
	 * Return a JSON encoded Bounds or null
	 * 
	 * @param address
	 *            Query string appropriate for implementation class
	 * @param extent
	 *            Implementation dependent: constrain search to bounds reprojected to required CRS
	 * @return JSONArray containing the bounding box returned by the Geocode implementation
	 */
	protected abstract JSONArray getJSONArray(String address, Bounds extent);

	protected abstract String getQuery(String address);

	/**
	 * Creates a point given the latitude and longitude in one current projection system into the new projection system.
	 * 
	 * @param lat
	 * @param lng
	 * @param currentEPSG
	 * @param newEPSG
	 * @return point in newEPSG
	 */
	protected Point createPoint(double lat, double lng, String currentEPSG, String newEPSG) {
		try {
			CoordinateReferenceSystem worldCRS = CRS.decode(newEPSG);
			CoordinateReferenceSystem geocodeCRS = CRS.decode(currentEPSG);
			boolean lenient = true; // allow for some error due to different
									// datums
			MathTransform transform = CRS.findMathTransform(geocodeCRS, worldCRS, lenient);

			// Create the point in currentEPSG
			GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
			Coordinate coord = new Coordinate(lat, lng);

			// Transfrom the point into newEPSG
			JTS.transform(coord, coord, transform);
			com.vividsolutions.jts.geom.Point point = geometryFactory.createPoint(coord);

			lat = point.getX();
			lng = point.getY();

			return new Point(lat, lng);

		} catch (NoSuchAuthorityCodeException e) {
			e.printStackTrace();
		} catch (FactoryException e) {
			e.printStackTrace();
		} catch (TransformException e) {
			e.printStackTrace();
		}

		return null;
	}
}
