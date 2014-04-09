package frostillicus.model;

import java.io.Serializable;
import java.util.List;

import javax.faces.model.DataModel;

import com.ibm.xsp.model.FileRowData;
import com.ibm.xsp.model.DataObject;
import com.ibm.xsp.model.ViewRowData;

public interface ModelObject extends Serializable, DataObject, ViewRowData {
	public boolean delete();

	public void deleteAttachment(final String fieldName, final String attachmentName);

	public List<FileRowData> getAttachmentList(final String fieldName);

	public String getId();

	public boolean isNew();

	public boolean isCategory();

	public int columnIndentLevel();

	public String viewRowPosition();

	public boolean save();

	public DataModel getAttachmentData(final String key);

	public List<FileRowData> getEmbeddedImageList(final String fieldName);
}