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
			String lat = reminder.getLatitude();
			String lon = reminder.getLongitude();
			
			String lat1 = String.valueOf(location.getLatitude());
			String lon1 = String.valueOf(location.getLongitude());
			
			Log.i("Location Service", "Current Lat : " + lat1 + "Lon : " + lon1 + "Data Lat : " + lat + "lon : " + lon);
			
			//Check for a distance of 1 kilometer
			double distance = getDistanceInKilometers(Double.valueOf(reminder.getLatitude()), Double.valueOf(reminder.getLongitude()), location.getLatitude(), location.getLongitude());
			if(distance <= 1)
			{
				reminderToShow = reminder;
				break;
			}
		}

		if(reminderToShow != null)
		{
			if(reminderToShow.getNotify() != null && reminderToShow.getReminder() != "" && reminderToShow.getReminder() != null && reminderToShow.getReminder() != "")
			{
				notify(reminderToShow);
				showReminder();
			}
			if(reminderToShow.getNotify() != null && reminderToShow.getReminder() != "")
			{
				notify(reminderToShow);
			}
			else if(reminderToShow.getReminder() != null && reminderToShow.getReminder() != "")
			{
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

	private double getDistanceInKilometers(double latitudeSource,
			double longitudeSource, double latitudeDestination,
			double longitudeDestination) {
		int radiusOfEarth = 6371;
		double degreeLatitude = degreeToRadians(latitudeDestination
				- latitudeSource);
		double degreeLongitude = degreeToRadians(longitudeDestination
				- longitudeSource);

		double sinCos = Math.sin(degreeLatitude / 2)
				* Math.sin(degreeLatitude / 2)
				+ Math.cos(degreeToRadians(latitudeSource))
				* Math.cos(degreeToRadians(latitudeDestination))
				* Math.sin(degreeLongitude / 2) * Math.sin(degreeLongitude / 2);

		double tan = 2 * Math.atan2(Math.sqrt(sinCos), Math.sqrt(1 - sinCos));
		double distanceInKms = radiusOfEarth * tan;
		return distanceInKms;
	}

	private double degreeToRadians(double degree) {
		return degree * (Math.PI / 180);
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
