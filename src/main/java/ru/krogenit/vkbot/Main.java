package ru.krogenit.vkbot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.ConversationWithLastMessage;
import com.vk.api.sdk.objects.messages.LongpollMessages;
import com.vk.api.sdk.objects.messages.LongpollParams;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.MessageOld;
import com.vk.api.sdk.objects.messages.responses.GetConversationsResponse;
import com.vk.api.sdk.objects.messages.responses.GetHistoryResponse;
import com.vk.api.sdk.objects.messages.responses.GetLongPollHistoryResponse;
import com.vk.api.sdk.objects.wall.WallPostFull;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import com.vk.api.sdk.queries.wall.WallGetFilter;

import ru.krogenit.vkbot.callback.GroupCallback;
import ru.krogenit.vkbot.game.GameHandler;
import ru.krogenit.vkbot.notify.NotificationHandler;
import ru.krogenit.vkbot.raid.RaidHandler;
import ru.krogenit.vkbot.user.BlackList;

public class Main {
	public static final boolean isDebug = false;
	public static boolean isFromGroup = false;
	public static Main INSTANCE;

	public static VkApiClient vk;
	public static UserActor actor;
	public static GroupActor group;
	public GroupActor backupGroup;

	public static boolean isRun = true;
	public static final int APP_ID = 0;
	public static final String APP_KEY = "";
	public static final String GROUP_KEY = "";
	public static final String BACKUP_GROUP_KEY = "";
	public static final Integer GROUP_ID = 0;
	public static final Integer BACKUP_GROUP_ID = 1;
	
	
	MessageHandler msgHandler;
	NotificationHandler notifyHandler;
	RaidHandler raids;
	GameHandler game;

	private long lastNotifyUpdate;

	public static final int OWNER_ID = 0;
	public static final int BOT_ID = 0;
	private static final String BOT_KEY = "";
	public static final String BACKUP_BOT_KEY = "";
	public static boolean BOT_BANNED = false;
	
	public Main() {
		INSTANCE = this;
	}
	
	public void run() {
		TransportClient transportClient = HttpTransportClient.getInstance();
		vk = new VkApiClient(transportClient);

		actor = new UserActor(BOT_ID, BOT_KEY);
		group = new GroupActor(GROUP_ID, GROUP_KEY);
		backupGroup = new GroupActor(BACKUP_GROUP_ID, BACKUP_GROUP_KEY);

		raids = new RaidHandler(vk, actor);
		WhiteList.load();
		BlackList.load();

		start();
	}

