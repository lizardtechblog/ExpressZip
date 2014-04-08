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

import java.util.ArrayList;
import java.util.List;

import org.vaadin.vol.Bounds;

import com.lizardtech.expresszip.model.MapModel;
import com.lizardtech.expresszip.model.OSMGeocode;
import com.lizardtech.expresszip.vaadin.ExpressZipButton.Style;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

public class MapToolbarViewComponent extends CustomComponent implements com.lizardtech.expresszip.ui.MapToolbarPresenter.View,
		Button.ClickListener {

	private static final long serialVersionUID = -5297579119173537360L;
	private HorizontalLayout hznToolbar = new HorizontalLayout();
	private TextField txtZoomTo = new TextField();
	private Button btnJobQueueStatus = new Button();
	private Button restart;
	private Button help;
	private List<MapToolbarViewListener> listeners = new ArrayList<MapToolbarViewListener>();

	public MapToolbarViewComponent() {
		hznToolbar.setMargin(true);
		hznToolbar.setSpacing(true);

		help = new ExpressZipButton("Help", Style.MENU, this);
		restart = new ExpressZipButton("Start Over", Style.MENU, this);

		txtZoomTo.setInputPrompt("Search...");
		txtZoomTo.setDescription("Geocoding courtesy of Open Street Map");
		txtZoomTo.addStyleName("searchbox");
		txtZoomTo.setWidth("260px");
		txtZoomTo.setImmediate(true);

		btnJobQueueStatus.setDescription("Job Manager");
		ThemeResource gear = new ThemeResource("img/JobManagementStandard23px.png");
		btnJobQueueStatus.setIcon(gear);
		btnJobQueueStatus.setStyleName(Button.STYLE_LINK);
		btnJobQueueStatus.addListener(this);

		Embedded logo = new Embedded(null, new ThemeResource("img/ExpZip_Logo161x33px.png"));
		hznToolbar.addComponent(logo);
		hznToolbar.setComponentAlignment(logo, Alignment.MIDDLE_LEFT);
		hznToolbar.setExpandRatio(logo, 0.35f);
		hznToolbar.addComponent(txtZoomTo);
		hznToolbar.setComponentAlignment(txtZoomTo, Alignment.MIDDLE_LEFT);
		hznToolbar.setExpandRatio(txtZoomTo, 0.25f);
		hznToolbar.addComponent(help);
		hznToolbar.setComponentAlignment(help, Alignment.MIDDLE_RIGHT);
		hznToolbar.setExpandRatio(help, 0.3f);
		hznToolbar.addComponent(restart);
		hznToolbar.setComponentAlignment(restart, Alignment.MIDDLE_LEFT);
		hznToolbar.setExpandRatio(restart, 0.15f);
		hznToolbar.addComponent(btnJobQueueStatus);
		hznToolbar.setComponentAlignment(btnJobQueueStatus, Alignment.MIDDLE_CENTER);
		hznToolbar.setExpandRatio(btnJobQueueStatus, 0.15f);

		hznToolbar.setSizeFull();

		hznToolbar.addStyleName("header");

		setCompositionRoot(hznToolbar);

		// textFieldListener puts a marker with given input
		txtZoomTo.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 8461586871780709805L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				String target = txtZoomTo.getValue().toString();
				markAddress(target);
			}
		});
	}

	private void markAddress(String address) {

		MapModel mm = ((ExpressZipWindow) getApplication().getMainWindow()).getMapModel();
		Bounds bounds = OSMGeocode.getInstance().getBounds(address, mm.getCurrentProjection(), mm.getMap().getExtend());
		if (bounds == null) {
			((ExpressZipWindow) getApplication().getMainWindow()).showNotification("Could not find location!");
			return;
		}

		Bounds expandedBounds = MapModel.expandBounds(bounds, ((ExpressZipWindow) getApplication().getMainWindow()).getMapModel()
				.getCurrentProjection(), 0.1);

		((ExpressZipWindow) getApplication().getMainWindow()).getMapModel().zoomToExtent(
				expandedBounds != null ? expandedBounds : bounds);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button b = event.getButton();
		if (b == btnJobQueueStatus) {
			Window subWindow = new Window("Job Manager");
			subWindow.setWidth("500px");
			subWindow.center();
			getApplication().getMainWindow().addWindow(subWindow);

			Panel p = new Panel(new JobsStatusViewComponent(getApplication().getURL()));
			p.getContent().setWidth("100%");
			p.setWidth("100%");
			subWindow.addComponent(p);
			subWindow.setModal(true);
		} else if (b == help) {
			String HelpURL = getApplication().getURL().toExternalForm() + "doc";
			getApplication().getMainWindow().open(new ExternalResource(HelpURL), "_blank");
		} else if (b == restart) {
			((ExpressZipWindow) getApplication().getMainWindow()).getApplication().close();
		}
	}

	@Override
	public void addListener(MapToolbarViewListener listener) {
		listeners.add(listener);

	}
}
