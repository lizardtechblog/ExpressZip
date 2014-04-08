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

import java.math.BigInteger;

public class GriddingOptions {
	public enum GridMode {
		METERS, DIVISION
	}

	// if Division, use divX/divY. If Meters, use tileSizeX/tileSizeY
	private GridMode gridMode;

	private int exportWidth;
	private int exportHeight;
	private int tileSizeX;
	private int tileSizeY;
	private int divX;
	private int divY;
	private boolean isGridding;

	public GriddingOptions() {
		isGridding = false;
		gridMode = GridMode.DIVISION;
		exportWidth = 1;
		exportHeight = 1;
		tileSizeX = 1;
		tileSizeY = 1;
		divX = 1;
		divY = 1;
	}

	public GriddingOptions(GriddingOptions gridOptions) {
		isGridding = gridOptions.isGridding;
		setGridMode(gridOptions.getGridMode());
		setExportHeight(gridOptions.getExportHeight());
		setExportWidth(gridOptions.getExportWidth());
		setTileSizeX(gridOptions.getTileSizeX());
		setTileSizeY(gridOptions.getTileSizeY());
		setDivX(gridOptions.getDivX());
		setDivY(gridOptions.getDivY());
	}

	public void setGridding(boolean val) {
		isGridding = val;
	}

	public boolean isGridding() {
		return isGridding;
	}

	public int getExportWidth() {
		return exportWidth;
	}

	public void setExportWidth(int exportWidth) {
		this.exportWidth = exportWidth;
	}

	public int getExportHeight() {
		return exportHeight;
	}

	public void setExportHeight(int exportHeight) {
		this.exportHeight = exportHeight;
	}

	public int getTileSizeX() {
		return tileSizeX;
	}

	public void setTileSizeX(int tileSizeX) {
		this.tileSizeX = tileSizeX;
	}

	public int getTileSizeY() {
		return tileSizeY;
	}

	public void setTileSizeY(int tileSizeY) {
		this.tileSizeY = tileSizeY;
	}

	public int getDivX() {
		return divX;
	}

	public void setDivX(int divX) {
		this.divX = divX;
	}

	public int getDivY() {
		return divY;
	}

	public void setDivY(int divY) {
		this.divY = divY;
	}

	public GridMode getGridMode() {
		return gridMode;
	}

	public void setGridMode(GridMode gridMode) {
		this.gridMode = gridMode;
	}

	public long getNumTiles() {
		long number = 1;
		if (isGridding()) {
			// if Division, use divX/divY. If Meters, use tileSizeX/tileSizeY
			if (gridMode == GridMode.DIVISION) {
				number = divX * divY;
			} else {
				number = (long) Math.ceil((double) exportWidth / (double) tileSizeX)
						* (long) Math.ceil((double) exportHeight / (double) tileSizeY);
			}
		}
		return number;
	}

	public BigInteger getExportSize() {
		BigInteger size = BigInteger.valueOf(exportWidth).multiply(BigInteger.valueOf(exportHeight));
		size = size.multiply(BigInteger.valueOf(3L)); // 3 banded, 8-bit RGB
		return size;
	}
}
