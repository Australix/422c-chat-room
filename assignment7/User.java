package assignment7;

/* Chat Room User.java
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

import java.io.Serializable;
import java.util.ArrayList;
/**
 * User stores a unique id, name, whether or not the user is online,
 * and a friendlist of a person/login. 
 */
public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	private int userNum;
	private String name;
	private boolean isOnline;
	private ArrayList<Integer> friends = new ArrayList<Integer>();
	// Constructor
	public User(int num, String nm) {
		setUserNum(num);
		setName(nm);
	}
	// Functions below are pretty self-explanatory
	public void addFriend(int friend) {
		friends.add(friend);
	}
	public ArrayList<Integer> getFriends() {
		return friends;
	}
	public void setFriends(ArrayList<Integer> friends) {
		this.friends = friends;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getUserNum() {
		return userNum;
	}
	public void setUserNum(int userNum) {
		this.userNum = userNum;
	}
	public boolean isOnline() {
		return isOnline;
	}
	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
}
