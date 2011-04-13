package com.angelstone.android.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import com.angelstone.android.net.ProxySocketFactory;

public class HttpUtils {
	public static final int REGISTRATION_TIMEOUT = 60 * 1000; // ms

	public static String postData(String url, String paramName,
			String uploadData, Proxy proxy) throws IOException {
		HttpResponse resp;

		final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(paramName, uploadData));
		HttpEntity entity = null;
		entity = new UrlEncodedFormEntity(params, "UTF-8");

		HttpPost post = new HttpPost(url);
		post.addHeader(entity.getContentType());
		post.setEntity(entity);
		HttpClient mHttpClient = null;

		mHttpClient = createHttpClient(proxy);
		resp = mHttpClient.execute(post);
		if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			return getResponse(resp);
		} else {
			return String.valueOf(resp.getStatusLine().getStatusCode()) + ":"
					+ getResponse(resp);
		}
	}

	public static String postData(String postUrl, byte[] uploadData, Proxy proxy)
			throws IOException {
		HttpResponse resp;

		HttpEntity entity = new ByteArrayEntity(uploadData);

		HttpPost post = new HttpPost(postUrl);
		post.addHeader(entity.getContentType());
		post.setEntity(entity);
		HttpClient mHttpClient = null;

		mHttpClient = createHttpClient(proxy);
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
	private static HttpClient createHttpClient(Proxy proxy)
			throws UnknownHostException {
		SchemeRegistry registry = registerCustomizedSocketFactory(proxy);

		HttpClient mHttpClient = new DefaultHttpClient();
		final HttpParams params = mHttpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, REGISTRATION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, REGISTRATION_TIMEOUT);
		ConnManagerParams.setTimeout(params, REGISTRATION_TIMEOUT);

		mHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(
				params, registry), params);

		return mHttpClient;
	}

	private static SchemeRegistry registerCustomizedSocketFactory(Proxy proxy)
			throws UnknownHostException {

		Scheme http = new Scheme("http", new ProxySocketFactory(proxy), 80);

		SchemeRegistry sr = new SchemeRegistry();
		sr.register(http);
		return sr;
	}
}
