package assignment7;

/* Chat Room ServerMain.java
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Observable;

public class ServerMain extends Observable {
	private static ArrayList<User> users;
	private static ArrayList<String> passwords;
	private static ArrayList<Chatroom> chatrooms;
	private static ArrayList<ClientObserver> observers;
	// Values used for creating User and Chatroom IDs.
	private int usersCount = 0;
	private int chatroomsCount = 1;
	
	public static void main(String[] args) {
		try {
			new ServerMain().setUpNetworking();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setUpNetworking() throws Exception {
		// Initializations
		users = new ArrayList<User>();
		chatrooms = new ArrayList<Chatroom>();
		observers = new ArrayList<ClientObserver>();
		passwords = new ArrayList<String>();
		chatrooms.add(0, new Chatroom(0, "Global", ""));
		
		@SuppressWarnings("resource")
		ServerSocket serverSocket = new ServerSocket(5000);
		while (true) {
			// Makes a new thread for each client connection
			Socket clientSocket = serverSocket.accept();
			ClientObserver writer = new ClientObserver(clientSocket.getOutputStream());
			new Thread(new Runnable(){
				@Override
				public void run() {
					Object message;
					try {
						ObjectInputStream reader = new ObjectInputStream(
								new BufferedInputStream(clientSocket.getInputStream()));
						// Periodically checks for messages
						while ((message = reader.readObject()) != null) {
							processMessage(message, writer); 
						}
					} catch (SocketException e) {
						System.out.println("Client disconnected!");
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}).start();
			this.addObserver(writer);
		}
	}
	/**
	 * Primary code for processing messages that are sent to the server.
	 * @param message - The Object passed to the server.
	 * @param writer - The ClientObserver that communicates with the client.
	 */
	private void processMessage(Object message, ClientObserver writer) {
		// Strings are used for background client-server communication.
		if (message instanceof String) {
			try {
				String msg = (String) message;
				String[] msg_split = msg.split(" ");
				// Creates new user
				if (msg_split[0].equals("/SIGNUP")) {
					for (User u : users) {
						if (u.getName().equals(msg_split[1])) {
							writer.writeObject("name-taken ");
							writer.flush();
							return;
						}
					}
					int userNum = usersCount++;
					User this_user = new User(userNum, msg_split[1]);
					this_user.setOnline(true);
					users.add(this_user);
					passwords.add(msg_split[2]);
					observers.add(userNum, writer);
					chatrooms.get(0).addMember(userNum);
					writer.writeObject("registered " + userNum + " " + msg_split[1]);
					writer.flush();
					return;
				// Client logs in as existing user
				} else if (msg_split[0].equals("/SIGNIN")) {
					String this_name = msg_split[1];
					String this_password = msg_split[2];
					int id = getUserId(this_name);
					if (id == -1) {
						writer.writeObject("name-not-found ");
						writer.flush();
						return;
					}
					if (this_password.equals(passwords.get(id))) {
						User u = users.get(id);
						if (u.isOnline()) {
							writer.writeObject("already-online ");
							writer.flush();
							return;
						} else {
							u.setOnline(true);
							for (Chatroom c : chatrooms) {
								if (c.isMember(id)) {
									c.addObserver(writer);
									c.sendChatroom();
								}
							}
							observers.set(id, writer);
							writer.writeObject("logged-in " + id + " " + this_name);
							writer.flush();
							return;
						}
					} else {
						writer.writeObject("wrong-password ");
						writer.flush();
						return;
					}
				// Client logs out
				} else if (msg_split[0].equals("/LOGOUT")) {
					for (User u : users) {
						if (u.getName().equals(getUserName(Integer.parseInt(msg_split[1])))) {
							u.setOnline(false);
							int id = u.getUserNum();
							ClientObserver obs = observers.get(id);
							if (obs != null) {
								for (Chatroom c : chatrooms) {
									c.deleteObserver(obs);
								}
								this.deleteObserver(obs);
							}
							observers.set(id, null);
							
							return;
						}
					}
				// Client requests data
				} else if (msg_split[0].equals("/getData")) {
					// Requests chatroom
					if (msg_split[1].equals("chatroom")) {
						writer.writeObject(chatrooms.get(Integer.parseInt(msg_split[2])));
						writer.flush();
					// requests User based on string
					} else if (msg_split[1].equals("userstring")) {
						int id = getUserId(msg_split[2]);
						if (id >= 0) writer.writeObject(users.get(id));
						else writer.writeObject("name-not-found ");
						writer.flush();
					// requests User based on ID
					} else if (msg_split[1].equals("user")) {
						writer.writeObject(users.get(Integer.parseInt(msg_split[2])));
						writer.flush();
					}
					return;
				}
				//setChanged();
				//notifyObservers(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		// Messages are usually used for actual messages, although several
		// additional commands exist.
		} else if (message instanceof Message) {
			Message msg = (Message) message;
			String[] tokens = msg.getMsg().split(" ");
			String s = tokens[0];
			// creates new chatroom
			if (s.equals("/createChatroom")
			 || s.equals("/newroom")) {
				Chatroom cr = new Chatroom(chatroomsCount, "Chatroom #" + chatroomsCount, "");
				cr.addMember(msg.getUserNum());
				for (int i = 1; i < tokens.length; i++) {
					int id = getUserId(tokens[i]);
					if (id >= 0) cr.addMember(id);
				}
				chatrooms.add(chatroomsCount, cr);
				chatroomsCount++;
				cr.sendChatroom();
			// creates a PM chatbox
			} else if (s.equals("/pm")
					|| s.equals("message")) {
				Chatroom cr = new Chatroom(chatroomsCount, "Placeholder", "");
				cr.addMember(msg.getUserNum());
				int id = getUserId(tokens[1]);
				if (id >= 0) cr.addMember(id);
				cr.setPM();
				chatrooms.add(chatroomsCount, cr);
				chatroomsCount++;
				cr.sendChatroom();
			// adds members to a chatroom
			} else if (s.equals("/addMember")
					|| s.equals("/addMembers")) {
				int user;
				Chatroom cr = chatrooms.get(msg.getChatroomNum());
				if (!cr.isPM()) {
					for (int i = 1; i < tokens.length; i++) {
						user = getUserId(tokens[i]);
						if (user >= 0) cr.addMember(user);
					}
				}
				cr.sendChatroom();
			// changes name of chatroom
			} else if (s.equals("/changeChatroomName")
					|| s.equals("/chatname")
					|| s.equals("/roomname")) {
				if (tokens.length > 1) {
					Chatroom cr = chatrooms.get(msg.getChatroomNum());
					if (!cr.isPM()) {
						cr.setName(tokens[1]);
						cr.sendChatroom();
					}
				}
			// changes nickname of person
			} else if (s.equals("/changeNickname")
					|| s.equals("/nick")
					|| s.equals("/changename")) {
				if (tokens.length > 1) {
					users.get(msg.getUserNum()).setName(tokens[1]);
				}
				// TODO make sure chatrooms and users see this update
			// adds users to friendlist
			} else if (s.equals("/addFriend")
					|| s.equals("/addFriends")
					|| s.equals("/add")) {
				for (int i = 1; i < tokens.length; i++) {
					int id = getUserId(tokens[i]);
					if (id >= 0) users.get(msg.getUserNum()).addFriend(id);
				}
				// TODO make sure users see this update
			// removes self from chatroom
			} else if (s.equals("/leaveChatroom")
					|| s.equals("/leave")) {
				int cr = msg.getChatroomNum();
				chatrooms.get(cr).removeMember(msg.getUserNum());
			} else { // just plain old message
				chatrooms.get(msg.getChatroomNum()).sendMessage(msg);
			}
		} else {
			System.out.println("Unfamiliar object type input by client. Input ignored. Fix later.");
		}
	}
	/**
	 * Returns ID of user.
	 * @param name  - name of user
	 * @return - ID
	 */
	public int getUserId(String name) {
		for (User u : users) {
			if (u.getName().equals(name)) {
				return u.getUserNum();
			}
		}
		return -1;
	}
	/**
	 * Returns name of user.
	 * @param id - id of user
	 * @return name as a string
	 */
	public static String getUserName(int id) {
		if (users.size() <= id) return null;
		return users.get(id).getName();
	}
	/**
	 * Returns name of chatroom.
	 * @param id - id of chatroom
	 * @return name as a string
	 */
	public static String getChatroomName(int id) {
		if (chatrooms.size() <= id) return null;
		return chatrooms.get(id).getName();
	}
	/**
	 * Returns observer that is related to a user.
	 * @param id - id of user
	 * @return the ClientObserver that is related to it.
	 */
	public static ClientObserver getObserver(int id) {
		return observers.get(id);
	}
}