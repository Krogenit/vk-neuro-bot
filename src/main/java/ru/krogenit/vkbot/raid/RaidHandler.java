package ru.krogenit.vkbot.raid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiAccessException;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.messages.Dialog;
import com.vk.api.sdk.objects.messages.responses.GetDialogsResponse;

import ru.krogenit.vkbot.CommandHandler;
import ru.krogenit.vkbot.Main;
import ru.krogenit.vkbot.chat.ChatHandler;
import ru.krogenit.vkbot.chat.VkChat;
import ru.krogenit.vkbot.user.VkUser;

public class RaidHandler {
	public static RaidHandler INSTANCE;
	public static final int BACKUP_BOT_ID = 398503537;

	public VkApiClient vk;
	public UserActor actor;

	public VkApiClient backupVk;
	public UserActor backupActor;

	public HashMap<Integer, Raid> raids = new HashMap<Integer, Raid>();
	public HashMap<Integer, Raid> prepareRaids = new HashMap<Integer, Raid>();
	public List<Integer> chatsCantRaid = new ArrayList<Integer>();
	public List<Integer> saveRaid = new ArrayList<Integer>();

	public RaidHandler(VkApiClient vk, UserActor actor) {
		this.vk = vk;
		this.actor = actor;
		INSTANCE = this;

		TransportClient transportClient = HttpTransportClient.getInstance();
		backupVk = new VkApiClient(transportClient);
		UserAuthResponse authResponse;

		//authResponse = backupVk.oauth().userAuthorizationCodeFlow(Main.APP_ID, Main.APP_KEY, "https://oauth.vk.com/blank.html", Main.BACKUP_BOT_KEY).execute();
		backupActor = new UserActor(BACKUP_BOT_ID, Main.BACKUP_BOT_KEY);//authResponse.getUserId(), authResponse.getAccessToken());

		loadRaids();
	}

