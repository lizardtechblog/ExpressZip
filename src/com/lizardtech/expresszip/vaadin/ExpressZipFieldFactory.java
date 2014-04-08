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

import com.vaadin.data.Item;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormFieldFactory;

public class ExpressZipFieldFactory implements FormFieldFactory {
	private static final long serialVersionUID = -1219306186092769321L;

	@Override
	public Field createField(Item item, Object propertyId, Component uiContext) {
		Field field = DefaultFieldFactory.get().createField(item, propertyId, uiContext);
		if (propertyId.equals("exportProps"))
			field = null;
		else if (propertyId.equals("exportProps.jobName")) {
			field.setCaption("Job Name");
		} else if (propertyId.equals("exportProps.email")) {
			field.setCaption("Email");
		} else if (propertyId.equals("exportProps.userNotation")) {
			field.setCaption("User name");
		} else if (propertyId.equals("DL_URL")) {
			field = new LinkField(item, propertyId);
			String tt = (String) item.getItemProperty("DL_URL").getValue();
			field.setDescription(tt);
			field.setCaption("Download URL");
		} else if (propertyId.equals("log_URL")) {
			field = new LinkField(item, propertyId);
			String tt = (String) item.getItemProperty("log_URL").getValue();
			field.setDescription(tt);
			field.setCaption("Log URL");
		}

		if (field != null) {
			field.setReadOnly(true);
			field.setWidth("100%");
		}
		return field;
	}

}
