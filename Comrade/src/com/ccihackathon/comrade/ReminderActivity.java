package com.ccihackathon.comrade;

import com.ccihackathon.comrade.data.ReminderManager;
import com.ccihackathon.comrade.db.Reminder;

import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ReminderActivity extends Activity {
	
	public static String REMINDER_TEXT = "IntententReminderText";
	public static String REMINDER_ID = "ReminderID";
	
	Button dismissReminderButton;
	TextView reminderTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reminder);
		
		reminderTextView = (TextView)findViewById(R.id.reminderText);
		
		if(getIntent().getStringExtra(REMINDER_TEXT) != null)
		{
			reminderTextView.setText(getIntent().getStringExtra(REMINDER_TEXT));
		}
		
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
		r.play();
		dismissReminderButton = (Button) findViewById(R.id.dismissReminderButton);
		dismissReminderButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ReminderManager reminderManager = new ReminderManager(ReminderActivity.this);
				reminderManager.deleteReminder(getIntent().getStringExtra(REMINDER_ID));
				
				ReminderActivity.this.finish();
				moveTaskToBack(true); 
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.reminder, menu);
		return true;
	}

}
