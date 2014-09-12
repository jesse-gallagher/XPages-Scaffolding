package controller;

import config.AppConfig;
import config.Translation;
import java.util.*;
import java.io.*;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import frostillicus.xsp.controller.BasicXPageController;
import frostillicus.xsp.util.FrameworkUtils;

public class appconfig extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforePageLoad() throws Exception {
		super.beforePageLoad();

		Map<String, Object> configData = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		AppConfig appConfig = AppConfig.get();
		for(String key : appConfig.keySet()) {
			configData.put(key, appConfig.getValue(key));
		}

		if(!configData.containsKey("aliases") || "".equals(configData.get("aliases"))) {
			configData.put("aliases", new ArrayList<Map<String, Object>>());
		}


		FrameworkUtils.getViewScope().put("appConfigData", configData);
	}

	@Override
	@SuppressWarnings("unchecked")
	public String save() throws IOException {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		Map<String, Object> appConfigData = (Map<String, Object>)viewScope.get("appConfigData");

		AppConfig appConfig = AppConfig.get();
		for(Map.Entry<String, Object> configEntry : appConfigData.entrySet()) {
			appConfig.setValue(configEntry.getKey(), configEntry.getValue());
		}
		appConfig.save();

		FrameworkUtils.flashMessage("confirmation", Translation.get().getValue("configChangeConfirmation"));

		return "xsp-success";
	}

	/* ******************************************************************************
	 * Aliases
	 ********************************************************************************/

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getAliases() {
		Map<String, Object> viewScope = FrameworkUtils.getViewScope();
		Map<String, Object> appConfigData = (Map<String, Object>)viewScope.get("appConfigData");
		return (List<Map<String, Object>>)appConfigData.get("aliases");
	}

	public void addAlias() {
		getAliases().add(new HashMap<String, Object>());
	}

	public void removeAlias() {
		int index = (Integer)FrameworkUtils.resolveVariable("aliasIndex");
		getAliases().remove(index);
	}

	public void moveAliasUp() {
		int index = (Integer)FrameworkUtils.resolveVariable("aliasIndex");
		Collections.swap(getAliases(), index, index-1);
	}

	public void moveAliasDown() {
		int index = (Integer)FrameworkUtils.resolveVariable("aliasIndex");
		Collections.swap(getAliases(), index, index+1);
	}
}
