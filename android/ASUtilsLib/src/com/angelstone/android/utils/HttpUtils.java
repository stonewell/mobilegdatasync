package com.angelstone.android.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;

public class HttpUtils {
	public static final int REGISTRATION_TIMEOUT = 60 * 1000; // ms

	public static String postData(Context context, String url, String paramName,
			String uploadData) throws IOException {
		HttpResponse resp;

		final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(paramName, uploadData));
		HttpEntity entity = null;
		entity = new UrlEncodedFormEntity(params,"UTF-8");

		HttpPost post = new HttpPost(url);
		post.addHeader(entity.getContentType());
		post.setEntity(entity);
		HttpClient mHttpClient = null;

		mHttpClient = createHttpClient();
		resp = mHttpClient.execute(post);
		if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			return getResponse(resp);
		} else {
			return String.valueOf(resp.getStatusLine().getStatusCode()) + ":"
					+ getResponse(resp);
		}
	}

	public static String postData(Context context, String url, String paramName,
			byte[] uploadData) throws IOException {
		HttpResponse resp;

		//final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		//params.add(new BasicNameValuePair(paramName, HtmuploadData));
		HttpEntity entity = null;
		//entity = new UrlEncodedFormEntity(params,"UTF-8");
		entity = new ByteArrayEntity(uploadData);

		HttpPost post = new HttpPost(url);
		post.addHeader(entity.getContentType());
		post.setEntity(entity);
		HttpClient mHttpClient = null;

		mHttpClient = createHttpClient();
		resp = mHttpClient.execute(post);
		if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			return getResponse(resp);
		} else {
			return String.valueOf(resp.getStatusLine().getStatusCode()) + ":"
					+ getResponse(resp);
		}
	}

	private static String getResponse(HttpResponse response) throws IOException {
		String result = "";
		InputStream in = response.getEntity().getContent();
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
	 */
	private static HttpClient createHttpClient() {
		HttpClient mHttpClient = new DefaultHttpClient();
		final HttpParams params = mHttpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, REGISTRATION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, REGISTRATION_TIMEOUT);
		ConnManagerParams.setTimeout(params, REGISTRATION_TIMEOUT);

		return mHttpClient;
	}
}
