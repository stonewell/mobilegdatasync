package com.angelstone.sync.utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpsConnection;
import javax.microedition.pki.CertificateException;

public class HttpsUtil {

	private static final String DEFAULT_REDIRECT_URL = "???";
	static int lastResponseCode = 0;
	static String lastResponseMsg = null;

	public static int getLastResponseCode() {
		return lastResponseCode;
	}

	public static String getLastResponseMsg() {
		return lastResponseMsg;
	}

	public static byte[] sendRequest(String url, String method,
			String postData, String authorization) {
		return HttpsUtil.sendRequest(url, method, postData, authorization,
				"application/x-www-form-urlencoded");
	}

	public static byte[] sendAtomRequest(String url, String method,
			String postData, String authorization) {
		return HttpsUtil.sendRequest(url, method, postData, authorization,
				"application/atom+xml");
	}

	public static byte[] sendRequest(String url, String method,
			String postData, String authorization, String contentType) {
		HttpsConnection connection = null;
		OutputStream out = null;
		DataInputStream in = null;
		byte[] responseData = null;
		int status = -1;

		try {

			// Open the connection and check for re-directs
			while (true) {
				try {
					connection = (HttpsConnection) Connector.open(url);
					if (connection == null) {
						throw new IllegalStateException(
								"null connection when opening " + url);
					}
				} catch (Exception e) {
					// #ifdef DEBUG_ERR
//@					 System.err.println("sendRequest() err: " + e);
					// #endif
					e.printStackTrace();
					return null;
				}

				try {
					if (HttpsConnection.GET.equals(method)
							|| HttpsConnection.POST.equals(method)) {
						connection.setRequestMethod(method);
					} else {
						connection.setRequestMethod(HttpsConnection.POST);
						connection.setRequestProperty("X-HTTP-Method-Override",
								method);
					}
				} catch (Exception e) {
					// #ifdef DEBUG_ERR
//@					 System.err.println("sendRequest() err: " + e);
					// #endif
					return null;
				}

				if (authorization != null) {
					connection.setRequestProperty("Authorization",
							authorization);
				}
				if (postData != null) {
					byte[] data = null;
					try {
						data = postData.getBytes("UTF-8");
					} catch (UnsupportedEncodingException e) {
						data = postData.getBytes();
					}
					// connection.setRequestMethod(HttpsConnection.POST);
					connection.setRequestProperty("Content-Type", contentType);

					connection.setRequestProperty("Content-Length", Integer
							.toString(data.length));
					out = connection.openOutputStream();
					out.write(data);

					try {
						out.close();
					} catch (IOException e) {
						// #ifdef DEBUG_ERR
//@						 log("failed to close out for " + url + " due to " +
//@						// e);
						// #endif
						if (e instanceof CertificateException) {
							CertificateException ce = (CertificateException) e;
							throw new RuntimeException(
									"SSL certificate not accepted. Reason: "
											+ ce.getReason()
											+ ", certificate: "
											+ ce.getCertificate());
						}
						e.printStackTrace();
					}
					out = null;
				}

				// Get the status code, causing the connection to be made
				// log("Getting response code...");
				status = connection.getResponseCode();
				lastResponseCode = status;
				lastResponseMsg = connection.getResponseMessage();

				if (status == HttpsConnection.HTTP_TEMP_REDIRECT
						|| status == HttpsConnection.HTTP_MOVED_TEMP
						|| status == HttpsConnection.HTTP_MOVED_PERM) {
					// Get the new location and close the connection
					url = connection.getHeaderField("location");
					connection.close();
					if (url == null) {
						// Could not read URL to redirect to, use default
						url = DEFAULT_REDIRECT_URL;
					}
					// #ifdef DEBUG_INFO
//@					 log("Redirecting to " + url);
					// #endif
				} else {
					// #ifdef DEBUG_INFO
//@					 // no redirect
//@					 log("No redirect");
					// #endif
					break;
				}
			}

			int length = (int) connection.getLength();
			if (length > 0) {
				responseData = new byte[length];
				in = new DataInputStream(connection.openInputStream());
				in.readFully(responseData);
			} else {
				// If content length is not given, read in chunks.
				int chunkSize = 512;
				int index = 0;
				int readLength = 0;
				in = new DataInputStream(connection.openInputStream());
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
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("HTTPS connection to " + url
					+ " failed due to " + e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (status != 200) {
			String responseString = new String(responseData);
			lastResponseMsg += "[" + responseString + "]";
		}

		return responseData;
	}
	// #if DEBUG || DEBUG_INFO || DEBUG_WARN || DEBUG_ERR
	 private static void log(String message) {
	 System.out.println(message);
	 }
	// #endif
}
