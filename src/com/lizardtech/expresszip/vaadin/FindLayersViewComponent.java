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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.vaadin.vol.Layer;
import org.vaadin.vol.ProjectedLayer;
import org.vaadin.vol.WebMapServiceLayer;

import com.lizardtech.expresszip.model.ExpressZipLayer;
import com.lizardtech.expresszip.model.Filter;
import com.lizardtech.expresszip.model.Filter.AxisFilters;
import com.lizardtech.expresszip.model.Filter.FilterObject;
import com.lizardtech.expresszip.model.MapModel;
import com.lizardtech.expresszip.ui.FindLayersView;
import com.lizardtech.expresszip.vaadin.ExpressZipButton.Style;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

public class FindLayersViewComponent extends CustomComponent implements FindLayersView, Button.ClickListener, Action.Handler {
	private static final long serialVersionUID = 912537433588738669L;
	private static final String RENDER = "Render";
	private static final String EXPORT = "Export";
	private static final String BASEMAP = "Basemap";

	private ExpressZipButton btnBack;
	private ExpressZipButton btnNext;
	private ExpressZipButton btnAddFilter;
	private TreeTable treeTable;
	private AxisSelected axisSelectedListener;
	private FilterListeners filterButtonListener;
	private HashMap<Button, FilterObject> hshFilterButtons;
	private Window wndAddFilter = null;
	private HorizontalLayout hznCriteria;
	private CssLayout cssLayers;
	private ComboBox basemapSelector;
	private CheckBox includeBasemap;
	private AxisFilters filteringOn;
	private Filter filter;
	private List<FindLayersViewListener> listeners;
	private MapModel mapModel;
	private TreeTable popupTable;
	private ValueChangeListener popupSelectionListener;

	// Actions for the context menu
	private static final Action ACTION_ZOOM = new Action("Zoom to");
	private static final Action SELECT_ALL = new Action("Select All");
	private static final Action SELECT_NONE = new Action("Clear selection");
	private static final Action EXPORT_ALL = new Action("Export all");
	private static final Action EXPORT_NONE = new Action("Export none");
	private static final Action[] ACTIONS = new Action[] { ACTION_ZOOM, EXPORT_ALL, EXPORT_NONE, SELECT_ALL, SELECT_NONE };

	public FindLayersViewComponent() {

		treeTable = new ExpressZipTreeTable();
		popupTable = new ExpressZipTreeTable();
		configureTable(treeTable);

		popupSelectionListener = new Property.ValueChangeListener() {
			private static final long serialVersionUID = 625365970493526725L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				// the current popup selection
				Set<ExpressZipLayer> popupSelection = (Set<ExpressZipLayer>) event.getProperty().getValue();

				// get the tree's current selection
				HashSet<ExpressZipLayer> treeSelection = new HashSet<ExpressZipLayer>((Set<ExpressZipLayer>) treeTable.getValue());

				// remove all items in common with popup
				treeSelection.removeAll(popupTable.getItemIds());

				// set the treeTable selection to the union
				Set<ExpressZipLayer> unionSelection = new HashSet<ExpressZipLayer>();
				unionSelection.addAll(popupSelection);
				unionSelection.addAll(treeSelection);
				treeTable.setValue(unionSelection);
			}
		};
		popupTable.addListener(popupSelectionListener);

		treeTable.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 6236114836521221107L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Set<ExpressZipLayer> highlightedLayers = (Set<ExpressZipLayer>) event.getProperty().getValue();
				for (FindLayersViewListener listener : listeners) {
					listener.layerHighlightedEvent(highlightedLayers);
				}

				// reset selection of popup table
				popupTable.removeListener(popupSelectionListener);

				// intersection of treeTable's selection and popupTable items
				Set<ExpressZipLayer> popupSelection = new HashSet<ExpressZipLayer>();
				popupSelection.addAll(highlightedLayers);
				popupSelection.retainAll(popupTable.getItemIds());
				popupTable.setValue(popupSelection);

