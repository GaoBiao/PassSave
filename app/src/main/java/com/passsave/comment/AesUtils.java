package com.passsave.comment;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesUtils {
	private String transformation = "AES/CBC/NoPadding";
	private String iv = "vYdKUfTV01TA4t41";

	public String getTransformation() {
		return transformation;
	}

	public void setTransformation(String transformation) {
		this.transformation = transformation;
	}

	public String getIv() {
		return iv;
	}

	public void setIv(String iv) {
		this.iv = iv;
	}

	/**
	 * AES加密算法
	 */
	public AesUtils() {
	}

	public AesUtils(String transformation, String iv) {
		this.transformation = transformation;
		this.iv = iv;
	}

	/**
	 * @param content
	 *            需要加密的内容
	 * @param key
	 *            加密密钥
	 * @return String 加密后的字符串
	 */
	public String encrypttoStr(String content, String key) {
		return parseByte2HexStr(encrypt(content.getBytes(), key));
	}

	/**
	 * 将二进制转换成16进制
	 *
	 * @param buf
	 * @return String
	 */
	public String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * 将16进制转换为二进制
	 *
	 * @param hexStr
	 * @return byte[]
	 */
	public byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}

	public byte[] encrypt(byte[] dataBytes, String key) {
		try {

			Cipher cipher = Cipher.getInstance(transformation);
			int blockSize = cipher.getBlockSize();
			int plaintextLength = dataBytes.length;
			if (plaintextLength % blockSize != 0) {
				plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
			}
			byte[] plaintext = new byte[plaintextLength];
			System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
			SecretKeySpec keySpec = new SecretKeySpec(getKeyBytes(key), "AES");
			IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivspec);// 初始化
			byte[] encrypted = cipher.doFinal(plaintext);
			return encrypted; // 加密
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public byte[] decrypt(byte[] dataBytes, String key) {
		try {
			Cipher cipher = Cipher.getInstance(transformation);
			SecretKeySpec keyspec = new SecretKeySpec(getKeyBytes(key), "AES");
			IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
			byte[] original = cipher.doFinal(dataBytes);
			int len = original.length;
			for (int i = 0; i < original.length; i++) {
				if (original[i] == 0) {
					len = i;
					break;
				}
			}
			byte[] b = new byte[len];
			System.arraycopy(original, 0, b, 0, len);
			return b;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private byte[] getKeyBytes(String key) {
		byte[] keyBytes = key.getBytes();
		byte[] keyBs = new byte[16];
		for (int i = 0; i < keyBytes.length && i < keyBs.length; i++) {
			keyBs[i] = keyBytes[i];
		}
		return keyBs;
	}

	/**
	 * @param content
	 *            待解密内容(字符串)
	 * @param keyWord
	 *            解密密钥
	 * @return byte[]
	 */
	public byte[] decrypt(String content, String keyWord) {
		return decrypt(parseHexStr2Byte(content), keyWord);
	}

	/**
	 * @param content
	 *            待解密内容(字符串)
	 * @param keyWord
	 *            解密密钥
	 * @return String
	 */
	public String decrypttoStr(String content, String keyWord) {
		return new String(decrypt(content, keyWord));
	}

}