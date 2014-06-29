package org.openntf.xsp.extlib.components;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import com.ibm.xsp.component.UIComponentTag;

public abstract class AbstractHtmlTag extends UIComponentTag {
	public static final String COMPONENT_TYPE = "org.openntf.xsp.HtmlTag";
	public static final String RENDERER_TYPE = "org.openntf.xsp.HtmlTag";
	private String dir;
	private String lang;
	private String onclick;
	private String ondblclick;
	private String onkeydown;
	private String onkeypress;
	private String onkeyup;
	private String onmousedown;
	private String onmousemove;
	private String onmouseout;
	private String onmouseover;
	private String onmouseup;
	private String role;
	private String style;
	private String styleClass;
	private String tagName;
	private String title;

	public String getDir() {
		return getStringProperty("dir", this.dir);
	}

	public String getLang() {
		return getStringProperty("lang", this.lang);
	}

	public String getOnclick() {
		return getStringProperty("onclick", this.onclick);
	}

	public String getOndblclick() {
		return getStringProperty("ondblclick", this.ondblclick);
	}

	public String getOnkeydown() {
		return getStringProperty("onkeydown", this.onkeydown);
	}

	public String getOnkeypress() {
		return getStringProperty("onkeypress", this.onkeypress);
	}

	public String getOnkeyup() {
		return getStringProperty("onkeyup", this.onkeyup);
	}

	public String getOnmousedown() {
		return getStringProperty("onmousedown", this.onmousedown);
	}

	public String getOnmousemove() {
		return getStringProperty("onmousemove", this.onmousemove);
	}

	public String getOnmouseout() {
		return getStringProperty("onmouseout", this.onmouseout);
	}

	public String getOnmouseover() {
		return getStringProperty("onmouseover", this.onmouseover);
	}

	public String getOnmouseup() {
		return getStringProperty("onmouseup", this.onmouseup);
	}

	public String getRole() {
		return getStringProperty("role", this.role);
	}

	protected String getStringProperty(String propertyName, String localValue) {
		if (localValue != null) {
			return localValue;
		}
		ValueBinding vb = getValueBinding(propertyName);
		if (vb != null) {
			return ((String) vb.getValue(getFacesContext()));
		}
		return null;
	}

	public String getStyle() {
		return getStringProperty("style", this.style);
	}

	public String getStyleClass() {
		return getStringProperty("styleClass", this.styleClass);
	}

	public String getTagname() {
		return getStringProperty("tagName", this.tagName);
	}

	public String getTagName() {
		return this.tagName;
	}

	public String getTitle() {
		return getStringProperty("title", this.title);
	}

	@Override
	public void restoreState(FacesContext context, Object state) {
		Object[] properties = (Object[]) state;
		int idx = 0;
		super.restoreState(context, properties[idx++]);
		this.dir = ((String) properties[idx++]);
		this.lang = ((String) properties[idx++]);
		this.onclick = ((String) properties[idx++]);
		this.ondblclick = ((String) properties[idx++]);
		this.onkeydown = ((String) properties[idx++]);
		this.onkeypress = ((String) properties[idx++]);
		this.onkeyup = ((String) properties[idx++]);
		this.onmousedown = ((String) properties[idx++]);
		this.onmousemove = ((String) properties[idx++]);
		this.onmouseout = ((String) properties[idx++]);
		this.onmouseover = ((String) properties[idx++]);
		this.onmouseup = ((String) properties[idx++]);
		this.role = ((String) properties[idx++]);
		this.style = ((String) properties[idx++]);
		this.styleClass = ((String) properties[idx++]);
		this.tagName = ((String) properties[idx++]);
		this.title = ((String) properties[idx++]);
	}

	@Override
	public Object saveState(FacesContext context) {
		Object[] properties = new Object[18];
		int idx = 0;
		properties[idx++] = super.saveState(context);
		properties[idx++] = this.dir;
		properties[idx++] = this.lang;
		properties[idx++] = this.onclick;
		properties[idx++] = this.ondblclick;
		properties[idx++] = this.onkeydown;
		properties[idx++] = this.onkeypress;
		properties[idx++] = this.onkeyup;
		properties[idx++] = this.onmousedown;
		properties[idx++] = this.onmousemove;
		properties[idx++] = this.onmouseout;
		properties[idx++] = this.onmouseover;
		properties[idx++] = this.onmouseup;
		properties[idx++] = this.role;
		properties[idx++] = this.style;
		properties[idx++] = this.styleClass;
		properties[idx++] = this.tagName;
		properties[idx++] = this.title;
		return properties;
	}

	public void setDir(String value) {
		this.dir = value;
	}

	public void setLang(String value) {
		this.lang = value;
	}

	public void setOnclick(String value) {
		this.onclick = value;
	}

	public void setOndblclick(String value) {
		this.ondblclick = value;
	}

	public void setOnkeydown(String value) {
		this.onkeydown = value;
	}

	public void setOnkeypress(String value) {
		this.onkeypress = value;
	}

	public void setOnkeyup(String value) {
		this.onkeyup = value;
	}

	public void setOnmousedown(String value) {
		this.onmousedown = value;
	}

	public void setOnmousemove(String value) {
		this.onmousemove = value;
	}

	public void setOnmouseout(String value) {
		this.onmouseout = value;
	}

	public void setOnmouseover(String value) {
		this.onmouseover = value;
	}

	public void setOnmouseup(String value) {
		this.onmouseup = value;
	}

	public void setRole(String value) {
		this.role = value;
	}

	public void setStyle(String value) {
		this.style = value;
	}

	public void setStyleClass(String value) {
		this.styleClass = value;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public void setTitle(String value) {
		this.title = value;
	}
}
