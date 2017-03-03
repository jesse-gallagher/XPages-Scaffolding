// Based heavily on com.ibm.xsp.extlib.component.dynamicview.DominoDynamicColumnBuilder

package frostillicus.xsp.model.component;

import javax.faces.context.FacesContext;

import lotus.domino.View;

import com.ibm.xsp.extlib.component.dynamicview.DominoDynamicColumnBuilder;
import com.ibm.xsp.extlib.component.dynamicview.UIDynamicViewPanel;

import frostillicus.xsp.model.domino.DominoModelList;
import frostillicus.xsp.model.ModelObject;

/**
 * @since 1.0
 */
public class ModelListDynamicColumnBuilder extends DominoDynamicColumnBuilder {

	@SuppressWarnings("unused")
	private final FacesContext context_;
	private final UIDynamicViewPanel panel_;

	public ModelListDynamicColumnBuilder(final FacesContext context, final UIDynamicViewPanel viewPanel) {
		super(context, viewPanel);

		context_ = context;
		panel_ = viewPanel;
	}

	@Override
	protected View findView() {
		@SuppressWarnings("unchecked")
		DominoModelList<? extends ModelObject> modelList = (DominoModelList<? extends ModelObject>)panel_.getDataModel();
		return modelList.getView();
	}
}
