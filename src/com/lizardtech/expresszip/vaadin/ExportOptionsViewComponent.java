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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.lizardtech.expresszip.model.BackgroundExecutor;
import com.lizardtech.expresszip.model.ExecutorListener;
import com.lizardtech.expresszip.model.ExportProps;
import com.lizardtech.expresszip.model.ExportProps.OutputPackageFormat;
import com.lizardtech.expresszip.model.GriddingOptions;
import com.lizardtech.expresszip.ui.ExportOptionsView;
import com.lizardtech.expresszip.vaadin.ExpressZipButton.Style;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.data.validator.DoubleValidator;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ExportOptionsViewComponent extends CustomComponent implements ExportOptionsView, ExecutorListener {

	private static final String PACKAGE = "Packaging options";
	private static final String GRID_THE_RESULT = "Tile the result";
	private static final String HEIGHT = "Height";
	private static final String WIDTH = "Width";
	private static final String EXPORT_CONFIGURATION = "Export resolution";
	private static final String DIMENSIONS = "Dimensions";
	private static final String TILING = "Tiling";
	private static final String FORMAT_OPTIONS = "Format options";
	private static final String OUTPUT_FORMAT = "Tile output format";
	private static final String JOB_NAME = "Job name";
	private static final String JOB_USER_NOTATION = "User name";
	private static final String EMAIL_ADDRESS = "Email address";
	private static final String JOB_DETAILS = "Job details";

	private static final String GRID_TILE_DIMENSIONS = "Tile by pixel area";
	private static final String GRID_GROUND_DISTANCE = "Tile by ground distance";
	private static final String GRID_NUM_TILES = "Divide into tiles";
	private static final List<String> GRIDDING_METHOD = Arrays.asList(new String[] { GRID_TILE_DIMENSIONS, GRID_GROUND_DISTANCE,
			GRID_NUM_TILES });

	private static final String NUMBER_OF_TILES = "Number of tiles: ";
	private static final String DISK_ESTIMATE = "Disk usage estimate: ";

	private Accordion accordian;
	VerticalLayout jobDetailsLayout;
	VerticalLayout griddingLayout;
	VerticalLayout formatOptionsLayout;
	VerticalLayout vrtOutputResolution;
	VerticalLayout outputDetails;

	private OptionGroup optGridOpt = new OptionGroup(null, GRIDDING_METHOD);
	private CheckBox gridCheckbox = new CheckBox(GRID_THE_RESULT);
	private TextField xTilesTextBox = new TextField();
	private TextField yTilesTextBox = new TextField();
	private TextField xPixelsTextBox = new TextField();
	private TextField yPixelsTextBox = new TextField();
	private TextField xDistanceTextBox = new TextField();
	private TextField yDistanceTextBox = new TextField();
	private ComboBox packageComboBox = new ComboBox(PACKAGE);

	private ExpressZipButton backButton;
	private ExpressZipButton submitButton;

	private TextField txtGroundResolution = new TextField("PLACEHOLDER");
	private TextField txtDimWidth = new TextField(WIDTH);
	private TextField txtDimHeight = new TextField(HEIGHT);

	// Format Option panel
	private static final String JPEG = "JPEG";
	private static final String PNG = "PNG";
	private static final String TIFF = "TIFF";
	private static final String GIF = "GIF";
	private static final String BMP = "BMP";
	private static final List<String> OUTPUT_FORMATS = Arrays.asList(new String[] { JPEG, PNG, TIFF, GIF, BMP });
	private ComboBox outputFormatComboBox;

	// Job option panel
	private TextField txtJobName;
	private TextField txtEmail;
	private TextField txtUserNotation;
	private List<ExportOptionsViewListener> listeners;
	private Label numTilesLabel;
	private Label exportSizeEstimate;

	private boolean griddingDrawEnabled;

	private ExportProps exportProps;

	private ValueChangeListener griddingValuesChangeListener = new ValueChangeListener() {
		@Override
		public void valueChange(ValueChangeEvent event) {
			if (updateGriddingValidity())
				configureGridding();
			updateSubmitEnabledState();
		}
	};

	private ValueChangeListener resolutionValuesChangeListener = new ValueChangeListener() {
		@Override
		public void valueChange(ValueChangeEvent event) {
			updateSubmitEnabledState();
		}
	};

	private ValueChangeListener griddingModeChangeListener = new ValueChangeListener() {
		@Override
		public void valueChange(ValueChangeEvent event) {
			updateGriddingEnabledState();
		}
	};

	private ValueChangeListener widthValChangeListener = new ValueChangeListener() {
		@Override
		public void valueChange(ValueChangeEvent event) {
			if (txtDimWidth.isValid()) {
				for (ExportOptionsViewListener listener : listeners)
					listener.updateHeightAndResFromWidth(getDimensionWidth());
				if (forceGriddingCheck())
					configureGridding();
			}
			updateSubmitEnabledState();
		}
	};

	private ValueChangeListener heightValChangeListener = new ValueChangeListener() {
		@Override
		public void valueChange(ValueChangeEvent event) {
			if (txtDimHeight.isValid()) {
				for (ExportOptionsViewListener listener : listeners)
					listener.updateWidthAndResFromHeight(getDimensionHeight());
				if (forceGriddingCheck())
					configureGridding();
			}
			updateSubmitEnabledState();
		}
	};

	private ValueChangeListener groundResValChangeListener = new ValueChangeListener() {
		@Override
		public void valueChange(ValueChangeEvent event) {
			if (txtGroundResolution.isValid()) {
				for (ExportOptionsViewListener listener : listeners)
					listener.updateWidthAndHeightFromRes(getGroundResolution());
				if (forceGriddingCheck())
					configureGridding();
			}
			updateSubmitEnabledState();
		}
	};
	private double maximumResolution = 1.0d;

	private static final String SMALL = "Small";
	private static final String MEDIUM = "Medium";
	private static final String LARGE = "Large";
	private static final String NATIVE = "Native resolution";
	private static final String CUSTOM = "Custom";
	private static final List<String> exportSizes = Arrays.asList(new String[] { SMALL, MEDIUM, LARGE, NATIVE, CUSTOM });
	private ComboBox exportSizeComboBox;

	public ExportOptionsViewComponent(ExportProps exportProps) {

		this.exportProps = exportProps;

		listeners = new ArrayList<ExportOptionsViewListener>();
		txtJobName = new TextField(JOB_NAME);
		txtEmail = new TextField(EMAIL_ADDRESS);
		txtUserNotation = new TextField(JOB_USER_NOTATION);
		numTilesLabel = new Label();
		exportSizeEstimate = new Label();
		outputFormatComboBox = new ComboBox(OUTPUT_FORMAT, OUTPUT_FORMATS);
		outputFormatComboBox.setTextInputAllowed(false);
		outputFormatComboBox.addListener(griddingValuesChangeListener);

		setSizeFull();

		/**
		 * Setup output resolution
		 */

		exportSizeComboBox = new ComboBox(null, exportSizes);
		exportSizeComboBox.setNullSelectionAllowed(false);
		exportSizeComboBox.setNewItemsAllowed(false);
		exportSizeComboBox.setTextInputAllowed(false);
		exportSizeComboBox.setImmediate(true);
		exportSizeComboBox.addListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				Object choice = event.getProperty().getValue();
				String value = "";
				if (SMALL.equals(choice)) {
					gridCheckbox.setValue(Boolean.FALSE);
					gridCheckbox.setEnabled(false);
					value = "512";
				} else if (MEDIUM.equals(choice)) {
					gridCheckbox.setValue(Boolean.FALSE);
					gridCheckbox.setEnabled(false);
					value = "1280";
				} else if (LARGE.equals(choice)) {
					gridCheckbox.setValue(Boolean.FALSE);
					gridCheckbox.setEnabled(false);
					value = "5000";
				}

				boolean custom = CUSTOM.equals(choice);
				if (!custom) {
					if (NATIVE.equals(choice)) {
						txtGroundResolution.setValue(Double.toString(maximumResolution));
					} else {
						if (getExportProps().getAspectRatio() > 1.0d) {
							txtDimHeight.setValue(value);
						} else
							txtDimWidth.setValue(value);
					}
				}

				txtDimWidth.setEnabled(custom);
				txtDimHeight.setEnabled(custom);
				txtGroundResolution.setEnabled(custom);
			}
		});

		// Add Output Resolution to view
		HorizontalLayout dimensionsLayout = new HorizontalLayout();
		dimensionsLayout.addComponent(txtDimWidth);
		dimensionsLayout.addComponent(txtDimHeight);
		dimensionsLayout.setSpacing(true);
		dimensionsLayout.setWidth("100%");

		// Format dimensions layout
		txtDimHeight.setMaxLength(10);
		txtDimHeight.setWidth("100%");
		txtDimHeight.setImmediate(true);
		txtDimHeight.addListener(heightValChangeListener);
		txtDimHeight.setRequired(true);
		txtDimHeight.addValidator(new WidthHeightValidator());

		txtDimWidth.setMaxLength(10);
		txtDimWidth.setWidth("100%");
		txtDimWidth.setImmediate(true);
		txtDimWidth.addListener(widthValChangeListener);
		txtDimWidth.setRequired(true);
		txtDimWidth.addValidator(new WidthHeightValidator());

		// Format Ground Resolution layout
		txtGroundResolution.setValue("0");
		txtGroundResolution.setImmediate(true);
		txtGroundResolution.addListener(groundResValChangeListener);
		txtGroundResolution.setRequired(true);
		txtGroundResolution.addValidator(new GroundResolutionValidator());

		vrtOutputResolution = new VerticalLayout();
		vrtOutputResolution.setSpacing(true);
		vrtOutputResolution.addComponent(exportSizeComboBox);
		vrtOutputResolution.addComponent(dimensionsLayout);
		txtGroundResolution.setWidth("75%");
		vrtOutputResolution.addComponent(txtGroundResolution);
		vrtOutputResolution.setComponentAlignment(txtGroundResolution, Alignment.BOTTOM_CENTER);

		/**
		 * Setup Gridding options
		 */

		// Add Gridding option to view
		griddingLayout = new VerticalLayout();
		griddingLayout.setSpacing(true);

		// Format GridCheckbox layout
		griddingLayout.addComponent(gridCheckbox);
		gridCheckbox.setImmediate(true);
		gridCheckbox.setValue(false);
		gridCheckbox.addListener(griddingModeChangeListener);

		xPixelsTextBox.setWidth("100%");
		xPixelsTextBox.setImmediate(true);
		xPixelsTextBox.addValidator(new TileWidthValidator());
		xPixelsTextBox.addListener(griddingValuesChangeListener);

		yPixelsTextBox.setWidth("100%");
		yPixelsTextBox.setImmediate(true);
		yPixelsTextBox.addValidator(new TileHeightValidator());
		yPixelsTextBox.addListener(griddingValuesChangeListener);

		xDistanceTextBox.setWidth("100%");
		xDistanceTextBox.setImmediate(true);
		xDistanceTextBox.addValidator(new TileGeoXValidator());
		xDistanceTextBox.addListener(griddingValuesChangeListener);

		yDistanceTextBox.setWidth("100%");
		yDistanceTextBox.setImmediate(true);
		yDistanceTextBox.addValidator(new TileGeoYValidator());
		yDistanceTextBox.addListener(griddingValuesChangeListener);

		// Format gridding options
		xTilesTextBox.setWidth("100%");
		xTilesTextBox.setImmediate(true);
		xTilesTextBox.addValidator(new TileXDivisorValidator());
		xTilesTextBox.addListener(griddingValuesChangeListener);

		yTilesTextBox.setWidth("100%");
		yTilesTextBox.setImmediate(true);
		yTilesTextBox.addValidator(new TileYDivisorValidator());
		yTilesTextBox.addListener(griddingValuesChangeListener);

		optGridOpt.setValue(GRID_TILE_DIMENSIONS);
		optGridOpt.setImmediate(true);
		optGridOpt.addListener(griddingModeChangeListener);

		HorizontalLayout hznGridOptions = new HorizontalLayout();
		griddingLayout.addComponent(hznGridOptions);
		hznGridOptions.setWidth("100%");
		hznGridOptions.setSpacing(true);
		hznGridOptions.addComponent(optGridOpt);

		VerticalLayout vrtGridComboFields = new VerticalLayout();
		hznGridOptions.addComponent(vrtGridComboFields);
		vrtGridComboFields.setWidth("100%");
		hznGridOptions.setExpandRatio(vrtGridComboFields, 1.0f);

		HorizontalLayout hznTileDim = new HorizontalLayout();
		hznTileDim.setWidth("100%");
		vrtGridComboFields.addComponent(hznTileDim);
		hznTileDim.addComponent(xPixelsTextBox);
		hznTileDim.addComponent(yPixelsTextBox);

		HorizontalLayout hznDistanceDim = new HorizontalLayout();
		hznDistanceDim.setWidth("100%");
		vrtGridComboFields.addComponent(hznDistanceDim);
		hznDistanceDim.addComponent(xDistanceTextBox);
		hznDistanceDim.addComponent(yDistanceTextBox);

		HorizontalLayout hznDivideGrid = new HorizontalLayout();
		hznDivideGrid.setWidth("100%");
		vrtGridComboFields.addComponent(hznDivideGrid);
		hznDivideGrid.addComponent(xTilesTextBox);
		hznDivideGrid.addComponent(yTilesTextBox);
		hznDivideGrid.setSpacing(true);
		hznTileDim.setSpacing(true);
		hznDistanceDim.setSpacing(true);

		/**
		 * Format options panel
		 */

		// Add Format options to view
		formatOptionsLayout = new VerticalLayout();
		formatOptionsLayout.setWidth("100%");
		formatOptionsLayout.setSpacing(true);
		formatOptionsLayout.setMargin(true);

		// Format outputformat
		formatOptionsLayout.addComponent(outputFormatComboBox);

		outputFormatComboBox.setNullSelectionAllowed(false);

		formatOptionsLayout.addComponent(packageComboBox);
		packageComboBox.addItem(ExportProps.OutputPackageFormat.TAR);
		packageComboBox.addItem(ExportProps.OutputPackageFormat.ZIP);
		packageComboBox.setNullSelectionAllowed(false);
		packageComboBox.setTextInputAllowed(false);
		packageComboBox.setValue(ExportProps.OutputPackageFormat.ZIP);

		/**
		 * Job Details
		 */

		// Set Jobname panel
		jobDetailsLayout = new VerticalLayout();
		jobDetailsLayout.setSpacing(true);
		jobDetailsLayout.setMargin(true);

		jobDetailsLayout.addComponent(txtJobName);
		txtJobName.setRequired(true);
		txtJobName.setRequiredError("Please enter a job name.");
		txtJobName.setWidth("100%");
		txtJobName.setImmediate(true);
		String jobname_regexp = "^[ A-Za-z0-9._-]{1,128}$";
		txtJobName.addValidator(new RegexpValidator(jobname_regexp,
				"Job names should be alpha-numeric, less than 128 characters and may include spaces, dashes and underscores"));
		txtJobName
				.addValidator(new JobNameUniqueValidator("A job by that name already exists in your configured export directory"));
		txtJobName.addListener(resolutionValuesChangeListener);

		jobDetailsLayout.addComponent(txtUserNotation);
		txtUserNotation.setWidth("100%");
		txtUserNotation.setImmediate(true);
		String usernotation_regexp = "^[ A-Za-z0-9_-]{0,32}$";
		txtUserNotation.addValidator(new RegexpValidator(usernotation_regexp,
				"User names should be alpha-numeric, less than 32 characters and may include spaces, dashes and underscores"));
		txtUserNotation.addListener(resolutionValuesChangeListener);

		// Format Email
		boolean enableEmail = new BackgroundExecutor.Factory().getBackgroundExecutor().getMailServices().isValidEmailConfig();
		txtEmail.setEnabled(enableEmail);
		if (enableEmail) {
			jobDetailsLayout.addComponent(txtEmail);
			txtEmail.setWidth("100%");
			txtEmail.setInputPrompt("enter your email address");
			txtEmail.setImmediate(true);
			txtEmail.addValidator(new EmailValidator("Invalid format for email address."));
			txtEmail.addListener(new ValueChangeListener() {

				@Override
				public void valueChange(ValueChangeEvent event) {
					updateSubmitEnabledState();
				}
			});
		}

		VerticalLayout exportSummary = new VerticalLayout();
		exportSummary.addComponent(numTilesLabel);
		exportSummary.addComponent(exportSizeEstimate);
		jobDetailsLayout.addComponent(new Panel(("Export summary"), exportSummary));

		// Set submit and back buttons
		// Add listeners to all fields
		backButton = new ExpressZipButton("Back", Style.STEP);
		backButton.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				((ExpressZipWindow) getApplication().getMainWindow()).regressToPrev();
			}
		});

		submitButton = new ExpressZipButton("Submit", Style.STEP);
		submitButton.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					txtJobName.validate();
				} catch (InvalidValueException e) {
					txtJobName.requestRepaint();
					updateSubmitEnabledState();
					return;
				}
				for (ExportOptionsViewListener listener : listeners) {
					listener.submitJobEvent(getExportProps());
				}
			}
		});

		accordian = new Accordion();
		accordian.addStyleName("expresszip");
		accordian.setImmediate(true);
		accordian.addTab(jobDetailsLayout, JOB_DETAILS);
		accordian.addTab(formatOptionsLayout, FORMAT_OPTIONS);
		accordian.setSizeFull();

		outputDetails = new VerticalLayout();
		outputDetails.setMargin(true);
		outputDetails.setSpacing(true);
		outputDetails.addComponent(new Panel(DIMENSIONS, vrtOutputResolution));
		outputDetails.addComponent(new Panel(TILING, griddingLayout));
		accordian.addTab(outputDetails, EXPORT_CONFIGURATION);

		HorizontalLayout backSubmitLayout = new HorizontalLayout();
		backSubmitLayout.setWidth("100%");
		backSubmitLayout.addComponent(backButton);
		backSubmitLayout.addComponent(submitButton);
		backSubmitLayout.setComponentAlignment(backButton, Alignment.BOTTOM_LEFT);
		backSubmitLayout.setComponentAlignment(submitButton, Alignment.BOTTOM_RIGHT);

		VerticalLayout navLayout = new VerticalLayout();
		navLayout.addComponent(backSubmitLayout);
		navLayout.setSpacing(true);

		ThemeResource banner = new ThemeResource("img/ProgressBar3.png");
		navLayout.addComponent(new Embedded(null,banner));
		
		// add scrollbars around formLayout
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSizeFull();
		layout.setSpacing(true);

		Label step = new Label("Step 3: Configure Export Options");
		step.addStyleName("step");
		layout.addComponent(step);

		layout.addComponent(accordian);
		layout.setExpandRatio(accordian, 1.0f);

		layout.addComponent(navLayout);
		layout.setComponentAlignment(navLayout, Alignment.BOTTOM_CENTER);


		setCompositionRoot(layout);

		outputFormatComboBox.select(OUTPUT_FORMATS.get(0));

		forceGriddingCheck();
		updateGriddingEnabledState();
	}

	private boolean updateGriddingFieldValidity(TextField f, String method) {
		if (f == null)
			return false;
		boolean fieldActive = gridCheckbox.booleanValue();
		try {
			fieldActive &= ((String) optGridOpt.getValue()).equals(method);
			f.setComponentError(null);
			f.setValidationVisible(fieldActive);
			f.setRequired(fieldActive);
			if (fieldActive)
				f.validate();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private boolean updateGriddingValidity() {
		boolean valid = true;
		valid = updateGriddingFieldValidity(xPixelsTextBox, GRID_TILE_DIMENSIONS) && valid;
		valid = updateGriddingFieldValidity(yPixelsTextBox, GRID_TILE_DIMENSIONS) && valid;
		valid = updateGriddingFieldValidity(xDistanceTextBox, GRID_GROUND_DISTANCE) && valid;
		valid = updateGriddingFieldValidity(yDistanceTextBox, GRID_GROUND_DISTANCE) && valid;
		valid = updateGriddingFieldValidity(xTilesTextBox, GRID_NUM_TILES) && valid;
		valid = updateGriddingFieldValidity(yTilesTextBox, GRID_NUM_TILES) && valid;
		return valid;
	}

	public void updateUI() {

		forceGriddingCheck();

		StringBuilder groundResCaption = new StringBuilder("Ground resolution: (");
		String currentUoM = "unknown";
		try {
			CoordinateReferenceSystem crs = CRS.getAuthorityFactory(true).createCoordinateReferenceSystem(
					exportProps.getMapProjection());

			Pattern pattern = Pattern.compile("UoM: ([a-zA-Z]*)");
			Matcher matcher = pattern.matcher(crs.getCoordinateSystem().getName().getCode());

			currentUoM = matcher.find() ? matcher.group(1) : "unkown";
			groundResCaption.append(currentUoM);
			groundResCaption.append("/px)");

		} catch (NoSuchAuthorityCodeException e) {
			e.printStackTrace();
		} catch (FactoryException e) {
			e.printStackTrace();
		}
		txtGroundResolution.setCaption(groundResCaption.toString());
		optGridOpt.setItemCaption(GRID_GROUND_DISTANCE, "Tile by " + currentUoM);
	}

	public void paneEntered() {
		exportSizeComboBox.select(CUSTOM);
		updateUI();
		updateGriddingEnabledState();
		exportSizeComboBox.select(MEDIUM); // reset to medium
	}

	private void configureGridding() {
		GriddingOptions griddingOptions = new GriddingOptions();

		ExportProps props = getExportProps();
		griddingOptions.setExportWidth(props.getWidth());
		griddingOptions.setExportHeight(props.getHeight());

		griddingOptions.setGridding(gridCheckbox.booleanValue() && griddingDrawEnabled);

		if (griddingOptions.isGridding()) {

			if ((String) optGridOpt.getValue() == GRID_NUM_TILES) {
				griddingOptions.setGridMode(GriddingOptions.GridMode.DIVISION);

				int divX = Integer.parseInt(xTilesTextBox.getValue().toString());
				int divY = Integer.parseInt(yTilesTextBox.getValue().toString());
				griddingOptions.setDivX(divX > 0 ? divX : griddingOptions.getDivX());
				griddingOptions.setDivY(divY > 0 ? divY : griddingOptions.getDivY());

			} else { // GRID_TILE_DIMENSIONS or GRID_GROUND_DISTANCE

				griddingOptions.setGridMode(GriddingOptions.GridMode.METERS);
				if ((String) optGridOpt.getValue() == GRID_GROUND_DISTANCE) {
					Double groundResolution = getGroundResolution();
					griddingOptions.setTileSizeX((int) (Double.parseDouble(getDistance_X()) / groundResolution));
					griddingOptions.setTileSizeY((int) (Double.parseDouble(getDistance_Y()) / groundResolution));
				} else {
					griddingOptions.setTileSizeX(Integer.parseInt(getTile_X()));
					griddingOptions.setTileSizeY(Integer.parseInt(getTile_Y()));
				}
			}
		}
		getExportProps().setGriddingOptions(griddingOptions);

		// update job summary text
		numTilesLabel.setValue(NUMBER_OF_TILES + griddingOptions.getNumTiles());

		BigInteger size = griddingOptions.getExportSize();
		String format = getImageFormat();
		if (format.equals(PNG))
			size = size.multiply(BigInteger.valueOf(85)).divide(BigInteger.valueOf(100));
		else if (format.equals(JPEG))
			size = size.divide(BigInteger.valueOf(15));
		else if (format.equals(GIF))
			size = size.multiply(BigInteger.valueOf(15)).divide(BigInteger.valueOf(100));
		else if (format.equals(BMP))
			size = size.multiply(BigInteger.valueOf(85)).divide(BigInteger.valueOf(100));
		exportSizeEstimate.setValue(DISK_ESTIMATE + FileUtils.byteCountToDisplaySize(size));
		for (ExportOptionsViewListener listener : listeners)
			listener.updateGridding(getExportProps());
	}

	private void constrainTileDimension(TextField f, int constraint) {
		int v = -1;
		if (f.getValue() != null) {
			try {
				v = Integer.parseInt((String) f.getValue());
			} catch (NumberFormatException e) {
			}
		}
		if (f.getValue() == null || v > constraint || v < 1)
			f.setValue(Integer.toString(constraint));
	}

	// returns true if gridding parameters are valid
	private boolean forceGriddingCheck() {
		if (getDimensionWidth() > ExportProps.MAX_TILE_WIDTH || getDimensionHeight() > ExportProps.MAX_TILE_HEIGHT) {
			gridCheckbox.setValue(true);
			gridCheckbox.setEnabled(false);
			getExportProps().setTileOutput(true);

			// since we are forcing, let's make sure we pick valid tiling parameters
			constrainTileDimension(xPixelsTextBox, Math.min(getDimensionWidth(), ExportProps.MAX_TILE_WIDTH));
			constrainTileDimension(yPixelsTextBox, Math.min(getDimensionHeight(), ExportProps.MAX_TILE_HEIGHT));
		} else {
			gridCheckbox.setEnabled(true);
		}
		return updateGriddingValidity();
	}

	private void updateGriddingEnabledState() {
		boolean griddingChecked = gridCheckbox.booleanValue();
		String gridMode = (String) optGridOpt.getValue();
		getExportProps().setTileOutput(griddingChecked);
		optGridOpt.setEnabled(griddingChecked);
		xPixelsTextBox.setEnabled(griddingChecked && gridMode.equals(GRID_TILE_DIMENSIONS));
		yPixelsTextBox.setEnabled(griddingChecked && gridMode.equals(GRID_TILE_DIMENSIONS));
		xDistanceTextBox.setEnabled(griddingChecked && gridMode.equals(GRID_GROUND_DISTANCE));
		yDistanceTextBox.setEnabled(griddingChecked && gridMode.equals(GRID_GROUND_DISTANCE));
		xTilesTextBox.setEnabled(griddingChecked && gridMode.equals(GRID_NUM_TILES));
		yTilesTextBox.setEnabled(griddingChecked && gridMode.equals(GRID_NUM_TILES));

		if (updateGriddingValidity())
			configureGridding();
		updateSubmitEnabledState();
	}

	@Override
	public void addListener(ExportOptionsViewListener listener) {
		listeners.add(listener);
	}

	/**
	 * Display errorMessage on to screen.
	 * 
	 * @param errorMessage
	 */
	public void setErrorMessage(String errorMessage) {
		((ExpressZipWindow) getApplication().getMainWindow()).showNotification(errorMessage);
	}

	public String getEmail() {
		if (txtEmail.isEnabled())
			return txtEmail.getValue().toString();
		return null;
	}

	public String getUserNotation() {
		return txtUserNotation.getValue().toString();
	}

	public String getImageFormat() {
		return outputFormatComboBox.getValue().toString();
	}

	public OutputPackageFormat getPackageFormat() {
		OutputPackageFormat fmt = OutputPackageFormat.valueOf(packageComboBox.getValue().toString());
		return fmt;
	}

	public String getTile_X() {
		return xPixelsTextBox.getValue().toString();
	}

	public String getTile_Y() {
		return yPixelsTextBox.getValue().toString();
	}

	public String getDistance_X() {
		return xDistanceTextBox.getValue().toString();
	}

	public String getDistance_Y() {
		return yDistanceTextBox.getValue().toString();
	}

	public String getJobName() {
		return txtJobName.getValue().toString();
	}

	private void updateSubmitEnabledState() {
		boolean jobDetailsValid = txtJobName.isValid() && (getUserNotation().isEmpty() || txtUserNotation.isValid());
		if (txtEmail.isEnabled()) {
			if (!txtEmail.getValue().toString().isEmpty()) {
				jobDetailsValid &= txtEmail.isValid();
			}
		}

		boolean outputValid = txtDimHeight.isValid() && txtDimWidth.isValid();
		if (gridCheckbox.booleanValue()) {
			String gridOpt = (String) optGridOpt.getValue();
			if (gridOpt.equals(GRID_TILE_DIMENSIONS)) {
				outputValid &= xPixelsTextBox.isValid() && yPixelsTextBox.isValid();
			} else if (gridOpt.equals(GRID_NUM_TILES)) {
				outputValid &= xTilesTextBox.isValid() && yTilesTextBox.isValid();
			} else if (gridOpt.equals(GRID_GROUND_DISTANCE)) {
				outputValid &= xDistanceTextBox.isValid() && yDistanceTextBox.isValid();
			}
		}

		accordian.getTab(jobDetailsLayout).setIcon(jobDetailsValid ? null : new ThemeResource("img/error.png"));
		accordian.getTab(outputDetails).setIcon(outputValid ? null : new ThemeResource("img/error.png"));

		submitButton.setEnabled(jobDetailsValid && outputValid);
	}

	public int getDimensionHeight() {
		String strval = (String) txtDimHeight.getValue();
		if (strval.isEmpty())
			return 0;

		return Integer.parseInt(strval);
	}

	public int getDimensionWidth() {
		String strval = (String) txtDimWidth.getValue();
		if (strval.isEmpty())
			return 0;

		return Integer.parseInt(strval);
	}

	public Double getGroundResolution() {
		Double groundResolution;
		try {
			groundResolution = Double.parseDouble(txtGroundResolution.getValue().toString());
		} catch (NumberFormatException e) {
			// just return 1.0 when not initialized yet
			groundResolution = 1.0;
		}
		return groundResolution;
	}

	// Vaadin doesn't support turning off events, so remove the
	// listener/re-add it to prevent update cycles
	public void setTxtDimensionHeight(int height) {
		txtDimHeight.removeListener(heightValChangeListener);
		txtDimHeight.setValue(Integer.toString(height));
		getExportProps().setHeight(height);
		txtDimHeight.addListener(heightValChangeListener);
	}

	public void setTxtDimensionWidth(int width) {
		txtDimWidth.removeListener(widthValChangeListener);
		txtDimWidth.setValue(Integer.toString(width));
		getExportProps().setWidth(width);
		txtDimWidth.addListener(widthValChangeListener);
	}

	public void setGroundResolution(double groundResolution) {
		txtGroundResolution.removeListener(groundResValChangeListener);
		txtGroundResolution.setValue(Double.toString(groundResolution));
		txtGroundResolution.addListener(groundResValChangeListener);
	}

	public void setMaximumResolution(double resolution) {
		maximumResolution = resolution;
	}

	public void setTxtDistanceX(int x) {
		xDistanceTextBox.setValue(Integer.toString(x));
	}

	public void setTxtDistanceY(int y) {
		yDistanceTextBox.setValue(Integer.toString(y));
	}

	public AbstractComponent getTxtWidth() {
		return txtDimWidth;
	}

	public void setComponentErrorMessage(AbstractComponent c, String message) {
		c.setComponentError(new UserError(message));
	}

	public void clearComponentErrorMessage(AbstractComponent c) {
		c.setComponentError(null);
	}

	public AbstractComponent getTxtHeight() {
		return txtDimHeight;
	}

	public ExportProps getExportProps() {
		return exportProps;
	}

	public class TileByPixelDimensionValidator extends IntegerValidator {
		private String dimensionName;

		public TileByPixelDimensionValidator(String dimensionName) {
			super(new String("Tile " + dimensionName + " must be a positive integer."));
			this.dimensionName = dimensionName;
		}

		protected Integer getValidDimension(TileByPixelDimensionValidator vtor, String value, int tilemax, int jobmax) {
			Integer ivalue = null;
			try {
				ivalue = new Integer(Integer.parseInt(value));
				if (ivalue > tilemax) {
					if (vtor != null)
						vtor.setErrorMessage("{0} exceeds the maximum allowable tile " + dimensionName + " (" + tilemax + ").");
					return null;
				}
				if (ivalue > jobmax) {
					if (vtor != null)
						vtor.setErrorMessage("{0} exceeds the " + dimensionName + " of the total job.");
					return null;
				}
				if (ivalue <= 0) {
					if (vtor != null)
						vtor.setErrorMessage("{0} is not a positive integer.");
					return null;
				}
			} catch (NumberFormatException e) {
				if (vtor != null)
					vtor.setErrorMessage("{0} is not a positive integer.");
				return null;
			}
			return ivalue;
		}

		protected boolean checkNumTiles(TileByPixelDimensionValidator vtor, int tilewidth, int tileheight) {
			long tileswide = ((getDimensionWidth() - 1) / tilewidth) + 1;
			long tileshigh = ((getDimensionHeight() - 1) / tileheight) + 1;
			if (tileswide > ExportProps.MAX_TILES_PER_JOB || tileshigh > ExportProps.MAX_TILES_PER_JOB
					|| tileswide * tileshigh > ExportProps.MAX_TILES_PER_JOB) {
				if (vtor != null)
					this.setErrorMessage(tilewidth + " x " + tileheight
							+ " tiles are too small and will exceed the maximum number allowed per job ("
							+ ExportProps.MAX_TILES_PER_JOB + ").");
				return false;
			}
			return true;
		}
	}

	public class TileWidthValidator extends TileByPixelDimensionValidator {
		public TileWidthValidator() {
			super("width");
		}

		@Override
		protected boolean isValidString(String value) {
			Integer wvalue = getValidDimension(this, value, ExportProps.MAX_TILE_WIDTH, getDimensionWidth());
			if (wvalue == null)
				return false;
			if (yPixelsTextBox.getValue() != null) {
				Integer hvalue = getValidDimension(null, yPixelsTextBox.getValue().toString(), ExportProps.MAX_TILE_HEIGHT,
						getDimensionHeight());
				if (hvalue != null)
					return checkNumTiles(this, wvalue.intValue(), hvalue.intValue());
			}
			return true;
		}
	}

	public class TileHeightValidator extends TileByPixelDimensionValidator {
		public TileHeightValidator() {
			super("height");
		}

		@Override
		protected boolean isValidString(String value) {
			Integer hvalue = getValidDimension(this, value, ExportProps.MAX_TILE_HEIGHT, getDimensionHeight());
			if (hvalue == null)
				return false;
			if (xPixelsTextBox.getValue() != null) {
				Integer wvalue = getValidDimension(null, xPixelsTextBox.getValue().toString(), ExportProps.MAX_TILE_WIDTH,
						getDimensionWidth());
				if (wvalue != null)
					return checkNumTiles(this, wvalue.intValue(), hvalue.intValue());
			}
			return true;
		}
	}

	public class TileByGeoDimensionValidator extends DoubleValidator {
		private String dimensionName;

		public TileByGeoDimensionValidator(String dimensionName) {
			super(new String("Tile " + dimensionName + " must be a positive decimal number."));
			this.dimensionName = dimensionName;
		}

		protected Double getValidDimension(TileByGeoDimensionValidator vtor, String value, int tilemax, int jobmax) {
			Double dvalue = null;
			try {
				dvalue = new Double(Double.parseDouble(value));
				double dmax = tilemax * getGroundResolution();
				if (dvalue > dmax) {
					this.setErrorMessage("{0} exceeds the maximum allowable tile " + dimensionName + " at this resolution (" + dmax
							+ ").");
					return null;
				}
				double djob = jobmax * getGroundResolution();
				if (dvalue > djob) {
					this.setErrorMessage("{0} exceeds the " + dimensionName + " of the total job at this resolution (" + djob + ".");
					return null;
				}
				if (dvalue <= 0.0) {
					this.setErrorMessage("{0} is not a positive decimal number");
					return null;
				}
			} catch (NumberFormatException e) {
				this.setErrorMessage("{0} is not a positive decimal number.");
				return null;
			}
			return dvalue;
		}

		protected boolean checkNumTiles(TileByGeoDimensionValidator vtor, double tilewidth, double tileheight) {
			long tileswide = (long) Math.ceil(getDimensionWidth() * getGroundResolution() / tilewidth);
			long tileshigh = (long) Math.ceil(getDimensionHeight() * getGroundResolution() / tileheight);
			if (tileswide > ExportProps.MAX_TILES_PER_JOB || tileshigh > ExportProps.MAX_TILES_PER_JOB
					|| tileswide * tileshigh > ExportProps.MAX_TILES_PER_JOB) {
				if (vtor != null)
					vtor.setErrorMessage(tilewidth + " x " + tileheight
							+ " tiles are too small and will exceed the maximum number allowed per job ("
							+ ExportProps.MAX_TILES_PER_JOB + ").");
				return false;
			}
			return true;
		}
	}

	public class TileGeoXValidator extends TileByGeoDimensionValidator {
		public TileGeoXValidator() {
			super("width");
		}

		@Override
		protected boolean isValidString(String value) {
			Double wvalue = getValidDimension(this, value, ExportProps.MAX_TILE_WIDTH, getDimensionWidth());
			if (wvalue == null)
				return false;
			if (yDistanceTextBox.getValue() != null) {
				Double hvalue = getValidDimension(null, yDistanceTextBox.getValue().toString(), ExportProps.MAX_TILE_HEIGHT,
						getDimensionHeight());
				if (hvalue != null)
					return checkNumTiles(this, wvalue.doubleValue(), hvalue.doubleValue());
			}
			return true;
		}
	}

	public class TileGeoYValidator extends TileByGeoDimensionValidator {
		public TileGeoYValidator() {
			super("height");
		}

		@Override
		protected boolean isValidString(String value) {
			Double hvalue = getValidDimension(this, value, ExportProps.MAX_TILE_HEIGHT, getDimensionHeight());
			if (hvalue == null)
				return false;
			if (xDistanceTextBox.getValue() != null) {
				Double wvalue = getValidDimension(null, xDistanceTextBox.getValue().toString(), ExportProps.MAX_TILE_WIDTH,
						getDimensionWidth());
				if (wvalue != null)
					return checkNumTiles(this, wvalue.doubleValue(), hvalue.doubleValue());
			}
			return true;
		}
	}

	public class TileByDivisionValidator extends IntegerValidator {
		private String dimensionName;
		private String spanName;

		public TileByDivisionValidator(String dimensionName, String spanName) {
			super("Tile divisor must be a positive integer.");
			this.dimensionName = dimensionName;
			this.spanName = spanName;
		}

		protected Integer getValidDivisor(TileByDivisionValidator vtor, String value, int tilemax, int jobmax) {
			Integer ivalue = null;
			try {
				ivalue = new Integer(Integer.parseInt(value));
				if (ivalue <= 0) {
					this.setErrorMessage("{0} is not a positive integer");
					return null;
				}
				if (((jobmax - 1) / ivalue) + 1 > tilemax) {
					this.setErrorMessage("Dividing into {0} tile " + spanName
							+ "s produces tiles that exceed the maximum allowable tile " + dimensionName + " (" + tilemax + ").");
					return null;
				}
			} catch (NumberFormatException e) {
				this.setErrorMessage("{0} is not a positive integer.");
				return null;
			}
			return ivalue;
		}

		protected boolean checkNumTiles(TileByDivisionValidator vtor, int columns, int rows) {
			long tileswide = columns;
			long tileshigh = rows;
			if (tileswide > ExportProps.MAX_TILES_PER_JOB || tileshigh > ExportProps.MAX_TILES_PER_JOB
					|| tileswide * tileshigh > ExportProps.MAX_TILES_PER_JOB) {
				if (vtor != null)
					vtor.setErrorMessage(columns + " x " + rows + " exceeds the maximum number of tiles allowed per job ("
							+ ExportProps.MAX_TILES_PER_JOB + ").");
				return false;
			}
			return true;
		}
	}

	public class TileXDivisorValidator extends TileByDivisionValidator {
		public TileXDivisorValidator() {
			super("width", "column");
		}

		@Override
		protected boolean isValidString(String value) {
			Integer wvalue = getValidDivisor(this, value, ExportProps.MAX_TILE_WIDTH, getDimensionWidth());
			if (wvalue == null)
				return false;
			if (yTilesTextBox.getValue() != null) {
				Integer hvalue = getValidDivisor(null, yTilesTextBox.getValue().toString(), ExportProps.MAX_TILE_HEIGHT,
						getDimensionHeight());
				if (hvalue != null)
					return checkNumTiles(this, wvalue.intValue(), hvalue.intValue());
			}
			return true;
		}
	}

	public class TileYDivisorValidator extends TileByDivisionValidator {
		public TileYDivisorValidator() {
			super("height", "row");
		}

		@Override
		protected boolean isValidString(String value) {
			Integer hvalue = getValidDivisor(this, value, ExportProps.MAX_TILE_HEIGHT, getDimensionHeight());
			if (hvalue == null)
				return false;
			if (xTilesTextBox.getValue() != null) {
				Integer wvalue = getValidDivisor(null, xTilesTextBox.getValue().toString(), ExportProps.MAX_TILE_WIDTH,
						getDimensionWidth());
				if (wvalue != null)
					return checkNumTiles(this, wvalue.intValue(), hvalue.intValue());
			}
			return true;
		}
	}

	public class WidthHeightValidator extends IntegerValidator {
		public WidthHeightValidator() {
			super(new String("Must be a positive integer less than " + Integer.MAX_VALUE));
		}

		@Override
		protected boolean isValidString(String value) {
			try {
				int val = Integer.parseInt(value);
				return val > 0;
			} catch (Exception e) {
				return false;
			}
		}
	}

	public class GroundResolutionValidator extends DoubleValidator {
		public GroundResolutionValidator() {
			super(new String("Must be a positive floating point number."));
		}

		@Override
		protected boolean isValidString(String value) {
			try {
				double val = Double.parseDouble(value);
				return val > 0;
			} catch (Exception e) {
				return false;
			}
		}
	}

	private class JobNameUniqueValidator extends AbstractStringValidator {
		public JobNameUniqueValidator(String errorMessage) {
			super(errorMessage);
		}

		@Override
		protected boolean isValidString(String value) {
			return ExportProps.jobNameIsUnique(value);
		}
	}

	@Override
	public void taskFinished(Runnable r) {
	}

	@Override
	public void taskRunning(Runnable r) {
	}

	@Override
	public void taskQueued(Runnable r) {
		((ExpressZipWindow) getApplication().getMainWindow()).showNotification(String.format("Job \"%s\" Queued", r.toString()));
	}

	@Override
	public void taskError(Runnable r, Throwable e) {
	}

	public void setGriddingDrawEnabled(boolean griddingDrawEnabled) {
		this.griddingDrawEnabled = griddingDrawEnabled;
		updateGriddingEnabledState();
	}

	public static Panel getScrollableComponent(Component form) {
		VerticalLayout vl = new VerticalLayout();
		vl.addComponent(form);

		Panel panel = new Panel();
		panel.setContent(vl);
		return panel;
	}
}
