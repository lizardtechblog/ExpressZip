package org.vaadin.vol.client.wrappers;

import java.util.HashMap;
import java.util.Vector;

import org.vaadin.vol.client.wrappers.control.Control;
import org.vaadin.vol.client.wrappers.control.SelectFeature;
import org.vaadin.vol.client.wrappers.layer.Layer;

import com.google.gwt.core.client.JsArray;

/**
 * Factory to create new SelectFeature instances or deliver existing ones
 * @author eiko
 *
 */
public final class SelectFeatureFactory {
	private static SelectFeatureFactory inst=null;
	private HashMap<String,SelectFeatureContainer> selectFeatureControls=null;	
	
	private SelectFeatureFactory() {
	}
	
	public static SelectFeatureFactory getInst() {
		if (inst==null)
			inst = new SelectFeatureFactory();
		return inst;
	}
	
	
	/**
	 * return an existing SelectFeature instance for the specific id or create a new one
	 * @param selectionCtrlId
	 * @param map
	 * @param targetLayer
	 * @return
	 */
	public SelectFeature getOrCreate(String selectionCtrlId,Map map,Layer targetLayer) {
		if (selectionCtrlId==null) {
			SelectFeature control = SelectFeature.create(targetLayer);
			map.addControl(control);
            control.activate();
            return control;
		}
		else {
			if (selectFeatureControls==null) {
				selectFeatureControls = new HashMap<String,SelectFeatureContainer>();
				SelectFeatureContainer cont = new SelectFeatureContainer(targetLayer,map);
				selectFeatureControls.put(selectionCtrlId,cont);
				return cont.getControl();				
			}
			else {
				SelectFeatureContainer cont = selectFeatureControls.get(selectionCtrlId);
				if (cont==null) {
					cont = new SelectFeatureContainer(targetLayer,map);
					selectFeatureControls.put(selectionCtrlId,cont);
					return cont.getControl();					
				}
				else {
					cont.addLayer(targetLayer);
					return cont.getControl();
				}
			}			
		}
	}
	
	public void removeLayer(SelectFeature control,String selectionCtrlId,Map map,Layer targetLayer) {
		if (selectionCtrlId==null) {
            control.deActivate();
            map.removeControl(control);
		}
		else {
			SelectFeatureContainer cont = selectFeatureControls.get(selectionCtrlId);
			cont.removeLayer(targetLayer,map);
		}
	}
	
	class SelectFeatureContainer {
		private Vector<Layer> layerVector;
		private SelectFeature control;
		SelectFeatureContainer(Layer targetLayer, Map map) {
			layerVector = new Vector<Layer>();
			layerVector.add(targetLayer);
			control=SelectFeature.create(targetLayer);
			map.addControl(control);
            control.activate();
		}
		
		void addLayer(Layer targetLayer) {
			if (!layerVector.contains(targetLayer))
				layerVector.add(targetLayer);
			// OpenLayers documentation tells for this function: Attach a new layer to the control, overriding any existing layers.
			// so reinit with all to this point registrated layers
			JsArray<Layer> layerArray = (JsArray<Layer>) JsArray.createArray();
			for (int i=0;i<layerVector.size();i++) {
				layerArray.push(layerVector.get(i));
			}
			control.setLayer(layerArray);
		}
		
		void removeLayer(Layer targetLayer,Map map) {
			if (layerVector.contains(targetLayer)) {
				layerVector.remove(targetLayer);
				JsArray<Layer> layerArray = (JsArray<Layer>) JsArray.createArray();
				for (int i=0;i<layerVector.size();i++) {
					layerArray.push(layerVector.get(i));
				}

				control.setLayer(layerArray);				
			}
			if (layerVector.size()==0) {
				this.control=null;
	            control.deActivate();
	            map.removeControl(control);
			}
		}
		
		
		SelectFeature getControl() {
			return control;
		}
		
	}
}
