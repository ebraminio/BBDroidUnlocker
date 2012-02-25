package ir.irtci.bbdroidunlocker;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BBDroidUnlocker {

	/**
	 * 
	 */
	public Map<String, BigInteger> mepsMap;

	public void loadMepsList(InputStream rawStream) throws Exception {
		mepsMap = new HashMap<String, BigInteger>();
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();

		Document doc = builder.parse(rawStream);

		NodeList meps = doc.getElementsByTagName("mep");
		for (int i = 0; i < meps.getLength(); i++) {
			Node node = meps.item(i);
			String key = node.getAttributes().getNamedItem("name")
					.getTextContent();
			BigInteger value = new BigInteger(node.getTextContent(), 16);

			mepsMap.put(key, value);
		}
	}

	/**
	 * @param imei
	 * @param selectedMep
	 * @return
	 */
	public String generateUnlockCode(String imei, String selectedMep) {
		if (mepsMap != null) {
			try {
				BigInteger biOfItem = mepsMap.get(selectedMep);
				byte[] mep = biOfItem.toByteArray();
				byte[] pass = createPrivatePass(mep);
		
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i <= 4; i++) {
					byte[] sign = mySha1(imei, pass, (byte) i);
		
					sb.append("MEP");
					sb.append(i + 1);
					sb.append(": ");
					sb.append(myHashToCode(sign, selectedMep));
					sb.append("\n");
				}
		
				return sb.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "Some problems on calculation...";
	}
	
	/**
	 * 
	 */
	String[] m8ds = new String[] { "MEP-23361-001", "MEP-30218-002", "MEP-15326-002", "MEP-04626-002", "MEP-27501-003", "MEP-31845-001", "MEP-22793-001", "MEP-04103-001" };
	
	/**
	 * @param mep
	 * @return
	 */
	private boolean meps8Digit(String mep) {
		for (String i : m8ds) {
			if (i.equals(mep))
				return true;
		}
		return false;
	}
	
	/**
	 * @param b
	 * @return
	 */
	public int byteToUnsignedInt(byte b) {
		int i = b;
		if (b < 0) {
			i = 256 + i;
		}
		return i;
	}
	
	/**
	 * @param hash
	 * @param mep
	 * @return
	 */
	private String myHashToCode(byte[] hash, String mep) {
		String buf, st;
		int size;
		
		buf = "";
		
		size = 16;
		if (meps8Digit(mep)) {
			size = 7;
		}
		
		for (int i = 0; i <= size; i++) {
			st = Integer.toString(byteToUnsignedInt(hash[i]));
			buf = buf + st.charAt(st.length() - 1);
		}
		return buf;
	}

	/**
	 * @param a
	 * @param i
	 * @return
	 */
	private int safeByteArrayToInt(byte[] a, int i) {
		if (a.length <= i)
			return 0;
		return a[i];
	}
	
	/**
	 * 
	 */
	byte[] mepKey = new BigInteger("162799C2C899B8C9DEED77A262D2665E", 16)
			.toByteArray();
	
	/**
	 * @param mepFileId
	 * @return
	 */
	private byte[] createPrivatePass(byte[] mepFileId) {
		byte[] result = new byte[16];
		for (int i = 0; i < 16; i++) {
			result[i] = (byte) (safeByteArrayToInt(mepFileId, i) ^ safeByteArrayToInt(mepKey, i));
		}
		return result;
	}

	/**
	 * @param ch
	 * @return
	 */
	private byte strNumber2Byte(String ch) {
		if (Integer.parseInt(ch) > 9) {
			return (byte)0;
		}
		return (byte)(0x30 + Integer.parseInt(ch));
	}
	
	/**
	 * @param imei_i
	 * @param prPass
	 * @param mepNumber
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws DigestException
	 */
	private byte[] mySha1(String imei_i, byte[] prPass, byte mepNumber)
			throws NoSuchAlgorithmException, DigestException {
		int size = 15;
		byte[] kopad = new byte[64];
		byte[] kipad = new byte[64];
		byte[] imei = new byte[16];

		for (int i = 0; i < size; i++) {
			imei[i] = strNumber2Byte(imei_i.charAt(i) + "");
		}
		imei[size] = mepNumber;

		for (int i = 0; i < 64; i++) {
			kopad[i] = (byte) (safeByteArrayToInt(prPass, i) ^ 0x5C);
			kipad[i] = (byte) (safeByteArrayToInt(prPass, i) ^ 0x36);
		}
		
		byte[] digest1;

		MessageDigest digester1 = MessageDigest.getInstance("SHA1");
		digester1.update(kipad);
		digester1.update(imei);
		digest1 = digester1.digest();

		byte[] digest2;

		MessageDigest digester2 = MessageDigest.getInstance("SHA1");
		digester2 = MessageDigest.getInstance("SHA1");
		digester2.update(kopad);
		digester2.update(digest1);
		digest2 = digester2.digest();
		return digest2;
	}
}
