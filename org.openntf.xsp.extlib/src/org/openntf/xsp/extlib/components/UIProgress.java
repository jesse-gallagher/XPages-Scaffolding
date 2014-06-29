package org.openntf.xsp.extlib.components;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.openntf.xsp.extlib.Activator;

public class UIProgress extends UIComponentBase {
    private static final String RENDERER_TYPE = "org.openntf.xsp.extlib.xsp.UIProgress";
	private static final String COMPONENT_FAMILY = "org.openntf.xsp.extlib.UIProgress";
	private final static boolean _debug = Activator._debug;
	protected String barStyle;
	protected String barType;
	protected String barWidth;
	
	public UIProgress(){
		if (_debug) System.out.println(getClass().getName() + " created");
        setRendererType(RENDERER_TYPE);
    }

    /**
	 * @return the barStyle
	 */
	public String getBarStyle() {
		 if (this.barStyle != null) {
		        return this.barStyle;
		    }
		    ValueBinding _vb = getValueBinding("barStyle");
		    if (_vb != null) {
		        return (String) _vb.getValue(getFacesContext());
		    } else {
		        return "Plain";
		    }
	}
	
    /**
	 * @return the barType
	 */
	public String getBarType() {
		 if (this.barType != null) {
		        return this.barType;
		    }
		    ValueBinding _vb = getValueBinding("barType");
		    if (_vb != null) {
		        return (String) _vb.getValue(getFacesContext());
		    } else {
		        return "Info";
		    }
	}

	/**
	 * @return the barWidth
	 */
	public String getBarWidth() {
		 if (this.barWidth != null) {
		        return this.barWidth;
		    }
		    ValueBinding _vb = getValueBinding("barWidth");
		    if (_vb != null) {
		        return (String) _vb.getValue(getFacesContext());
		    } else {
		        return "0%";
		    }
	}
	
	@Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }
	
	@Override
	public void restoreState(FacesContext _context, Object _state) {
		Object _values[] = (Object[]) _state;
		super.restoreState(_context, _values[0]);
		this.barStyle = (String) _values[1];
		this.barType = (String) _values[2];
		this.barWidth = (String) _values[3];
	}

	@Override
	public Object saveState(FacesContext _context) {
		Object _values[] = new Object[4];
		_values[0] = super.saveState(_context);
		_values[1] = getBarStyle();
		_values[2] = getBarType();
		_values[2] = getBarWidth();
		return _values;
	}
	
	/**
	 * @param barStyle the barStyle to set
	 */
	public void setBarStyle(String barStyle) {
		this.barStyle = barStyle;
	}
	
	/**
	 * @param barWidth the barWidth to set
	 */
	public void setBarWidth(String barWidth) {
		this.barWidth = barWidth;
	}

	/**
	 * @param barType the barType to set
	 */
	public void setBarType(String ribbonText) {
		this.barType = ribbonText;
	}
}

