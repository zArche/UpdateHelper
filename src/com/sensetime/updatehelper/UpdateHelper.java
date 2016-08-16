package com.sensetime.updatehelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.json.JSONException;
import org.json.JSONObject;

import com.sensetime.updatehelper.api.ApiClient;
import com.sensetime.updatehelper.api.ApiClient.Callback;
import com.sensetime.updatehelper.utils.FileUtil;
import com.sensetime.updatehelper.utils.ApplicationUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class UpdateHelper {
	
	private static final boolean DEBUG = true;
	private static final String TAG = "UpdateHelper";
	
	private static final int HANDLER_DOWNLOAD_START = 0x0001;
	private static final int HANDLER_DOWNLOAD_PROGRESS = 0x0002;
	private static final int HANDLER_DOWNLOAD_FINISHED = 0x0003;
	private static final int HANDLER_DOWNLOAD_INTERRUPT = 0x0004;
	
	private Context context;
	private String verName;
	private String title;
	private String msg;
	private String url;
	private int level;
	
	private ProgressDialog dialog;
	private String downloadPath;
	private String filePath;
	
	private UpdateHelperHandler mHandler = new UpdateHelperHandler(this);
	
	public static UpdateHelper newInstance(Context context){
		if(!(context instanceof Activity)){ //因为要强制结束activity
			throw new RuntimeException("Context is not instanceof Activity");
		}
		return new UpdateHelper(context);
	}
	
	private UpdateHelper(Context context){
		this.context = context;
		dialog = new ProgressDialog(context,ProgressDialog.THEME_HOLO_LIGHT);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMax(100);
		dialog.setMessage("正在下载中，请稍后...");
		dialog.setCancelable(false);
		downloadPath = Environment.getExternalStorageDirectory() + "/" + ApplicationUtil.getApplicationName(context) + "/download/";
		mkdir(downloadPath);

	}
	
	private void mkdir(String dirPath){
		File dir = new File(dirPath);
		if (!dir.exists())
			dir.mkdirs();
	}
	
	static class UpdateHelperHandler extends Handler{
		
		final WeakReference<UpdateHelper> outer;
		
	    public UpdateHelperHandler(UpdateHelper outer) {
	    	
	    	this.outer = new WeakReference<UpdateHelper>(outer);
		}
		
		
		@Override
		public void handleMessage(Message msg) {
			UpdateHelper mOuter = this.outer.get();
			if(mOuter==null){
				return;
			}
			
			switch (msg.what) {
			case HANDLER_DOWNLOAD_START:
				
				mOuter.onDownloadStart();
				
				break;
			case HANDLER_DOWNLOAD_PROGRESS:
				//TODO
				mOuter.onDownloadProgress(msg.obj);
				
				break;
			case HANDLER_DOWNLOAD_FINISHED:
				mOuter.onDownloadFinished(msg.obj);
				break;
			case HANDLER_DOWNLOAD_INTERRUPT:
				
				mOuter.onDownloadInterrupt();
				break;

			default:
				break;
			}
			
			super.handleMessage(msg);
		}
	}
	
	private void onDownloadStart(){
		dialog.show();
	}
	
	private void onDownloadProgress(Object progress){
		dialog.setProgress((Integer)progress);
	}
	
	private void onDownloadFinished(Object success){
		dialog.dismiss();
		if(!(Boolean)success){
			mHandler.sendEmptyMessage(HANDLER_DOWNLOAD_INTERRUPT);
			return;
		}
		if(!ApplicationUtil.getAppPackageName(context).equals(ApplicationUtil.getPackageNameOfApk(context, filePath))){
			showApkCheckFailDialog();
			return;
		}
		
		File apk = new File(filePath);
		isInstall(apk);
	}
	
	private void onDownloadInterrupt(){
		if(dialog != null && dialog.isShowing())
			dialog.dismiss();
		showFailDialog();
	}
	
	private void showApkCheckFailDialog(){
		new AlertDialog.Builder(context)
				.setTitle("警告!")
				.setMessage("包名验证失败!请确保网络环境安全后再次尝试!")
				.setPositiveButton("确定", null)
				.show();
	}

	public void checkUpdate(){ //默认自动更新
		checkUpdate(false);
	}
	
	public void checkUpdate(final boolean isAutoCheck) {
		String appVerCode = ApplicationUtil.getVersionCode(context);
		String appVerName = ApplicationUtil.getVersionName(context);

		ApiClient.getVersoinInfo(appVerCode, appVerName,ApplicationUtil.getAppPackageName(context), new Callback() {
			@Override
			public void onPostExecute(Object result) {
				// TODO Auto-generated method stub
				String json = InputStreamTOString((InputStream)result);
				try {
					JSONObject response = new JSONObject(json);
					Log.d("response", response.toString());
					if (response != null) {
						if (response.optInt("errCode") != 200) {
							Toast.makeText(context, response.optString("errMsg"), Toast.LENGTH_SHORT).show();
							return;
						}
						JSONObject data = response.optJSONObject("data");
						if (data.optInt("isUpdate") == 1) { // 需要更新
							verName  = data.optString("verName");
							title = data.optString("title");
							msg = data.optString("msg");
							url  = data.optString("url");
							level = data.optInt("level"); // 1:强制更新 0:非强制更新

							filePath = downloadPath + ApplicationUtil.getApplicationName(context) + "_v" + verName + ".apk";
							
							AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle("温馨提示")
									.setMessage("最新版本号：" + verName + "\n" + "发布时间：" + "2016/8/9" + "\n" + "新版特性：\n" + msg)
									.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											download();
										}
									});
							if (level == 0) {
								builder.setNegativeButton("稍后更新", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
							}else if(level == 1){
								builder.setCancelable(false);
							}
							builder.show();
						}else if(!isAutoCheck){//手动更新
							Toast.makeText(context, "已经是最新版本!", Toast.LENGTH_SHORT).show();
						}

					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Toast.makeText(context, "JSON解析错误", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}

			}
		});
	}

	private void download() {
		ApiClient.downloadFile(url, filePath, new Callback() {
			@Override
			public void onPreExecute() {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(HANDLER_DOWNLOAD_START);
			}
			@Override
			public void onPostExecute(Object result) {
				// TODO Auto-generated method stub
				Message msg = mHandler.obtainMessage(HANDLER_DOWNLOAD_FINISHED, result);
				mHandler.sendMessage(msg);
			}
			
			@Override
			public void onProgressUpdate(int progress) {
				// TODO Auto-generated method stub
				Message msg = mHandler.obtainMessage(HANDLER_DOWNLOAD_PROGRESS, progress);
				mHandler.sendMessage(msg);
			}
		});

	}

	private  void isInstall(File file) {
		if (file != null && file.exists()) {
			showInstallDialog(context,file);
		} else {
			showFailDialog();
		}
	}

	private  void showInstallDialog(final Context context,final File file) {
		new AlertDialog.Builder(context).setTitle("提示").setMessage("下载完成，已保存为" + file.getAbsolutePath() + "，是否安装?")
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						openFile(file);
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	private  void showFailDialog() {
		new AlertDialog.Builder(context).setTitle("提示").setMessage("下载失败，请重新下载")
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						download();
						dialog.dismiss();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	private  void openFile(File file) {
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				FileUtil.deleteFile(filePath);
			}
		}, 30 * 1000);
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	private  String InputStreamTOString(InputStream in) {

		String result = "";
		if (in != null) {
			try {
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				byte[] data = new byte[4096];
				int count = -1;
				while ((count = in.read(data, 0, 4096)) != -1) {
					outStream.write(data, 0, count);
				}
				data = null;
				result = new String(outStream.toByteArray(), "utf-8");
				outStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

}
