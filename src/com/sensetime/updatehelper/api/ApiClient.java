package com.sensetime.updatehelper.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;

public class ApiClient {
	private static final boolean DEBUG = true;
	private static HttpURLConnection conn;
	private static int timeOut = 10;
	private static final String HOST = "192.168.2.128";

	public static final String HTTP = "http://";
	public static final String SEPARATOR = "/";
	public static String BASE_URL = HTTP + HOST + SEPARATOR;
	public static final String GET_VERSION_INFO = BASE_URL + "st/get_update.php";

	// Http Get请求
	private static InputStream requestGet(String url, Map<String, String> params) {

		StringBuffer sb = new StringBuffer(url);
		if (params != null) {
			sb.append("?");
			for (Map.Entry<String, String> entry : params.entrySet()) {
				sb.append(entry.getKey()).append("=").append(entry.getValue());
				sb.append("&");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		if (DEBUG) {
			Log.i("url", sb.toString());
		}
		InputStream is = null;
		try {
			URL httpUrl = new URL(sb.toString());
			conn = (HttpURLConnection) httpUrl.openConnection();
			conn.setConnectTimeout(timeOut * 1000);

			if (conn.getResponseCode() == 200) {
				is = conn.getInputStream();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}
		return is;
	}

	// 异步Get
	private static void requestGetWithCallback(final String url, final Map<String, String> params, final Callback cb) {
		new AsyncTask<Void, Integer, InputStream>() {
			@Override
			protected InputStream doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				return requestGet(url, params);
			}

			@Override
			protected void onPostExecute(InputStream is) {
				if (cb != null)
					cb.onPostExecute(is);
			};

			@Override
			protected void onProgressUpdate(Integer... values) {
				if (cb != null)
					cb.onProgressUpdate(values[0]);
			}

			protected void onPreExecute() {
				if (cb != null)
					cb.onPreExecute();
			};

		}.execute();
	}

	public static void downloadFile(final String url, final String filePath, final Callback cb) {
		new AsyncTask<Void, Integer, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				boolean result = true;
				InputStream is = null;
				try {
					URL httpUrl = new URL(url);
					conn = (HttpURLConnection) httpUrl.openConnection();
					conn.setConnectTimeout(timeOut * 1000);
					if (conn.getResponseCode() == 200) {
						is = conn.getInputStream();
						int fileSize = conn.getContentLength();
						File out = new File(filePath);
						FileOutputStream fos;
						try {
							if (out.exists())
								out.createNewFile();
							fos = new FileOutputStream(out);
							int len = 0;
							float progress = 0;
							byte[] buf = new byte[10 * 1024];
							while ((len = is.read(buf)) != -1) {
								fos.write(buf, 0, len);
								progress += len;
//								Log.d("progress", "fileSize: " + fileSize + ",current: " + progress );
								cb.onProgressUpdate((int)(progress/fileSize * 100));
							}
							fos.flush();
							fos.close();
							is.close();
						} catch (Exception e) {
							result = false;
							e.printStackTrace();
						}
					} else
						result = false;

				} catch (Exception e) {
					e.printStackTrace();
					result = false;
				} finally {
					if (conn != null) {
						conn.disconnect();
						conn = null;
					}
				}
				return result;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (cb != null)
					cb.onPostExecute(result);
			};

			protected void onPreExecute() {
				if (cb != null)
					cb.onPreExecute();
			};

		}.execute();
	}

	public static void getVersoinInfo(String verCode, String verName,String packageName, Callback cb) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("verCode", verCode);
		params.put("verName", verName);
		params.put("packageName", packageName);
		requestGetWithCallback(GET_VERSION_INFO, params, cb);
	}

	public static abstract class Callback {
		public void onPreExecute() {
		}

		public void onProgressUpdate(int progress) {
		}

		public abstract void onPostExecute(Object result);
	}

}
