package com.mwdev.sxsmcardpay.iso8583;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

public class util {
	static final String HEXES = "0123456789ABCDEF";

	public static String byteArray2Hex(byte[] raw) {
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (final byte b : raw) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(
					HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}

	/**
	 * 16���Ƹ�ʽ���ַ�ת��16����byte 44 --> byte 0x44
	 * 
	 * @param hexString
	 * @return
	 */
	public static byte[] HexStringToByteArray(String hexString) {//
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		if (hexString.length() == 1 || hexString.length() % 2 != 0) {
			hexString = "0" + hexString;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * �����ַ�ת��16��������
	 * 
	 * @param str
	 * @return
	 */
	public static byte[] CNToHex(String str) {
		// String string = "";
		// for (int i = 0; i < str.length(); i++) {
		// String s = String.valueOf(str.charAt(i));
		// byte[] bytes = null;
		// try {
		// bytes = s.getBytes("gbk");
		// } catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// }
		// for (int j = 0; j < bytes.length; j++) {
		// string += Integer.toHexString(bytes[j] & 0xff);
		// }
		// }
		byte[] b = null;
		try {
			b = str.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return b;
	}

	/**
	 * byte ת����16���Ƹ�ʽ�ַ���ʾ
	 * 
	 * @param b
	 * @return
	 */
	public static String getHexString(byte[] b) {
		StringBuffer result = new StringBuffer("");
		for (int i = 0; i < b.length; i++) {
			result.append("0x"
					+ Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1)
					+ ",");
		}
		return result.substring(0, result.length() - 1);
	}

	/**
	 * intת��16���Ƶ�byte
	 * 
	 * @param i
	 * @return
	 */
	public static byte[] IntToHex(int i) {
		String string = null;
		if (i >= 0 && i < 10) {
			string = "0" + i;
		} else {
			string = Integer.toHexString(i);
		}
		return HexStringToByteArray(string);
	}

	/**
	 * ��ָ��byte������16���Ƶ���ʽ��ӡ������̨
	 * 
	 * @param b
	 */
	public static void printHexString(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			System.out.print(hex.toUpperCase());
		}

	}

	/**
	 * ��16�����ֽ�ת����int
	 * 
	 * @param b
	 * @return
	 */
	public static int byteArrayToInt(byte[] b) {
		int result = 0;
		for (int i = 0; i < b.length; i++) {
			result <<= 8;
			result |= (b[i] & 0xff); //
		}
		return result;
	}

	/**
	 * ��������ֽ���
	 * 
	 * @param b
	 * @param startPos
	 * @param Len
	 * @return
	 */
	public static byte XorByteStream(byte[] b, int startPos, int Len) {
		byte bRet = 0x00;
		for (int i = 0; i < Len; i++) {
			bRet ^= b[startPos + i];
		}
		return bRet;
	}

	/**
	 * Gets the subarray from <tt>array</tt> that starts at <tt>offset</tt>.
	 */
	public static byte[] get(byte[] array, int offset) {
		return get(array, offset, array.length - offset);
	}

	/**
	 * Gets the subarray of length <tt>length</tt> from <tt>array</tt> that
	 * starts at <tt>offset</tt>.
	 */
	public static byte[] get(byte[] array, int offset, int length) {
		byte[] result = new byte[length];
		System.arraycopy(array, offset, result, 0, length);
		return result;
	}

	/*
         * 非压缩BCD码转成压缩BCD码(去零)
         */
	public static String Delete0(String data) {
		String result = "";
		if(data!=null){
			if (data.length() % 2 == 0) {
				for (int i = 0; i < data.length(); i++) {
					if (i % 2 == 0) {
						if (!"0".equalsIgnoreCase(data.charAt(i) + "")) {
							return null;
						}

					} else {
						result += data.charAt(i);
					}

				}
			} else {
				return null;
			}
		}else return null;

		return result;

	}

/*
 * 加零
 */
	public static String add0(String data) {
		String ss = "";
		if (data != null) {
			for (int i = 0; i < data.length(); i++) {
				ss += "0" + data.charAt(i);
			}
		}else return null;

		return ss;

	}


}
