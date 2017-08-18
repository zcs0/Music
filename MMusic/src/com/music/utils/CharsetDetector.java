package com.music.utils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.chardet.nsDetector;
import com.chardet.nsICharsetDetectionObserver;
import com.chardet.nsPSMDetector;

/**
 * @ClassName: CharsetDetector.java
 * @author zcs
 * @version V1.0
 * @Date 2015年12月11日 上午11:11:32
 * @Description: 编码格式查询
 */
public class CharsetDetector {
	private static boolean found = false;
	private static String result;
	private static int lang;

	public static String[] detectChineseCharset(InputStream in) throws IOException {
		lang = nsPSMDetector.CHINESE;
		String[] prob;
		// Initalize the nsDetector() ;
		nsDetector det = new nsDetector(lang);
		// Set an observer...
		// The Notify() will be called when a matching charset is found.

		det.Init(new nsICharsetDetectionObserver() {

			public void Notify(String charset) {
				found = true;
				result = charset;
			}
		});
		BufferedInputStream imp = new BufferedInputStream(in);
		byte[] buf = new byte[1024];
		int len;
		boolean isAscii = true;
		while ((len = imp.read(buf, 0, buf.length)) != -1) {
			// Check if the stream is only ascii.
			if (isAscii)
				isAscii = det.isAscii(buf, len);
			// DoIt if non-ascii and not done yet.
			if (!isAscii) {
				if (det.DoIt(buf, len, false))
					break;
			}
		}
		imp.close();
		in.close();
		det.DataEnd();
		if (isAscii) {
			found = true;
			prob = new String[] { "ASCII" };
		} else if (found) {
			prob = new String[] { result };
		} else {
			prob = det.getProbableCharsets();
		}
		return prob;
	}

	public static String[] detectAllCharset(InputStream in) throws IOException {
		try {
			lang = nsPSMDetector.ALL;
			return detectChineseCharset(in);
		} catch (IOException e) {
			throw e;
		}
	}
	/**
	 * 获得编码
	 * @param in
	 * @return
	 */
	public static String getEncode(InputStream in){
		String[] detectAllCharset=null;
		try {
			detectAllCharset = detectAllCharset(in);
		} catch (IOException e) {
			System.out.println("编码查询出错");
			e.printStackTrace();
		}
		if(detectAllCharset==null||detectAllCharset.length<=0){
			return "GBK";
		}
		if("Big5".equals(detectAllCharset[0])){
			detectAllCharset[0]="GBK";
		}
		return detectAllCharset[0];
	}
}
