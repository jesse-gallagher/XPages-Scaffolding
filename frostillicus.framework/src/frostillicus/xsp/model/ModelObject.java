package frostillicus.xsp.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.faces.model.DataModel;

import com.ibm.xsp.model.FileRowData;
import com.ibm.xsp.model.DataObject;
import com.ibm.xsp.model.ViewRowData;

public interface ModelObject extends Serializable, DataObject, ViewRowData {
	public boolean delete();

	public void deleteAttachment(String fieldName, String attachmentName);

	public List<FileRowData> getAttachmentList(String fieldName);

	public String getId();

	public boolean isNew();

	public boolean category();

	public int columnIndentLevel();

	public String viewRowPosition();

	public boolean save();

	public DataModel getAttachmentData(String key);

	public List<FileRowData> getEmbeddedImageList(String fieldName);

	public Set<String> propertyNames(boolean includeSystem);

	public Set<String> columnPropertyNames();

	public Date lastModified();

	public Date created();

	public List<String> modifiedBy();

	public boolean readonly();

	public void freeze();

	public void unfreeze();
}