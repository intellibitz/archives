/**
 * 
 */
package com.uc.dca.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.uc.dca.util.HttpHandler;
import com.uc.dca.content.IncidentReport;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

/**
 * @author muthu
 * 
 */
public class IRService extends Service {

	static private final String TAG = "IRService";
	private final IBinder localBinder = new LocalBinder();
	private PhoneStateListener phoneStateListener;
	private TelephonyManager telephonyManager;
	private LocationManager locationManager;
	private WifiManager wifiManager;
	
	private String batteryInfo;

	private BroadcastReceiver batteryChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int level = intent.getIntExtra("level", 0);
			batteryInfo = "Battery Level = "+String.valueOf(level) + "%";
			Log.i("BatteryStatus: ", batteryInfo);
            int health = intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN);
            switch (health){
            case BatteryManager.BATTERY_HEALTH_GOOD:
            	batteryInfo = batteryInfo.concat(" Health = Good");
            	break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
            	batteryInfo = batteryInfo.concat(" Health = Dead");
            	break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
            	batteryInfo = batteryInfo.concat(" Health = OverVoltage");
            	break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
            	batteryInfo = batteryInfo.concat(" Health = Overheat");
            	break;
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
            	batteryInfo = batteryInfo.concat(" Health = Unknown");
            	break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
            	batteryInfo = batteryInfo.concat(" Health = Unspecified Failure");
            	break;
            }
		}
	};

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public IRService getService() {
			return IRService.this;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return localBinder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		startIncidentReports(startId);
	}

	
	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		this.registerReceiver(batteryChangedReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.unregisterReceiver(batteryChangedReceiver);
	}

	/**
	 * 
	 */
	private void initManagers() {
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
	}

	/**
	 * @param startId
	 */
	private void startIncidentReports(int startId) {
		Log.i(TAG, "Service started - " + startId);
		if (null == phoneStateListener) {
			initManagers();
			registerPhoneStateListener();
		}
	}

	private void registerPhoneStateListener() {
		Log.i(TAG, "PhoneStateListener registered with TelephonyManager ");

		phoneStateListener = new PhoneStateListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.telephony.PhoneStateListener#onCallStateChanged(int,
			 * java.lang.String)
			 */
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				// TODO Auto-generated method stub
				super.onCallStateChanged(state, incomingNumber);
				Log
						.i("onCallStateChanged: ",
								" ==> =================================================================== <==");
				switch (state) {
				case TelephonyManager.CALL_STATE_IDLE:
					Log.i("onCallStateChanged: ", "==> No Call Activity");
					storeCallStateChange("IDLE - No Call activity.");
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					Log.i("onCallStateChanged: ", "==> Call in Progress");
					storeCallStateChange("OFF-HOOK - At least one call exists that is dialing, active, or on hold, and no calls are ringing or waiting.");
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					Log
							.i("onCallStateChanged: ",
									"==> Incoming Call - Ringing");
					storeCallStateChange("RINGING - A new call arrived and is ringing or waiting. In the latter case, another call is already active.");
					break;
				}
				Log
						.i("onCallStateChanged: ",
								" ==> =================================================================== <==");
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.telephony.PhoneStateListener#onCellLocationChanged
			 * (android.telephony.CellLocation)
			 */
			@Override
			public void onCellLocationChanged(CellLocation location) {
				// TODO Auto-generated method stub
				super.onCellLocationChanged(location);
				GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
				Log.i("onCellLocationChanged: ", gsmCellLocation.getCid()
						+ " <==> " + gsmCellLocation.getLac());
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * android.telephony.PhoneStateListener#onDataConnectionStateChanged
			 * (int)
			 */
			@Override
			public void onDataConnectionStateChanged(int state) {
				// TODO Auto-generated method stub
				super.onDataConnectionStateChanged(state);
				Log
						.i("onDataConnectionStateChanged: ",
								" ==> =================================================================== <==");
				switch (state) {
				case TelephonyManager.DATA_DISCONNECTED:
					Log.i("onDataConnectionStateChanged: ",
							"==> Data is DISCONNECTED");
					storeDataConnectionStateChange("DISCONNECTED");
					break;
				case TelephonyManager.DATA_CONNECTED:
					Log.i("onDataConnectionStateChanged: ",
							"==> Data is CONNECTED");
					storeDataConnectionStateChange("CONNECTED");
					break;
				case TelephonyManager.DATA_CONNECTING:
					Log.i("onDataConnectionStateChanged: ",
							"==> Data is CONNECTING");
					storeDataConnectionStateChange("CONNECTING");
					break;
				case TelephonyManager.DATA_SUSPENDED:
					Log.i("onDataConnectionStateChanged: ",
							"==> Data is SUSPENDED");
					storeDataConnectionStateChange("SUSPENDED");
					break;
				}
//				collectTelephonyManagerStats();
				Log
						.i("onDataConnectionStateChanged: ",
								" ==> =================================================================== <==");
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.telephony.PhoneStateListener#onServiceStateChanged
			 * (android.telephony.ServiceState)
			 */
			@Override
			public void onServiceStateChanged(ServiceState serviceState) {
				// TODO Auto-generated method stub
				super.onServiceStateChanged(serviceState);
				Log
						.i("onServiceStateChanged: ",
								" ==> =================================================================== <==");
				storeServiceStateChange(serviceState);
				Log
						.i("onServiceStateChanged: ",
								" ==> =================================================================== <==");
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.telephony.PhoneStateListener#onSignalStrengthChanged
			 * (int)
			 */
			@Override
			public void onSignalStrengthChanged(int asu) {
				// TODO Auto-generated method stub
				super.onSignalStrengthChanged(asu);
				storeSignalStrengthChange(asu);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @seeandroid.telephony.PhoneStateListener#
			 * onCallForwardingIndicatorChanged(boolean)
			 */
			@Override
			public void onCallForwardingIndicatorChanged(boolean cfi) {
				// TODO Auto-generated method stub
				super.onCallForwardingIndicatorChanged(cfi);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.telephony.PhoneStateListener#onDataActivity(int)
			 */
			@Override
			public void onDataActivity(int direction) {
				// TODO Auto-generated method stub
				super.onDataActivity(direction);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @seeandroid.telephony.PhoneStateListener#
			 * onMessageWaitingIndicatorChanged(boolean)
			 */
			@Override
			public void onMessageWaitingIndicatorChanged(boolean mwi) {
				// TODO Auto-generated method stub
				super.onMessageWaitingIndicatorChanged(mwi);
			}

		};
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE
						| PhoneStateListener.LISTEN_CELL_LOCATION
						| PhoneStateListener.LISTEN_SERVICE_STATE
						| PhoneStateListener.LISTEN_SIGNAL_STRENGTH
						| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
						| PhoneStateListener.LISTEN_DATA_ACTIVITY);
	}

	private String collectLocationStats() {
		String currentKnownLocation = null;
		Location lm = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (null == lm) {
			Log.i("Location: ", " NO LOCATION provided by GPS");
			lm = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		if (null == lm) {
			Log.i("Location: ", " NO LOCATION provided by Network");
		} else {
			currentKnownLocation = lm.toString();
			Log.i("Location: ", currentKnownLocation);
		}
		return currentKnownLocation;
	}

	private String collectWiFiStats() {
		String info = "WiFi: NOT Connected";
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		wifiInfo.describeContents();
		int id = wifiInfo.getNetworkId();
		Log.i("WiFiInfo: ", wifiInfo.toString());
		if (-1 == id) {
			Log.i("WiFi: ", "NOT Connected");
		} else {
			Log.i("WiFi: ", "Network id = " + id);
			info = wifiInfo.toString();
		}
		return info;
	}

	private HashMap<String, String> collectConnectivityManagerStats() {
		HashMap<String, String> stats = new HashMap<String, String>();
		ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		DetailedState detailedState = networkInfo.getDetailedState();
		Log.i("ConnectivityManager: ", detailedState.toString());
		stats.put("Network Detailed State", detailedState.toString());

/*		networkInfo = connectivityManager.getActiveNetworkInfo();
		Log.i("Connected state: ", networkInfo.getState().toString());
*/		if (networkInfo.isConnected()) {
			Log.i("Connected: ", networkInfo.toString());
			stats.put("Network Info", networkInfo.toString());
		}
		return stats;
	}

	/**
	 * @return 
	 * 
	 */
	private HashMap<String, String> collectTelephonyManagerStats() {
		HashMap<String,String> stats = new HashMap<String, String>();
		Log.i("TelephonyManager: ",
				" ==> Collecting TelephonyManager stats <==");
		
		stats.put("Subscriber Id", telephonyManager.getSubscriberId());
		stats.put("Line1 Number", telephonyManager.getLine1Number());
		String timenow = SimpleDateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
		stats.put("Time", timenow);
		stats.put("Location", collectLocationStats());
		Log.i("TelephonyManager: ", " device id => "
				+ telephonyManager.getDeviceId());
		stats.put("Device Id", telephonyManager.getDeviceId());
		Log.i("TelephonyManager: ", " device software version => "
				+ telephonyManager.getDeviceSoftwareVersion());
		stats.put("Device software version", telephonyManager.getDeviceSoftwareVersion());
		
		stats.putAll(collectConnectivityManagerStats());
		
		stats.put("WiFi", collectWiFiStats());
		
//		todo: collect battery information synchronously for storing in db
		stats.put("Battery", batteryInfo);

		Log.i("TelephonyManager: ", " subscriber id => "
				+ telephonyManager.getSubscriberId());
		int nt = telephonyManager.getNetworkType();
		switch (nt) {
		case TelephonyManager.NETWORK_TYPE_EDGE:
			Log.i("TelephonyManager: ", " ==> EDGE Network <==");
			stats.put("Network type", "EDGE");
			break;
		case TelephonyManager.NETWORK_TYPE_GPRS:
			Log.i("TelephonyManager: ", " ==> GPRS Network <==");
			stats.put("Network type", "GPRS");
			break;
		case TelephonyManager.NETWORK_TYPE_UMTS:
			Log.i("TelephonyManager: ", " ==> UMTS Network <==");
			stats.put("Network type", "UMTS");
			break;
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			Log.i("TelephonyManager: ", " ==> Unknown Network <==");
			stats.put("Network type", "UNKNOWN");
			break;
		}
		int pt = telephonyManager.getPhoneType();
		switch (pt) {
		case TelephonyManager.PHONE_TYPE_GSM:
			Log.i("TelephonyManager: ", " ==> PHONE IS GSM TYPE <==");
			stats.put("Phone type", "GSM");
			break;
		case TelephonyManager.PHONE_TYPE_NONE:
			Log.i("TelephonyManager: ", " ==> PHONE TYPE IS UNKNOWN <==");
			break;
		}
		int ss = telephonyManager.getSimState();
		switch (ss) {
		case TelephonyManager.SIM_STATE_ABSENT:
			Log.i("TelephonyManager: ", " ==> SIM STATE ABSENT <==");
			stats.put("SIM State", "Absent");
			break;
		case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
			Log.i("TelephonyManager: ", " ==> SIM STATE NETWORK LOCKED <==");
			stats.put("SIM State", "Network Locked");
			break;
		case TelephonyManager.SIM_STATE_PIN_REQUIRED:
			Log.i("TelephonyManager: ", " ==> SIM STATE PIN REQUIRED <==");
			stats.put("SIM State", "PIN Required");
			break;
		case TelephonyManager.SIM_STATE_PUK_REQUIRED:
			Log.i("TelephonyManager: ", " ==> SIM STATE PUK REQUIRED <==");
			stats.put("SIM State", "PUK Required");
			break;
		case TelephonyManager.SIM_STATE_READY:
			Log.i("TelephonyManager: ", " ==> SIM STATE READY <==");
			stats.put("SIM State", "Ready");
			break;
		case TelephonyManager.SIM_STATE_UNKNOWN:
			Log.i("TelephonyManager: ", " ==> SIM STATE UNKNOWN <==");
			stats.put("SIM State", "Unknown");
			break;
		}
		int da = telephonyManager.getDataActivity();
		switch (da) {
		case TelephonyManager.DATA_ACTIVITY_IN:
			Log.i("TelephonyManager: ", " ==> Data being Downloaded <==");
			break;
		case TelephonyManager.DATA_ACTIVITY_OUT:
			Log.i("TelephonyManager: ", " ==> Data being Uploaded <==");
			break;
		case TelephonyManager.DATA_ACTIVITY_INOUT:
			Log.i("TelephonyManager: ",
					" ==> Data being Downloaded & Uploaded <==");
			break;
		case TelephonyManager.DATA_ACTIVITY_NONE:
			Log.i("TelephonyManager: ", " ==> No Data Activity <==");
			break;
		}
		return stats;
	}

	/**
	 * @param serviceState
	 * @return 
	 */
	private HashMap<String, String> collectServiceStateStats(ServiceState serviceState) {
		HashMap<String, String>stats = new HashMap<String, String>();
		Log.i("ServiceState: ", serviceState.toString());
		stats.put("ServiceState", serviceState.toString());
		String oplong = serviceState.getOperatorAlphaLong();
		stats.put("Operator", oplong);
		Log.i("ServiceState: ", oplong);
		Log.i("ServiceState: ", " is roaming => " + serviceState.getRoaming());
		stats.put("Roaming", Boolean.toString(serviceState.getRoaming()));
		Log.i("ServiceState: ", " manual network selection => "
				+ serviceState.getIsManualSelection());
		stats.put("Manual Network selection", Boolean.toString(serviceState.getIsManualSelection()));
		int state = serviceState.getState();
		parseSignalState(stats, state);
		return stats;
	}

	/**
	 * @param stats
	 * @param state
	 */
	private void parseSignalState(HashMap<String, String> stats, int state) {
//		signal strength can also be parsed for state
//		makes separate entry for signal strength here.. might not apply for state
//		todo: check if this is correct
		stats.put("Signal Strength", state+"asu");
		switch (state) {
		case ServiceState.STATE_IN_SERVICE:
			Log.i("ServiceState: ", " ==> IN SERVICE <==");
			stats.put("State", "IN SERVICE");
			break;
		case ServiceState.STATE_OUT_OF_SERVICE:
			Log.i("ServiceState: ", " ==> OUT OF SERVICE <==");
			stats.put("State", "OUT OF SERVICE");
			break;
		case ServiceState.STATE_EMERGENCY_ONLY:
			Log.i("ServiceState: ", " ==> EMERGENCY ONLY <==");
			stats.put("State", "EMERGENCY ONLY");
			break;
		case ServiceState.STATE_POWER_OFF:
			Log.i("ServiceState: ", " ==> POWER OFF <==");
			stats.put("State", "POWER OFF");
			break;
		}
	}

	/**
	 * @param serviceState
	 */
	private void storeServiceStateChange(ServiceState serviceState) {
		Log.i("IRService#storeServiceStateChange: ",
				" ==> Collecting ServiceState stats <==");
		
        HashMap<String,String> stats = new HashMap<String, String>(); 
		stats.put("Event", "Service State Change");
        stats.putAll(collectTelephonyManagerStats());
		stats.putAll(collectServiceStateStats(serviceState));
        storeStatsInDB(stats);

	}

	private void storeDataConnectionStateChange (String reason){
        HashMap<String,String> stats = new HashMap<String, String>(); 
		stats.put("Event", "DataConnection State Change");
		stats.put("Data Connection", reason);
        stats.putAll(collectTelephonyManagerStats());
        storeStatsInDB(stats);
	}

	/**
	 * @param asu
	 */
	private void storeSignalStrengthChange(int asu) {
		HashMap<String, String> stats = new HashMap<String, String>();
		stats.put("Event", "Signal Strength Change");
		parseSignalState(stats, asu);
        stats.putAll(collectTelephonyManagerStats());
		storeStatsInDB(stats);
	}
	
	private void storeCallStateChange (String reason){
        HashMap<String,String> stats = new HashMap<String, String>(); 
		stats.put("Event", "Call State Change");
		stats.put("Call State", reason);
        stats.putAll(collectTelephonyManagerStats());
        storeStatsInDB(stats);		
	}

	/**
	 * @param stats
	 */
	private void storeStatsInDB(HashMap<String, String> stats) {
		JSONObject serviceStateDetails = new JSONObject(stats);

        ContentValues contentValues = new ContentValues();
        contentValues.put(IncidentReport.Details.ID, stats.get("Subscriber Id"));
        try {
			contentValues.put(IncidentReport.Details.DETAILS, serviceStateDetails.toString(2));
	        Uri uri = getContentResolver().insert(IncidentReport.Details.CONTENT_URI, contentValues);
	        Log.d("IRService#storeStatsInDB: ", uri.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "IRService#storeStatsInDB: failed to extract JSON data");
		}
//		also uploads to server - realtime
//		todo: change this to timed update
		uploadStatsToServer(stats);
	}

    private void uploadStatsToServer (HashMap<String, String> stats){
        HttpHandler httpHandler = new HttpHandler();
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        try {
    		JSONObject serviceStateDetails = new JSONObject(stats);
            nvps.add(new BasicNameValuePair("content", serviceStateDetails.toString(2)));
            String response = httpHandler.post("http://ibt.appspot.com/upload", nvps);
            Log.i(TAG, ">"+response+"<");
            if ("OK".equalsIgnoreCase(response.trim())) {
            	Log.i(TAG, "Successfully Uploaded to Server");
            } else {
            	Log.e(TAG, "Failed to Upload to Server");
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
		}
    }
    
}
