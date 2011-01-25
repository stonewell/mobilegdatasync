package com.angelstone.android.utils;

public class ASTextUtils {
	public static byte[] fromText(String text) {
		final int N = text.length() / 2;

		byte[] sig = new byte[N];

		for (int i = 0; i < N; i++) {

			char c = text.charAt(i * 2);

			byte b = (byte) (

			(c >= 'a' ? (c - 'a' + 10) : (c - '0')) << 4);

			c = text.charAt(i * 2 + 1);

			b |= (byte) (c >= 'a' ? (c - 'a' + 10) : (c - '0'));

			sig[i] = b;

		}

		return sig;
	}

	public static String toText(byte[] buf) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < buf.length; i++) {
			sb.append(Integer.toHexString(buf[i] & 0xFF));
		}

		return sb.toString();
	}
}
