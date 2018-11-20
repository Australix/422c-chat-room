package assignment7;

/* Chat Room ClientMain.java
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
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ClientMain extends Application {
	
	private ObjectInputStream reader;
	private ObjectOutputStream writer;
	private User user;
	private int userNum;
	private HashMap<Integer, Tab> tabs = new HashMap<Integer, Tab>();
	// experimental
	private Message temp;
	private Object temp4User;
	
	private String loginName;
	private String loginPassword;
	
	// all JavaFX UI stuff. MAKE NEW ONES HERE
	Label text = new Label();
	TextField msgInput = new TextField();
	Button send = new Button();
	TextField enterNameField = new TextField();
	Text namePrompt = new Text();
	PasswordField enterPasswordField = new PasswordField();
	Text passwordPrompt = new Text();
	Button signIn = new Button();
	Button registerBtn = new Button();
	Button logoutBtn = new Button();
	Text loginError = new Text();
	Pane pane = new Pane();
	TabPane tabPane = new TabPane();
	Tab globalTab = new Tab("Global");
	TextArea globalTextArea = new TextArea();
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		ClientMain client = new ClientMain();
		
		//ensures that a client logs out when disconnected from the server
		Platform.setImplicitExit(true);
		primaryStage.setOnCloseRequest((ae) -> {
			try {
				client.writer.writeObject("/LOGOUT " + userNum);
				client.writer.flush();
			} catch (Exception e1) {
			}
			Platform.exit();
			System.exit(0);
		});
		
		// set up JavaFX window
		pane.setBackground(new Background(new BackgroundFill(Color.gray(0.175), CornerRadii.EMPTY, Insets.EMPTY)));
		
		// area for text
		//Label text = new Label();
		text.setPrefWidth(350);
		text.setLayoutX(0);
		text.setLayoutY(680);
		text.setTextFill(Color.WHITE);
		pane.getChildren().add(text);
		text.setVisible(false);
		
		// box to input message
		//TextField msgInput = new TextField();
		msgInput.setPromptText("Enter your message here");
		msgInput.setPrefWidth(700);
		msgInput.setPrefHeight(40);
		msgInput.setLayoutX(0);
		msgInput.setLayoutY(640);
		msgInput.setStyle("-fx-background-color: #d3d3d3; -fx-font: 14 arial");
		pane.getChildren().add(msgInput);
		msgInput.setVisible(false);
		
		// button to send message
		//Button send = new Button();
		send.setText("Send");
		send.setLayoutX(710);
		send.setLayoutY(640);
		send.setPrefHeight(40);
		send.setPrefWidth(70);
		send.setStyle("-fx-font: 14 arial; -fx-base: #a9a9a9");
		pane.getChildren().add(send);
		send.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	try {
		    		// TODO
		    		//client.writer.println(msgInput.getText());
		    		Message outgoing = createMessage();
		    		if (outgoing != null) {
		    			client.writer.writeObject(createMessage());
		    			client.writer.flush();
		    			msgInput.setText("");
		    			text.setText("Message sent.");
		    		}
	    		} catch (Exception f){
	    			text.setText("Error sending message!");
	    		}
		    }
		});
		send.setVisible(false);
		
		logoutBtn.setText("Logout");
		logoutBtn.setLayoutX(900);
		logoutBtn.setLayoutY(640);
		logoutBtn.setPrefHeight(40);
		logoutBtn.setPrefWidth(70);
		logoutBtn.setStyle("-fx-font: 14 arial; -fx-base: #a9a9a9");
		pane.getChildren().add(logoutBtn);
		logoutBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				for (Iterator<HashMap.Entry<Integer, Tab>> it = tabs.entrySet().iterator(); it.hasNext(); ) {
					Map.Entry<Integer, Tab> entry = it.next();
					if (entry.getKey() != 0) {
						tabPane.getTabs().remove(entry.getValue());
						it.remove();
						
					}
				}
				//for (HashMap.Entry<Integer, Tab> t : tabs.entrySet()) {
					//if (t.getKey() != 0) 
				//		tabPane.getTabs().remove(t.getValue());
				//}
				//tabs.clear();
				//tabPane.getTabs().add(globalTab);
				//tabs.put(0, globalTab);
				
				openLoginScreen();
				try {
					client.writer.writeObject("/LOGOUT " + userNum);
					client.writer.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		logoutBtn.setVisible(false);
		
		//TextField enterNameField = new TextField();
		enterNameField.setPrefWidth(200);
		enterNameField.setLayoutX(400);
		enterNameField.setLayoutY(300);
		enterNameField.setStyle("-fx-background-color: #d3d3d3; -fx-font: 14 arial");
		pane.getChildren().add(enterNameField);
		//Text namePrompt = new Text();
		namePrompt.setLayoutX(335);
		namePrompt.setLayoutY(320);
		namePrompt.setFont(Font.font("Verdana", 18));
		namePrompt.setFill(Color.WHITE);
		namePrompt.setText("Name: ");
		pane.getChildren().add(namePrompt);
		
		//PasswordField enterPasswordField = new PasswordField();
		enterPasswordField.setPrefWidth(200);
		enterPasswordField.setLayoutX(400);
		enterPasswordField.setLayoutY(340);
		enterPasswordField.setStyle("-fx-background-color: #d3d3d3; -fx-font: 14 arial");
		pane.getChildren().add(enterPasswordField);
		//Text passwordPrompt = new Text();
		passwordPrompt.setLayoutX(305);
		passwordPrompt.setLayoutY(360);
		passwordPrompt.setFont(Font.font("Verdana", 18));
		passwordPrompt.setFill(Color.WHITE);
		passwordPrompt.setText("Password: ");
		pane.getChildren().add(passwordPrompt);
		
		//error when logging in or registering
		loginError.setLayoutX(350);
		loginError.setLayoutY(380);
		loginError.setFill(Color.RED);
		pane.getChildren().add(loginError);
		
		//Button signIn = new Button();
		signIn.setText("Sign In");
		signIn.setPrefWidth(100);
		signIn.setPrefHeight(20);
		signIn.setStyle("-fx-font: 14 arial; -fx-base: #a9a9a9");
		signIn.setLayoutX(430);
		signIn.setLayoutY(390);
		pane.getChildren().add(signIn);
		signIn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				try {
					String name = enterNameField.getText();
					String password = enterPasswordField.getText();
					if (!name.equals("") && !password.equals("")) {
						if (name.contains(" ") || password.contains(" ")) {
							loginError.setText("Your username or password may not contain spaces.");
						} else {
							loginError.setText("");
							temp4User = name;
							loginName = name;
							loginPassword = password;
							client.writer.writeObject("/getData userstring " + name);
							//client.writer.writeObject("/SIGNIN " + name + " " + password);
							client.writer.flush();
							enterNameField.clear();
							enterPasswordField.clear();
						}
					}
				} catch (IOException f) {
					f.printStackTrace();
				}
			}
			
		});
		
		//Button registerBtn = new Button();
		registerBtn.setText("Sign Up");
		registerBtn.setPrefWidth(100);
		registerBtn.setPrefHeight(20);
		registerBtn.setLayoutX(430);
		registerBtn.setLayoutY(420);
		registerBtn.setStyle("-fx-font: 14 arial; -fx-base: #a9a9a9");
		pane.getChildren().add(registerBtn);
		registerBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				try {
					String name = enterNameField.getText();
					String password = enterPasswordField.getText();
					if (!name.equals("") && !password.equals("")) {
						if (name.contains(" ") || password.contains(" ")) {
							loginError.setText("Your username or password may not contain spaces.");
						} else {
							loginError.setText("");
							client.writer.writeObject("/SIGNUP " + name + " " + password);
							client.writer.flush();
							enterNameField.clear();
							enterPasswordField.clear();
						}
					}
				} catch (IOException f) {
					f.printStackTrace();
				}
			}
			
		});
		
		// sets up connection with server
		try {
			@SuppressWarnings("resource")
			Socket socket = new Socket(InetAddress.getLocalHost().getHostAddress(), 5000);
			//Socket socket = new Socket("2602:302:d1bc:fbd0:ccfa:2818:aca0:9ac4", 5000);
			client.reader = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
			client.writer = new ObjectOutputStream(socket.getOutputStream());
			System.out.println("connected");
			new Thread(new Runnable(){
				@Override
				public void run() {
					Object message;
					try {
						while ((message = client.reader.readObject()) != null) {
							/*
							 * If the client receives a chatroom, a tab is created for said 
							 * chatroom so that messages can be sent/received through it. 
							 * Chatroom receives a generic name that can be changed later, with
							 * the exception of private messages, which share a name with the user
							 * being messaged.
							 */
							if (message instanceof Chatroom) {
								Chatroom cr = (Chatroom) message;
								Tab crTab = tabs.get(cr.getChatroomNum());
								if (crTab == null) {
									Tab tab = new Tab();
									TextArea textArea = new TextArea();
									textArea.setPrefWidth(1000);
									textArea.setPrefHeight(600);
									textArea.setStyle("-fx-control-inner-background: #555555; -fx-font-family: verdana; -fx-text-fill: #e5e4e2");
									textArea.setEditable(false);
									tab.setContent(textArea);
									//tab.setText(cr.getName());
									if (!cr.isPM()) {
										tab.setText(cr.getName());
									} else {
										temp4User = cr;
										client.writer.writeObject("/getData user " 
												+ cr.otherMember(userNum));
										client.writer.flush();
									}
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											tabs.put(cr.getChatroomNum(), tab);
											tabPane.getTabs().add(tab);
										}
									});
									crTab = tabs.get(cr.getChatroomNum());
								} else {
									final Tab crTab_ = crTab;
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											try {
												if (!cr.isPM()) {
													crTab_.setText(cr.getName());
												} else {
													temp4User = cr;
													client.writer.writeObject("/getData user "
															+ cr.otherMember(userNum));
													client.writer.flush();
												}
											} catch (IOException e) {
												e.printStackTrace();
											}
												
										}
									});
								}
								if (temp != null) {
									Message msg = temp;
									temp = null;
									Tab tab = tabs.get(msg.getChatroomNum());
									if (tab == null) {
										temp = msg;
										client.writer.writeObject("/getData chatroom " + msg.getChatroomNum());
										client.writer.flush();
									} else {
										temp4User = msg;
										client.writer.writeObject("/getData user " + msg.getUserNum());
										client.writer.flush();
									}
								}
								/*
								 * When a message is sent, the client requests appropriate data in order
								 * to properly display the message. 
								 */
							} else if (message instanceof Message) {
								Message msg = (Message) message;
								Tab tab = tabs.get(msg.getChatroomNum());
								if (tab == null) {
									temp = msg;
									client.writer.writeObject("/getData chatroom " + msg.getChatroomNum());
									client.writer.flush();
								} else {
									temp4User = msg;
									client.writer.writeObject("/getData user " + msg.getUserNum());
									client.writer.flush();
								}
								/*
								 * User data is requested in order to either display a message properly,
								 * allow the client data to update appropriately for logins, or set up
								 * a private message with another user.
								 */
							} else if (message instanceof User) {
								User user_ = (User) message;
								if (temp4User != null && temp4User instanceof Message) {
									Message msg =  (Message) temp4User;
									temp4User = null;
									if (user_.getUserNum() == msg.getUserNum()) {
										String mesg = user_.getName() + ": " + msg.getMsg();
										TextArea ta = (TextArea) tabs.get(msg.getChatroomNum()).getContent();
										//ta.appendText("\n" + mesg);
										ta.appendText(mesg + "\n");
									}
								} else if (temp4User != null && temp4User instanceof String) {
									String nm = (String) temp4User;
									temp4User = null;
									if (user_.getName().equals(nm)) {
										userNum = user_.getUserNum();
										user = new User(userNum, user_.getName());
										client.writer.writeObject("/SIGNIN " + loginName + " " + loginPassword);
										client.writer.flush();
									}
								} else if (temp4User != null && temp4User instanceof Chatroom) {
									Chatroom cr = (Chatroom) temp4User;
									temp4User = null;
									final Tab crTab = tabs.get(cr.getChatroomNum());
									String userNm = user_.getName();
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											if (crTab != null && user_ != null) crTab.setText(userNm);
										}
									});
								}
							} else if (message instanceof String) {
								String msg = (String) message; 
								processString(msg);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		tabPane.setMinWidth(560);
		tabPane.setMinHeight(100);
		pane.getChildren().add(tabPane);
		//adding global chatroom right off the bat
		//Tab globalTab = new Tab("Global");
		globalTextArea.setPrefWidth(1000);
		globalTextArea.setPrefHeight(600);
		globalTextArea.setStyle("-fx-control-inner-background: #555555; -fx-font-family: verdana; -fx-text-fill: #e5e4e2");
		globalTextArea.setEditable(false);
		globalTab.setContent(globalTextArea);
		tabPane.getTabs().add(globalTab);
		tabs.put(0, globalTab);
		tabPane.setVisible(false);
		
		primaryStage.setScene(new Scene(pane, 1000, 700));
		primaryStage.show();
	}
	/**
	 * This method processes confirmations sent by the server after the client requests
	 * something of it. It either updates user info upon registration/login, or displays
	 * error messages if something went wrong on the server side.
	 * @param message sent by the server
	 */
	private void processString(String message) {
		String[] split_msg = ((String)message).split(" ");
		if (split_msg[0].equals("registered")) {
			user = new User(Integer.parseInt(split_msg[1]), split_msg[2]);
			userNum = user.getUserNum();
			closeLoginScreen();
		} else if (split_msg[0].equals("logged-in")) {
			user = new User(Integer.parseInt(split_msg[1]), split_msg[2]);
			userNum = user.getUserNum();
			closeLoginScreen();
		} else if (split_msg[0].equals("name-taken")){
			loginError.setText("That username is taken. Please try again.");
		} else if (split_msg[0].equals("name-not-found")){
			loginError.setText("Username not recognized. Please try again.");
		} else if (split_msg[0].equals("already-online")) {
			loginError.setText("This user is already online. Please try again.");
		} else if (split_msg[0].equals("wrong-password")) {
			loginError.setText("Password is incorrect. Please try again.");
		} else {
			System.out.println("String input not recognized as command. Fix later.");
		}
	}
	/**
	 * This closes the login screen and opens the chat UI after a client logs in.
	 */
	public void closeLoginScreen() {
		registerBtn.setVisible(false);
		signIn.setVisible(false);
		enterPasswordField.setVisible(false);
		enterNameField.setVisible(false);
		namePrompt.setVisible(false);
		passwordPrompt.setVisible(false);
		send.setVisible(true);
		msgInput.setVisible(true);
		text.setVisible(true);
		logoutBtn.setVisible(true);
		tabPane.setVisible(true);
		loginError.setText("");
		loginError.setVisible(false);
	}
	/**
	 * Brings back the login screen after a client logs out.
	 */
	public void openLoginScreen() {
		registerBtn.setVisible(true);
		signIn.setVisible(true);
		enterPasswordField.setVisible(true);
		enterNameField.setVisible(true);
		namePrompt.setVisible(true);
		passwordPrompt.setVisible(true);
		send.setVisible(false);
		msgInput.setVisible(false);
		text.setVisible(false);
		logoutBtn.setVisible(false);
		loginError.setText("");
		loginError.setVisible(true);
		tabPane.setVisible(false);
	}
	/**
	 * This creates a message to be sent in the form of a Message object.
	 * It uses the user-input string as input.
	 * @return a Message, or null if the user has not input anything
	 */
	public Message createMessage() {
		int currentChatroom = 0;
		Tab tab = tabPane.getSelectionModel().getSelectedItem();
		for (HashMap.Entry<Integer, Tab> t : tabs.entrySet()) {
			if (tab.equals(t.getValue())) {
				currentChatroom = t.getKey();
				break;
			}
		}
		if (!msgInput.getText().equals(""))
			return new Message(currentChatroom, userNum, msgInput.getText());
		else return null;
	}
	@SuppressWarnings("unused")
	private void removeTab(int chatroom) {
		tabPane.getTabs().remove(tabs.get(chatroom));
		tabs.remove(chatroom);
	}
}
