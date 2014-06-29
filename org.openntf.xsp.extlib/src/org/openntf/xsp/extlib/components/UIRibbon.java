package org.openntf.xsp.extlib.components;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.openntf.xsp.extlib.Activator;

public class UIRibbon extends UIComponentBase {
    private static final String RENDERER_TYPE = "org.openntf.xsp.extlib.xsp.UIRibbon";
	private static final String COMPONENT_FAMILY = "org.openntf.xsp.extlib.UIRibbon";
	private final static boolean _debug = Activator._debug;
	protected String ribbonColor;
	protected String ribbonText;
	
	public UIRibbon(){
		if (_debug) System.out.println(getClass().getName() + " created");
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }
	
    /**
	 * @return the ribbonColor
	 */
	public String getRibbonColor() {
		 if (this.ribbonColor != null) {
		        return this.ribbonColor;
		    }
		    ValueBinding _vb = getValueBinding("ribbonColor");
		    if (_vb != null) {
		        return (String) _vb.getValue(getFacesContext());
		    } else {
		        return "Green";
		    }
	}

	/**
	 * @return the ribbonText
	 */
	public String getRibbonText() {
		 if (this.ribbonText != null) {
		        return this.ribbonText;
		    }
		    ValueBinding _vb = getValueBinding("ribbonText");
		    if (_vb != null) {
		        return (String) _vb.getValue(getFacesContext());
		    } else {
		        return null;
		    }
	}

	/**
	 * @param ribbonColor the ribbonColor to set
	 */
	public void setRibbonColor(String ribbonColor) {
		this.ribbonColor = ribbonColor;
	}

	/**
	 * @param ribbonText the ribbonText to set
	 */
	public void setRibbonText(String ribbonText) {
		this.ribbonText = ribbonText;
	}
	
	@Override
	public void restoreState(FacesContext _context, Object _state) {
		Object _values[] = (Object[]) _state;
		super.restoreState(_context, _values[0]);
		this.ribbonColor = (String) _values[1];
		this.ribbonText = (String) _values[2];
	}

	@Override
	public Object saveState(FacesContext _context) {
		Object _values[] = new Object[3];
		_values[0] = super.saveState(_context);
		_values[1] = getRibbonColor();
		_values[2] = getRibbonText();
		return _values;
	}
}

