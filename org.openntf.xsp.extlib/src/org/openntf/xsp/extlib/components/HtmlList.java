package org.openntf.xsp.extlib.components;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

public class HtmlList extends AbstractHtmlTag {

	private static final String RENDERER_TYPE = "org.openntf.xsp.extlib.HtmlList";
	private static final String FAMILY = "org.openntf.xsp.extlib";
	private Boolean ordered;

	public HtmlList() {
		setRendererType(RENDERER_TYPE);
	}

	public String getFamily() {
		return FAMILY;
	}

	public boolean isOrdered() {
		if (ordered != null) {
			return ordered;
		} else {
			ValueBinding vb = getValueBinding("order");
			if (vb != null && vb.getValue(FacesContext.getCurrentInstance()) != null) {
				return (Boolean) vb.getValue(FacesContext.getCurrentInstance());
			} else {
				return false;
			}
		}
	}

	public void setOrdered(boolean ordered) {
		this.ordered = ordered;
	}
}
