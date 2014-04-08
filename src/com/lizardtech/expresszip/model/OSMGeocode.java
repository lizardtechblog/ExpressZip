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

import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.vaadin.vol.Bounds;
import org.vaadin.vol.Point;

/**
 * Geocoding via http://wiki.openstreetmap.org/wiki/Nominatim geocoding API. Terms of service at:
 * http://wiki.openstreetmap.org/wiki/Nominatim_usage_policy
 */
public class OSMGeocode extends Geocode {
	final private String osmGeocodeURL = "http://nominatim.openstreetmap.org/search";
	private static volatile OSMGeocode instance;

	private OSMGeocode() {
	}

	public static OSMGeocode getInstance() {
		if (instance == null) {
			synchronized (OSMGeocode.class) {
				if (instance == null) {
					instance = new OSMGeocode();
				}
			}
		}
		return instance;
	}

	public Bounds getBounds(String address, String mapProjection, Bounds extent) {

		Bounds viewboxConstraint = MapModel.reprojectBounds(extent, mapProjection, MapModel.EPSG4326);
		JSONArray result = getJSONArray(address, viewboxConstraint);
		if (result == null || result.size() == 0)
			return null;
		JSONObject resultData = result.getJSONObject(0);
		JSONArray bbox = resultData.getJSONArray("boundingbox");
		if (bbox == null) {
			Double lat = resultData.getDouble("lat");
			Double lon = resultData.getDouble("lon");
			bbox = new JSONArray();
			if (lat == null || lon == null)
				return null;
			bbox.add(lat);
			bbox.add(lat);
			bbox.add(lon);
			bbox.add(lon);
		}

		Bounds bounds = new Bounds();
		bounds.setMinLat(bbox.getDouble(0));
		bounds.setMaxLat(bbox.getDouble(1));
		bounds.setMinLon(bbox.getDouble(2));
		bounds.setMaxLon(bbox.getDouble(3));

		if (!mapProjection.equals(MapModel.EPSG4326)) {
			bounds = MapModel.reprojectBounds(bounds, MapModel.EPSG4326, mapProjection);
		}

		return bounds;
	}

	@Override
	public String getAddress(Point coordinates) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	/**
	 * extent must be reprojected to EPSG:4326 for OSM Nominatim
	 */
	protected JSONArray getJSONArray(String address, Bounds extent) {
		String query = getQuery(address);
		String jsonText = null;
		try {
			StringBuilder viewBoxConstraint = new StringBuilder();
			if (extent != null) {
				viewBoxConstraint.append("&bounded=0&viewbox=");
				viewBoxConstraint.append(String.format("%f", extent.getLeft()) + ",");
				viewBoxConstraint.append(String.format("%f", extent.getTop()) + ",");
				viewBoxConstraint.append(String.format("%f", extent.getRight()) + ",");
				viewBoxConstraint.append(String.format("%f", extent.getBottom()));
			}
			String params = "&format=json" + viewBoxConstraint.toString();
			URLConnection connection = new URL(osmGeocodeURL + query + params).openConnection();
			Scanner scanner = new Scanner(connection.getInputStream());
			scanner.useDelimiter("\\Z");
			jsonText = scanner.next();
			System.out.println(jsonText);
			scanner.close();
			JSONArray jsonArr = JSONArray.fromObject(jsonText);
			return jsonArr;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	protected String getQuery(String address) {
		String[] addressSplit = address.split("[ ]+");
		// String addressQuery = "?address=";
		String addressQuery = "?q=";
		for (String str : addressSplit) {
			addressQuery += str + "+";
		}
		addressQuery = addressQuery.substring(0, addressQuery.length() - 1);
		// addressQuery += "&sensor=false";
		return addressQuery;
	}

}
