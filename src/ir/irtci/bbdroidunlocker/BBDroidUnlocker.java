/*
 * Logic of BBDroidUnlocker that can be used 
 * in not android applications also.
 * 
 * Ported by ebrahim@byagowi.com from a Delphi project.
 * 
 * Some needed data of this code moved to an external xml (see #loadMepsList)
 * for reducing LOC and preventing code duplication.
 */
package ir.irtci.bbdroidunlocker;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BBDroidUnlocker {

	private Map<String, BigInteger> mepsMap;

	/**
	 * Loading meps details from given xml (through xmlStream parameter).
	 * This method most called after creating BBDroidUnlocker object.
	 * 
	 * @param xmlStream, needed data xml InputStream 
	 * @throws Exception, document parsing exceptions
	 */
	public void loadMepsList(InputStream xmlStream) throws Exception {
		
		mepsMap = new HashMap<String, BigInteger>();
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();

		Document document = builder.parse(xmlStream);

		NodeList meps = document.getElementsByTagName("mep");
		for (int i = 0; i < meps.getLength(); i++) {
			Node node = meps.item(i);
			String key = node.getAttributes().getNamedItem("name")
					.getTextContent();
			BigInteger value = new BigInteger(node.getTextContent(), 16);

			mepsMap.put(key, value);
		}
	}
	
	/**
	 * @return a sorted list supported meps.
	 */
	public List<String> getSortedMepsList() {
		
		ArrayList<String> sortedMepsList = new ArrayList<String>(mepsMap.keySet());
		sortedMepsList.add("");
		Collections.sort(sortedMepsList);
		
		return sortedMepsList;
	}

	/**
	 * Generating unlock from given parameters.
	 * 
	 * @param imei
	 * @param selectedMep
	 * @return UnlockCode
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
	
	private String[] m8ds = new String[] { "MEP-23361-001", "MEP-30218-002", "MEP-15326-002", "MEP-04626-002", "MEP-27501-003", "MEP-31845-001", "MEP-22793-001", "MEP-04103-001" };
	
	/**
	 * Detect that unlock code must be in 8 digit or not.
	 * 
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
	private int byteToUnsignedInt(byte b) {
		
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
		
		String buf = "", st;
		int size = 16;
		
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
	 * Safely converting a byte array to int.
	 * 
	 * @param byteArray
	 * @param index
	 * @return
	 */
	private int safeByteArrayToInt(byte[] byteArray, int index) {
		
		if (byteArray.length <= index)
			return 0;
		
		return byteArray[index];
	}
	
	/**
	 * A special key, I don't know what means really :D
	 */
	private byte[] mepKey = new BigInteger("162799C2C899B8C9DEED77A262D2665E", 16)
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
		
		byte[] digest;
		MessageDigest digester = MessageDigest.getInstance("SHA1");
		
		digester.update(kipad);
		digester.update(imei);
		digest = digester.digest();
		
		digester.reset();
		digester.update(kopad);
		digester.update(digest);
		return digester.digest();
	}
}
