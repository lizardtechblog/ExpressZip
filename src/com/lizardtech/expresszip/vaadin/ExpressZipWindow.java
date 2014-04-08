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

import org.apache.log4j.Logger;

import com.github.wolfie.refresher.Refresher;
import com.lizardtech.expresszip.model.ExportProps;
import com.lizardtech.expresszip.model.MapModel;
import com.lizardtech.expresszip.model.SetupMapModel;
import com.lizardtech.expresszip.ui.ExportOptionsPresenter;
import com.lizardtech.expresszip.ui.FindLayersPresenter;
import com.lizardtech.expresszip.ui.SetupMapPresenter;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings({ "serial" })
public class ExpressZipWindow extends Window {

	private Refresher refresher;

	private HorizontalSplitPanel splDivider;
	private VerticalLayout mainLayout;
	private VerticalLayout left;
	private VerticalLayout right;
	private float widthLeftPane;

	// Views
	private FindLayersViewComponent findLayers;
	private SetupMapViewComponent modifyLayers;

	private MapToolbarViewComponent mapToolbar;
	private OpenLayersMapViewComponent mapComponent;

	// Models
	private SetupMapModel setupMapModel;
	private MapModel mapModel;

	// Presenters
	private FindLayersPresenter findLayersPresenter;
	private SetupMapPresenter setupMapPresenter;
	private ExportOptionsPresenter exportOptionsPresenter;

	private ExportProps exportProps;
	private ExportOptionsViewComponent exportOptions;
	static final Logger logger = Logger.getLogger(ExpressZipWindow.class);

	// Create Export model and view
	// The code has to be declared in this order or else there will be null
	// references to map

	public ExpressZipWindow() {
	}

	@Override
	public void attach() {
		super.attach();

		// Setup the map in the findLayers pane
		splDivider = new HorizontalSplitPanel();
		mainLayout = new VerticalLayout();
		left = new VerticalLayout();
		right = new VerticalLayout();

		exportProps = new ExportProps();
		mapModel = new MapModel();

		// Views
		findLayers = new FindLayersViewComponent();
		modifyLayers = new SetupMapViewComponent();
		exportOptions = new ExportOptionsViewComponent(exportProps);

		mapToolbar = new MapToolbarViewComponent();
		mapComponent = new OpenLayersMapViewComponent();

		// Models
		setupMapModel = new SetupMapModel(this);

		// Presenters
		findLayersPresenter = new FindLayersPresenter(findLayers, mapComponent, mapModel);
		setupMapPresenter = new SetupMapPresenter(getSetupMapModel(), modifyLayers, mapComponent, mapModel);
		exportOptionsPresenter = new ExportOptionsPresenter(exportOptions);

		exportOptions.addListener(setupMapPresenter);

		refresher = new Refresher();
		addComponent(refresher);

		setCaption("ExpressZip");
		setSizeFull();
		splDivider.setSplitPosition(351, UNITS_PIXELS);

		mainLayout.setSizeFull();
		mainLayout.addComponent(mapToolbar);
		mainLayout.addComponent(splDivider);
		mainLayout.setExpandRatio(splDivider, 1f);

		splDivider.setSizeFull();
		splDivider.addComponent(left);
		splDivider.addComponent(right);

		setContent(mainLayout);

		right.setSpacing(true);

		right.setSizeFull();
		left.setSizeFull();

		left.addComponent(findLayers);
		setTheme("ExpressZip");
		// http://docs.geotools.org/latest/userguide/library/referencing/order.html
		System.setProperty("org.geotools.referencing.forceXY", "true");

		right.addComponent(mapComponent);

		right.setExpandRatio(mapComponent, 1f);

		try {
			findLayersPresenter.paneEntered();
		} catch (Exception e) {
			logger.error("Failed setting up map", e);
			showNotification("Setting Up Map Failed", "Check logs on server", Notification.TYPE_ERROR_MESSAGE);
		}
	}

	public void advanceToNext() {
		if (left.getComponent(0) == findLayers) {
			left.removeComponent(findLayers);
			left.addComponent(modifyLayers);
			findLayersPresenter.paneExited();
			if (!setupMapPresenter.paneEntered())
				regressToPrev();
		} else if (left.getComponent(0) == modifyLayers) {
			left.removeComponent(modifyLayers);
			exportOptions.setGriddingDrawEnabled(true);
			left.addComponent(exportOptions);
			setupMapPresenter.paneExited();
			exportOptionsPresenter.paneEntered();
		}
	}

	public void regressToPrev() {
		if (left.getComponent(0) == modifyLayers) {
			left.removeComponent(modifyLayers);
			left.addComponent(findLayers);
			setupMapPresenter.paneExited();
			findLayersPresenter.paneEntered();
		} else if (left.getComponent(0) == exportOptions) {
			exportOptions.setGriddingDrawEnabled(false);
			left.removeComponent(exportOptions);
			left.addComponent(modifyLayers);
			setupMapPresenter.paneEntered();
		}
	}

	public SetupMapModel getSetupMapModel() {
		return setupMapModel;
	}

	public void setSetupMapModel(SetupMapModel setupMapModel) {
		this.setupMapModel = setupMapModel;
	}

	public MapModel getMapModel() {
		return mapModel;
	}

	public void setMapModel(MapModel mapModel) {
		this.mapModel = mapModel;
	}

	public ExportProps getExportProps() {
		return exportProps;
	}

	public void setExportProps(ExportProps exportProps) {
		this.exportProps = exportProps;
	}
}
