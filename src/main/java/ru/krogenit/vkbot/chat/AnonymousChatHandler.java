package ru.krogenit.vkbot.chat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.objects.audio.Audio;
import com.vk.api.sdk.objects.docs.Doc;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.MessageAttachment;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.objects.video.Video;

import ru.krogenit.vkbot.CommandHandler;
import ru.krogenit.vkbot.user.VkUser;

public class AnonymousChatHandler {

	public static AnonymousChatHandler INSTANCE;
	VkApiClient vk;
	UserActor actor;
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	Random rand = new Random();

	public class Dialog {
		public int userId;
		public long lastMsgDate;

		public Dialog(int userId, long lastMsgDate) {
			this.userId = userId;
			this.lastMsgDate = lastMsgDate;
		}

	}

	HashMap<Integer, List<Dialog>> dialogs = new HashMap<Integer, List<Dialog>>();
	HashMap<Integer, Integer> userAndAnon = new HashMap<Integer, Integer>();

	public AnonymousChatHandler(VkApiClient vk, UserActor actor) {
		this.vk = vk;
		this.actor = actor;
		INSTANCE = this;
		loadDialogs();
	}

	public boolean processMsg(Message msg, String message, VkUser user, int chatId, int msgId, int userId) throws Exception {
		Integer value = Integer.valueOf(userId);
		if (userAndAnon.containsKey(value) && chatId == 0) {
			Integer anonId = userAndAnon.get(value);
			int date = msg.getDate();

			List<Dialog> userDialogs = dialogs.get(anonId);
			if (userDialogs != null) for (Dialog dialog : userDialogs) {
				if (dialog.userId == userId) {
					long time = System.currentTimeMillis();
					if (dialog.lastMsgDate != 0 && time - dialog.lastMsgDate > 1000 * 60 * 30) {
						while (userDialogs.contains(dialog))
							userDialogs.remove(dialog);
						userAndAnon.remove(value);
						if (userDialogs.size() > 0) dialogs.put(userId, userDialogs);
						else dialogs.remove(userId);
						saveDialogs();
						// CommandHandler.INSTANCE.sender.sendMessageWithForwards("диалог с "
						// + userId + " остановлен", 0, msgId, anonId,
						// ""+msg.getId());
					} else {
						dialog.lastMsgDate = time;
						CommandHandler.INSTANCE.sender.sendMessage("", 0, msgId, anonId, false, null, "" + msg.getId());
					}
					return true;
				}
			}
		}

		return false;
	}

