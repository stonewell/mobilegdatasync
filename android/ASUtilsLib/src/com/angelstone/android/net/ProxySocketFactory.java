package com.angelstone.android.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.params.HttpParams;

public class ProxySocketFactory implements SocketFactory {
	private Proxy mProxy = null;
	private PlainSocketFactory mPlainSocketFactory = null;
	
	public ProxySocketFactory() {
		mPlainSocketFactory = new PlainSocketFactory();
	}
	
	public ProxySocketFactory(Proxy proxy) {
		this();
		mProxy = proxy;
	}

	@Override
	public Socket connectSocket(Socket sock, String host, int port,
			InetAddress localAddress, int localPort, HttpParams params)
			throws IOException, UnknownHostException, ConnectTimeoutException {
		
		return mPlainSocketFactory.connectSocket(sock, host, port, localAddress, localPort, params);
	}

	@Override
	public Socket createSocket() throws IOException {
		if (mProxy == null)
			return mPlainSocketFactory.createSocket();
		
		return new Socket(mProxy);
	}

	@Override
	public boolean isSecure(Socket sock) throws IllegalArgumentException {
		return mPlainSocketFactory.isSecure(sock);
	}

}
