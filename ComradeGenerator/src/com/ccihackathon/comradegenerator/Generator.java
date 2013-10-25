package com.ccihackathon.comradegenerator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class Generator {
	
	private static String outDir = "/CreativeCapsule/CCIHackathon/Comrade/src-gen";
	
public static void main(String args[]) throws Exception{
		
		Schema schema = new Schema(1,"com.ccihackathon.comrade.db");
		schema.enableKeepSectionsByDefault();
		schema.setDefaultJavaPackageDao("com.ccihackathon.comrade.dao");
		schema.setDefaultJavaPackageTest("com.ccihackathon.comrade.test");
		
		addReminder(schema);
		
		new DaoGenerator().generateAll(schema, outDir);
	}

	private static void addReminder(Schema schema) 
	{
		Entity notification = schema.addEntity("Reminder");
		notification.addIdProperty();
		notification.addStringProperty("reminder");
		notification.addStringProperty("location");
		notification.addStringProperty("latitude");
		notification.addStringProperty("longitude");
		notification.addStringProperty("notify");
	}
	
}
