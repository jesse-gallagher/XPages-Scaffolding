package org.openntf.xsp.extlib.application;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;

import com.ibm.xsp.registry.FacesDefinition;
import com.ibm.xsp.registry.FacesLibrary;
import com.ibm.xsp.registry.FacesSharableRegistry;
import com.ibm.xsp.registry.config.XspRegistryManager;
import com.ibm.xsp.registry.config.XspRegistryProvider;

public class XspRegistry {
	private final Map<Class<? extends UIComponent>, FacesDefinition> _reverseDefMap;
	private final Map<Class<? extends UIComponent>, String> _reverseTagMap;
	private final Map<String, Class<? extends UIComponent>> _tagMap;

	public XspRegistry() {
		_reverseDefMap = new HashMap<Class<? extends UIComponent>, FacesDefinition>();
		_reverseTagMap = new HashMap<Class<? extends UIComponent>, String>();
		_tagMap = new HashMap<String, Class<? extends UIComponent>>();
		initRegistryMaps();
	}

	@SuppressWarnings("unchecked")
	private void initRegistryMaps() {
		XspRegistryManager xrm = XspRegistryManager.getManager();
		Collection<String> pids = xrm.getRegistryProviderIds();
		for (String id : pids) {
			XspRegistryProvider curProv = xrm.getRegistryProvider(id);
			FacesSharableRegistry curReg = curProv.getRegistry();
			Collection<String> uris = curReg.getLocalNamespaceUris();
			for (String curUri : uris) {
				FacesLibrary fl = curReg.getLocalLibrary(curUri);
				List<FacesDefinition> defsList = fl.getDefs();
				for (FacesDefinition curDef : defsList) {
					_reverseDefMap.put((Class<? extends UIComponent>) curDef.getJavaClass(), curDef);
					if (curDef.isTag()) {
						_reverseTagMap.put((Class<? extends UIComponent>) curDef.getJavaClass(), curDef.getTagName());
						_tagMap.put(curDef.getTagName(), (Class<? extends UIComponent>) curDef.getJavaClass());
					}

				}
			}
		}
	}

	public FacesDefinition getDefForClass(Class<? extends UIComponent> clazz) {
		FacesDefinition result = null;
		if (_reverseDefMap.containsKey(clazz)) {
			return _reverseDefMap.get(clazz);
		} else {
			while (clazz.getSuperclass() != null) {
				result = _reverseDefMap.get(clazz.getSuperclass());
				if (result != null)
					return result;
			}
		}
		return result;
	}

	public FacesDefinition getTagForClass(Class<? extends UIComponent> clazz) {
		FacesDefinition def = getDefForClass(clazz);
		if (def != null) {
			return _reverseDefMap.get(def);
		}
		return null;
	}
}
