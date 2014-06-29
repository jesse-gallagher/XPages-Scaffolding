package org.openntf.xsp.extlib.components;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.openntf.xsp.extlib.Activator;

public class UIStackOverflowBadge extends UIComponentBase {
	private static final String RENDERER_TYPE = "org.openntf.xsp.extlib.xsp.UIStackOverflowBadge";
	private static final String COMPONENT_FAMILY = "org.openntf.xsp.extlib.UIStackOverflowBadge";
	private final static boolean _debug = Activator._debug;
	protected String badgeColor;
	protected String badgeText;
	protected Boolean badgeType;

	public UIStackOverflowBadge() {
		if (_debug)
			System.out.println(getClass().getName() + " created");
		setRendererType(RENDERER_TYPE);
	}

	@Override
	public String getFamily() {
		return COMPONENT_FAMILY;
	}

	/**
	 * @return the ribbonColor
	 */
	public String getBadgeColor() {
		if (this.badgeColor != null) {
			return this.badgeColor;
		}
		ValueBinding _vb = getValueBinding("badgeColor");
		if (_vb != null) {
			return (String) _vb.getValue(getFacesContext());
		} else {
			return "Green";
		}
	}

	/**
	 * @return the ribbonText
	 */
	public String getBadgeText() {
		if (this.badgeText != null) {
			return this.badgeText;
		}
		ValueBinding _vb = getValueBinding("badgeText");
		if (_vb != null) {
			return (String) _vb.getValue(getFacesContext());
		} else {
			return null;
		}
	}

	/**
	 * @return the badgeType
	 */
	public Boolean getBadgeType() {
		return badgeType;
	}

	/**
	 * @param ribbonColor
	 *            the ribbonColor to set
	 */
	public void setBadgeColor(String ribbonColor) {
		this.badgeColor = ribbonColor;
	}

	/**
	 * @param ribbonText
	 *            the ribbonText to set
	 */
	public void setBadgeText(String ribbonText) {
		this.badgeText = ribbonText;
	}

	/**
	 * @param badgeType
	 *            the badgeType to set
	 */
	public void setBadgeType(Boolean badgeType) {
		this.badgeType = badgeType;
	}

	@Override
	public void restoreState(FacesContext _context, Object _state) {
		Object _values[] = (Object[]) _state;
		super.restoreState(_context, _values[0]);
		this.badgeColor = (String) _values[1];
		this.badgeText = (String) _values[2];
		this.badgeType = (Boolean) _values[3];
	}

	@Override
	public Object saveState(FacesContext _context) {
		Object _values[] = new Object[4];
		_values[0] = super.saveState(_context);
		_values[1] = getBadgeColor();
		_values[2] = getBadgeText();
		_values[3] = getBadgeType();
		return _values;
	}
}