	public void start() {
		msgHandler = new MessageHandler(vk, actor, group);
		notifyHandler = new NotificationHandler(vk, actor);
		System.out.println("Loading game");
		game = new GameHandler();
		game.load();
		System.out.println("Game loaded");

		System.out.println("Creating group callback");
		GroupCallback callback = new GroupCallback(vk, group, actor);
		System.out.println("Checking ls...");
		synchronized (CommandHandler.INSTANCE.neuro.look) {
			if(!BOT_BANNED) msgHandler.checkLs();
		try {
			callback.run();
			System.out.println("Group callback started");
			System.out.println("Getting suggested posts in group...");
			GetResponse resp = vk.wall().get(actor).ownerId(-GROUP_ID).filter(WallGetFilter.SUGGESTS).count(100).execute();
			
			List<WallPostFull> posts = resp.getItems();
			
			for(WallPostFull post : posts) {
				callback.wallPostNew(-GROUP_ID, post);
			}
			
			int dialogsOffset = 0;
			isFromGroup = true;
			while(true) {
				System.out.println("Getting new messages for group...");
				GetConversationsResponse response = vk.messages().getConversations(group).filter("unread").offset(dialogsOffset).groupId(GROUP_ID).count(200).execute();
				
				List<ConversationWithLastMessage> dialogs = response.getItems();
				if(dialogs.size() == 0) break;
				for(ConversationWithLastMessage d : dialogs) {
					Message msg = d.getLastMessage();
					if(msg.getUserId() != 0 && msg.getChatId() == 0) {
						MessageHandler.INSTANCE.processMessage(msg, false);
						//GetHistoryResponse resp1 = vk.messages().getHistory(group).peerId(msg.getUserId()).count(1).groupId(GROUP_ID).execute();
						
						//List<Message> msgs = resp1.getItems();
						//List<Integer> msgIds = new ArrayList<Integer>();
//						Integer startMessageId = null;
//						for(int i=msgs.size()-1;i>=0;i--) {
//							Message msg1 = msgs.get(i);
//							if(msg1.getReadState() != null && msg1.getReadState() == 0) {
//								MessageHandler.INSTANCE.processMessage(msg1, false);
//								if(startMessageId == null)
//									startMessageId = msg1.getId();
//								//msgIds.add(msg1.getId());
//							}
//						}
						
//						GetByConversationMessageIdResponse resp2 = vk.messages().getByConversationMessageId(group).peerId(msg.getUserId()).conversationMessageIds(msgIds).execute();
//						List<Message> newMsgs = resp2.getItems();
//						for(int i=newMsgs.size()-1;i>=0;i--) {
//							Message msg1 = newMsgs.get(i);
//							callback.messageNew(-GROUP_ID, msg1);
//						}
//						if(startMessageId != null ) {
//							vk.messages().markAsRead(group).startMessageId(startMessageId).execute();
//							
//						}
					}
				}
				dialogsOffset += 200;
			}
		} catch (ClientException | ApiException e1) {
			e1.printStackTrace();
		}
		}
		String server = "";
		String key = "";
		Integer ts = null;
		Integer pts = null;
		
		if(!BOT_BANNED) {
		System.out.println("Connecting to long poll server");

		try {
			LongpollParams longpoll = vk.messages().getLongPollServer(actor).lpVersion(3).needPts(true).execute();
			
			ts = longpoll.getTs();
			server = longpoll.getServer();
			key = longpoll.getKey();
			pts = longpoll.getPts();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Connected.");
		}
		
		isFromGroup = false;
		long lastLsCheck = System.currentTimeMillis();

		while (isRun) {
			long time = System.currentTimeMillis();

			try {
				System.out.println("Getting new messages...");
				if(!BOT_BANNED) {
					GetLongPollHistoryResponse resp = vk.messages().getLongPollHistory(actor).ts(ts).pts(pts).execute();
				
				pts = resp.getNewPts();

				LongpollMessages longpollmessages = resp.getMessages();
				List<MessageOld> messages = longpollmessages.getMessages();

				synchronized (CommandHandler.INSTANCE.neuro.look) {
					for (MessageOld message : messages) {
						msgHandler.processMessage(message, true);
					}

				if (time - lastNotifyUpdate > 1 * 60 * 1000) {
					System.out.println("Checking notifications...");
					lastNotifyUpdate = time;
					notifyHandler.processNotifications();
					//msgHandler.cmd.saver.save();
				}
				
				isFromGroup = true;
				//System.out.print("Checking group...");
				callback.update();
				//System.out.println(" Group checked");
				isFromGroup = false;
				
				if (time - lastLsCheck > 10 * 60 * 1000) {
					lastLsCheck = time;
					//msgHandler.checkLs();
					//System.out.println("Saving game...");
					game.save();
					
					try {
						System.out.println("Checking suggesting posts...");
						GetResponse resp1 = vk.wall().get(actor).ownerId(-GROUP_ID).filter(WallGetFilter.SUGGESTS).count(100).execute();
						
						List<WallPostFull> posts = resp1.getItems();
						
						for(WallPostFull post : posts) {
							callback.wallPostNew(Integer.valueOf(-GROUP_ID), post);
						}
//						List<Integer> readedIds = new ArrayList<Integer>();
						int dialogsOffset = 0;
						isFromGroup = true;
						System.out.println("Checking group messages...");
						while(true) {
							GetConversationsResponse response = vk.messages().getConversations(group).filter("unread").offset(dialogsOffset).groupId(GROUP_ID).count(200).execute();
							
							List<ConversationWithLastMessage> dialogs = response.getItems();
							if(dialogs.size() == 0) break;
							for(ConversationWithLastMessage d : dialogs) {
								Message msg = d.getLastMessage();
								if(msg.getUserId() != 0 && msg.getChatId() == 0) {
									MessageHandler.INSTANCE.processMessage(msg, false);
//									readedIds.add(msg.getId());
									try {
										vk.messages().markAsRead(group).peerId(msg.getPeerId()+"").execute();
									} catch(Exception e) {
										e.printStackTrace();
									}
								}
							}
							dialogsOffset += 200;
						}
						isFromGroup = false;
//						if(readedIds.size() > 0) {
//							
//						}
					} catch(Exception e) {
						e.printStackTrace();
						isFromGroup = false;
					}
				}
				
				//System.out.println("Updating game...");
				game.update(time);

				// msgHandler.update();
				// messageSender.sendMessages();
				ImageDistortionQueue.INSTANCE.processQueue();
				} }
				else {
					synchronized (CommandHandler.INSTANCE.neuro.look) {
						isFromGroup = true;
						//System.out.print("Checking group...");
						callback.update();
						//System.out.println(" Group checked");
						isFromGroup = false;
						
						if (time - lastLsCheck > 10 * 60 * 1000) {
							lastLsCheck = time;
							//msgHandler.checkLs();
							//System.out.println("Saving game...");
							game.save();
							
							try {
								if(!BOT_BANNED) {
								System.out.println("Checking suggesting posts...");
								GetResponse resp1 = vk.wall().get(actor).ownerId(-GROUP_ID).filter(WallGetFilter.SUGGESTS).count(100).execute();
								
								List<WallPostFull> posts = resp1.getItems();
								
								for(WallPostFull post : posts) {
									callback.wallPostNew(Integer.valueOf(-GROUP_ID), post);
								}
//								List<Integer> readedIds = new ArrayList<Integer>();
								}
								int dialogsOffset = 0;
								isFromGroup = true;
								System.out.println("Checking group messages...");
								while(true) {
									GetConversationsResponse response = vk.messages().getConversations(group).filter("unread").offset(dialogsOffset).groupId(GROUP_ID).count(200).execute();
									
									List<ConversationWithLastMessage> dialogs = response.getItems();
									if(dialogs.size() == 0) break;
									for(ConversationWithLastMessage d : dialogs) {
										Message msg = d.getLastMessage();
										if(msg.getUserId() != 0 && msg.getChatId() == 0) {
											MessageHandler.INSTANCE.processMessage(msg, false);
//											readedIds.add(msg.getId());
											try {
												vk.messages().markAsRead(group).peerId(msg.getPeerId()+"").execute();
											} catch(Exception e) {
												e.printStackTrace();
											}
										}
									}
									dialogsOffset += 200;
								}
								isFromGroup = false;
//								if(readedIds.size() > 0) {
//									
//								}
							} catch(Exception e) {
								e.printStackTrace();
								isFromGroup = false;
							}
						}
						
						//System.out.println("Updating game...");
						game.update(time);

						// msgHandler.update();
						// messageSender.sendMessages();
						ImageDistortionQueue.INSTANCE.processQueue();
					}
				}
			} catch (ApiException e) {
				e.printStackTrace();
			} catch (ClientException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
//		try {
			CommandHandler.INSTANCE.neuro.isRunning = false;
//			CommandHandler.INSTANCE.neuro.saveModel();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		game.save();
	}

	private boolean scanHistory(MessageHandler msgHandler) throws ApiException, ClientException, IOException {
		int size = 200;
		int offset = -643810;
		long time = System.currentTimeMillis();
		long workTime = 5 * 60 * 1000;

		GetHistoryResponse resp1 = vk.messages().getHistory(actor).offset(offset).count(size).peerId(2000000094).startMessageId(offset + size).rev(false).execute();
		List<Message> items = resp1.getItems();

		if (items == null || items.size() == 0) {
			System.out.println("Закончились сообщения продожить работу?");
			Scanner scanner = new Scanner(System.in);
			String anwser = scanner.nextLine();
			if (anwser.equals("d")) {
				time = System.currentTimeMillis();
				msgHandler.cmd.saver.save();
			} else {
				msgHandler.cmd.saver.save();
				msgHandler.cmd.saver.stop();

				FileWriter fw = new FileWriter(new File(".", "last.txt"));
				fw.write("" + offset);
				fw.flush();
				fw.close();
				return false;
			}
		}

		for (int i = items.size() - 1; i >= 0; i--) {
			Message msg = items.get(i);
			msgHandler.processMessage(msg, false);
		}

		offset -= size;

		System.out.println(workTime - (System.currentTimeMillis() - time));

		if (System.currentTimeMillis() - time > workTime) {
			System.out.println("Продожить работу?");
			Scanner scanner = new Scanner(System.in);
			String anwser = scanner.nextLine();
			if (anwser.equals("d")) {
				time = System.currentTimeMillis();
				msgHandler.cmd.saver.save();
			} else {
				msgHandler.cmd.saver.save();
				msgHandler.cmd.saver.stop();

				FileWriter fw = new FileWriter(new File(".", "last.txt"));
				fw.write("" + offset);
				fw.flush();
				fw.close();
				return false;
			}
		}

		return true;
	}

	public static void main(String[] args) {
		new Main().run();
	}
}