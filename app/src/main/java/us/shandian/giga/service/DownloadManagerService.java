package us.shandian.giga.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import us.shandian.giga.R;
import us.shandian.giga.get.DownloadManager;
import us.shandian.giga.get.DownloadMission;
import us.shandian.giga.ui.main.MainActivity;
import static us.shandian.giga.BuildConfig.DEBUG;

public class DownloadManagerService extends Service implements DownloadMission.MissionListener
{
	
	private static final String TAG = DownloadManagerService.class.getSimpleName();
	
	private DMBinder mBinder;
	private DownloadManager mManager;
	private Notification mNotification;
	private int mRunningCount = 0;

	@Override
	public void onCreate() {
		super.onCreate();
		
		if (DEBUG) {
			Log.d(TAG, "onCreate");
		}
		
		mBinder = new DMBinder();
		if (mManager == null) {

			if (DEBUG) {
				Log.d(TAG, "mManager == null");
			}

			mManager = new DownloadManager(this, "/storage/sdcard0/GigaGet");
		}
		
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(this, MainActivity.class);
		mNotification = new Notification.Builder(this)
			.setContentIntent(PendingIntent.getActivity(this, 0, i, 0))
			.setContentTitle(getString(R.string.msg_running))
			.setContentText(getString(R.string.msg_running_detail))
			.setLargeIcon(((BitmapDrawable) getDrawable(R.drawable.gigaget)).getBitmap())
			.setSmallIcon(android.R.drawable.stat_sys_download)
			.build();
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (DEBUG) {
			Log.d(TAG, "Starting");
		}
		
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (DEBUG) {
			Log.d(TAG, "Destroying");
		}
		
		for (int i = 0; i < mManager.getCount(); i++) {
			mManager.pauseMission(i);
		}
		
		stopForeground(true);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	

	@Override
	public void onProgressUpdate(long done, long total) {
		// Do nothing
	}

	@Override
	public void onFinish() {
		mRunningCount--;
		updateState();
	}

	@Override
	public void onError(int errCode) {
		mRunningCount--;
		updateState();
	}
	
	private void updateState() {
		if (mRunningCount == 0) {
			stopForeground(true);
		} else {
			startForeground(1000, mNotification);
		}
	}
	
	
	// Wrapper of DownloadManager
	public class DMBinder extends Binder {
		// Do not start missions from outside
		public DownloadManager getDownloadManager() {
			return mManager;
		}
		
		public int startMission(final String url, final String name, final int threads) {
			mRunningCount++;
			updateState();
			return mManager.startMission(url, name, threads);
		}
		
		public void resumeMission(final int id) {
			mRunningCount++;
			updateState();
			mManager.resumeMission(id);
		}
		
		public void pauseMission(final int id) {
			mRunningCount--;
			updateState();
			mManager.pauseMission(id);
		}
		
	}

}
