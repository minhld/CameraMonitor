package com.usu.db;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class EventDb {
	static MongoClient mongoClient;
	static MongoDatabase db;
	static MongoCollection<Document> eventTable;
	
	static String dbIp = "";
	
	/**
	 * initiate/retrieve the database  
	 * 
	 * @param dbIp
	 */
	public static void init(final String dbIp) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mongoClient = new MongoClient(dbIp, 27017);
					
					// create db name cam-event
					db = mongoClient.getDatabase("camevent");
					
					// create table events
					eventTable = db.getCollection("events");
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}).start();
	}
	
	/**
	 * add a new event to the database
	 * 
	 * @param e
	 */
	public static void addEvent(final Event e) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Document doc = new Document();
				doc.put("time", e.time);
				doc.put("type", e.type.toString());
				doc.put("info", e.info);
				eventTable.insertOne(doc);
			}
		}).start();
	}
	
	public static void close() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				mongoClient.close();
			}
		}).start();
	}
	
	public static boolean isDbAvail() {
		return mongoClient != null;
	}
}