	private void loadRaids() {
		try {

			BufferedReader r = new BufferedReader(new FileReader(new File("raids", "raids.txt")));
			BufferedReader r1 = new BufferedReader(new FileReader(new File("raids", "notraids.txt")));

			int size = Integer.parseInt(r.readLine());

			for (int i = 0; i < size; i++) {
				int id = Integer.parseInt(r.readLine());
				saveRaid.add(id);
			}
			r.close();
			size = Integer.parseInt(r1.readLine());

			for (int i = 0; i < size; i++) {
				int id = Integer.parseInt(r1.readLine());
				chatsCantRaid.add(id);
			}
			r1.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveRaids() {
		try {
			FileWriter f = new FileWriter(new File("raids", "raids.txt"));
			FileWriter f1 = new FileWriter(new File("raids", "notraids.txt"));
			Iterator<Integer> iter = raids.keySet().iterator();
			f.write(raids.size() + "\n");
			while (iter.hasNext()) {
				Integer key = iter.next();
				Raid r = raids.get(key);
				f.write(r.chatId + "\n");
			}

			f.close();

			f1.write(chatsCantRaid.size() + "\n");
			for (Integer i : chatsCantRaid) {
				f1.write(i.intValue() + "\n");
			}

			f1.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startRaid(String message, int userId, int chatid) throws Exception {
		CommandHandler cmd = CommandHandler.INSTANCE;
		String[] args = message.split(" ");
		if (args.length > 1) {
			try {
				int chatId = Integer.parseInt(args[1]);
				if (prepareRaids.containsKey(chatId)) {
					Raid r = prepareRaids.remove(chatId);
					INSTANCE.raids.put(chatId, r);
					try {
						try {
							vk.messages().removeChatUser(actor, chatId, BACKUP_BOT_ID + "").execute();
						} catch (Exception e) {

						}
						vk.messages().addChatUser(actor, chatId, BACKUP_BOT_ID).execute();
					} catch (ApiAccessException e) {
						cmd.sender.sendMessage("блядь кикнули с чата", chatid, 0, userId, false);
						return;
					} catch (Exception e) {
						cmd.sender.sendMessage("ошибка добавления бота в чат " + e.getMessage(), chatid, 0, userId, false);
						return;
					}

					try {
						GetDialogsResponse c = backupVk.messages().getDialogs(backupActor).unread(true).execute();
						List<Dialog> dialogs = c.getItems();
						for (Dialog d : dialogs) {
							if (d != null && d.getMessage() != null && d.getMessage().getChatId() != 0) {
								int id = d.getMessage().getChatId();
								try {
									backupVk.messages().removeChatUser(backupActor, id, BACKUP_BOT_ID + "").execute();
									r.backupChatid = id;
								} catch (Exception e) {

								}
							}
						}	
					} catch(Exception e) {
						e.printStackTrace();
						cmd.sender.sendMessage("ошибка получения диалогов " + e.getMessage(), chatid, 0, userId, false);
						return;
					}

					saveRaids();
					cmd.sender.sendMessage("рейд начат", chatid, 0, userId, false);
					addMembers(chatId, r);
				} else {
					cmd.sender.sendMessage("рейд не подготовлен, рейдсоздать <айди чата>", chatid, 0, userId, false);
				}
			} catch (NumberFormatException e) {
				cmd.sender.sendMessage("рейдсоздать <айди чата>", chatid, 0, userId, false);
			}
		} else {
			cmd.sender.sendMessage("рейдстарт <айди чата>", chatid, 0, userId, false);
		}
	}

	private void addMembers(int chatId, Raid r) throws Exception {
		CommandHandler cmd = CommandHandler.INSTANCE;
		cmd.sender.sendMessage("ЗОХВАТ КОНФЫ", chatId, 0, 0, false);

		for (Integer i : r.members) {
			try {
				vk.messages().addChatUser(actor, chatId, i).execute();
			} catch (Exception e) {

			}
		}
	}

	private void createRaid(String message, int userId, int chatid) throws Exception {
		CommandHandler cmd = CommandHandler.INSTANCE;
		String[] args = message.split(" ");
		if (args.length > 1) {
			try {
				int chatId = Integer.parseInt(args[1]);
				if (chatsCantRaid.contains(chatId)) {
					cmd.sender.sendMessage("этот чат под защитой срана. сори.", chatid, 0, userId, false);
				} else if (raids.containsKey(chatId)) {
					// Raid r = raids.get(chatId);
					cmd.sender.sendMessage("рейд уже идет, рейдвступить " + chatId, chatid, 0, userId, false);
				} else if (prepareRaids.containsKey(chatId)) {
					// Raid r = prepareRaids.get(chatId);
					cmd.sender.sendMessage("рейд уже подготавливается, рейдвступить " + chatId, chatid, 0, userId, false);
				} else {
					VkChat chat1 = ChatHandler.INSTACNE.getChat(chatId);
					if (chat1 != null) {
						Raid r = new Raid(chatId, userId);
						prepareRaids.put(chatId, r);

						cmd.sender.sendMessage("рейд создан, вступить в рейд: рейдвступить " + chatId, chatid, 0, userId, false);
					} else {
						cmd.sender.sendMessage("чат не найден", chatid, 0, userId, false);
					}
				}
			} catch (NumberFormatException e) {
				cmd.sender.sendMessage("рейдсоздать <айди чата>", chatid, 0, userId, false);
			}
		} else {
			cmd.sender.sendMessage("рейдсоздать <айди чата>", chatid, 0, userId, false);
		}
	}

	private void raidList(int userId, int chatid) throws Exception {
		String out = "рейды в процессе: \n";

		Object[] keysSet = raids.keySet().toArray();
		if (keysSet.length > 50) {
			out += "10 рандомных рейдов в процессе: \n";
			List<Integer> already = new ArrayList<Integer>();
			Random rand = new Random();
			while (already.size() < 10) {
				int random = rand.nextInt(keysSet.length);
				Integer key = (Integer) keysSet[random];
				if (!already.contains(key)) {
					Raid r = raids.get(key);
					out += "id: " + r.chatId + " имя: " + ChatHandler.INSTACNE.getChat(r.chatId).title + ", юзеров: " + ChatHandler.INSTACNE.getChat(r.chatId).users.size() + ", участников рейда: " + r.members.size() + "\n";
					already.add(key);
				}
			}
		} else {
			Iterator<Integer> keys = raids.keySet().iterator();
			while (keys.hasNext()) {
				Integer key = keys.next();
				Raid r = raids.get(key);
				VkChat chat = ChatHandler.INSTACNE.getChat(r.chatId);
				String title = chat.title != null ? chat.title : "unknown";
				int userSize = chat.users != null ? chat.users.size() : 0;
				out += "id: " + r.chatId + " имя: " + title + ", юзеров: " + userSize
						+ ", участников рейда: " + r.members.size() + "\n";
			}
		}

		out += "\n";
		out += "готовятся: \n";

		keysSet = prepareRaids.keySet().toArray();
		if (keysSet.length > 50) {
			out += "10 рандомных рейдов на подготовке: \n";
			List<Integer> already = new ArrayList<Integer>();
			Random rand = new Random();
			while (already.size() < 10) {
				int random = rand.nextInt(keysSet.length);
				Integer key = (Integer) keysSet[random];
				if (!already.contains(key)) {
					Raid r = prepareRaids.get(key);
					out += "id: " + r.chatId + " имя: " + ChatHandler.INSTACNE.getChat(r.chatId).title + ", юзеров: " + ChatHandler.INSTACNE.getChat(r.chatId).users.size() + ", участников рейда: " + r.members.size() + "\n";
					already.add(key);
				}
			}
		} else {
			Iterator<Integer> keys = prepareRaids.keySet().iterator();
			while (keys.hasNext()) {
				Integer key = keys.next();
				Raid r = prepareRaids.get(key);
				out += "id: " + r.chatId + " имя: " + ChatHandler.INSTACNE.getChat(r.chatId).title + ", юзеров: " + ChatHandler.INSTACNE.getChat(r.chatId).users.size() + ", участников рейда: " + r.members.size() + "\n";
			}
		}

		out += "\n";
		out += "чаты на которые можно рейдить: \n";
		keysSet = ChatHandler.INSTACNE.chats.keySet().toArray();
		if (keysSet.length > 50) {
			out += "30 рандомных чатов: \n";
			List<Integer> already = new ArrayList<Integer>();
			Random rand = new Random();
			while (already.size() < 30) {
				int random = rand.nextInt(keysSet.length);
				Integer key = (Integer) keysSet[random];
				if (!already.contains(key)) {
					VkChat chat = ChatHandler.INSTACNE.chats.get(key);
					if (!chatsCantRaid.contains(chat.chatId) && !raids.containsKey(chat.chatId) && !prepareRaids.containsKey(chat.chatId)) {
						out += "id: " + chat.chatId + " имя: " + chat.title + ", юзеров: " + chat.users.size() + "\n";
						already.add(key);
					}
				}
			}
		} else {
			Iterator<Integer> keys = ChatHandler.INSTACNE.chats.keySet().iterator();
			while (keys.hasNext()) {
				Integer key = keys.next();
				VkChat chat = ChatHandler.INSTACNE.chats.get(key);
				if (!chatsCantRaid.contains(chat.chatId) && !raids.containsKey(chat.chatId) && !prepareRaids.containsKey(chat.chatId)) {
					out += "id: " + chat.chatId + " имя: " + chat.title + ", юзеров: " + chat.users.size() + "\n";
				}
			}
		}

		CommandHandler.INSTANCE.sender.sendMessage(out, chatid, 0, userId, false);
	}

	private void addMember(String message, int userId, int chatid) throws Exception {
		CommandHandler cmd = CommandHandler.INSTANCE;
		String[] args = message.split(" ");
		if (args.length > 1) {
			try {
				int chatId = Integer.parseInt(args[1]);
				if (prepareRaids.containsKey(chatId)) {
					Raid r = prepareRaids.get(chatId);
					if (!r.members.contains(userId)) r.members.add(userId);

					cmd.sender.sendMessage("добавил тебя в участники, рейдначать <айди чата>", chatid, 0, userId, false);
				} else if (raids.containsKey(chatId)) {
					Raid r = raids.get(chatId);
					if (!r.members.contains(userId)) r.members.add(userId);
					try {
						vk.messages().addChatUser(actor, chatId, userId).execute();
						cmd.sender.sendMessage("добавил тебя в участники и закинул в чат", chatid, 0, userId, false);
					} catch (Exception e) {
						cmd.sender.sendMessage("ты и так в чате, либо мя кикнули", chatid, 0, userId, false);
					}
				} else {
					cmd.sender.sendMessage("рейд не найден, рейдсоздать <айди чата>", chatid, 0, userId, false);
				}
			} catch (NumberFormatException e) {
				cmd.sender.sendMessage("рейдвступить <айди чата>", chatid, 0, userId, false);
			}
		} else {
			cmd.sender.sendMessage("рейдвступить <айди чата>", chatid, 0, userId, false);
		}
	}

	private void removeMember(String message, int userId, int chatid) throws Exception {
		CommandHandler cmd = CommandHandler.INSTANCE;
		String[] args = message.split(" ");
		if (args.length > 1) {
			try {
				int chatId = Integer.parseInt(args[1]);
				if (prepareRaids.containsKey(chatId)) {
					Raid r = prepareRaids.get(chatId);
					if (r.members.contains(userId)) r.members.remove(Integer.valueOf(userId));

					cmd.sender.sendMessage("удалил тебя из участников рейда", chatid, 0, userId, false);
				} else if (raids.containsKey(chatId)) {
					Raid r = raids.get(chatId);
					if (r.members.contains(userId)) r.members.remove(Integer.valueOf(userId));

					cmd.sender.sendMessage("удалил тебя из участников рейда", chatid, 0, userId, false);
				} else {
					cmd.sender.sendMessage("рейд не найден", chatid, 0, userId, false);
				}
			} catch (NumberFormatException e) {
				cmd.sender.sendMessage("рейдвступить <айди чата>", chatid, 0, userId, false);
			}
		} else {
			cmd.sender.sendMessage("рейдвступить <айди чата>", chatid, 0, userId, false);
		}
	}

	public void inviteBot(int chatId) throws Exception {
		if(chatId != 0) {
			try {
				if (raids.containsKey(chatId) || saveRaid.contains(chatId)) {
					backupVk.messages().addChatUser(backupActor, raids.get(chatId).backupChatid, BACKUP_BOT_ID).execute();
					backupVk.messages().addChatUser(backupActor, raids.get(chatId).backupChatid, Main.BOT_ID).execute();
					backupVk.messages().removeChatUser(backupActor, raids.get(chatId).backupChatid, "" + BACKUP_BOT_ID).execute();
					CommandHandler.INSTANCE.sender.sendMessage("саси жепу", chatId, 0, 0, false);
				} else {

				}
			} catch (ClientException | ApiException e) {
				System.out.println("[Raids] Не удалось добавить бота в конфу " + chatId);
				e.printStackTrace();
			}
		}
	}

	public void raidStop(int chatId) throws Exception {
		if (raids.containsKey(chatId) || saveRaid.contains(chatId)) {
			vk.messages().addChatUser(actor, chatId, BACKUP_BOT_ID).execute();
			backupVk.messages().removeChatUser(backupActor, raids.get(chatId).backupChatid, "" + BACKUP_BOT_ID).execute();
			CommandHandler.INSTANCE.sender.sendMessage("саси", chatId, 0, 0, false);
			// raids.remove(chatId);
		} else {

		}
	}

	public void raidStopByUser(String message, int userId, int chatid) throws Exception {
		CommandHandler cmd = CommandHandler.INSTANCE;
		String[] args = message.split(" ");
		if (args.length > 1) {
			try {
				int chatId = Integer.parseInt(args[1]);
				if (raids.containsKey(chatId) && raids.get(chatId).ownerId == userId) {
					raids.remove(chatId);
					cmd.sender.sendMessage("рейд остановлен", chatid, 0, userId, false);
				} else if (prepareRaids.containsKey(chatId) && prepareRaids.get(chatId).ownerId == userId) {
					prepareRaids.remove(chatId);
					cmd.sender.sendMessage("рейд остановлен", chatid, 0, userId, false);
				} else {
					cmd.sender.sendMessage("рейд не найден", chatid, 0, userId, false);
				}
			} catch (NumberFormatException e) {
				cmd.sender.sendMessage("рейдстоп <айди чата>", chatid, 0, userId, false);
			}
		} else {
			cmd.sender.sendMessage("рейдстоп <айди чата>", chatid, 0, userId, false);
		}
	}

	private void offRaid(int chatId, int userId) {
		this.chatsCantRaid.add(chatId);
		saveRaids();
	}

	public void onRaid(int chatId, int userId) {
		if (chatsCantRaid.contains(chatId)) this.chatsCantRaid.remove(Integer.valueOf(chatId));
		saveRaids();
	}

	public boolean checkCommands(String message, VkUser user, int chatId, int msgId, int userId) throws Exception {
		if(Main.BOT_BANNED) {
			return false;
		}
		
		if ((StringUtils.startsWithIgnoreCase(message, "рейдсписок") || StringUtils.startsWithIgnoreCase(message, "рейдлист")) && user.canCmd(true)) {
			this.raidList(userId, chatId);
		} else if (StringUtils.startsWithIgnoreCase(message, "рейдсоздать")) {
			this.createRaid(message, userId, chatId);
		} else if ((StringUtils.startsWithIgnoreCase(message, "рейдначать") || StringUtils.startsWithIgnoreCase(message, "рейдстарт")) && user.canCmd(true)) {
			this.startRaid(message, userId, chatId);
		} else if (StringUtils.startsWithIgnoreCase(message, "рейдвступить") && user.canCmd(true)) {
			this.addMember(message, userId, chatId);
		} else if (StringUtils.startsWithIgnoreCase(message, "рейдпокинуть") && user.canCmd(true)) {
			this.removeMember(message, userId, chatId);
		} else if (StringUtils.startsWithIgnoreCase(message, "рейдстоп") && user.canCmd(true)) {
			this.raidStopByUser(message, userId, chatId);
		} else if (StringUtils.startsWithIgnoreCase(message, "рейдотключить") && chatId != 0 && user.canCmd(true)) {
			this.offRaid(chatId, userId);
			CommandHandler.INSTANCE.sender.sendMessage("эта конфа под защитой срана", chatId, msgId, userId, false);
		} else if (StringUtils.startsWithIgnoreCase(message, "рейдвключить") && chatId != 0 && user.canCmd(true)) {
			this.onRaid(chatId, userId);
			CommandHandler.INSTANCE.sender.sendMessage("эта конфа может быть атакована", chatId, msgId, userId, false);
		} else {
			return false;
		}

		return true;
	}
}
