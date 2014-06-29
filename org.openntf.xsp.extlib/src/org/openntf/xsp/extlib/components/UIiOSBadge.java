package org.openntf.xsp.extlib.components;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.openntf.xsp.extlib.Activator;



public class UIiOSBadge extends UIComponentBase {
    private static final String RENDERER_TYPE = "org.openntf.xsp.extlib.xsp.UIiOSBadge";
	private static final String COMPONENT_FAMILY = "org.openntf.xsp.extlib.UIiOSBadge";
	private final static boolean _debug = Activator._debug;
	protected String badgerText;
	protected String badgerSize;
	protected String badgerPosition;
	protected String badgerColor;
	protected String badgerType;
	
	public UIiOSBadge(){
		if (_debug) System.out.println(getClass().getName() + " created");
        setRendererType(RENDERER_TYPE);
    }
	
	@Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    /**
	 * @return the badgerColor
	 */
	public String getBadgerColor() {
		 if (this.badgerColor != null) {
		        return this.badgerColor;
		    }
		    ValueBinding _vb = getValueBinding("badgerColor");
		    if (_vb != null) {
		        return (String) _vb.getValue(getFacesContext());
		    } else {
		        return "red";
		    }
	}
	

	/**
	 * @return the badgerPosition
	 */
	public String getBadgerPosition() {
		 if (this.badgerPosition != null) {
		        return this.badgerPosition;
		    }
		    ValueBinding _vb = getValueBinding("badgerPosition");
		    if (_vb != null) {
		        return (String) _vb.getValue(getFacesContext());
		    } else {
		        return "top-right";
		    }
	}

	/**
	 * @return the badgerSize
	 */
	public String getBadgerSize() {
		 if (this.badgerSize != null) {
		        return this.badgerSize;
		    }
		    ValueBinding _vb = getValueBinding("badgerSize");
		    if (_vb != null) {
		        return (String) _vb.getValue(getFacesContext());
		    } else {
		        return "24";
		    }
	}
	
	/**
	 * @return the badgerText
	 */
	public String getBadgerText() {
		 if (this.badgerText != null) {
		        return this.badgerText;
		    }
		    ValueBinding _vb = getValueBinding("badgerText");
		    if (_vb != null) {
		        return (String) _vb.getValue(getFacesContext());
		    } else {
		        return null;
		    }
	}

	/**
	 * @return the badgerType
	 */
	public String getBadgerType() {
		 if (this.badgerType != null) {
		        return this.badgerType;
		    }
		    ValueBinding _vb = getValueBinding("badgerType");
		    if (_vb != null) {
		        return (String) _vb.getValue(getFacesContext());
		    } else {
		        return "number";
		    }
	}





	/**
	 * @param badgerColor the badgerColor to set
	 */
	public void setBadgerColor(String badgerColor) {
		this.badgerColor = badgerColor;
	}

	/**
	 * @param badgerPosition the badgerPosition to set
	 */
	public void setBadgerPosition(String badgerPosition) {
		this.badgerPosition = badgerPosition;
	}

	/**
	 * @param badgerSize the badgerSize to set
	 */
	public void setBadgerSize(String badgerSize) {
		this.badgerSize = badgerSize;
	}

	/**
	 * @param badgerText the badgerText to set
	 */
	public void setBadgerText(String badgerText) {
		this.badgerText = badgerText;
	}

	/**
	 * @param badgerType the badgerType to set
	 */
	public void setBadgerType(String badgerType) {
		this.badgerType = badgerType;
	}
	
	@Override
	public void restoreState(FacesContext _context, Object _state) {
		Object _values[] = (Object[]) _state;
		super.restoreState(_context, _values[0]);
		this.badgerColor = (String) _values[1];
		this.badgerPosition = (String) _values[2];
		this.badgerSize = (String) _values[3];
		this.badgerText = (String) _values[4];
		this.badgerType = (String) _values[5];
	}

	@Override
	public Object saveState(FacesContext _context) {
		Object _values[] = new Object[6];
		_values[0] = super.saveState(_context);
		_values[1] = getBadgerColor();
		_values[2] = getBadgerPosition();
		_values[3] = getBadgerSize();
		_values[4] = getBadgerText();
		_values[5] = getBadgerType();
		return _values;
	}
}

