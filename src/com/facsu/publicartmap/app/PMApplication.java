package com.facsu.publicartmap.app;

import com.baidu.mapapi.BMapManager;
import com.dennytech.common.app.CLApplication;

public class PMApplication extends CLApplication {
	
	private static PMApplication instance;
	
	public PMApplication() {
		instance = this;
	}

	public static PMApplication instance() {
		if (instance == null) {
			throw new IllegalStateException("Application has not been created");
		}

		return instance;
	}
	
	private BMapManager mapManager = null;
	
	public BMapManager mapManager() {
		if (mapManager == null) {
			mapManager = new BMapManager(this);
			mapManager.init("sRIp5TPXhwtPyI0BwaVpA9U0", null);
			mapManager.start();
		}
		return mapManager;
	}
	
	@Override
	public void onApplicationStop() {
		if (mapManager != null) {
			mapManager.destroy();
		}
		super.onApplicationStop();
	}

}
