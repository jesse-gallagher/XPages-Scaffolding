package frostillicus.xsp.model;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.faces.model.DataModel;
import javax.validation.metadata.ConstraintDescriptor;

import com.ibm.xsp.model.FileRowData;
import com.ibm.xsp.model.DataObject;
import com.ibm.xsp.model.ViewRowData;

public interface ModelObject extends Serializable, DataObject, ViewRowData {
	public boolean delete();

	public void deleteAttachment(Object key, String attachmentName);

	public List<FileRowData> getAttachmentList(Object key);

	public String getId();

	public boolean isNew();

	public boolean category();

	public int columnIndentLevel();

	public String viewRowPosition();

	public boolean save();

	public DataModel getAttachmentData(Object key);

	public List<FileRowData> getEmbeddedImageList(Object key);

	public Set<String> propertyNames(boolean includeSystem, boolean includeAll);

	public Set<String> columnPropertyNames();

	public Date lastModified();

	public Date created();

	public List<String> modifiedBy();

	public boolean readonly();

	public void freeze();

	public void unfreeze();

	public boolean frozen();

	public Type getGenericType(Object key);

	public Set<ConstraintDescriptor<?>> getConstraintDescriptors(Object key);

	public Field getField(Object key);
}