				popupTable.addListener(popupSelectionListener);
			}
		});
		configureTable(popupTable);

		filter = new Filter(this);
		filterButtonListener = new FilterListeners();
		axisSelectedListener = new AxisSelected();
		listeners = new ArrayList<FindLayersViewListener>();
		btnNext = new ExpressZipButton("Next", Style.STEP);
		btnBack = new ExpressZipButton("Back", Style.STEP);
		
		btnAddFilter = new ExpressZipButton("Add Filter", Style.ACTION);
		btnAddFilter.addStyleName("filter-flow");

		hshFilterButtons = new HashMap<Button, FilterObject>();
		cssLayers = new CssLayout();

		basemapSelector = new ComboBox();
		basemapSelector.setWidth(100.0f, UNITS_PERCENTAGE);
		basemapSelector.setTextInputAllowed(false);
		basemapSelector.setImmediate(true);
		basemapSelector.setNullSelectionAllowed(false);
		basemapSelector.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = -7358667131762099215L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				ProjectedLayer l = (ProjectedLayer) basemapSelector.getValue();
				boolean enableCheckbox = false;
				if (l instanceof WebMapServiceLayer) {
					for (ExpressZipLayer local : mapModel.getLocalBaseLayers()) {
						if (l.toString().equals(local.getName())) {
							enableCheckbox = true;
							break;
						}
					}
				}
				includeBasemap.setEnabled(enableCheckbox);
				if(!enableCheckbox) {
					includeBasemap.setValue(false);
				}

				if (mapModel.getBaseLayerTerms(l) != null && !mapModel.getBaseLayerTermsAccepted(l)) {
					final Window modal = new Window("Terms of Use");
					modal.setModal(true);
					modal.setClosable(false);
					modal.setResizable(false);
					modal.getContent().setSizeUndefined(); // trick to size around content
					Button bOK = new ExpressZipButton("OK", Style.ACTION, new ClickListener() {
						private static final long serialVersionUID = -2872178665349848542L;

						@Override
						public void buttonClick(ClickEvent event) {
							ProjectedLayer l = (ProjectedLayer) basemapSelector.getValue();
							mapModel.setBaseLayerTermsAccepted(l);
							for (FindLayersViewListener listener : listeners)
								listener.baseMapChanged(l);
							((ExpressZipWindow) getApplication().getMainWindow()).removeWindow(modal);
						}
					});
					Button bCancel = new ExpressZipButton("Cancel", Style.ACTION, new ClickListener() {
						private static final long serialVersionUID = -3044064554876422836L;

						@Override
						public void buttonClick(ClickEvent event) {
							basemapSelector.select(mapModel.getBaseLayers().get(0));
							((ExpressZipWindow) getApplication().getMainWindow()).removeWindow(modal);
						}
					});
					HorizontalLayout buttons = new HorizontalLayout();
					buttons.setSpacing(true);
					buttons.addComponent(bOK);
					buttons.addComponent(bCancel);
					Label termsText = new Label(mapModel.getBaseLayerTerms(l));
					termsText.setContentMode(Label.CONTENT_XHTML);
					VerticalLayout vlay = new VerticalLayout();
					vlay.addComponent(termsText);
					vlay.addComponent(buttons);
					vlay.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT);
					vlay.setWidth(400, UNITS_PIXELS);
					modal.getContent().addComponent(vlay);
					((ExpressZipWindow) getApplication().getMainWindow()).addWindow(modal);
				} else {
					for (FindLayersViewListener listener : listeners)
						listener.baseMapChanged(l);
				}
			}
		});

		includeBasemap = new CheckBox();
		includeBasemap.setDescription("Include this basemap in the exported image.");
		includeBasemap.setWidth(64f, UNITS_PIXELS);

		HorizontalLayout basemapLayout = new HorizontalLayout();
		basemapLayout.setWidth(100f, UNITS_PERCENTAGE);

		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSizeFull();
		setSizeFull();

		Label step = new Label("Step 1: Select Layers");
		step.addStyleName("step");
		layout.addComponent(step);

		layout.addComponent(treeTable);
		layout.setSpacing(true);
		treeTable.setSizeFull();
		layout.setExpandRatio(treeTable, 1f);

		layout.addComponent(new Panel(BASEMAP, basemapLayout));
		basemapLayout.addComponent(basemapSelector);
		basemapLayout.setExpandRatio(basemapSelector, 1f);
		basemapLayout.addComponent(includeBasemap);

		layout.addComponent(cssLayers);
		cssLayers.addComponent(btnAddFilter);

		HorizontalLayout backSubmitLayout = new HorizontalLayout();
		backSubmitLayout.setWidth("100%");
		backSubmitLayout.addComponent(btnBack);
		backSubmitLayout.addComponent(btnNext);
		backSubmitLayout.setComponentAlignment(btnBack, Alignment.BOTTOM_LEFT);
		backSubmitLayout.setComponentAlignment(btnNext, Alignment.BOTTOM_RIGHT);

		VerticalLayout navLayout = new VerticalLayout();
		navLayout.addComponent(backSubmitLayout);
		navLayout.setSpacing(true);

		ThemeResource banner = new ThemeResource("img/ProgressBar1.png");
		navLayout.addComponent(new Embedded(null,banner));
		
		layout.addComponent(navLayout);
		layout.setComponentAlignment(navLayout, Alignment.BOTTOM_CENTER);
		
		btnNext.addListener(this);
		btnNext.setEnabled(false);
		btnBack.setEnabled(false); // always disabled
		btnAddFilter.addListener(this);

		layout.addStyleName("findlayers");
		setCompositionRoot(layout);
	}

	public TreeTable getPopupTable() {
		return popupTable;
	}

	// FindLayersView implementations
	public void populateLayers(ExpressZipLayer rootLayer) {
		if (rootLayer != null) {
			addChildren(rootLayer);
		}
	}

	@Override
	public void addListener(FindLayersViewListener listener) {
		listeners.add(listener);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == btnNext) {
			// For 'Next' being pressed
			String localBaseLayerName = null;
			if (includeBasemap.booleanValue()) {
				ProjectedLayer basemap = (ProjectedLayer) basemapSelector.getValue();
				if (basemap instanceof WebMapServiceLayer) {
					localBaseLayerName = ((WebMapServiceLayer) basemap).getDisplayName();
				}
			}
			for (FindLayersViewListener l : listeners) {
				l.setIncludedLocalBaseMap(localBaseLayerName);
			}
			((ExpressZipWindow) getApplication().getMainWindow()).advanceToNext();
		} else if (event.getButton() == btnAddFilter) {
			// For 'Add Filter'

			// This window is already created and setup, add it and return
			ExpressZipWindow parent = (ExpressZipWindow) getApplication().getMainWindow();
			if (wndAddFilter != null) {
				parent.addWindow(wndAddFilter);
				return;
			}
			wndAddFilter = new Window("Add Filter");
			wndAddFilter.setModal(true);
			int height = 250;
			int width = 400;

			wndAddFilter.setWidth(width + "px");
			wndAddFilter.setHeight(height + "px");
			// Center the window based on browser width/height
			wndAddFilter.setPositionX((parent.getBrowserWindowWidth() - width) / 2);
			wndAddFilter.setPositionY((parent.getBrowserWindowHeight() - height) / 2);

			setupAddFilterWindow(wndAddFilter);

			parent.addWindow(wndAddFilter);
		}
	}

	private void setupAddFilterWindow(Window window) {
		// General variables

		// Layouts
		GridLayout mainLayout = new GridLayout(1, 3);
		HorizontalLayout axisLayout = new HorizontalLayout();
		HorizontalLayout criteriaLayout = new HorizontalLayout();
		HorizontalLayout buttonLayout = new HorizontalLayout();
		hznCriteria = criteriaLayout;

		// Buttons
		ExpressZipButton btnAdd = new ExpressZipButton("Add", Style.ACTION);
		btnAdd.setClickShortcut(KeyCode.ENTER);
		btnAdd.addStyleName("primary");
		ExpressZipButton btnCancel = new ExpressZipButton("Cancel", Style.ACTION);
	
		// Fields
		ComboBox cmbAxis = new ComboBox();
		cmbAxis.setTextInputAllowed(false);
		cmbAxis.setNullSelectionAllowed(false);

		// Labels
		Label lblAxis = new Label("Axis");

		btnAdd.addListener(filterButtonListener);
		btnCancel.addListener(filterButtonListener);

		for (Filter.AxisFilters f : Filter.axisArray) {
			cmbAxis.addItem(filter.getNameOfFilter(f));
		}
		cmbAxis.setImmediate(true);
		cmbAxis.addListener(axisSelectedListener);
		cmbAxis.setValue(filter.getNameOfFilter(Filter.axisArray[0]));

		mainLayout.addComponent(axisLayout, 0, 0);
		mainLayout.addComponent(criteriaLayout, 0, 1);
		mainLayout.addComponent(buttonLayout, 0, 2);
		mainLayout.setSpacing(true);

		axisLayout.setSpacing(true);

		axisLayout.addComponent(lblAxis);
		axisLayout.addComponent(cmbAxis);
		axisLayout.setExpandRatio(lblAxis, .2f);
		axisLayout.setExpandRatio(cmbAxis, .8f);
		axisLayout.setComponentAlignment(lblAxis, Alignment.MIDDLE_LEFT);
		axisLayout.setComponentAlignment(cmbAxis, Alignment.MIDDLE_LEFT);
		axisLayout.setSizeFull();

		criteriaLayout.setSizeFull();

		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(btnAdd);
		buttonLayout.addComponent(btnCancel);
		buttonLayout.setComponentAlignment(btnAdd, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(btnCancel, Alignment.BOTTOM_RIGHT);
		buttonLayout.setExpandRatio(btnAdd, 1f);
		buttonLayout.setExpandRatio(btnCancel, 0f);
		buttonLayout.setSizeFull();

		mainLayout.setRowExpandRatio(0, 1f);
		mainLayout.setRowExpandRatio(1, 1f);
		mainLayout.setRowExpandRatio(2, 1f);
		mainLayout.setSizeFull();

		window.addComponent(mainLayout);
		window.getContent().setSizeFull();
	}

	@Override
	public void setupMap(MapModel model) {
		this.mapModel = model;

		List<ProjectedLayer> baseLayers = model.getBaseLayers();
		BeanItemContainer<Layer> container = new BeanItemContainer<Layer>(Layer.class, baseLayers);
		basemapSelector.setContainerDataSource(container);
		if (baseLayers.size() > 0)
			basemapSelector.select(baseLayers.get(0));

		refreshMap();
	}

	private void refreshMap() {
		Set<ExpressZipLayer> selected = (Set<ExpressZipLayer>) treeTable.getValue();
		treeTable.removeAllItems();
		populateLayers(mapModel.getRootRasterLayer());
		treeTable.setValue(selected);
		for (ExpressZipLayer layer : mapModel.getChosenLayers()) {
			resetParentCheckBoxes(layer, true);
		}
	}

	public class FilterListeners implements Button.ClickListener {
		private static final long serialVersionUID = -7059229918698817362L;
		FilterObject fo = null;
		Button btnFilter;

		@Override
		public void buttonClick(ClickEvent event) {
			String buttCaption = event.getButton().getCaption();
			if (buttCaption != null && buttCaption.compareTo("Add") == 0) {

				Label lblFilter = null;
				switch (filteringOn) {
				case LAYER_NAME:
					lblFilter = new Label("Name:" + axisSelectedListener.getTxtString());
					fo = new FilterObject(AxisFilters.LAYER_NAME, axisSelectedListener.getCmbNamOperation(),
							axisSelectedListener.getTxtString());
					break;
				case KEYWORD:
					lblFilter = new Label("Keywd:" + axisSelectedListener.getTxtKeyword());
					fo = new FilterObject(AxisFilters.KEYWORD, axisSelectedListener.getCmbKeyOperation(),
							axisSelectedListener.getTxtKeyword());
					break;
				case LOCATION:
					lblFilter = new Label("Loc:" + axisSelectedListener.getTxtLocation());
					fo = new FilterObject(AxisFilters.LOCATION, axisSelectedListener.getCmbLocOp(),
							axisSelectedListener.getTxtLocation());
					break;
				case SRS:
					lblFilter = new Label("SRS:" + axisSelectedListener.getTxtEPSG());
					fo = new FilterObject(AxisFilters.SRS, axisSelectedListener.getTxtEPSG());
					break;
				}
				filter.addFilter(fo);
				btnFilter = new Button();
				btnFilter.addListener(filterButtonListener);
				btnFilter.setIcon(new ThemeResource("img/LayerLayerRendered16px.png"));
				btnFilter.setStyleName(BaseTheme.BUTTON_LINK);
				HorizontalLayout filterdiv = new HorizontalLayout();
				filterdiv.addStyleName("filter-flow");
				filterdiv.addComponent(btnFilter);
				filterdiv.setSizeUndefined();
				lblFilter.setWidth(Sizeable.SIZE_UNDEFINED, 0);
				filterdiv.addComponent(lblFilter);
				cssLayers.addComponent(filterdiv);
				hshFilterButtons.put(btnFilter, fo);
				((ExpressZipWindow) getApplication().getMainWindow()).removeWindow(wndAddFilter); // close the window
				applyFilters();
			} else if (buttCaption != null && buttCaption.compareTo("Cancel") == 0) {
				((ExpressZipWindow) getApplication().getMainWindow()).removeWindow(wndAddFilter);
			} else {
				// This is for clicking on filters currently in the app
				filter.delFilter(hshFilterButtons.get(event.getButton()), mapModel);
				hshFilterButtons.remove(event.getButton());
				cssLayers.removeComponent(event.getButton().getParent());
				applyFilters();
			}
		}
	}

	public class AxisSelected implements Property.ValueChangeListener {
		private static final long serialVersionUID = -521006477002348622L;
		// Layer name
		private ComboBox cmbNameOperation = null;
		private TextField txtString = null;

		// Keyword
		private ComboBox cmbKeyOperation = null;
		private TextField txtKeyword = null;

		// Location
		private ComboBox cmbLocOp = null;
		private TextField txtLocation = null;

		// SRS
		private TextField txtEPSG = null;

		@Override
		public void valueChange(ValueChangeEvent event) {
			switch (filter.getFilterByName((String) event.getProperty().getValue())) {
			case LAYER_NAME:
				filteringOn = AxisFilters.LAYER_NAME;
				hznCriteria.removeAllComponents();

				if (cmbNameOperation == null && txtString == null) {
					cmbNameOperation = new ComboBox();
					cmbNameOperation.setTextInputAllowed(false);
					cmbNameOperation.setNullSelectionAllowed(false);
					txtString = new TextField();

					cmbNameOperation.addItem("contains");
					cmbNameOperation.addItem("starts with");
					cmbNameOperation.addItem("ends with");
					cmbNameOperation.addItem("does not contain");
					cmbNameOperation.select("contains"); // default
					txtString.setWidth("100%");
					txtString.setInputPrompt("e.g. Columbia");
				}

				hznCriteria.setSpacing(true);
				hznCriteria.addComponent(cmbNameOperation);
				hznCriteria.addComponent(txtString);
				hznCriteria.setExpandRatio(txtString, 1f);

				break;
			case KEYWORD:
				filteringOn = AxisFilters.KEYWORD;
				hznCriteria.removeAllComponents();

				if (cmbKeyOperation == null && txtKeyword == null) {
					cmbKeyOperation = new ComboBox();
					cmbKeyOperation.setTextInputAllowed(false);
					cmbKeyOperation.setNullSelectionAllowed(false);
					txtKeyword = new TextField();

					cmbKeyOperation.addItem("contains");
					cmbKeyOperation.addItem("starts with");
					cmbKeyOperation.addItem("ends with");
					cmbKeyOperation.addItem("does not contain");
					cmbKeyOperation.select("contains"); // default

					txtKeyword.setWidth("100%");
					txtKeyword.setInputPrompt("e.g. Paris");
				}

				hznCriteria.setSpacing(true);
				hznCriteria.addComponent(cmbKeyOperation);
				hznCriteria.addComponent(txtKeyword);
				hznCriteria.setExpandRatio(txtKeyword, 1f);

				break;
			case LOCATION:
				filteringOn = AxisFilters.LOCATION;
				hznCriteria.removeAllComponents();

				if (cmbLocOp == null && txtLocation == null) {
					txtLocation = new TextField();
					cmbLocOp = new ComboBox();
					cmbLocOp.setTextInputAllowed(false);
					cmbLocOp.setNullSelectionAllowed(false);

					cmbLocOp.addItem("inside");
					cmbLocOp.addItem("outside");
					cmbLocOp.select("inside"); // default

					txtLocation.setInputPrompt("e.g. Washington");
					txtLocation.setWidth("100%");
				}

				hznCriteria.setSpacing(true);
				hznCriteria.addComponent(cmbLocOp);
				hznCriteria.addComponent(txtLocation);
				hznCriteria.setExpandRatio(txtLocation, 1f);

				break;
			case SRS:
				filteringOn = AxisFilters.SRS;
				hznCriteria.removeAllComponents();

				if (txtEPSG == null) {
					txtEPSG = new TextField();
				}

				hznCriteria.addComponent(txtEPSG);
				hznCriteria.setComponentAlignment(txtEPSG, Alignment.TOP_CENTER);

				break;
			}
		}

		public String getCmbNamOperation() {
			return cmbNameOperation.getValue().toString();
		}

		public String getTxtString() {
			return txtString.getValue().toString();
		}

		public String getCmbKeyOperation() {
			return cmbKeyOperation.getValue().toString();
		}

		public String getTxtKeyword() {
			return txtKeyword.getValue().toString();
		}

		public String getTxtLocation() {
			return txtLocation.getValue().toString();
		}

		public String getTxtEPSG() {
			return txtEPSG.getValue().toString();
		}

		public String getCmbLocOp() {
			return cmbLocOp.getValue().toString();
		}
	}

	public void applyFilters() {
		filter.filterLayers(mapModel);
		refreshMap();
		for (FindLayersViewListener listener : listeners) {
			listener.filtersChangedEvent();
		}
	}

	/*
	 * Returns the set of available actions
	 */
	@Override
	public Action[] getActions(Object target, Object sender) {
		return ACTIONS;
	}

	/*
	 * Handle actions
	 */

	@Override
	public void handleAction(Action action, Object sender, Object layer) {
		if (sender instanceof Table) {
			Table table = (Table) sender;
			if (action == ACTION_ZOOM) {
				table.select(layer);
				for (FindLayersViewListener listener : listeners) {
					listener.zoomToLayersEvent((Set<ExpressZipLayer>) table.getValue());
				}
			} else if (action == SELECT_ALL || action == SELECT_NONE) {
				for (Object itemId : table.getItemIds()) {
					if (action == SELECT_ALL)
						table.select(itemId);
					else
						table.unselect(itemId);
				}
			} else if (action == EXPORT_ALL || action == EXPORT_NONE) {
				for (Object itemId : table.getItemIds()) {
					IndeterminateCheckBox cb = (IndeterminateCheckBox) table.getContainerProperty(itemId, EXPORT).getValue();
					if ((cb.isOn() && action == EXPORT_NONE) || (!cb.isOn() && action == EXPORT_ALL)) {
						cb.click();
					}
				}
			}
		}
	}

	public void configureTable(Table table) {
		table.addContainerProperty(ExpressZipTreeTable.LAYER, ExpressZipLayer.class, null);
		table.addContainerProperty(EXPORT, IndeterminateCheckBox.class, null);
		table.addContainerProperty(RENDER, RenderButton.class, null);
		table.setColumnHeaders(new String[] { ExpressZipTreeTable.LAYER, "", "" });
		table.setColumnExpandRatio(ExpressZipTreeTable.LAYER, 1);

		// Add actions (context menu)
		table.addActionHandler(this);
	}

	private void addChildren(ExpressZipLayer parent) {
		for (final ExpressZipLayer child : parent.getChildren()) {
			if (child.filterAllows()) {
				// Don't show basemaps in the list of available layers.
				boolean skip_localBaseMap = false;
				for (Object itemId : basemapSelector.getItemIds()) {
					if (child.getName().equalsIgnoreCase(itemId.toString())) {
						skip_localBaseMap = true;
						break;
					}
				}
				if (skip_localBaseMap)
					continue;
				addLayerItem(child, treeTable);

				treeTable.setChildrenAllowed(parent, true);
				treeTable.setChildrenAllowed(child, false);

				if (parent.filterAllows()) {
					treeTable.setParent(child, parent);

					// set indeterminate on parent if appropriate
					if (child.isChosen() && !parent.isChosen()) {
						Property export = treeTable.getContainerProperty(parent, EXPORT);
						if (export != null) {
							IndeterminateCheckBox cb = (IndeterminateCheckBox) export.getValue();
							cb.setState(IndeterminateCheckBox.INDETERMINATE);
						}
					}
				} else {
					treeTable.setParent(child, mapModel.getRootRasterLayer());
				}
			}
			addChildren(child);
		}
	}

	public void addLayerItem(final ExpressZipLayer layer, final TreeTable table) {

		final IndeterminateCheckBox exportCheckBox = new IndeterminateCheckBox();
		if (layer.isChosen()) {
			exportCheckBox.setState(IndeterminateCheckBox.ON);
		}

		final RenderButton renderLayerButton = new RenderButton(layer.isRendered());
		
		table.addItem(new Object[] { layer, exportCheckBox, renderLayerButton }, layer);
		table.setChildrenAllowed(layer, false);
		
		exportCheckBox.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -1903183530883807821L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (exportCheckBox.isOff() || exportCheckBox.isIndeterminate()) {
					// choosing a parent clears all children
					clearChildCheckBoxes(layer);
					exportCheckBox.setState(IndeterminateCheckBox.ON);
				} else if (hasChildrenChosen(layer)) {
					// un-choosing a parent will keep the choose-state of any children
					exportCheckBox.setState(IndeterminateCheckBox.INDETERMINATE);
				} else {
					exportCheckBox.setState(IndeterminateCheckBox.OFF);
				}

				// synchronize popup and treetable states
				int exportState = exportCheckBox.getState();
				if (treeTable.containsId(layer))
					((IndeterminateCheckBox) treeTable.getContainerProperty(layer, EXPORT).getValue()).setState(exportState);
				if (popupTable.containsId(layer))
					((IndeterminateCheckBox) popupTable.getContainerProperty(layer, EXPORT).getValue()).setState(exportState);

				notifyLayerExport(layer, exportState == IndeterminateCheckBox.ON);
				resetParentCheckBoxes(layer, exportState == IndeterminateCheckBox.ON);

				btnNext.setEnabled(!mapModel.getChosenLayers().isEmpty());
			}
		});

		renderLayerButton.addListener(new ClickListener() {
			private static final long serialVersionUID = -462470414717524928L;

			@Override
			public void buttonClick(ClickEvent event) {
				layer.setRendered(!layer.isRendered()); // toggle rendered state of layer

				// toggle the treeTable's item as necessary
				RenderButton treeRenderButton = (RenderButton) treeTable.getContainerProperty(layer, RENDER).getValue();
				treeRenderButton.setState(layer.isRendered());
				
				// toggle the popupTable's item as necessary
				if (popupTable.containsId(layer)) {
					RenderButton popupRenderButton = (RenderButton) popupTable.getContainerProperty(layer, RENDER).getValue();
					popupRenderButton.setState(layer.isRendered());
				}

				for (FindLayersViewListener listener : listeners) {
					listener.renderLayerEvent(layer, layer.isRendered());
				}
			}
		});
	}

	protected void resetParentCheckBoxes(ExpressZipLayer layer, boolean export) {
		ExpressZipLayer parent = layer.getParent();
		if (parent != null) {
			IndeterminateCheckBox treeCB = null;
			IndeterminateCheckBox popupCB = null;
			if (treeTable.containsId(parent))
				treeCB = (IndeterminateCheckBox) treeTable.getContainerProperty(parent, EXPORT).getValue();
			if (popupTable.containsId(parent))
				popupCB = (IndeterminateCheckBox) popupTable.getContainerProperty(parent, EXPORT).getValue();
			if (export) {
				if (treeCB != null && treeCB.isOff())
					treeCB.setState(IndeterminateCheckBox.INDETERMINATE);
				if (popupCB != null && popupCB.isOff())
					popupCB.setState(IndeterminateCheckBox.INDETERMINATE);
			} else if (!hasChildrenChosen(parent)) {
				if (treeCB != null && treeCB.isIndeterminate())
					treeCB.setState(IndeterminateCheckBox.OFF);
				if (popupCB != null && popupCB.isIndeterminate())
					popupCB.setState(IndeterminateCheckBox.OFF);
			}
			resetParentCheckBoxes(parent, export);
		}
	}

	public void showRenderedLayers(List<ExpressZipLayer> renderedLayers) {
		for (ExpressZipLayer layer : renderedLayers) {
			if (treeTable.getItem(layer) != null) {
				RenderButton cb = (RenderButton) treeTable.getContainerProperty(layer, RENDER).getValue();
				cb.setState(layer.isRendered());
			}
		}
	}

	private void clearChildCheckBoxes(ExpressZipLayer parent) {
		for (ExpressZipLayer child : parent.getChildren()) {
			if (treeTable.containsId(child))
				((IndeterminateCheckBox) treeTable.getContainerProperty(child, EXPORT).getValue())
						.setState(IndeterminateCheckBox.OFF);
			if (popupTable.containsId(child))
				((IndeterminateCheckBox) popupTable.getContainerProperty(child, EXPORT).getValue())
						.setState(IndeterminateCheckBox.OFF);
			notifyLayerExport(child, false);
			clearChildCheckBoxes(child);
		}
	}

	private boolean hasChildrenChosen(ExpressZipLayer layer) {
		for (ExpressZipLayer l : layer.getChildren()) {
			if (l.isChosen() || hasChildrenChosen(l))
				return true;
		}
		return false;
	}

	private void notifyLayerExport(ExpressZipLayer layer, boolean export) {
		boolean highlighted = treeTable.isSelected(layer);
		for (FindLayersViewListener listener : listeners) {
			listener.chooseLayerForExportEvent(layer, export, highlighted);
		}
	}

	public class RenderButton extends Button {
		private static final long serialVersionUID = 2390825473656198993L;

		public RenderButton(boolean b) {
			super();
			setImmediate(true);
			setStyleName(BaseTheme.BUTTON_LINK);
			setState(b);
			setDescription("Preview the layer");
		}

		public void setState(boolean rendered) {
			if (rendered) {
				setIcon(new ThemeResource("img/LayerLayerRendered16px.png"));
			} else {
				setIcon(new ThemeResource("img/LayerRenderLayer16px.png"));
			}
		}
	}

	public class IndeterminateCheckBox extends Button {
		private static final long serialVersionUID = 1178313040045502351L;
		public static final int OFF = 1;
		public static final int ON = 2;
		public static final int INDETERMINATE = 3;
		private int state;

		public IndeterminateCheckBox() {
			super();
			setImmediate(true);
			setStyleName(BaseTheme.BUTTON_LINK);
			setState(OFF);
			setDescription("Select the layer for export");
		}

		public boolean isOff() {
			return (state == OFF);
		}

		public boolean isOn() {
			return (state == ON);
		}

		public boolean isIndeterminate() {
			return (state == INDETERMINATE);
		}

		public int getState() {
			return state;
		}

		public void setState(int state) {
			this.state = state;
			switch (state) {
			case OFF:
				setIcon(new ThemeResource("img/LayerChooseLayer16px.png"));
				break;
			case ON:
				setIcon(new ThemeResource("img/LayerChosen16px.png"));
				break;
			case INDETERMINATE:
				setIcon(new ThemeResource("img/LayerChildrenChosen16px.png"));
				break;

			default:
				break;
			}
		}

		public void toggle() {
			if (state == INDETERMINATE || state == OFF) {
				setState(ON);
			} else {
				setState(OFF);
			}
		}
	}

}
