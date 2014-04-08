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

import org.vaadin.addon.customfield.CustomField;

import com.vaadin.data.Item;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Link;

/**
 * Created with IntelliJ IDEA. User: mrosen Date: 9/24/13 Time: 10:04 AM To change this template use File | Settings | File
 * Templates.
 */
public class LinkField extends CustomField {
	private static final long serialVersionUID = -1873791297409887082L;
	private Link link;

	public LinkField(Item item, Object propertyId) {

		String url = (String) item.getItemProperty(propertyId).getValue();
		link = new Link(url, new ExternalResource(url));
		link.setTargetName("_blank");
		setCompositionRoot(link);
	}

	@Override
	public Class<?> getType() {
		return LinkField.class;
	}
}
