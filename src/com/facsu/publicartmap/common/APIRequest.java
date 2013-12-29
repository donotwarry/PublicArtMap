package com.facsu.publicartmap.common;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import com.dennytech.common.service.dataservice.StringInputStream;
import com.dennytech.common.service.dataservice.mapi.CacheType;
import com.dennytech.common.service.dataservice.mapi.MApiRequest;
import com.dennytech.common.service.dataservice.mapi.impl.BasicMApiRequest;

public class APIRequest extends BasicMApiRequest {

	public APIRequest(String url, String method, InputStream input,
			CacheType defaultCacheType, Class<?> resultClazz,
			List<NameValuePair> headers) {
		super(url, method, input, defaultCacheType, resultClazz, headers);
	}

	public static MApiRequest mapiPostJson(String url, Class<?> resultClazz,
			Map<String, ?> forms) {
		JSONObject jo = new JSONObject(forms);
		APIRequest r = new APIRequest(url, POST, new StringInputStream(
				jo.toString()), CacheType.DISABLED, resultClazz, null);
		return r;
	}

}
