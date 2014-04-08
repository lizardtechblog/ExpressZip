package com.lizardtech.expresszip.vaadin;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.NativeButton;

public class ExpressZipButton extends NativeButton {
	private static final long serialVersionUID = 1L;
	public enum Style {
		STEP, ACTION, MENU
	}

	public ExpressZipButton(String caption, Style buttonstyle) {
		super(caption);
		init(buttonstyle);
	}

	public ExpressZipButton(String caption, Style buttonstyle, ClickListener clickListener) {
		super(caption, clickListener);
		init(buttonstyle);
	}

	private void init(Style buttonstyle) {
		setStyleName("expresszip-button");
		addStyleName("expresszip-" + buttonstyle.toString().toLowerCase());
		if (getCaption().equals("Next")) {
			setHtmlContentAllowed(true);
			setCaption("Next&nbsp;<img src=\"/ExpressZip/VAADIN/themes/ExpressZip/img/arrow-next.png\">");
		} else if (getCaption().equals("Back")) {
			setHtmlContentAllowed(true);
			setCaption("<img src=\"/ExpressZip/VAADIN/themes/ExpressZip/img/arrow-back.png\">&nbsp;Back");
		}
	}
}
