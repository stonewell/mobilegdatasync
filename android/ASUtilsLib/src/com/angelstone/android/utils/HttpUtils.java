package com.angelstone.android.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.content.SharedPreferences;

import com.angelstone.android.R;
import com.angelstone.android.net.ProxySocketFactory;

public class HttpUtils {
	public static final int REGISTRATION_TIMEOUT = 60 * 1000; // ms

	public static String postData(Context context, String url,
			String paramName, String uploadData) throws IOException {
		HttpResponse resp;

		final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(paramName, uploadData));
		HttpEntity entity = null;
		entity = new UrlEncodedFormEntity(params, "UTF-8");

		HttpPost post = new HttpPost(url);
		post.addHeader(entity.getContentType());
		post.setEntity(entity);
		HttpClient mHttpClient = null;

		mHttpClient = createHttpClient(context);
		resp = mHttpClient.execute(post);
		if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			return getResponse(resp);
		} else {
			return String.valueOf(resp.getStatusLine().getStatusCode()) + ":"
					+ getResponse(resp);
		}
	}

	public static String postData(Context context, String postUrl,
			String paramName, byte[] uploadData) throws IOException {
		HttpResponse resp;

		HttpEntity entity = new ByteArrayEntity(uploadData);

		HttpPost post = new HttpPost(postUrl);
		post.addHeader(entity.getContentType());
		post.setEntity(entity);
		HttpClient mHttpClient = null;

		mHttpClient = createHttpClient(context);
		resp = mHttpClient.execute(post);
		if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			return getResponse(resp);
		} else {
			return String.valueOf(resp.getStatusLine().getStatusCode()) + ":"
					+ getResponse(resp);
		}
	}

	private static String getResponse(HttpResponse response) throws IOException {
		InputStream in = response.getEntity().getContent();
		return readResponseString(in);
	}

	private static String readResponseString(InputStream in) throws IOException {
		String result = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder str = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (str.length() > 0)
				str.append("\n");
			str.append(line);
		}
		in.close();
		result = str.toString();
		return result;
	}

	/**
	 * Configures the httpClient to connect to the URL provided.
	 * 
	 * @throws UnknownHostException
	 */
	private static HttpClient createHttpClient(Context context)
			throws UnknownHostException {
		SchemeRegistry registry = registerCustomizedSocketFactory(context);

		HttpClient mHttpClient = new DefaultHttpClient();
		final HttpParams params = mHttpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, REGISTRATION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, REGISTRATION_TIMEOUT);
		ConnManagerParams.setTimeout(params, REGISTRATION_TIMEOUT);

		mHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(
				params, registry), params);

		return mHttpClient;
	}

	private static SchemeRegistry registerCustomizedSocketFactory(
			Context context) throws UnknownHostException {
		Proxy proxy = createProxyFromPreference(context);

		Scheme http = new Scheme("http", new ProxySocketFactory(proxy), 80);

		SchemeRegistry sr = new SchemeRegistry();
		sr.register(http);
		return sr;
	}

	private static Proxy createProxyFromPreference(Context context)
			throws UnknownHostException {
		SharedPreferences perf = context.getSharedPreferences(
				context.getPackageName() + "_preferences", 0);

		if (!perf.getBoolean("proxy_enable", false))
			return null;

		String[] proxy_types = context.getResources().getStringArray(
				R.array.proxytypes);

		String proxy_type = perf.getString("proxy_type", proxy_types[0]);
		String proxy_host = perf.getString("proxy_host", null);
		String proxy_port = perf.getString("proxy_port", null);

		if (proxy_host == null || proxy_port == null)
			return null;

		try {
			return new Proxy(Proxy.Type.valueOf(proxy_type),
					new InetSocketAddress(proxy_host,
							Integer.parseInt(proxy_port)));
		} catch (Throwable t) {
			ActivityLog.logError(context, context.getString(R.string.app_name),
					t.getMessage());

			return null;
		}
	}
}
