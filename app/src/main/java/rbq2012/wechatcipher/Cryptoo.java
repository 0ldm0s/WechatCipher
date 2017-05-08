package rbq2012.wechatcipher;

//import android.util.Base64;
import android.util.Base64;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static rbq2012.wechatcipher.Constants.*;
//import org.apache.commons.codec.binary.Hex;
//import org.apache.commons.codec.binary.Base64;

public class Cryptoo
{
	static public String encrypt(String text,CryptoRule rule){
		String code=rule.getExtra();
		String prefix=rule.getTxtPrefix();
		int method=rule.getMethod();
		int postp=rule.getPostp();
		text=prefix+text;
		byte[] data;String result;
		switch(method){
		case 0:
			data=encryptAES128(code,text);
			break;
		default:
			return null;
		}
		switch(postp){
		case 0:
			result=bytesToHexString(data);
			break;
		case 1:
			result=Base64.encodeToString(data,0);
			break;
		case 2:
			result=Base64.encodeToString(data,0);
			break;
		default:
			return null;
		}
		//if(0==0)return "";
		return result;
	}
	
	static public String decrypt(String text,CryptoRule rule){
		String code=rule.getExtra();
		String prefix=rule.getTxtPrefix();
		int method=rule.getMethod();
		int postp=rule.getPostp();
		try{
		byte[] mid;String result;
		switch(postp){
				case 0:
				mid=hexStringToBytes(text);
				break;
				case 1:
				mid=Base64.decode(text,0);
				break;
				case 2:
				mid=Base64.decode(text,0);
				break;
				default:
				return null;
		}
		switch(method){
		case 0:
			result=decryptAES128(code,mid);
			break;
		default:
			return null;
		}
		if(!result.startsWith(prefix)) return null;
		return result.substring(prefix.length());
		}catch(Exception e){
			return null;
		}
	}
	
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
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
	//byte——>String
	public static String bytesToHexString(byte[] src){
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i ++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}
	static private byte[] endecryptAES128(String skey, byte[] data, int mode) throws Exception {
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        sr.setSeed(skey.getBytes());
        keygen.init(128, sr);
        SecretKey kkey = keygen.generateKey();
        byte[] bkey = kkey.getEncoded();
        SecretKeySpec fkey = new SecretKeySpec(bkey, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, fkey);
        return cipher.doFinal(data);
    }

    static private byte[] encryptAES128(String skey, String data) {
        try {
            byte[] result = endecryptAES128(skey, data.getBytes(), Cipher.ENCRYPT_MODE);
            //return Base64.encodeToString(result, 0);
			return result;
        } catch (Exception e) {
            return null;
        }
    }

    static private String decryptAES128(String skey, byte[] data) {
        try {
            //byte[] datta = Base64.decode(data, 0);
            byte[] result = endecryptAES128(skey, data, Cipher.DECRYPT_MODE);
            return new String(result);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String s = sw.toString();
            pw.close();
            return s;
        }
    }
	
	/*static private String enHex(byte[] arr){
		Hex.
		StringBuilder sb=new StringBuilder();
		for(byte b:arr){
			int i;
			if(b>=0) i=b;
			else i=256-b;
			sb.append((char)(i/16+97)).append((char)(i%16+97));
		}
		return sb.toString();
	}*/
}
