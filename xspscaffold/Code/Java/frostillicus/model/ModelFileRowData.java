package frostillicus.model;

import java.io.Serializable;

import com.ibm.xsp.model.FileRowData;

public class ModelFileRowData implements FileRowData, Serializable {
	private static final long serialVersionUID = 1L;

	private long created_;
	private String href_;
	private long lastModified_;
	private long length_;
	private String name_;
	private String type_;

	public long getCreated() { return created_; }
	public void setCreated(final long created) { created_ = created; }

	public String getHref() { return href_; }
	public void setHref(final String href) { href_ = href; }

	public long getLastModified() { return lastModified_; }
	public void setLastModified(final long lastModified) { lastModified_ = lastModified; }

	public long getLength() { return length_; }
	public void setLength(final long length) { length_ = length; }

	public String getName() { return name_; }
	public void setName(final String name) { name_ = name; }

	public String getType() { return type_; }
	public void setType(final String type) { type_ = type; }
}