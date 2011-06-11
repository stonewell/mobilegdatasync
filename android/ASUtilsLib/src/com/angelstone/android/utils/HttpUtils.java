package com.angelstone.android.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
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
			String uploadData, Proxy proxy, int timeout) throws IOException {
		HttpResponse resp;

		final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(paramName, uploadData));
		HttpEntity entity = null;
		entity = new UrlEncodedFormEntity(params, "UTF-8");

		HttpPost post = new HttpPost(url);
		post.addHeader(entity.getContentType());
		post.setEntity(entity);
		HttpClient mHttpClient = null;

		mHttpClient = createHttpClient(proxy, timeout, url);
		resp = mHttpClient.execute(post);
		if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			return getResponse(resp);
		} else {
			return String.valueOf(resp.getStatusLine().getStatusCode()) + ":"
					+ getResponse(resp);
		}
	}

	public static String postData(String postUrl, 
			byte[] uploadData, Object proxy, int timeout)
			throws IOException {
		HttpResponse resp;

		HttpEntity entity = new ByteArrayEntity(uploadData);

		HttpPost post = new HttpPost(postUrl);
		post.addHeader(entity.getContentType());
		post.setEntity(entity);
		HttpClient mHttpClient = null;

		mHttpClient = createHttpClient(proxy, timeout, postUrl);
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
	private static HttpClient createHttpClient(Object proxy, int timeout, String url)
			throws UnknownHostException, MalformedURLException {
		SchemeRegistry registry = registerCustomizedSocketFactory(proxy, url);

		HttpClient mHttpClient = new DefaultHttpClient();
		final HttpParams params = mHttpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, timeout > 0 ? timeout * 1000 : REGISTRATION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, timeout > 0 ? timeout * 1000 : REGISTRATION_TIMEOUT);
		ConnManagerParams.setTimeout(params, timeout > 0 ? timeout * 1000 : REGISTRATION_TIMEOUT);

		mHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(
				params, registry), params);

		return mHttpClient;
	}

	private static SchemeRegistry registerCustomizedSocketFactory(Object proxy, String url)
			throws UnknownHostException, MalformedURLException {

		int port = new URL(url).getPort();
		
		Scheme http = new Scheme("http", new ProxySocketFactory(proxy), port >= 0 ? port : 80);

		SchemeRegistry sr = new SchemeRegistry();
		sr.register(http);
		return sr;
	}
}
