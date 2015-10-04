package frostillicus.xsp.model.component;

import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;

import com.ibm.xsp.extlib.component.dynamicview.DynamicColumnBuilder;
import com.ibm.xsp.extlib.component.dynamicview.DynamicColumnBuilderFactory;
import com.ibm.xsp.extlib.component.dynamicview.UIDynamicViewPanel;

import frostillicus.xsp.model.AbstractModelList;

/**
 * @since 1.0
 */
public class ModelListDynamicColumnBuilderFactory extends DynamicColumnBuilderFactory {
	@Override
	public DynamicColumnBuilder createColumnBuilder(final FacesContext context, final UIDynamicViewPanel viewPanel, final DataModel dataModel) {
		if(dataModel instanceof AbstractModelList) {
			return new ModelListDynamicColumnBuilder(context, viewPanel);
		}
		return null;
	}
}
