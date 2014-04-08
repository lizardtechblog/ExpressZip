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
package com.lizardtech.expresszip.vaadin;

import java.io.Serializable;

import org.vaadin.vol.OpenLayersMap;

import com.lizardtech.expresszip.ui.OpenLayersMapView;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

public class OpenLayersMapViewComponent extends CustomComponent implements OpenLayersMapView, Serializable {

	private static final long serialVersionUID = -1897768227190404590L;

	VerticalLayout mapLayout = new VerticalLayout();
	OpenLayersMap displayedMap = null;

	public OpenLayersMapViewComponent() {
		setSizeFull();

		mapLayout.setSizeFull();
		mapLayout.setSizeFull();

		setCompositionRoot(mapLayout);
	}

	// display 'map' with 'bounds' and gridding 'options'
	@Override
	public void displayMap(OpenLayersMap map) {

		if (displayedMap != null) {
			mapLayout.removeComponent(displayedMap);
		}
		displayedMap = map;

		mapLayout.setSizeFull();
		mapLayout.addComponent(displayedMap);
	}

	@Override
	public void removeMap() {
		if (displayedMap != null) {
			mapLayout.removeComponent(displayedMap);
		}
		displayedMap = null;
	}
}
