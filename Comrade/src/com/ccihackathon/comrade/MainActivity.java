package com.ccihackathon.comrade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ccihackathon.comrade.data.ReminderManager;
import com.ccihackathon.comrade.db.Reminder;
import com.ccihackathon.comrade.locationService.GetLocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;

public class MainActivity extends Activity implements LocationListener {
	private static GoogleMap googleMap;
	private static double latitude, longitude;
	public static int PICK_CONTACT = 100;
	public static LinearLayout remindLinearLayout;
	public static LinearLayout notifyLinearLayout;
	static RadioGroup radioSelect;
	static TextView pickcontactTextView;
	public static TextView contactNumberTextView, locationText;
	public static Button doneButton;
	public static String locationName;
	public static Button cancelDialogButton;
	public static DialogFragment dialogFragment;
	public static LatLng coordinates;
	public static EditText reminderText;
	public static ReminderManager reminderManager;
	public static boolean remind, notify, both;
	public static List<Reminder> reminderList = new ArrayList<Reminder>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.startService(new Intent(MainActivity.this, GetLocationService.class));

	}

	// setting up database for storing user info
	private void setupDatabase() {
		reminderManager = new ReminderManager(this);
		reminderList = reminderManager.getReminders();
		for(int i=0 ; i<reminderList.size() ; i++){
			MarkerOptions options = new MarkerOptions().position(new LatLng(Double.valueOf(reminderList.get(i).getLatitude()), Double.valueOf(reminderList.get(i).getLongitude())));
			options.title(reminderList.get(i).getLocation());
			options.snippet(reminderList.get(i).getReminder() + reminderList.get(i).getNotify());
			googleMap.addMarker(options);
		}
		
	}
	
	

	@Override
	protected void onResume() {
		super.onResume();

		setupMap();
		setupDatabase();
		addListenersToMap();
	}

	
	
	@Override
	protected void onStop() {
		super.onStop();
		googleMap = null;
	}

	// adding on click and drag listeners to the map for adding and removing
	// markers.
	private void addListenersToMap() {

		googleMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng point) {
				locationName = convertPointToLocation(new GeoPoint(
						(int) (point.latitude * 1E6),
						(int) (point.longitude * 1E6)));
				coordinates = point;
				double distance = getDistanceInKilometers(point.latitude * 1E6,
						point.longitude * 1E6, point.latitude * 1E6,
						point.longitude * 1E6);

				dialogFragment = new SetReminderDialogFragment();
				dialogFragment.setStyle(DialogFragment.STYLE_NORMAL,
						android.R.style.Theme);
				dialogFragment.show(MainActivity.this.getFragmentManager(),
						"SetReminderDialogFragment");
			}
		});

		googleMap.setOnMarkerDragListener(new OnMarkerDragListener() {

			@Override
			public void onMarkerDragStart(Marker marker) {
				// remove marker from map
				reminderList.clear();
				String id = marker.getId();
				
				reminderManager.deleteReminder(id);
				marker.remove();

			}

			@Override
			public void onMarkerDragEnd(Marker marker) {

			}

			@Override
			public void onMarkerDrag(Marker marker) {

			}
		});
	}

	public String convertPointToLocation(GeoPoint point) {
		String address = "";
		Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
		try {
			List<Address> addresses = geoCoder.getFromLocation(
					point.getLatitudeE6() / 1E6, point.getLongitudeE6() / 1E6,
					1);

			if (addresses.size() > 0) {
				for (int index = 0; index < addresses.get(0)
						.getMaxAddressLineIndex(); index++)
					address += addresses.get(0).getAddressLine(index) + " ";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return address;
	}

	// setting up the google map
	private void setupMap() {

		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
		}

		if (googleMap != null) {
			googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			googleMap.setMyLocationEnabled(true);

			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			String provider = locationManager.getBestProvider(criteria, true);
			Location location = locationManager.getLastKnownLocation(provider);
			if (location != null) {
				onLocationChanged(location);
			}
		}

	}

	// get distance between two latlng points in kilometers.
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
	public void onLocationChanged(Location location) {
		latitude = location.getLatitude();
		longitude = location.getLongitude();

		LatLng latLng = new LatLng(latitude, longitude);
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	public static class SetReminderDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			final Dialog dialog = new Dialog(getActivity());
			dialog.setTitle("Add Details");
			dialog.setCanceledOnTouchOutside(true);
			dialog.getWindow().setGravity(Gravity.CENTER);

			View view = getActivity().getLayoutInflater().inflate(
					R.layout.activity_set_reminder_dialog_fragment, null);

			radioSelect = (RadioGroup) view.findViewById(R.id.radioSelect);

			remindLinearLayout = (LinearLayout) view
					.findViewById(R.id.remindLinearLayout);

			notifyLinearLayout = (LinearLayout) view
					.findViewById(R.id.notifyLinearLayout);

			contactNumberTextView = (TextView) view
					.findViewById(R.id.contactNumberTextView);

			doneButton = (Button) view.findViewById(R.id.doneButton);
			cancelDialogButton = (Button) view
					.findViewById(R.id.cancelDialogButton);
			locationText = (TextView) view.findViewById(R.id.locationText);
			reminderText = (EditText) view.findViewById(R.id.toDoTextView);

			locationText.setText(locationName);
			both = true;
			this.setRadioButtonListeners(view);
			this.setContactListListeners(view);
			this.doneButtonListeners(view);
			this.cancelButtonListeners(view);
			dialog.setContentView(view);
			dialog.setCanceledOnTouchOutside(false);
			dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);

			return dialog;
		}

		public void setRadioButtonListeners(View view) {
			radioSelect
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(RadioGroup group,
								int checkedId) {

							switch (checkedId) {

							case R.id.remindRadioButton:
								remind = true;
								notify = false;
								both = false;
								notifyLinearLayout.setVisibility(8);
								remindLinearLayout.setVisibility(0);
								break;

							case R.id.notifyRadioButton:
								notify = true;
								remind = false;
								both = false;
								notifyLinearLayout.setVisibility(0);
								remindLinearLayout.setVisibility(8);
								break;

							case R.id.bothRadioButton:
								both = true;
								remind = false;
								notify = false;
								notifyLinearLayout.setVisibility(0);
								remindLinearLayout.setVisibility(0);
								break;
							}
						}
					});
		}

		public void setContactListListeners(View view) {

			pickcontactTextView = (TextView) view
					.findViewById(R.id.pickcontactTextView);
			pickcontactTextView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_PICK,
							ContactsContract.Contacts.CONTENT_URI);
					getActivity().startActivityForResult(intent, PICK_CONTACT);
				}
			});

		}

		public void doneButtonListeners(View v) {

			doneButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					String phoneNumber = contactNumberTextView.getText().toString();
					String message = "I have reached" + locationName;
					StringBuilder builder = new StringBuilder();
					MarkerOptions markerOptions = new MarkerOptions();
					markerOptions.title(locationName).draggable(true)
							.position(coordinates);
					
					if(remind){
						if(reminderText.getText().toString().length() > 0){
							builder.delete(0, builder.length());
							builder.append("Reminder: ");
							builder.append(reminderText.getText().toString());
							markerOptions.snippet(builder.toString());
							
							Marker marker = googleMap.addMarker(markerOptions);
							insertReminderInDatabase(marker.getId());
							dialogFragment.dismiss();
						}
						else{
							Toast.makeText(getActivity(), "Please enter reminders", Toast.LENGTH_SHORT).show();
						}
					}
					
					
					else if(notify){
						if(contactNumberTextView.getText().toString().trim().length() > 0){
							builder.delete(0, builder.length());
							builder.append("SMS To: ");
							builder.append(contactNumberTextView.getText()
									.toString());
							markerOptions.snippet(builder.toString());
							Marker marker = googleMap.addMarker(markerOptions);
							insertReminderInDatabase(marker.getId());
							dialogFragment.dismiss();
						}else{
							Toast.makeText(getActivity(), "Please enter contact number", Toast.LENGTH_SHORT).show();
						}
					}

					
					else if(both){
						if(contactNumberTextView.getText().toString().trim().length() > 0 && reminderText.getText().toString().length() > 0){
							builder.delete(0, builder.length());
							builder.append("Reminder: ");
							builder.append(reminderText.getText().toString());
							builder.append("\n");
							builder.append("SMS To: ");
							builder.append(contactNumberTextView.getText()
									.toString());
							markerOptions.snippet(builder.toString());
							Marker marker = googleMap.addMarker(markerOptions);
							insertReminderInDatabase(marker.getId());
							dialogFragment.dismiss();
						}else{
							Toast.makeText(getActivity(), "Please enter details", Toast.LENGTH_SHORT).show();
						}
					}
					
					
					/*if (reminderText.getText().toString().trim().length() > 0) {
						builder.append("Reminder: ");
						builder.append(reminderText.getText().toString());
					}

					if (contactNumberTextView.getText().toString().trim()
							.length() > 0) {
						builder.append("\n");
						builder.append("SMS To: ");
						builder.append(contactNumberTextView.getText()
								.toString());
					}

					

					

					if (phoneNumber.trim().length() > 0) {
						SmsManager smsManager = SmsManager.getDefault();
						smsManager.sendTextMessage(phoneNumber, null, message,
								null, null);
					} else {
						Toast.makeText(getActivity(),
								"Please enter a contact number",
								Toast.LENGTH_SHORT).show();
					}*/

					
				}

				private void insertReminderInDatabase(String string) {
					Reminder reminder = new Reminder(null, string, reminderText
							.getText().toString(), locationName,
							String.valueOf(coordinates.latitude), String.valueOf(coordinates.longitude),
							contactNumberTextView.getText().toString());
					reminderManager.insertNotification(reminder);
					
				}
			});

		}

		public void cancelButtonListeners(View v) {

			cancelDialogButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialogFragment.dismiss();
				}
			});

		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);
		if (reqCode == PICK_CONTACT) {

			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				System.out.println("contactData" + contactData);
				Cursor c = managedQuery(contactData, null, null, null, null);
				if (c.moveToFirst()) {
					String id = c
							.getString(c
									.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

					String hasPhone = c
							.getString(c
									.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

					if (hasPhone.equalsIgnoreCase("1")) {
						Cursor phones = getContentResolver()
								.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
										null,
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
												+ " = " + id, null, null);
						phones.moveToFirst();
						String cNumber = phones
								.getString(phones
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

						System.out.println("cNumber" + cNumber);
						contactNumberTextView.setText(cNumber);

					}
				}
			}

		}

	}

}
