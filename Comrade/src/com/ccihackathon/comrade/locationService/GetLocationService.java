package com.ccihackathon.comrade.locationService;

import java.util.List;

import com.ccihackathon.comrade.ReminderActivity;
import com.ccihackathon.comrade.data.ReminderManager;
import com.ccihackathon.comrade.db.Reminder;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class GetLocationService extends Service implements LocationListener {

	private LocationManager locManager;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	 public void onStart(Intent intent, int startId){
		   Toast.makeText(getBaseContext(), "Service Started..", Toast.LENGTH_SHORT).show();
	        Log.v("Debug", "Service Started..");        

	        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
	        try{
	            gps_enabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	        }
	        catch(Exception ex){}
	        try{
	            network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);           
	        }
	        catch(Exception ex){}

	        if (gps_enabled) {
	            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 10, this);
	            Log.v("Debug", "gps_enabled..");
	        }
	        if (network_enabled) {
	            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 10, this);
	            Log.v("Debug", "network_enabled..");
	        }  

	        // We want this service to continue running until it is explicitly
	        // stopped, so return sticky.
	 }
	
	@Override
	public void onLocationChanged(Location location) {
		Log.i("Location Service", "Location Changed");
		Toast.makeText(this,"Location Changed", Toast.LENGTH_SHORT).show();
		
		ReminderManager reminderManger = new ReminderManager(this);
		List<Reminder> reminderList = reminderManger.getReminders();
		
		Reminder reminderToShow = null;
		for (Reminder reminder : reminderList)
		{
			if(reminder.getLatitude() == String.valueOf(location.getLatitude()) && reminder.getLatitude() == String.valueOf(location.getLatitude()) )
			{
				reminderToShow = reminder;
				break;
			}
		}

		if(reminderToShow != null)
		{
			if(reminderToShow.getNotify() != null && reminderToShow.getReminder() != "")
			{
				notify(reminderToShow);
			}
			else if(reminderToShow.getReminder() != null && reminderToShow.getReminder() != "")
			{
				showReminder();
			}
			else
			{
				notify(reminderToShow);
				showReminder();
			}
			
		}
		
	}

	private void showReminder() {
		Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(
		        Intent.CATEGORY_LAUNCHER).setClassName(getApplicationContext(),
		        		ReminderActivity.class.getName()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		        .addFlags(Intent.FLAG_FROM_BACKGROUND).setComponent(new ComponentName(getApplicationContext(),
		        		ReminderActivity.class));
		getApplicationContext().startActivity(intent);
	}

	private void notify(Reminder reminderToShow) {
		String phoneNumber = reminderToShow.getNotify();
		String message = "I have reached : " + reminderToShow.getLocation();
		
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(phoneNumber, null, message,
				null, null);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}
