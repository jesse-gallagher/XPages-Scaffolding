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
}
