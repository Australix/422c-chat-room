package assignment7;

/* Chat Room Chatroom.java
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
import java.util.Observable;
/**
 * Chatroom stores a unique number, its name, whether it is a PM, 
 * a log of message history, and a list of members. It has functions
 * for handling the notification of its users of new messages etc.
 */
public class Chatroom extends Observable implements Serializable {
	private static final long serialVersionUID = 1L;
	private int chatroomNum;
	private String name;
	private String password;
	private boolean isPM;
	private ArrayList<Message> history = new ArrayList<Message>();
	private ArrayList<Integer> members = new ArrayList<Integer>();
	// Constructor
	public Chatroom(int num, String nm, String pw) {
		setChatroomNum(num);
		setName(nm);
		setPassword(pw);
		isPM = false;
	}
	// Most of the functions below are pretty self-explanatory.
	public void setPM() {
		isPM = true;
	}
	public boolean isPM() {
		return isPM;
	}
	/**
	 * Sends a copy of the chatroom to its observers. Used for updates
	 * to name/users.
	 */
	public void sendChatroom() {
		setChanged();
		notifyObservers(this);
	}
	/**
	 * Notifies observers of this chatroom of the message sent.
	 * @param message - message sent. 
	 */
	public void sendMessage(Message message) {
		setChanged();
		notifyObservers(message);
	}
	/**
	 * Adds a user to the member list and adds the user's observer.
	 * @param member - user
	 */
	public void addMember(int member) {
		members.add(member);
		this.addObserver(ServerMain.getObserver(member));
	}
	/**
	 * Removes a user and it's observer from the chatroom.
	 * @param member - user
	 */
	public void removeMember(int member) {
		members.remove(member);
		this.deleteObserver(ServerMain.getObserver(member));
	}
	/**
	 * Returns whether or not the user specified is a member of
	 * this chatroom.
	 * @param member - user specified
	 * @return - see desc.
	 */
	public boolean isMember(int member) {
		for (Integer id : members) {
			if (id == member) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns the number of members in the chatroom. 
	 * @return
	 */
	public int numMembers() {
		return members.size();
	}
	/**
	 * For PMs only. Returns the member that is not the member
	 * that called the function.
	 * @param id - member that called the function. 
	 * @return - the member that did not call the function. 
	 */
	public int otherMember(int id) {
		if (id == members.get(0)) return members.get(1);
		else return members.get(0);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public ArrayList<Message> getHistory() {
		return history;
	}
	public void addHistory(Message msg) {
		history.add(msg);
	}
	public int getChatroomNum() {
		return chatroomNum;
	}
	public void setChatroomNum(int chatroomNum) {
		this.chatroomNum = chatroomNum;
	}
}
