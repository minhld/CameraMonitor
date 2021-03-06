package com.usu.db;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

public class EventHandler {
	static SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy_hhmmssa");
	static String IMAGE_FOLDER = "/usr/local/imgs";
	static String IMAGE_EXT = "jpg";
	
	static Event prev;
	
	public static void init(String dbIp) {
		// initiate Mongo database
		EventDb.init(dbIp);
		
		// initiate image folder if it doesn't exist
		File f = new File(IMAGE_FOLDER);
		if (!f.exists()) f.mkdirs();
	}
	
	public static void close() {
		EventDb.close();
	}
	
	/**
	 * raise an event
	 * 
	 * @param light
	 * @param contours
	 */
	public static void raiseEvent(int contours, BufferedImage image) {
		if (!EventDb.isDbAvail() || contours == 0) return;
		
		Event e = new Event(Event.Type.Moving);
		
		// if the messages are in the same type and 
		// are consecutive, then it shouldn't be added
		if (prev != null && prev.type == e.type && 
				e.currentTime - prev.currentTime <= 1000) return;
	
		try {
			// save the image to the file
			String file = IMAGE_FOLDER + "/cam_" + sdf.format(new Date(e.time)) + "." + IMAGE_EXT;
			ImageIO.write(image, IMAGE_EXT, new File(file));
			
			e.info = "{ \"file\": \"" + file + "\" }";
			
			// save to database
			EventDb.addEvent(e);
		
			prev = e;
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		/*
		if (contours == 0) {
			prev = new Event(Event.Type.Nothing);
		} else if (contours > 0) {
			Event e = new Event(Event.Type.Moving);
			handle(e);
			prev = e;
		}
		*/
	}
	
}
