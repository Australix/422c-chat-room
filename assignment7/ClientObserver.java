package assignment7;

/* Chat Room ClientObserver.java
 * EE422C Project 7 submission by
 * Timberlon Gray
 * tg22698
 * 16235
 * Raiyan Chowdhury
 * rac4444
 * 16235
 * Slip days used: <1>
 * Spring 2017
 */

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Observable;
import java.util.Observer;

public class ClientObserver extends ObjectOutputStream implements Observer {
	public ClientObserver(OutputStream out) throws IOException {
		super(out);
	}
	
	/**
	 * Updates observers as appropriate when called.
	 */
	@Override
	public void update(Observable obs, Object obj) {
		try{
			this.reset();
			this.writeObject(obj);
			this.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
