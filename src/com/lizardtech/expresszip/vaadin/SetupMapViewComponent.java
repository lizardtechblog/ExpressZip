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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.vaadin.easyuploads.UploadField;
import org.vaadin.easyuploads.UploadField.FieldType;
import org.vaadin.vol.Bounds;

import com.lizardtech.expresszip.model.ExportProps;
import com.lizardtech.expresszip.model.ExpressZipLayer;
import com.lizardtech.expresszip.ui.SetupMapView;
import com.lizardtech.expresszip.vaadin.ExpressZipButton.Style;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.Action;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class SetupMapViewComponent extends CustomComponent implements SetupMapView, ClickListener, Action.Handler {
	private static final String DRAG_PROPERTY = "drag";
	private static final int LEFT = 0;
	private static final int RIGHT = 1;
	private static final int TOP = 2;
	private static final int BOTTOM = 3;
	private static final String PROJECTION = "Projection";
	private static final String SHAPEFILE_UPLOAD = "Cropping Shapefile";

	// Actions for the context menu
	private static final Action ACTION_ZOOM = new Action("Zoom to");
	private static final Action[] ACTIONS = new Action[] { ACTION_ZOOM };

	private List<SetupMapViewListener> listeners;
	private ComboBox cmbProjection;
	private ExpressZipTreeTable treeTable;
	private HierarchicalContainer treeHier;

	private UploadField btnUploadShapeFile = new UploadField() {
		// TODO: multi-file uploads if we want to allow pieces of a the
		// shapefiles (ie., not zipped)
		// see here:
		// http://code.google.com/p/easyuploads-addon/source/browse/trunk/EasyUploads/src/org/vaadin/easyuploads/tests/UploadfieldExampleApplication.java

		protected void updateDisplay() {
			// this is called when upload is completed

			String filename = getLastFileName();
			for (SetupMapViewListener listener : listeners)
				listener.shapeFileUploadedEvent(filename, new ByteArrayInputStream((byte[]) getValue()));
		}
	};
	private Button btnRemoveShapeFile = new Button();
	private Label lblCurrentShapeFile = new Label();

	private ExpressZipButton btnNext;
	private ExpressZipButton btnBack;

	private TextField topTextField = new TextField("Top");
	private Property.ValueChangeListener topListener = new Property.ValueChangeListener() {
		@Override
		public void valueChange(ValueChangeEvent event) {
			sanitizeCoords(TOP, event.getProperty().getValue().toString());
		}
	};

	private TextField leftTextField = new TextField("Left");
	private Property.ValueChangeListener leftListener = new Property.ValueChangeListener() {
		@Override
		public void valueChange(ValueChangeEvent event) {
			sanitizeCoords(LEFT, event.getProperty().getValue().toString());
		}
	};

	private TextField bottomTextField = new TextField("Bottom");
	private Property.ValueChangeListener bottomListener = new Property.ValueChangeListener() {
		@Override
		public void valueChange(ValueChangeEvent event) {
			sanitizeCoords(BOTTOM, event.getProperty().getValue().toString());
		}
	};

	private TextField rightTextField = new TextField("Right");
	private Property.ValueChangeListener rightListener = new Property.ValueChangeListener() {
		@Override
		public void valueChange(ValueChangeEvent event) {
			sanitizeCoords(RIGHT, event.getProperty().getValue().toString());
		}
	};

	public SetupMapViewComponent() {

		listeners = new ArrayList<SetupMapViewListener>();
		cmbProjection = new ComboBox();
		cmbProjection.setTextInputAllowed(false);
		HorizontalLayout projectionLayout = new HorizontalLayout();
		projectionLayout.addComponent(cmbProjection);
		projectionLayout.setWidth(100f, UNITS_PERCENTAGE);

		setSizeFull();

		// main layout
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSizeFull();

		// instruction banner
		Label step = new Label("Step 2: Select Export Region");
		step.addStyleName("step");
		layout.addComponent(step);

		//
		// setup tree data source
		//
		treeHier = new HierarchicalContainer();
		treeHier.addContainerProperty(ExpressZipTreeTable.LAYER, ExpressZipLayer.class, null);
		treeHier.addContainerProperty(DRAG_PROPERTY, Embedded.class, null);

		// table holding layers
		treeTable = new ExpressZipTreeTable();
		treeTable.setContainerDataSource(treeHier);

		treeTable.setDragMode(TableDragMode.ROW);
		treeTable.setColumnHeaders(new String[] { ExpressZipTreeTable.LAYER, "" });
		treeTable.setColumnExpandRatio(ExpressZipTreeTable.LAYER, 1);
		treeTable.setColumnWidth(DRAG_PROPERTY, 23);

		
		// upload shape file
		btnUploadShapeFile.setFieldType(FieldType.BYTE_ARRAY);
		btnUploadShapeFile.setButtonCaption("");
		btnUploadShapeFile.setSizeUndefined();
		btnUploadShapeFile.addStyleName("shapefile");
		
		// remove shape file
		btnRemoveShapeFile.addListener(this);
		btnRemoveShapeFile.setSizeUndefined();
		btnRemoveShapeFile.setVisible(false);
		btnRemoveShapeFile.setIcon(new ThemeResource("img/RemoveShapefileStandard23px.png"));
		btnRemoveShapeFile.setStyleName(BaseTheme.BUTTON_LINK);
		btnRemoveShapeFile.addStyleName("shapefile");
		HorizontalLayout hznUpload = new HorizontalLayout();

		Panel coordPanel = new Panel("Export Extent");

		layout.addComponent(treeTable);
		layout.addComponent(new Panel(PROJECTION, projectionLayout));
		layout.addComponent(new Panel(SHAPEFILE_UPLOAD, hznUpload));
		layout.addComponent(coordPanel);

		layout.setSpacing(true);

		hznUpload.addComponent(btnUploadShapeFile);
		hznUpload.addComponent(btnRemoveShapeFile);
		hznUpload.addComponent(lblCurrentShapeFile);
		hznUpload.setExpandRatio(lblCurrentShapeFile, 1f);
		hznUpload.setComponentAlignment(lblCurrentShapeFile, Alignment.MIDDLE_LEFT);
		hznUpload.setWidth("100%");

		cmbProjection.setWidth("100%");

		topTextField.setWidth(150, UNITS_PIXELS);
		topTextField.setImmediate(true);
		topTextField.setRequired(true);
		topTextField.addListener(topListener);

		leftTextField.setWidth(150, UNITS_PIXELS);
		leftTextField.setImmediate(true);
		leftTextField.setRequired(true);
		leftTextField.addListener(leftListener);

		bottomTextField.setWidth(150, UNITS_PIXELS);
		bottomTextField.setImmediate(true);
		bottomTextField.setRequired(true);
		bottomTextField.addListener(bottomListener);

		rightTextField.setWidth(150, UNITS_PIXELS);
		rightTextField.setImmediate(true);
		rightTextField.setRequired(true);
		rightTextField.addListener(rightListener);

		VerticalLayout coordLayout = new VerticalLayout();
		coordLayout.setSizeFull();
		coordPanel.setContent(coordLayout);
		coordLayout.addComponent(topTextField);

		HorizontalLayout leftRightLayout = new HorizontalLayout();
		leftRightLayout.setWidth("100%");
		leftRightLayout.addComponent(leftTextField);
		leftRightLayout.addComponent(rightTextField);
		leftRightLayout.setComponentAlignment(leftTextField, Alignment.MIDDLE_LEFT);
		leftRightLayout.setComponentAlignment(rightTextField, Alignment.MIDDLE_RIGHT);
		coordLayout.addComponent(leftRightLayout);

		coordLayout.addComponent(bottomTextField);
		coordLayout.setComponentAlignment(topTextField, Alignment.TOP_CENTER);
		coordLayout.setComponentAlignment(bottomTextField, Alignment.BOTTOM_CENTER);

		btnNext = new ExpressZipButton("Next", Style.STEP, this);
		btnBack = new ExpressZipButton("Back", Style.STEP, this);

		HorizontalLayout backNextLayout = new HorizontalLayout();
		backNextLayout.addComponent(btnBack);
		backNextLayout.addComponent(btnNext);
		btnNext.setEnabled(false);

		backNextLayout.setComponentAlignment(btnBack, Alignment.BOTTOM_LEFT);
		backNextLayout.setComponentAlignment(btnNext, Alignment.BOTTOM_RIGHT);
		backNextLayout.setWidth("100%");

		VerticalLayout navLayout = new VerticalLayout();
		navLayout.addComponent(backNextLayout);
		navLayout.setSpacing(true);

		ThemeResource banner = new ThemeResource("img/ProgressBar2.png");
		navLayout.addComponent(new Embedded(null, banner));

		layout.addComponent(navLayout);
		layout.setComponentAlignment(navLayout, Alignment.BOTTOM_CENTER);

		layout.setExpandRatio(treeTable, 1.0f);
		setCompositionRoot(layout);

		// notify when selection changes
		treeTable.addListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				for (SetupMapViewListener listener : listeners) {
					listener.layersSelectedEvent((Set<ExpressZipLayer>) treeTable.getValue());
				}

			}
		});
		treeTable.addActionHandler(this);
		treeHier.removeAllItems();

		//
		// drag n' drop behavior
		//
		treeTable.setDropHandler(new DropHandler() {
			public AcceptCriterion getAcceptCriterion() {
				return AcceptAll.get();
			}

			// Make sure the drag source is the same tree
			public void drop(DragAndDropEvent event) {
				// Wrapper for the object that is dragged
				Transferable t = event.getTransferable();

				// Make sure the drag source is the same tree
				if (t.getSourceComponent() != treeTable)
					return;

				AbstractSelectTargetDetails target = (AbstractSelectTargetDetails) event.getTargetDetails();

				// Get ids of the dragged item and the target item
				Object sourceItemId = t.getData("itemId");
				Object targetItemId = target.getItemIdOver();

				// if we drop on ourselves, ignore
				if (sourceItemId == targetItemId)
					return;

				// On which side of the target the item was dropped
				VerticalDropLocation location = target.getDropLocation();

				// place source after target
				treeHier.moveAfterSibling(sourceItemId, targetItemId);

				// if top, switch them
				if (location == VerticalDropLocation.TOP) {
					treeHier.moveAfterSibling(targetItemId, sourceItemId);
				}

				Collection<ExpressZipLayer> layers = (Collection<ExpressZipLayer>) treeHier.rootItemIds();
				for (SetupMapViewListener listener : listeners)
					listener.layerMovedEvent(layers);
			}
		});

		cmbProjection.setImmediate(true);
		cmbProjection.setNullSelectionAllowed(false);
		cmbProjection.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = -5188369735622627751L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if (cmbProjection.getValue() != null) {
					selectedEpsg = (String) cmbProjection.getValue();
					String currentProjection = ((ExpressZipWindow) getApplication().getMainWindow()).getExportProps()
							.getMapProjection();
					if (!selectedEpsg.equals(currentProjection))
						for (SetupMapViewListener listener : new ArrayList<SetupMapViewListener>(listeners))
							listener.projectionChangedEvent(selectedEpsg);
				}
			}
		});
	}

	private void sanitizeCoords(int field, String v) {

		try {
			Double value = new Double(v);

			ExportProps props = ((ExpressZipWindow) getApplication().getMainWindow()).getExportProps();
			Bounds curRegion = props.getExportRegion();
			if (curRegion != null) {
				switch (field) {
				case LEFT:
					curRegion.setLeft(value);
					break;
				case RIGHT:
					curRegion.setRight(value);
					break;
				case TOP:
					curRegion.setTop(value);
					break;
				case BOTTOM:
					curRegion.setBottom(value);
					break;
				}
			}

			props.setExportRegion(curRegion);

			for (SetupMapViewListener listener : listeners)
				listener.exportFieldsChanged(curRegion);

		} catch (NumberFormatException exc) {
			((ExpressZipWindow) getApplication().getMainWindow())
					.showNotification("Please use only numbers for coordinate values.");
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		// do event for Next being pressed
		if (event.getButton() == btnNext) {
			((ExpressZipWindow) getApplication().getMainWindow()).advanceToNext();
			// do event for Back being processed
		} else if (event.getButton() == btnBack) {
			((ExpressZipWindow) getApplication().getMainWindow()).regressToPrev();
		} else if (event.getButton() == btnRemoveShapeFile) {
			for (SetupMapViewListener listener : listeners) {
				listener.shapeFileRemovedEvent();
			}
		}
	}

	// SetupMapView implementation
	@Override
	public void addListener(SetupMapViewListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(SetupMapViewListener listener) {
		listeners.remove(listener);
	}

	private String selectedEpsg = null;

	@Override
	public void updateShapeLayer(ExpressZipLayer shapeLayer) {
		btnRemoveShapeFile.setVisible(shapeLayer != null);
		lblCurrentShapeFile.setValue(shapeLayer == null ? "" : shapeLayer.getName());
	}

	@Override
	public void updateTree(List<ExpressZipLayer> chosenLayers, Set<String> supportedProjections) {
		treeHier.removeAllItems();
		for (ExpressZipLayer layer : chosenLayers) {
			Item item = treeHier.addItem(layer);
			item.getItemProperty(ExpressZipTreeTable.LAYER).setValue(layer);
			Embedded dragImg = new Embedded("",new ThemeResource("img/MoveUpDown23px.png"));
			dragImg.setDescription("Drag to change layer order");
			item.getItemProperty(DRAG_PROPERTY).setValue(dragImg);

			// using treetable just for drag-drop, no children
			treeTable.setChildrenAllowed(layer, false);
		}

		// update the EPSG combo box with the union of supported projections from the enabled layers
		cmbProjection.removeAllItems();
		for (String epsg : supportedProjections) {
			String displayName = epsg;
			final String EPSG = "EPSG:";
			try {
				CoordinateReferenceSystem sourceCRS = CRS.decode(epsg);
				displayName = sourceCRS.getName().toString();
				if (displayName.startsWith(EPSG))
					displayName = displayName.substring(EPSG.length());
				ReferenceIdentifier id = (ReferenceIdentifier) sourceCRS.getIdentifiers().iterator().next();
				displayName = String.format("%s (%s)", displayName, id.getCode());
			} catch (NoSuchAuthorityCodeException e) {
			} catch (FactoryException e) {
			}
			cmbProjection.addItem(epsg);
			cmbProjection.setItemCaption(epsg, displayName);
		}

		if (supportedProjections.size() > 0) {
			if (selectedEpsg == null || !cmbProjection.containsId(selectedEpsg)) {
				selectedEpsg = supportedProjections.iterator().next();
			}
			cmbProjection.select(selectedEpsg);
		}
	}

	@Override
	public void setProjection(String epsg) {
		// not sure this is necessary...
		// TODO: populate the the epsg selecting combo box with the new espg
	}

	@Override
	public void setExportBounds(Bounds b) {

		bottomTextField.removeListener(bottomListener);
		bottomTextField.setValue(b.getBottom());
		bottomTextField.addListener(bottomListener);

		leftTextField.removeListener(leftListener);
		leftTextField.setValue(b.getLeft());
		leftTextField.addListener(leftListener);

		topTextField.removeListener(topListener);
		topTextField.setValue(b.getTop());
		topTextField.addListener(topListener);

		rightTextField.removeListener(rightListener);
		rightTextField.setValue(b.getRight());
		rightTextField.addListener(rightListener);

		btnNext.setEnabled(true);
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
		if (action == ACTION_ZOOM) {
			for (SetupMapViewListener listener : listeners) {
				if (layer instanceof ExpressZipLayer) {
					listener.layerClickedEvent((ExpressZipLayer) layer);
				}
			}
		}
	}
}
