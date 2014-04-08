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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.wolfie.refresher.Refresher;
import com.github.wolfie.refresher.Refresher.RefreshListener;
import com.lizardtech.expresszip.model.Job;
import com.lizardtech.expresszip.model.Job.RunState;
import com.lizardtech.expresszip.vaadin.ExpressZipButton.Style;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class JobsStatusViewComponent extends CustomComponent implements RefreshListener {

	private HorizontalLayout tableControls;
	private Form form = null;
	private Button btnRemove;
	private Table table;
	private BeanItemContainer<Job> container;
	private static final List<String> formfields = Collections.unmodifiableList(Arrays.asList("exportProps.jobName", "status",
			"DL_URL", "exportProps.userNotation", "log_URL"));
	private static final long UPDATE_STATUS_INTERVAL = 5000;

	public JobsStatusViewComponent(URL appUrl) {

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setMargin(true);
		mainLayout.setWidth("100%");

		// Refresher will update UI as progress is made
		final Refresher refresher = new Refresher();
		refresher.setRefreshInterval(UPDATE_STATUS_INTERVAL);
		refresher.addListener(this);
		mainLayout.addComponent(refresher);

		mainLayout.addComponent(buildTableControls());

		form = new Form();
		form.setCaption("Selected Job");
		form.setWidth("420px");
		form.setFormFieldFactory(new ExpressZipFieldFactory());
		form.setVisible(true);
		form.setImmediate(true);

		table = new Table(null);
		table.addStyleName("expresszip");
		table.setWidth("100%");
		table.setSelectable(true);
		table.setImmediate(true);
		table.setNullSelectionAllowed(false);
		table.setPageLength(0);
		table.setHeight("250px");
		container = new BeanItemContainer<Job>(Job.class, Job.getJobQueue());
		container.addNestedContainerProperty("exportProps.jobName");
		container.addNestedContainerProperty("exportProps.userNotation");
		table.setContainerDataSource(container);

		table.setVisibleColumns(new String[] { "exportProps.jobName", "exportProps.userNotation", "status" });
		table.setColumnHeaders(new String[] { "Job Name", "User Name", "Status" });
		table.sort(new Object[] { "exportProps.jobName", "exportProps.userNotation" }, new boolean[] { true, true });
		table.setColumnExpandRatio("status", 0.8f);
		
		// use green bar to highlight selected row
		ExpressZipTreeTable.enableFirstColumnHighlighter(table);
		
		updateTableData();

		mainLayout.addComponent(table);
		mainLayout.setExpandRatio(table, 1.0f);

		mainLayout.addComponent(form);

		Link browseExports = new Link("Browse Archived Jobs", new ExternalResource(appUrl.getProtocol() + "://"
				+ appUrl.getAuthority() + "/exportdir/"));
		// Open the URL in a new window/tab
		browseExports.setTargetName("_blank");
		mainLayout.addComponent(browseExports);

		// setContent(mainLayout);
		setCompositionRoot(mainLayout);
	}

	private void removeJob(Job j) {

		Job.removeJobFromQueue(j);
		table.removeItem(j);
		if (table.firstItemId() != null) {
			table.setValue(table.firstItemId());
		}
	}

	private Component buildTableControls() {
		tableControls = new HorizontalLayout();
		tableControls.setWidth("100%");
		btnRemove = new ExpressZipButton("Remove", Style.ACTION, new Button.ClickListener() {
			private static final long serialVersionUID = 4560276967826799268L;
			@Override
			public void buttonClick(ClickEvent event) {
				final Job j = (Job) table.getValue();
				if( null == j )
					return;
				else if (j.getRunState() == RunState.Queued || j.getRunState() == RunState.Running) {

					getApplication().getMainWindow().addWindow(
							new YesNoDialog("Confirm:  Cancel Job?", "Are you sure you want to cancel the selected job?",
									new YesNoDialog.Callback() {
										@Override
										public void onDialogResult(boolean resultIsYes) {
											if (resultIsYes) {
												j.cancel();
												removeJob(j);
											}
										}
									}));
				} else {
					removeJob(j);
				}
			}
		});

		btnRemove.setEnabled(false);
		tableControls.addComponent(btnRemove);

		String version = "unknown";

		InputStream stream = null;
		BufferedReader reader = null;
		InputStreamReader inputStream = null;
		try {
			ClassLoader loader = ExpressZipWindow.class.getClassLoader();
			stream = loader.getResourceAsStream("/VERSION.txt");
			inputStream = new InputStreamReader(stream);
			reader = new BufferedReader(inputStream);
			StringBuilder sb = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append(String.format("%n"));
			}

			String versionFile = sb.toString();

			Pattern regex = Pattern.compile("(Product version: )(.*$)", Pattern.MULTILINE);
			Matcher groups = regex.matcher(versionFile);
			if (groups.find()) {
				version = groups.group(2);
			}

		} catch (Exception e) {
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
			}
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {
			}
		}

		Label versionAdminUI = new Label("version " + version);
		tableControls.addComponent(versionAdminUI);
		tableControls.setComponentAlignment(versionAdminUI, Alignment.MIDDLE_RIGHT);
		tableControls.setExpandRatio(btnRemove, 2);
		tableControls.setExpandRatio(versionAdminUI, 1);

		return tableControls;
	}

	private synchronized void updateTableData() {

		table.addListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				Object value = table.getValue();
				btnRemove.setEnabled(value != null);
				form.setItemDataSource(container.getItem(value), formfields);
			}
		});
		
		if (table.firstItemId() != null) {
			table.setValue(table.firstItemId());
		}

	}

	@Override
	public void refresh(Refresher source) {
		synchronized (this) {
			Object scrollLocation = table.getCurrentPageFirstItemId();
			table.refreshRowCache();
			form.setItemDataSource(container.getItem(table.getValue()), formfields);
			table.setCurrentPageFirstItemId(scrollLocation);
		}
	}
}
