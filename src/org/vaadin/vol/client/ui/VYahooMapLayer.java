package org.vaadin.vol.client.ui;

import org.vaadin.vol.client.wrappers.layer.YahooLayer;

public class VYahooMapLayer extends VAbstracMapLayer<YahooLayer> {

	@Override
	YahooLayer createLayer() {
		return YahooLayer.create(getDisplayName());
	}

}
