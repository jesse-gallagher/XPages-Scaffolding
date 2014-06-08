package frostillicus.xsp.model.component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import com.ibm.xsp.actions.document.DocumentAdapter;
import com.ibm.xsp.binding.ActionOutcomeUrl;
import com.ibm.xsp.model.FileRowData;

import frostillicus.xsp.model.ModelObject;

public class ModelDocumentAdapter implements DocumentAdapter {
	private final String id_;

	public ModelDocumentAdapter(final ModelObject modelObject) {
		id_ = modelObject.getId();
	}

	@Override
	public void addAttachment(final FacesContext context, final Object document, final String name, final InputStream istream, final int length, final String description, final String type) {
		// NOP
	}

	@Override
	public void addOpenPageParameters(final FacesContext context, final String var, final ActionOutcomeUrl outcomeUrl) {
		// NOP
	}

	@Override
	public void delete(final FacesContext context, final Object document) {
		((ModelObject)document).delete();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void deleteAttachments(final FacesContext context, final Object document, final String name, final boolean deleteAll) {
		// In this case, "document" is a HashMap tuple
		Map.Entry<ModelObject, String> tuple = ((Map<ModelObject, String>)document).entrySet().iterator().next();
		tuple.getKey().deleteAttachment(tuple.getValue(), name);
	}

	@Override
	public String getDocumentId(final FacesContext context, final String var) {
		return id_;
	}

	@Override
	public String getDocumentPage(final FacesContext context, final String documentId) {
		// TODO Do this, maybe?
		return null;
	}

	@Override
	public List<FileRowData> getEmbeddedImageList(final Object document, final String fieldName) {
		return ((ModelObject)document).getAttachmentList(fieldName);
	}

	@Override
	public String getParentId(final FacesContext context, final Object document) {
		return "";
	}

	@Override
	public boolean isEditable(final FacesContext context, final Object document) {
		return !((ModelObject)document).readonly();
	}

	@Override
	public void modifyField(final FacesContext context, final Object document, final String name, final Object value) {
		((ModelObject)document).setValue(name, value);
	}

	@Override
	public void save(final FacesContext context, final Object document) {
		((ModelObject)document).save();
	}

	@Override
	public void setDocument(final FacesContext context, final Object document, final Object value) {
		// NOP
	}

	@Override
	public void setUserReadOnly(final FacesContext context, final Object document, final boolean readOnly) {
		// TODO Implement when read-only support exists at the model level
		ModelObject model = (ModelObject)document;
		if(readOnly) {
			model.freeze();
		} else {
			model.unfreeze();
		}
	}

}