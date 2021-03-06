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

import org.vaadin.vol.ProjectedLayer;

import com.lizardtech.expresszip.model.MapModel;

public interface FindLayersView {

	public void addListener(FindLayersViewListener listener);

	public void setupMap(MapModel model);

	interface FindLayersViewListener extends ExpressZipLayerListener {
		void filtersChangedEvent();

		void baseMapChanged(ProjectedLayer base);

		void setIncludedLocalBaseMap(String localBaseLayerName);
	}
}