	public boolean checkCommands(Message msg, String message, VkUser user, int chatId, int msgId, int userId) throws Exception {
		if (StringUtils.startsWithIgnoreCase(message, "напиши") && user.canCmd(true)) {
			String[] args = message.split(" ");
			if (args.length > 1) {
				int id = 0;
				String domain = "";
				try {
					try {
						args[1] = args[1].replace("id", "");
						id = Integer.parseInt(args[1]);
					} catch (Exception e) {
						domain = args[1];
						// CommandHandler.INSTANCE.sender.sendMessage("напиши <айди юзера(цифры)> <смска>",
						// chatId, msgId, userId, false);return true;
					}

					String out = "";

					if (id == userId) {
						CommandHandler.INSTANCE.sender.sendMessage("ты ебнутый сам себе в лс писать?", chatId, msgId, userId, false);
						return true;
					}

					if (args.length > 2) {
						out = args[2];
						for (int i = 3; i < args.length; i++)
							out += (" " + args[i]);
					}

					List<MessageAttachment> atts = msg.getAttachments();
					List<String> outAtts = new ArrayList<String>();

					if (atts != null) for (MessageAttachment att : atts) {
						if (att.getPhoto() != null) {
							Photo item = att.getPhoto();
							outAtts.add("photo" + item.getOwnerId() + "_" + item.getId() + "_" + item.getAccessKey());
						} else if (att.getVideo() != null) {
							Video item = att.getVideo();
							outAtts.add("video" + item.getOwnerId() + "_" + item.getId() + "_" + item.getAccessKey());
						} else if (att.getAudio() != null) {
							Audio item = att.getAudio();
							outAtts.add("audio" + item.getOwnerId() + "_" + item.getId() + "_" + item.getAccessKey());
						} else if (att.getDoc() != null) {
							Doc item = att.getDoc();
							outAtts.add("doc" + item.getOwnerId() + "_" + item.getId() + "_" + item.getAccessKey());
						}
					}

					if (StringUtils.containsIgnoreCase(message, "(убери пробелы)") || StringUtils.containsIgnoreCase(message, "лучший сайт для накрутки") || StringUtils.containsIgnoreCase(message, "vto.pe") || StringUtils.containsIgnoreCase(message, "vkbot.ru") || StringUtils.containsIgnoreCase(message, "vkbot") || StringUtils.containsIgnoreCase(message, "vto")) {

					return true; }

					if (id == 0) {
						List<UserXtrCounters> usersCounters = vk.users().get(actor).userIds(domain).execute();
						if (usersCounters.size() == 0) {
							CommandHandler.INSTANCE.sender.sendMessage("юзер не найден", chatId, msgId, userId, false);
							return true;
						}
						UserXtrCounters c = usersCounters.get(0);
						id = c.getId().intValue();
					}

					boolean sended = false;
					
					if (outAtts.size() > 0) {
						sended = CommandHandler.INSTANCE.sender.sendMessage(out, 0, msgId, id, false, outAtts, null);
					} else if (out.length() > 0) {
						sended = CommandHandler.INSTANCE.sender.sendMessage(out, 0, msgId, id, false);
					} else {
						CommandHandler.INSTANCE.sender.sendMessage("напиши <айди юзера(цифры)> <смска>", chatId, msgId, userId, false);
						return true;
					}
					
					if(!sended) {
						CommandHandler.INSTANCE.sender.sendMessage("не удалось отправить, лс наверно закрыто или я в чс", chatId, msgId, userId, false);
						return true;
					}

					int random = rand.nextInt(3);
					if (random == 0) CommandHandler.INSTANCE.sender.sendMessage("сранский голубь доставил твою посылку", chatId, msgId, userId, false);
					else if (random == 1) CommandHandler.INSTANCE.sender.sendMessage("отправил твой высер", chatId, msgId, userId, false);
					else if (random == 2) CommandHandler.INSTANCE.sender.sendMessage("я черканул в лс", chatId, msgId, userId, false);

					List<Dialog> userDialogs = dialogs.get(Integer.valueOf(userId));
					if (userDialogs == null) userDialogs = new ArrayList<Dialog>();

					Dialog dialog = null;
					for (Dialog dialog1 : userDialogs) {
						if (dialog1.userId == id) dialog = dialog1;
					}

					if (dialog == null) userDialogs.add(new Dialog(id, 0));
					dialogs.put(Integer.valueOf(userId), userDialogs);
					userAndAnon.put(id, userId);
					saveDialogs();
					return true;
				} catch (Exception e) {
					CommandHandler.INSTANCE.sender.sendMessage("не удалось отправить, лс наверно закрыто или я в чс", chatId, msgId, userId, false);
				}
			} else {
				CommandHandler.INSTANCE.sender.sendMessage("напиши <айди юзера(цифры)> <смска>", chatId, msgId, userId, false);
			}
		} else if (StringUtils.startsWithIgnoreCase(message, "оффдиалог") && user.canCmd(true)) {
			if (!dialogs.containsKey(Integer.valueOf(userId))) {
				CommandHandler.INSTANCE.sender.sendMessage("у тя нету активных диалогов", chatId, msgId, userId, false);
				return true;
			}

			List<Dialog> userDialogs = dialogs.get(Integer.valueOf(userId));

			String[] args = message.split(" ");
			if (args.length > 1) {
				String domain = "";
				int id = 0;
				try {
					args[1] = args[1].replace("id", "");
					id = Integer.parseInt(args[1]);
				} catch (Exception e) {
					domain = args[1];
					// CommandHandler.INSTANCE.sender.sendMessage("оффдиалог <айди юзера(цифры)>",
					// chatId, msgId, userId, false);
					// return true;
				}

				if (id == 0) {
					List<UserXtrCounters> usersCounters = vk.users().get(actor).userIds(domain).execute();
					if (usersCounters.size() == 0) {
						CommandHandler.INSTANCE.sender.sendMessage("юзер не найден", chatId, msgId, userId, false);
						return true;
					}
					UserXtrCounters c = usersCounters.get(0);
					id = c.getId().intValue();
				}

				Dialog dialog = null;
				for (Dialog dialog1 : userDialogs) {
					if (dialog1.userId == id) dialog = dialog1;
				}

				if (dialog == null) {
					CommandHandler.INSTANCE.sender.sendMessage("диалог с этим юзером не ведёца", chatId, msgId, userId, false);
					return true;
				}

				while (userDialogs.contains(dialog))
					userDialogs.remove(dialog);
				userAndAnon.remove(Integer.valueOf(id));
				if (userDialogs.size() > 0) dialogs.put(userId, userDialogs);
				else dialogs.remove(userId);
				saveDialogs();

				CommandHandler.INSTANCE.sender.sendMessage("диалог офнут", chatId, msgId, userId, false);
				return true;
			} else {
				CommandHandler.INSTANCE.sender.sendMessage("оффдиалог <айди юзера(цифры)>", chatId, msgId, userId, false);
			}
		}

		return false;
	}

	private void loadDialogs() {
		try {
			File f = new File("dialogs", "dialogs.txt");
			if (!f.exists()) return;
			BufferedReader r = new BufferedReader(new FileReader(f));
			int size = Integer.parseInt(r.readLine());

			for (int i = 0; i < size; i++) {
				String line = r.readLine();
				if (line != null) {
					String[] data = line.split("=");
					int userId = Integer.parseInt(data[0]);
					List<Dialog> userIds = new ArrayList<Dialog>();

					String[] ids = data[1].split(",");
					for (int i1 = 0; i1 < ids.length - 1; i1 += 2) {
						int id = Integer.parseInt(ids[i1]);
						long time = Long.parseLong(ids[i1 + 1]);
						Dialog d = new Dialog(id, time);
						userIds.add(d);
						userAndAnon.put(id, userId);
					}

					dialogs.put(Integer.valueOf(userId), userIds);
				}
			}
			r.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveDialogs() {
		try {
			FileWriter f = new FileWriter(new File("dialogs", "dialogs.txt"));

			Iterator<Integer> iter = dialogs.keySet().iterator();
			f.write(dialogs.size() + "\n");
			while (iter.hasNext()) {
				Integer key = iter.next();
				List<Dialog> r = dialogs.get(key);
				if (r.size() > 0) {
					StringBuilder builder = new StringBuilder();
					for (int i = 0; i < r.size() - 1; i++)
						builder.append(r.get(i).userId + "," + r.get(i).lastMsgDate + ",");
					builder.append(r.get(r.size() - 1).userId + "," + r.get(r.size() - 1).lastMsgDate);
					f.write(key + "=" + builder.toString() + "\n");
				}
			}

			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
