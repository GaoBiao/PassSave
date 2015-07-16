package com.passsave.comment;

public class Md5Utils {
	public static String getMd5(String strInfo) {
		String strInfoDigest = "";
		try {
			java.security.MessageDigest messageDigest = java.security.MessageDigest
					.getInstance("MD5");
			messageDigest.update(strInfo.getBytes());
			byte[] bInfoDigest = messageDigest.digest();
			strInfoDigest = byteToHex(bInfoDigest);
		} catch (java.security.NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		return strInfoDigest.toLowerCase();
	}

	public static String byteToHex(byte[] bInfoDigest) {
		String strInfoDigest = "";
		String strTemp = "";
		for (int i = 0; i < bInfoDigest.length; i++) {
			strTemp = (Integer.toHexString(bInfoDigest[i] & 0XFF));
			if (strTemp.length() == 1) {
				strInfoDigest = strInfoDigest + "0" + strTemp;
			} else {
				strInfoDigest = strInfoDigest + strTemp;
			}
		}
		return strInfoDigest.toUpperCase();
	}


	public static byte[] hexToByte(String strInfo) {
		String strHexIndex = "0123456789abcdef0123456789ABCDEF";
		int iInfoLength = strInfo.length() / 2;
		byte bData[] = new byte[iInfoLength];
		int j = 0;
		for (int i = 0; i < iInfoLength; i++) {
			char c = strInfo.charAt(j++);
			int n, b;
			n = strHexIndex.indexOf(c);
			b = (n & 0xf) << 4;
			c = strInfo.charAt(j++);
			n = strHexIndex.indexOf(c);
			b += (n & 0xf);
			bData[i] = (byte) b;
		}
		return bData;
	}

}
