package frostillicus;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.tree.ITreeNode;
import com.ibm.xsp.extlib.tree.impl.*;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import lotus.domino.*;
import frostillicus.JSFUtil;

import javax.faces.context.FacesContext;
import java.net.URLEncoder;
import java.util.*;
import java.io.Serializable;

public class DominoOutline extends BasicNodeList {
	private static final long serialVersionUID = 1L;

	private String viewName;

	private Set<String> handledViews = new HashSet<String>();
	private Set<ViewDescription> viewDescriptions;
	private Map<String, String> xpageAlts = new HashMap<String, String>();
	@SuppressWarnings("unused")
	private Map<String, ViewDescription> viewDescriptionsByName = new HashMap<String, ViewDescription>();

	// Special node type
	private BasicLeafTreeNode otherViewsNode = null;
	private BasicLeafTreeNode otherFoldersNode = null;

	public DominoOutline() throws NotesException {
		try {
			Database database = ExtLibUtil.getCurrentDatabase();
			this.getViewDescriptions();

			Outline tempOut = database.getOutline("XSPOutline");
			viewName = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("viewName");
			viewName = viewName == null ? "" : viewName;

			OutlineEntry entry = tempOut.getFirst();
			while(entry != null) {
				entry = processNode(database, tempOut, entry, null);
			}

			// Now go back to see if there was an "other views" node
			if(this.otherViewsNode != null) {
				int insertionIndex = this.getChildren().indexOf(this.otherViewsNode);

				for(ViewDescription view : this.getViewDescriptions()) {
					if(!handledViews.contains(view.getUniversalID()) && !view.isFolder() && !view.isHidden()) {
						BasicLeafTreeNode viewNode = new BasicLeafTreeNode();
						viewNode.setLabel(view.getName());
						if(!StringUtil.isEmpty(view.getXpageAlt())) {
							viewNode.setHref("/" + view.getXpageAlt());
						} else {
							viewNode.setHref("/View.xsp?viewName=" + urlEncode(view.getName()));
						}
						viewNode.setImage("/tango/x-office-spreadsheet-mod.gif?Open&ImgIndex=1");

						viewNode.setSelected(view.matches(viewName));

						this.getChildren().add(++insertionIndex, viewNode);
					}
				}
			}
			// And now for "other folders"
			if(this.otherFoldersNode != null) {
				int insertionIndex = this.getChildren().indexOf(this.otherFoldersNode);

				for(ViewDescription view : this.getViewDescriptions()) {
					if(!handledViews.contains(view.getUniversalID()) && view.isFolder() & !view.isHidden()) {
						BasicLeafTreeNode viewNode = new BasicLeafTreeNode();
						viewNode.setLabel(view.getName());
						if(!StringUtil.isEmpty(view.getXpageAlt())) {
							viewNode.setHref("/" + view.getXpageAlt());
						} else {
							viewNode.setHref("/View.xsp?viewName=" + urlEncode(view.getName()));
						}
						viewNode.setImage("/tango/folder.png?Open&ImgIndex=1");

						viewNode.setSelected(view.matches(viewName));

						this.getChildren().add(++insertionIndex, viewNode);
					}
				}
			}

		} catch(NullPointerException npe) {
			npe.printStackTrace();
			addChild(new BasicLeafTreeNode());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	private OutlineEntry processNode(Database database, Outline outline, OutlineEntry entry, BasicContainerTreeNode root) throws NotesException {
		int level = entry.getLevel();

		switch(entry.getType()) {
		case OutlineEntry.OUTLINE_OTHER_UNKNOWN_TYPE:
			// Must be a container

			BasicContainerTreeNode containerNode = createSectionNode(entry);
			if(root == null) {
				addChild(containerNode);
			} else {
				root.addChild(containerNode);
			}
			// Look for its children
			OutlineEntry nextEntry = outline.getNext(entry);
			while(nextEntry != null && nextEntry.getLevel() > level) {
				nextEntry = processNode(database, outline, nextEntry, containerNode);
			}

			return nextEntry;
		case OutlineEntry.OUTLINE_TYPE_NAMEDELEMENT:
			View view = database.getView(entry.getNamedElement());
			if(view != null) {
				this.handledViews.add(view.getUniversalID());

				ITreeNode leafNode = createViewNode(entry);
				if(root == null) {
					addChild(leafNode);
				} else {
					root.addChild(leafNode);
				}
				view.recycle();
			}

			return outline.getNext(entry);
		case OutlineEntry.OUTLINE_OTHER_VIEWS_TYPE:
			this.otherViewsNode = this.createStubNode();
			this.otherViewsNode.setRendered(false);
			addChild(this.otherViewsNode);
			break;
		case OutlineEntry.OUTLINE_OTHER_FOLDERS_TYPE:
			this.otherFoldersNode = this.createStubNode();
			this.otherFoldersNode.setRendered(false);
			addChild(this.otherFoldersNode);
			break;
		case OutlineEntry.OUTLINE_TYPE_URL:
			BasicLeafTreeNode urlNode = this.createStubNode();
			urlNode.setLabel(entry.getLabel());

			if(entry.getImagesText().isEmpty()) {
				urlNode.setImage("/.ibmxspres/domino/icons/ecblank.gif");
			} else {
				urlNode.setImage("/" + urlEncode(entry.getImagesText()) + "?Open&ImgIndex=1");
			}
			String pageName = (entry.getURL().startsWith("/") ? "" : "/") + entry.getURL();
			urlNode.setHref(pageName);
			urlNode.setSelected(pageName.startsWith(JSFUtil.getViewRoot().getPageName()));
			urlNode.setRendered(resolveHideFormula(entry));
			if(root == null) {
				addChild(urlNode);
			} else {
				root.addChild(urlNode);
			}
		}

		return outline.getNext(entry);
	}

	private BasicContainerTreeNode createSectionNode(OutlineEntry entry) throws NotesException {
		BasicContainerTreeNode node = new BasicContainerTreeNode();
		node.setLabel(entry.getLabel());
		if(entry.getImagesText().isEmpty()) {
			node.setImage("/.ibmxspres/domino/icons/ecblank.gif");
		} else {
			node.setImage(urlEncode(entry.getImagesText()) + "?Open&ImgIndex=1");
		}

		return node;
	}
	private ITreeNode createViewNode(OutlineEntry entry) throws NotesException {
		BasicLeafTreeNode node = new BasicLeafTreeNode();
		node.setLabel(entry.getLabel());
		if(entry.getImagesText().isEmpty()) {
			node.setImage("/.ibmxspres/domino/icons/ecblank.gif");
		} else {
			node.setImage("/" + urlEncode(entry.getImagesText()) + "?Open&ImgIndex=1");
		}

		View view = entry.getParent().getParentDatabase().getView(entry.getNamedElement());
		Document viewDoc = view.getParent().getDocumentByUNID(view.getUniversalID());
		String xpageAlt = viewDoc.getItemValueString("$XPageAlt");
		if(!StringUtil.isEmpty(xpageAlt)) {
			node.setHref("/" + xpageAlt);
			node.setSelected(JSFUtil.getViewRoot().getPageName().equals("/" + xpageAlt));
		} else {
			node.setHref("/View.xsp?viewName=" + urlEncode(entry.getNamedElement()));
			node.setSelected(entry.getNamedElement().equals(viewName));
		}

		node.setRendered(resolveHideFormula(entry));

		return node;
	}
	private BasicLeafTreeNode createStubNode() {
		return new BasicLeafTreeNode();
	}

	private String urlEncode(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch(Exception e) { return value; }
	}
	private boolean resolveHideFormula(OutlineEntry entry) throws NotesException {
		String hideFormula = entry.getHideFormula();
		if(hideFormula != null && hideFormula.length() > 0) {
			Session session = ExtLibUtil.getCurrentSession();
			Database database = entry.getParent().getParentDatabase();
			Document contextDoc = database.createDocument();
			// @UserAccess gave me trouble, so I just did a simple string replacement, since I know that's the only way I used it
			hideFormula = hideFormula.replace("@UserAccess(@DbName; [AccessLevel])", "\"" + String.valueOf(database.getCurrentAccessLevel()) + "\"");
			double result = (Double)session.evaluate(hideFormula, contextDoc).get(0);
			contextDoc.recycle();
			return result != 1;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private Set<ViewDescription> getViewDescriptions() throws NotesException {
		if(this.viewDescriptions == null) {
			this.viewDescriptions = new TreeSet<ViewDescription>();
			for(View view : (List<View>)ExtLibUtil.getCurrentDatabase().getViews()) {
				ViewDescription desc = new ViewDescription();
				desc.setName(view.getName());
				desc.setAliases(view.getAliases());
				desc.setFolder(view.isFolder());
				desc.setUniversalID(view.getUniversalID());

				Document viewDoc = view.getParent().getDocumentByUNID(desc.getUniversalID());
				String xpageAlt = viewDoc.getItemValueString("$XPageAlt");
				if(!StringUtil.isEmpty(xpageAlt)) {
					xpageAlts.put(desc.getName(), xpageAlt);
					for(String alias : desc.getAliases()) {
						xpageAlts.put(alias, xpageAlt);
					}
				}
				desc.setXpageAlt(xpageAlt);
				viewDoc.recycle();

				this.viewDescriptions.add(desc);

				view.recycle();
			}
		}
		return this.viewDescriptions;
	}


	public class ViewDescription implements Serializable, Comparable<ViewDescription> {
		private static final long serialVersionUID = 1L;

		private String name;
		private List<String> aliases;
		private String universalID;
		private boolean folder;
		private String xpageAlt;

		public String getXpageAlt() { return xpageAlt; }
		public void setXpageAlt(String xpageAlt) { this.xpageAlt = xpageAlt; }
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }

		public List<String> getAliases() { return aliases; }
		public void setAliases(List<String> aliases) { this.aliases = aliases; }

		public String getUniversalID() { return universalID; }
		public void setUniversalID(String universalID) { this.universalID = universalID; }

		public boolean isFolder() { return folder; }
		public void setFolder(boolean folder) { this.folder = folder; }

		public boolean matches(String viewName) {
			List<String> lowerAliases = new ArrayList<String>(this.getAliases().size());
			for(String alias : this.getAliases()) {
				lowerAliases.add(alias.toLowerCase());
			}
			return this.getName().equalsIgnoreCase(viewName) || lowerAliases.contains(viewName.toLowerCase());
		}

		public boolean isHidden() {
			return this.getName().startsWith("(") && this.getName().endsWith(")");
		}

		public int compareTo(ViewDescription arg0) {
			return this.getName().compareToIgnoreCase(arg0.getName());
		}
	}
}