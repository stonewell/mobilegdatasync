package com.angelstone.sync.utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class HttpsUtil {

	public static final int UPLOADING_TIMEOUT = 30 * 1000; // ms
	private static final String DEFAULT_REDIRECT_URL = "???";
	static int lastResponseCode = 0;
	static String lastResponseMsg = null;

	public static int getLastResponseCode() {
		return lastResponseCode;
	}

	public static String getLastResponseMsg() {
		return lastResponseMsg;
	}

	public static byte[] sendRequest(String url, String postData,
			String authorization) {
		return HttpsUtil.sendRequest(url, postData, authorization,
				"application/x-www-form-urlencoded");
	}

	public static byte[] sendAtomRequest(String url, String postData,
			String authorization) {
		return HttpsUtil.sendRequest(url, postData, authorization,
				"application/atom+xml");
	}

	public static byte[] sendRequest(String url, String postData,
			String authorization, String contentType) {
		byte[] responseData = null;
		int status = -1;
		HttpResponse resp = null;
		HttpClient httpClient = null;
		DataInputStream in = null;

		try {
			httpClient = CreateHttpClient();
			byte[] buf = getDataBytes(postData);

			HttpEntity entity = null;
			if (buf != null) {
				entity = new ByteArrayEntity(buf);
				((ByteArrayEntity) entity).setContentType(contentType);
			}

			// Open the connection and check for re-directs
			while (true) {
				HttpPost request = new HttpPost(url);

				if (entity != null) {
					request.setEntity(entity);
					request.addHeader(entity.getContentType());
				}

				if (authorization != null) {
					request.addHeader("Authorization", authorization);
				}

				resp = httpClient.execute(request);

				lastResponseCode = resp.getStatusLine().getStatusCode();
				lastResponseMsg = resp.getStatusLine().getReasonPhrase();
				status = resp.getStatusLine().getStatusCode();

				if (status == HttpStatus.SC_OK) {
					break;
				} else if (status == HttpStatus.SC_MOVED_PERMANENTLY
						|| status == HttpStatus.SC_MOVED_TEMPORARILY
						|| status == HttpStatus.SC_TEMPORARY_REDIRECT) {
					if (resp.getFirstHeader("location") == null)
						url = DEFAULT_REDIRECT_URL;
					else
						url = resp.getFirstHeader("location").getValue();
				}
			}

			HttpEntity respEntity = resp.getEntity();

			try {
				int length = (int) respEntity.getContentLength();
				if (length > 0) {
					responseData = new byte[length];
					in = new DataInputStream(respEntity.getContent());
					in.readFully(responseData);
				} else {
					// If content length is not given, read in chunks.
					int chunkSize = 512;
					int index = 0;
					int readLength = 0;
					in = new DataInputStream(respEntity.getContent());
					responseData = new byte[chunkSize];
					do {
						if (responseData.length < index + chunkSize) {
							byte[] newData = new byte[index + chunkSize];
							System.arraycopy(responseData, 0, newData, 0,
									responseData.length);
							responseData = newData;
						}
						readLength = in.read(responseData, index, chunkSize);
						index += readLength;
					} while (readLength > 0);
					length = index;
				}
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Throwable t) {

					}
				}

				if (respEntity != null) {
					try {
						respEntity.consumeContent();
					} catch (Throwable t) {

					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("HTTPS connection to " + url
					+ " failed due to " + e);
		}

		if (status != HttpStatus.SC_OK) {
			String responseString = new String(responseData);
			lastResponseMsg += "[" + responseString + "]";
		}

		return responseData;
	}

	private static byte[] getDataBytes(String data) throws IOException {
		if (data == null)
			return null;
		return data.getBytes("UTF-8");
	}

	private static HttpClient CreateHttpClient() throws MalformedURLException {
		HttpClient c = null;
		c = new DefaultHttpClient();

		HttpParams params = c.getParams();
		HttpConnectionParams.setConnectionTimeout(params, UPLOADING_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, UPLOADING_TIMEOUT);
		ConnManagerParams.setTimeout(params, UPLOADING_TIMEOUT);

		return c;
	}
}
