package com.ccihackathon.comrade.data;

import java.util.ArrayList;
import java.util.List;

import com.ccihackathon.comrade.dao.DaoMaster;
import com.ccihackathon.comrade.dao.ReminderDao;
import com.ccihackathon.comrade.db.Reminder;

import de.greenrobot.dao.QueryBuilder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class ReminderManager {

	private String dbName  = "ComradeDB";
	private String dbPath  = "/data/data/com.ccihackathon.comrade/databases/";
	
	private ComradeDBHelper comradeDBHelper; 
	private com.ccihackathon.comrade.dao.DaoMaster comradeDaoMaster;
	private com.ccihackathon.comrade.dao.DaoSession comradeDaoSession;
	
	private static ReminderManager reminderManagerInstance;
	
	public ReminderManager(Context context) 
	{
		comradeDBHelper = new ComradeDBHelper(context, dbName, dbPath, null);
		
		SQLiteDatabase comradeDatabase = comradeDBHelper.getWritableDatabase();
		comradeDaoMaster = new DaoMaster(comradeDatabase);
		comradeDaoSession = comradeDaoMaster.newSession();
	}

	public static ReminderManager getInstance(Context context){
		
		if(reminderManagerInstance == null)
		{
			reminderManagerInstance = new ReminderManager(context);
		}
		return reminderManagerInstance;
	}
	
	public void insertNotification(Reminder reminder)
	{
		ReminderDao notificationDao = comradeDaoSession.getReminderDao();
		notificationDao.insert(reminder);
	}
	
	public List<Reminder> getReminders()
	{
		ReminderDao notificationDao = comradeDaoSession.getReminderDao();
		
		QueryBuilder<Reminder> queryBuilder = notificationDao.queryBuilder();
		
		List<Reminder> reminderList = new ArrayList<Reminder>();
		
		for (Reminder reminder : queryBuilder.listLazy())
		{
			reminderList.add(reminder);
		}
		return reminderList;
	}
	
	public void deleteReminder(String id)
	{
		ReminderDao notificationDao = comradeDaoSession.getReminderDao();
		
		QueryBuilder<Reminder> queryBuilder = notificationDao.queryBuilder();
		queryBuilder.where(ReminderDao.Properties.Guid.eq(id));
		
		
		if(queryBuilder.listLazy().size() != 0)
		{
			Reminder reminderToDelete = queryBuilder.listLazy().get(0);
			notificationDao.delete(reminderToDelete);
		}
		
	}
}
