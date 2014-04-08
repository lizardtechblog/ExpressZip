package com.lizardtech.expresszip.vaadin;

import java.util.Iterator;
import java.util.Set;

import com.lizardtech.expresszip.model.ExpressZipLayer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;

@SuppressWarnings("serial")
public class ExpressZipTreeTable extends TreeTable{

	protected static final String LAYER = "Layers";

	public ExpressZipTreeTable() {
		super();
		addStyleName("expresszip");

		setSizeFull();

		// turn off column reordering and collapsing
		setColumnReorderingAllowed(false);
		setPageLength(0); // turn off paging

		setMultiSelect(true);
		setImmediate(true);
		setMultiSelectMode(MultiSelectMode.DEFAULT);
		setSelectable(true);

		setItemDescriptionGenerator(new ItemDescriptionGenerator() {
			
			@Override
			public String generateDescription(Component source, Object itemId, Object propertyId) {
				String tooltip = "";
				if(null != propertyId && propertyId.equals(LAYER) && itemId instanceof ExpressZipLayer) {
					ExpressZipLayer l = (ExpressZipLayer)itemId;
					Set<String> projs = l.getSupportedProjections();
					tooltip = "<p>Click to view layer boundary. Right-click for options.</p><h2>" + l.getName() + "</h2><ul>";
					Iterator<String> iter = projs.iterator();
					while(iter.hasNext()) {
						tooltip += "<li>" + iter.next() + "</li>";
					}
					tooltip += "</ul>";
				}
  				return tooltip;
			}
		});

		enableFirstColumnHighlighter(this);
	}
	
	public static void enableFirstColumnHighlighter(final Table table) {
	
		table.setCellStyleGenerator(new Table.CellStyleGenerator() {
			@Override
			public String getStyle(Object itemId, Object propertyId) {
				if( table.getVisibleColumns()[0] == propertyId ) {
					return "greenbar";
				}
				return null;
			}
		});
	}
}
