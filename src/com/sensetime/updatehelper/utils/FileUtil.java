package com.sensetime.updatehelper.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtil {

	public static boolean saveFile2SDCard(InputStream in,String filePath) {
		boolean result = true;
		File apk = new File(filePath);
		FileOutputStream fos;
		try {
			if(apk.exists())
				apk.createNewFile();
			fos = new FileOutputStream(apk);
			int len;
			byte[] buf = new byte[10 * 1024];
			while ((len = in.read(buf)) != -1) {
				fos.write(buf, 0, len);
			}
			fos.flush();
			fos.close();
			in.close();
		}catch(Exception e){
			result = false;
			e.printStackTrace();
		}
		return result;
	}
	
	public static void deleteFile(String filePath){
		File file  = new File(filePath);
		if(file.exists() && file.isFile())
			file.delete();
	}
}
