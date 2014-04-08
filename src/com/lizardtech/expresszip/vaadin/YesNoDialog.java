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

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class YesNoDialog extends Window implements Button.ClickListener {
	private static final long serialVersionUID = 76813921235072263L;

	private Callback callback;
	private Button yes;
	private Button no;
	
	public YesNoDialog(String caption, String question, Callback callback) {
		super(caption);

		setModal(true);
		setWidth("350px");
		setHeight("150px");
		setClosable(false);
		setResizable(false);
		
		this.callback = callback;

		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.setMargin(true);
		
		if (question != null) {
			layout.addComponent(new Label(question));
		}

		yes = new ExpressZipButton("Yes", ExpressZipButton.Style.ACTION, this);
		no = new ExpressZipButton("No", ExpressZipButton.Style.ACTION, this);

		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		hl.addComponent(yes);
		hl.addComponent(no);
		layout.addComponent(hl);
		layout.setComponentAlignment(hl, Alignment.BOTTOM_RIGHT);

		setContent(layout);
		
	}

	public void buttonClick(ClickEvent event) {
		if (getParent() != null) {
			((Window) getParent()).removeWindow(this);
		}
		callback.onDialogResult(event.getSource() == yes);
	}

	public interface Callback {
		public void onDialogResult(boolean resultIsYes);
	}

}
