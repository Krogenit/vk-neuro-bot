package ru.krogenit.vkbot;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.deeplearning4j.examples.recurrent.encdec.EncoderDecoderLSTM;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiAccessException;
import com.vk.api.sdk.exceptions.ApiCaptchaException;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.objects.docs.Doc;
import com.vk.api.sdk.objects.docs.responses.GetUploadServerResponse;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.MessageAttachment;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoAlbumFull;
import com.vk.api.sdk.objects.photos.responses.GetResponse;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.objects.video.Video;
import com.vk.api.sdk.objects.wall.WallPostFull;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostAttachmentType;
import com.vk.api.sdk.queries.docs.DocsGetMessagesUploadServerType;
import com.vk.api.sdk.queries.users.UserField;

import ru.krogenit.vkbot.chat.AnonymousChatHandler;
import ru.krogenit.vkbot.chat.ChatHandler;
import ru.krogenit.vkbot.chat.VkChat;
import ru.krogenit.vkbot.command.AttachBssPost;
import ru.krogenit.vkbot.command.AttachPhotoFromAlbum;
import ru.krogenit.vkbot.command.AttachPostCommand;
import ru.krogenit.vkbot.command.BotCommand;
import ru.krogenit.vkbot.command.ChanceCommand;
import ru.krogenit.vkbot.command.GimnBSS;
import ru.krogenit.vkbot.command.ImageDistortion;
import ru.krogenit.vkbot.command.JoinChatByLink;
import ru.krogenit.vkbot.command.RandomRule;
import ru.krogenit.vkbot.command.Repeat;
import ru.krogenit.vkbot.command.SimpleAnswer;
import ru.krogenit.vkbot.command.WhenCommand;
import ru.krogenit.vkbot.command.WhoCommand;
import ru.krogenit.vkbot.command.WhoCommand1;
import ru.krogenit.vkbot.game.GameHandler;
//import core.query.MessagesSender;
import ru.krogenit.vkbot.raid.RaidHandler;
import ru.krogenit.vkbot.user.BlackList;
import ru.krogenit.vkbot.user.UserHandler;
import ru.krogenit.vkbot.user.VkUser;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import net.marketer.RuCaptcha;

public class CommandHandler {
	public static CommandHandler INSTANCE;

	public VkApiClient vk;
	public UserActor actor;
	public GroupActor group;

	private int actionSleep = 334;
	public EncoderDecoderLSTM neuro;
	private boolean needCaptcha;
	public String capcha, capchaSid;
	StreamSpeechRecognizer recognizer;
	MessageSaver saver;
	AnonymousChatHandler anonChat;
	List<BotCommand> botCommands;
	public MessageSender sender;

	private int botNameL;

	public Random rand = new Random();
	public ContentMover mover;

	HashMap<String, Integer> wallSize = new HashMap<String, Integer>();
	HashMap<String, Integer> albumSize = new HashMap<String, Integer>();

	public CommandHandler(VkApiClient vk, UserActor actor, GroupActor group) {
		this.vk = vk;
		this.actor = actor;
		this.group = group;
		INSTANCE = this;
		this.mover = new ContentMover();
		this.sender = new MessageSender(vk, actor, group);
		this.neuro = new EncoderDecoderLSTM();
		registerCommands();
		this.saver = new MessageSaver();
		this.anonChat = new AnonymousChatHandler(vk, actor);

		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					neuro.runDialog();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}; t.setName("Neuro"); t.start();
		
		while(!neuro.started) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
//		try {
//			this.neuro.runDialog();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	private void registerCommands() {
		botCommands = new ArrayList<BotCommand>();
		botCommands.add(new JoinChatByLink());
		botCommands.add(new SimpleAnswer("кто девиант и гоб", new String[] { "это ЕГАРАРАСССФАЪХ" }));
		botCommands.add(new SimpleAnswer("кто чорный", new String[] { "это маааааззыыыййй" }));
		botCommands.add(new SimpleAnswer("кто нидумоет", new String[] { "это дзурн НИДУМОЕТЪХАЪХФ)А)Ф)" }));
		botCommands.add(new WhoCommand());
		botCommands.add(new WhoCommand1());
		botCommands.add(new Repeat());
		botCommands.add(new SimpleAnswer(new String[] { "помощь", "команды" }, new String[] { "ну ты чекни мой профиль, там все написано" }));
		botCommands.add(new WhenCommand());
		botCommands.add(new ChanceCommand());
		botCommands.add(new SimpleAnswer("поцелуй", new String[] { "😘", "😚", "😗", "😙", "😽" }));
		botCommands.add(new ImageDistortion());
		botCommands.add(new GimnBSS());//TODO:
		botCommands.add(new AttachPostCommand("драмес", new int[] { -27923075, -23750039 }));
		botCommands.add(new AttachPostCommand("чил", new int[] { -27894770 }));
		botCommands.add(new AttachPostCommand(new String[] { "юморески", "юмореска" }, new int[] { -92876084 }));
		botCommands.add(new AttachPostCommand("постхард", new int[] { -23138673 }));
		botCommands.add(new AttachPostCommand("эмбиент", new int[] { -40397761, -29583317, -17408120, -3482921, -106569623, -3707002 }));
		botCommands.add(new AttachPostCommand("плоская", new int[] { -24096737 }));
		botCommands.add(new AttachPostCommand("илита", new int[] { -74598681 }));
		botCommands.add(new AttachPostCommand("галнет", new int[] { -94274533 }));
		botCommands.add(new AttachBssPost());
		botCommands.add(new AttachPhotoFromAlbum("пупсики", new String[] {"261894423_267994005"}).setOnlyForLocalChat());
//		botCommands.add(new AttachPhotoFromAlbum("гуро", new String[] {"-112217202_wall"}));
		botCommands.add(new AttachPhotoFromAlbum("скрин", new String[] {"-74598681_202388444"}));
		botCommands.add(new AttachPhotoFromAlbum("каменюка", new String[] {"-74598681_216908319"}));
//		botCommands.add(new AttachPhotoFromAlbum("лесби", new String[] {"-121242074_wall"}));
		botCommands.add(new RandomRule());
		botCommands.add(new AttachPhotoFromAlbum("кубач", new String[] {"-143621955_wall"}));
	
		String[] memPublics = new String[] {"-42954445_wall", "-66814271_wall", "-126516801_wall", 
				"-106403873_wall", "-120254617_wall",  "-136363489_wall", "-87960594_wall", "-56281837_wall", 
				"-75338985_wall", "-90839309_wall", "-123584109_wall", "-92879038_wall", "-113611000_wall",
				"-50505845_wall", "-105373808_wall", "-109388335_wall", "-92337511_wall", "-117431540_wall",
				"-120336438_wall", "-86217001_wall", "-135856652_wall", "-85000577_wall", "-46816292_wall",
				"-65434192_wall", "-104083566_wall", "-115106508_wall", "-48543569_wall", "-98210264_wall",
				"-118035223_wall", "-143311316_wall", "-140237409_wall", "-52024626_wall", "-117276333_wall",
				"-158619277_wall"};

		botCommands.add(new AttachPhotoFromAlbum("мем", memPublics));
		botCommands.add(new AttachPhotoFromAlbum("локал", new String[] {"66311705_244810132"}));
		botCommands.add(new AttachPhotoFromAlbum("пхс", new String[] {"150504557_266203647"}));
		
	}

	private int getWallSize(int ownerId) throws Exception {
		String wallKey = ownerId + "";
		Integer size = wallSize.get(wallKey);
		if (size == null) {
			com.vk.api.sdk.objects.wall.responses.GetResponse resp = vk.wall().get(actor).ownerId(ownerId).execute();
			size = resp.getCount();
			wallSize.put(wallKey, size);
			
		}

		return size;
	}

	private int getAlbumSizze(int ownerId, String album) throws Exception {
		String albumKey = ownerId + "_" + album;
		Integer size = albumSize.get(albumKey);

		if (size == null) {
			GetResponse resp = vk.photos().get(actor).ownerId(ownerId).albumId(album).execute();
			size = resp.getCount();
			albumSize.put(albumKey, size);
			
		}

		return size;
	}

	public void attachRandomPhoto(int chatId, int userId, String album, int ownerId) throws Exception {
		try {
			GetResponse photos = vk.photos().get(actor).ownerId(ownerId).albumId(album).offset(rand.nextInt(getAlbumSizze(ownerId, album))).count(1).execute();
			Photo photo = photos.getItems().get(0);
			ContentMover mover = CommandHandler.INSTANCE.mover;
			String newContent = null;
			if(mover.needMoveContent(photo.getOwnerId())) {
				newContent = mover.getNewContent("photo" + photo.getOwnerId() + "_" + photo.getId());
				if(newContent == null) {
					newContent = mover.movePhoto(photo);
				}
				
				if(newContent == null) {
					System.out.println("[Random photo] ERROR HASHING PHOTO: " + photo.getOwnerId() + " postId: " + photo.getId());
					return;
				}

				List<String> atts = new ArrayList<String>();
				atts.add(newContent);
				sender.sendMessage("", chatId, 0, userId, false, atts, null);
			} else {
				List<String> atts = new ArrayList<String>();
				atts.add("photo" + photo.getOwnerId() + "_" + photo.getId());
				sender.sendMessage("", chatId, 0, userId, false, atts, null);
			}
		} catch (Exception e) {
			sender.sendMessage("забанили группу " + ownerId, chatId, 0, userId, false);
		}
	}

	public void processRandomPhotos(int chatId, int userId) throws Exception {
		int random = rand.nextInt(10);
		if (random == 0) attachRandomPhoto(chatId, userId, "wall", -122615111);
		else if (random == 1) attachRandomPhoto(chatId, userId, "wall", -109290951);
		else if (random == 2) attachRandomPhoto(chatId, userId, "wall", -146716569);
		else if (random == 3) attachRandomPhoto(chatId, userId, "wall", -122772554);
		else if (random == 4) attachRandomPhoto(chatId, userId, "wall", -80795944);
		else if (random == 5) {
			int random1 = rand.nextInt(14);
			if (random1 == 0) attachRandomPhoto(chatId, userId, "wall", -40148170);
			else if (random1 == 1) attachRandomPhoto(chatId, userId, "240173263", -40148170);
			else if (random1 == 2) attachRandomPhoto(chatId, userId, "199626619", -40148170);
			else if (random1 == 3) attachRandomPhoto(chatId, userId, "200482531", -40148170);
			else if (random1 == 4) attachRandomPhoto(chatId, userId, "170712333", -40148170);
			else if (random1 == 5) attachRandomPhoto(chatId, userId, "175880857", -40148170);
			else if (random1 == 6) attachRandomPhoto(chatId, userId, "169681752", -40148170);
			else if (random1 == 7) attachRandomPhoto(chatId, userId, "168172959", -40148170);
			else if (random1 == 8) attachRandomPhoto(chatId, userId, "199563456", -40148170);
			else if (random1 == 9) attachRandomPhoto(chatId, userId, "178094240", -40148170);
			else if (random1 == 10) attachRandomPhoto(chatId, userId, "199973980", -40148170);
			else if (random1 == 11) attachRandomPhoto(chatId, userId, "200468536", -40148170);
			else if (random1 == 12) attachRandomPhoto(chatId, userId, "177019784", -40148170);
			else if (random1 == 13) attachRandomPhoto(chatId, userId, "200661400", -40148170);
		} else if (random == 6) attachRandomPhoto(chatId, userId, "wall", -93077522);
		else if (random == 7) attachRandomPhoto(chatId, userId, "wall", -53384168);
		else if (random == 8) attachRandomPhoto(chatId, userId, "wall", -134616548);
		else if (random == 9) attachRandomPhoto(chatId, userId, "wall", -121698829);
	}

	public void processAnser(String msg, int chatId, int msgId, int userId) throws Exception {
		if (msg != null && msg.length() > 0) {
			String outMsg = neuro.newMessage(msg);
			if (Main.isDebug) sender.sendMessage("ыля " + outMsg, chatId, msgId, userId, false);
			else sender.sendMessage(outMsg, chatId, msgId, userId, false);
		} else {
			String outMsg = neuro.newMessage("пересланное");
			if (Main.isDebug) sender.sendMessage("ыля " + outMsg, chatId, msgId, userId, false);
			else sender.sendMessage(outMsg, chatId, msgId, userId, false);
		}
	}

	public void randomRule(int chatId, int msgId, int userId) throws Exception {
		
	}

	public void disableTalking(String message, int chatId, int msgId, int userId) throws Exception {
		VkChat chat = ChatHandler.INSTACNE.getChat(chatId);
		if (chat.canTalk) {
			chat.canTalk = false;
			int rand1 = rand.nextInt(3);
			if (rand1 == 0) sender.sendMessage("лан", chatId, msgId, userId, false);
			else if (rand1 == 1) sender.sendMessage("ок сын", chatId, msgId, userId, false);
			else if (rand1 == 2) sender.sendMessage("бля ок", chatId, msgId, userId, false);
		}
	}

	public void enableTalking(String message, int chatId, int msgId, int userId) throws Exception {
		VkChat chat = ChatHandler.INSTACNE.getChat(chatId);
		if (!chat.canTalk) {
			if (message.length() > 8) {
				String ch = message.substring(8);
				try {
					int chance = Integer.parseInt(ch);
					if (chance <= 0) chance = 1;
					chat.talkChance = chance;
				} catch (Exception e) {
					chat.talkChance = 1;
				}
			} else {
				chat.talkChance = 1;
			}
			chat.talkStartTime = System.currentTimeMillis();
			chat.canTalk = true;
			this.processAnser("высирай", chatId, msgId, userId);
		}
	}

	public void addUserToChat(int chatId, int msgId, int userId) throws Exception {
		try {
			vk.messages().removeChatUser(actor, chatId, userId + "").execute();
			
		} catch (ApiAccessException e) {
			// System.out.println("нет прав для добавления юзера в чат");
		} catch (ApiException e) {

		}
		try {
			vk.messages().addChatUser(actor, chatId, userId).execute();
			System.out.println("[ADD USER] " + userId + " to chat " + chatId);
			int random = rand.nextInt(5);
			if (random == 0) sender.sendMessage("тут сиди.", chatId, msgId, userId, false);
			else if (random == 1) sender.sendMessage("куда собрался.", chatId, msgId, userId, false);
			else if (random == 2) sender.sendMessage("сказал же. тут сиди", chatId, msgId, userId, false);
			else if (random == 3) sender.sendMessage("а ну сидеть! тут.", chatId, msgId, userId, false);
			else if (random == 4) sender.sendMessage("я сказал! сиди. тута. здеся.", chatId, msgId, userId, false);
		} catch (ApiAccessException e) {
			// System.out.println("нет прав для добавления юзера в чат");
		} catch (ApiException e) {

		}
	}

	public void attachRandomVideoFromWall(int chatId, int msgId, int userId, int ownerId) throws Exception {
		boolean found = false;
		while (!found) {
			com.vk.api.sdk.objects.wall.responses.GetResponse resp;

			int offset1 = rand.nextInt(getWallSize(ownerId));
			resp = vk.wall().get(actor).ownerId(ownerId).count(1).offset(offset1).execute();
			
			WallPostFull post = resp.getItems().get(0);

			List<WallpostAttachment> atts = post.getAttachments();

			String text = post.getText();

			if (atts != null) {
				List<String> atts1 = new ArrayList<String>();
				for (WallpostAttachment att : atts) {
					if (att.getType() == WallpostAttachmentType.VIDEO) {
						Video video = att.getVideo();
						String attVideo = "video" + video.getOwnerId() + "_" + video.getId();
						atts1.add(attVideo);
						if (text != null && text.length() > 0) {
							sender.sendMessage(text, chatId, msgId, userId, false, atts1, null);
						} else {
							sender.sendMessage("", chatId, msgId, userId, false, atts1, null);
						}
						found = true;
						break;
					}

				}
			}
			System.out.println("[WEBM] not found");
		}
	}

	public void update() {

	}

	private void onKickUser(Message msg, int chatId, int msgId, int userId) throws Exception {
		int actionMid;
		if(msg.getAction() != null) actionMid = msg.getAction().getMemberId();
		else actionMid = msg.getActionMid();
		VkUser user1 = UserHandler.INSTACNE.getUser(actionMid);
		System.out.println("[KICK USER] " + actionMid + " from chat " + chatId);
		VkChat chat = ChatHandler.INSTACNE.getChat(chatId);
		if(chat != null) {
			chat.users.remove(Integer.valueOf(actionMid));
		}
		
		if (actionMid == Main.BOT_ID) {
			RaidHandler.INSTANCE.inviteBot(chatId);
		} else if (actionMid == RaidHandler.BACKUP_BOT_ID && msg.getFromId() != null) {
			RaidHandler.INSTANCE.raidStop(chatId);
		} else if (user1 != null && user1.canInviteInChat(chatId) && actionMid != RaidHandler.BACKUP_BOT_ID) {
			addUserToChat(chatId, msgId, actionMid);
		} else {
			if (chat != null && chat.canTalk()) {
				int chance = chat.talkChance;
				if (chance == 1 || (chance > 0 && rand.nextInt(chance) == 0)) {
					int r = rand.nextInt(6);
					if (r == 0) processAnser("кик", chatId, msgId, userId);
					else if (r == 1) sender.sendMessage("тупа слив.", chatId, msgId, userId, false);
					else if (r == 2) sender.sendMessage("слился лох", chatId, msgId, userId, false);
					else if (r == 3) sender.sendMessage("дак ура слилась нюфажина", chatId, msgId, userId, false);
					else if (r == 4) sender.sendMessage("минус нуб", chatId, msgId, userId, false);
					else if (r == 5) sender.sendMessage("моралфажнулся дибил", chatId, msgId, userId, false);
				}
			}
		}
	}

	private void onInviteUser(Message msg, int chatId, int msgId, int userId) throws Exception {
		int actionMid;
		if(msg.getAction() != null) actionMid = msg.getAction().getMemberId();
		else actionMid = msg.getActionMid();
		
		VkChat chat = ChatHandler.INSTACNE.getChat(chatId);
		if(chat != null && chat.users != null) {
			chat.users.add(Integer.valueOf(actionMid));
		}
		
		if (actionMid == Main.BOT_ID) {
			processAnser("инвайт", chatId, msgId, userId);
		} else {
			if (chat != null && chat.canTalk()) {
				int chance = chat.talkChance;
				if (chance == 1 || (chance != 0 && rand.nextInt(chance) == 0)) {
					int r = rand.nextInt(6);
					if (r == 0) processAnser("инвайт", chatId, msgId, userId);
					else if (r == 1) this.sender.sendMessage("дибила инвайтнули.", chatId, msgId, userId, false);
					else if (r == 2) this.sender.sendMessage("ура дибил ливнул. ой. нето", chatId, msgId, userId, false);
					else if (r == 3) this.sender.sendMessage("чо за лох", chatId, msgId, userId, false);
					else if (r == 4) this.sender.sendMessage("нюфаги наплывают", chatId, msgId, userId, false);
					else if (r == 5) this.sender.sendMessage("нубас ливай", chatId, msgId, userId, false);
				}
			}
		}
	}

	private void processChatTalk(Message msg, String message, int chatId, int msgId, int userId) throws Exception {
		if (isBotName(message)) {
			if (botNameL != 0) {
				message = message.substring(botNameL);
				if (message.length() == 0) message = "илья";
			}
			processAnser(message, chatId, msgId, userId);
		} else {
			VkChat chat = ChatHandler.INSTACNE.getChat(chatId);
			if (chat.canTalk()) {
				int chance = chat.talkChance;
				if (chance <= 1 || rand.nextInt(chance) == 0) {
					if (msg.getAttachments() != null && msg.getAttachments().size() == 1) {
						MessageAttachment att = msg.getAttachments().get(0);
						if (att.getSticker() != null) {
							int r = rand.nextInt(25);
							if (r == 0) sender.sendMessage("для пидоров стики.", chatId, msgId, userId, false);
							else if (r == 1) sender.sendMessage("буквы пиши сука.", chatId, msgId, userId, false);
							else processAnser("стикер", chatId, msgId, userId);
						} else if (att.getDoc() != null && att.getDoc().getType() == 5) {
							VkUser user = UserHandler.INSTACNE.getUser(userId);
							if(chat.isLocalChat()) {
								if (user.canRecognize) {
									recognizeMessage(att.getDoc(), chatId, msgId, userId);
								} else {
									int r = rand.nextInt(25);
									if (r == 0) this.sender.sendMessage("буквы бесплатные. их пиши блядь.", chatId, msgId, userId, false);
									else processAnser("голосове сообщение", chatId, msgId, userId);
								}
							} else {
								long time = System.currentTimeMillis();

								if (time - user.lastRecogTime > 1000 * 60 && (user.canRecognize && rand.nextInt(25) == 0)) {
									recognizeMessage(att.getDoc(), chatId, msgId, userId);
									user.lastRecogTime = time;
								} else {
									int r = rand.nextInt(25);
									if (r == 0) this.sender.sendMessage("буквы бесплатные. их пиши блядь.", chatId, msgId, userId, false);
									else processAnser("голосове сообщение", chatId, msgId, userId);
								}
							}
							//if (WhiteList.isWhitelisted(userId)) {
								//if ((user.canRecognize || rand.nextInt(25) == 0)) recognizeMessage(att.getDoc(), chatId, msgId, userId);
							//} else {
//								long time = System.currentTimeMillis();
//
//								if (time - user.lastRecogTime > 1000 * 30 && (user.canRecognize || rand.nextInt(25) == 0)) {
//									recognizeMessage(att.getDoc(), chatId, msgId, userId);
//									user.lastRecogTime = time;
//								} else {
//									int r = rand.nextInt(25);
//									if (r == 0) this.sender.sendMessage("буквы бесплатные. их пиши блядь.", chatId, msgId, userId, false);
//									else processAnser("голосове сообщение", chatId, msgId, userId);
//								}
							//}
						}
					} else processAnser(message, chatId, msgId, userId);
				}
			} else {
				if (msg.getAttachments() != null && msg.getAttachments().size() == 1) {
					MessageAttachment att = msg.getAttachments().get(0);
					if (att.getDoc() != null && att.getDoc().getType() == 5) {
						if(chat.isLocalChat()) {
							VkUser user = UserHandler.INSTACNE.getUser(userId);
							if (user.canRecognize ) {
								recognizeMessage(att.getDoc(), chatId, msgId, userId);
							}
						}
					}
				}
			}
			
			trollingUsersInChat(msg, message, chatId, msgId, userId);

		}
	}
	
	private void trollingUsersInChat(Message msg, String message, int chatId, int msgId, int userId) {
		if(userId == 228793240 && rand.nextInt(5) == 0) {
			sender.sendMessage("пхс", chatId, msgId, userId, false);
		}
//		} else if(userId == 145359779) {
//			int random = rand.nextInt(25);
//			if(random == 0) {
//				int randWord = rand.nextInt(10);
//				if(randWord == 0) sender.sendMessage("совушка сова ☺", chatId, msgId, userId, false);
//				else if(randWord == 1) sender.sendMessage("эх однушечка одна", chatId, msgId, userId, false);
//				else if(randWord == 2) sender.sendMessage("не могу жить без одной", chatId, msgId, userId, false);
//				else if(randWord == 3) sender.sendMessage("сова ☺☺☺☺☺☺", chatId, msgId, userId, false);
//				else if(randWord == 4) sender.sendMessage("чорный+сова=💜", chatId, msgId, userId, false);
//				else if(randWord == 5) sender.sendMessage("эх роднушечка родна", chatId, msgId, userId, false);
//				else if(randWord == 6) sender.sendMessage("а в пизду приятно)))))", chatId, msgId, userId, false);
//				else if(randWord == 7) sender.sendMessage("коектушева коекта ☺☺", chatId, msgId, userId, false);
//				else if(randWord == 8) sender.sendMessage("сава ☺", chatId, msgId, userId, false);
//				else if(randWord == 9) sender.sendMessage("сладусечка одна ☺☺", chatId, msgId, userId, false);
//			}
//		}
	}

	private void processUserTalk(Message msg, String message, int chatId, int msgId, int userId) throws Exception {
		VkUser user = UserHandler.INSTACNE.getUser(userId);
		if (user.canCmd(false)) {
			if(message!=null && message.length() > 0) {
				processAnser(message, chatId, msgId, userId);
			} else if (msg.getAttachments() != null) {
				if (msg.getAttachments().size() == 1) {
					MessageAttachment att = msg.getAttachments().get(0);
					if (att.getSticker() != null) {
						int r = rand.nextInt(25);
						if (r == 0) sender.sendMessage("для пидоров стики.", chatId, msgId, userId, false);
						else if (r == 1) sender.sendMessage("буквы пиши сука.", chatId, msgId, userId, false);
						else processAnser("стикер", chatId, msgId, userId);
					} else if (att.getDoc() != null && att.getDoc().getType() == 5) {
						//if (WhiteList.isWhitelisted(userId)) {
							//if (user.canRecognize || rand.nextInt(25) == 0) recognizeMessage(att.getDoc(), chatId, msgId, userId);
						//} else {
							long time = System.currentTimeMillis();
							if (time - user.lastRecogTime > 1000 * 60 && (user.canRecognize || rand.nextInt(25) == 0)) {
								recognizeMessage(att.getDoc(), chatId, msgId, userId);
								user.lastRecogTime = time;
							} else {
								int r = rand.nextInt(25);
								if (r == 0) this.sender.sendMessage("буквы бесплатные. их пиши блядь.", chatId, msgId, userId, false);
								else processAnser("голосове сообщение", chatId, msgId, userId);
							}
						//}
					} else if (att.getAudio() != null && message.length() == 0) {
						processAnser("музыка", chatId, msgId, userId);
					} else if (att.getPhoto() != null && message.length() == 0) {
						processAnser("фото", chatId, msgId, userId);
					} else if (att.getVideo() != null && message.length() == 0) {
						processAnser("видео", chatId, msgId, userId);
					} else if (att.getWall() != null && message.length() == 0) {
						processAnser("репост", chatId, msgId, userId);
					}
				} else {
					processAnser("говна накидал", chatId, msgId, userId);
				}
			}
		}
	}

	public void processTalk(Message msg, String message, String action, int chatId, int msgId, int userId) throws Exception {
		if (action != null && action.length() > 0) {
			if (action.equals("chat_kick_user")) {
				onKickUser(msg, chatId, msgId, userId);
			} else if (action.equals("chat_invite_user")) {
				onInviteUser(msg, chatId, msgId, userId);
			} else if (action.equals("chat_title_update")) {
				VkChat chat = ChatHandler.INSTACNE.getChat(chatId);
				if (chat.canTalk()) {
					int chance = chat.talkChance;
					if (chance == 1 || rand.nextInt(chance) == 0) {
						int r = rand.nextInt(6);
						if (r == 0) processAnser("название беседы", chatId, msgId, userId);
						else if (r == 1) this.sender.sendMessage("нука вернул быстро.", chatId, msgId, userId, false);
						else if (r == 2) this.sender.sendMessage("те кто резрешал менять то.", chatId, msgId, userId, false);
						else if (r == 3) this.sender.sendMessage("ты чо нажал а.", chatId, msgId, userId, false);
						else if (r == 4) this.sender.sendMessage("меняй обратно.", chatId, msgId, userId, false);
						else if (r == 5) this.sender.sendMessage("жепу плиз. а не название менять.", chatId, msgId, userId, false);
					}
				}
			}
		} else {
			if (chatId != 0) {
				processChatTalk(msg, message, chatId, msgId, userId);
			} else {
				processUserTalk(msg, message, chatId, msgId, userId);
			}
		}
	}

	public void attachRandomGif(int chatId, int userId, int ownerId) throws Exception {
		boolean found = false;
		while (!found) {
			com.vk.api.sdk.objects.wall.responses.GetResponse resp = vk.wall().get(actor).ownerId(ownerId).count(1).offset(rand.nextInt(getWallSize(ownerId))).execute();
			
			WallPostFull post = resp.getItems().get(0);
			List<WallpostAttachment> atts = post.getAttachments();
			String text = post.getText();
			List<String> atts1 = new ArrayList<String>();

			if (atts != null) {
				for (WallpostAttachment att : atts) {
					if (att.getType() == WallpostAttachmentType.DOC) {
						Doc doc = att.getDoc();
						String attVideo = "doc" + doc.getOwnerId() + "_" + doc.getId() + "_" + doc.getAccessKey();
						atts1.add(attVideo);
						if (text != null && text.length() > 0) {
							sender.sendMessage(text, chatId, 0, userId, false, atts1, null);
						} else {
							sender.sendMessage("", chatId, 0, userId, false, atts1, null);
						}
						found = true;
						break;
					}

				}
			}
			if (!found) System.out.println("[GIF] not found");
		}
	}

	public void attachRandomMem(int chatId, int userId) throws Exception {
		int r = rand.nextInt(34);
		if (r == 0) attachRandomPhoto(chatId, userId, "wall", -42954445);
		else if (r == 1) attachRandomPhoto(chatId, userId, "wall", -66814271);
		else if (r == 2) attachRandomPhoto(chatId, userId, "wall", -126516801);
		else if (r == 3) attachRandomPhoto(chatId, userId, "wall", -106403873);
		else if (r == 4) attachRandomPhoto(chatId, userId, "wall", -120254617);
		else if (r == 5) attachRandomPhoto(chatId, userId, "wall", -136363489);
		else if (r == 6) attachRandomPhoto(chatId, userId, "wall", -87960594);
		else if (r == 7) attachRandomPhoto(chatId, userId, "wall", -56281837);
		else if (r == 8) attachRandomPhoto(chatId, userId, "wall", -75338985);
		else if (r == 9) attachRandomPhoto(chatId, userId, "wall", -90839309);
		else if (r == 10) attachRandomPhoto(chatId, userId, "wall", -123584109);
		else if (r == 11) attachRandomPhoto(chatId, userId, "wall", -92879038);
		else if (r == 12) attachRandomPhoto(chatId, userId, "wall", -113611000);
		else if (r == 13) attachRandomPhoto(chatId, userId, "wall", -50505845);
		else if (r == 14) attachRandomPhoto(chatId, userId, "wall", -105373808);
		else if (r == 15) attachRandomPhoto(chatId, userId, "wall", -109388335);
		else if (r == 16) attachRandomPhoto(chatId, userId, "wall", -92337511);
		else if (r == 17) attachRandomPhoto(chatId, userId, "wall", -117431540);
		else if (r == 18) attachRandomPhoto(chatId, userId, "wall", -120336438);
		else if (r == 19) attachRandomPhoto(chatId, userId, "wall", -86217001);
		else if (r == 20) attachRandomPhoto(chatId, userId, "wall", -135856652);
		else if (r == 21) attachRandomPhoto(chatId, userId, "wall", -85000577);
		else if (r == 22) attachRandomPhoto(chatId, userId, "wall", -46816292);
		else if (r == 23) attachRandomPhoto(chatId, userId, "wall", -65434192);
		else if (r == 24) attachRandomPhoto(chatId, userId, "wall", -104083566);
		else if (r == 25) attachRandomPhoto(chatId, userId, "wall", -115106508);
		else if (r == 26) attachRandomPhoto(chatId, userId, "wall", -48543569);
		else if (r == 27) attachRandomPhoto(chatId, userId, "wall", -98210264);
		else if (r == 28) attachRandomPhoto(chatId, userId, "wall", -118035223);
		else if (r == 29) attachRandomPhoto(chatId, userId, "wall", -143311316);
		else if (r == 30) attachRandomPhoto(chatId, userId, "wall", -140237409);
		else if (r == 31) attachRandomPhoto(chatId, userId, "wall", -52024626);
		else if (r == 32) attachRandomPhoto(chatId, userId, "wall", -117276333);
		else if (r == 33) attachRandomPhoto(chatId, userId, "wall", -158619277);
	}

	public void processOr(String message, int chatId, int msgId, int userId) throws Exception {
		String[] lines = message.split(" или ");
		if (lines.length == 1) lines = message.split(" ИЛИ ");
		int r = rand.nextInt(lines.length);
		int r1 = rand.nextInt(4);

		String out = lines[r];
		if (out.length() > 0) {
			out = out.replace("?", "");
			out = out.toLowerCase();

			if (r1 == 0) this.sender.sendMessage("короч выбираю " + out, chatId, msgId, userId, false);
			else if (r1 == 1) this.sender.sendMessage("лучше " + out, chatId, msgId, userId, false);
			else if (r1 == 2) this.sender.sendMessage("суран советует " + out, chatId, msgId, userId, false);
			else if (r1 == 3) this.sender.sendMessage("базарю, лучше " + out, chatId, msgId, userId, false);
		} else {
			processAnser(message, chatId, msgId, userId);
		}
	}

	public void recognizeMessage(Doc doc, int chatId, int msgId, int userId) throws Exception {
		 if (true) return;
		InputStream in1 = new URL(doc.getUrl()).openStream();
		File file = new File("voice.ogg");
		FileUtils.copyInputStreamToFile(in1, file);

		HttpClient client = HttpClientBuilder.create().build();

		long curTime = System.currentTimeMillis();
		if (lastGetTime == 0 || curTime - lastGetTime > 1000 * 60 * 60 * 12) {
			String yandexOauth = "https://iam.api.cloud.yandex.net/iam/v1/tokens";
			StringEntity requestEntity = new StringEntity("{\"yandexPassportOauthToken\": \"\"}", ContentType.APPLICATION_JSON);

			HttpPost request = null;

			try {
				request = new HttpPost(yandexOauth);
				request.setEntity(requestEntity);
			} catch (Exception e) {
				this.sender.sendMessage("пиши норм текст", chatId, msgId, userId, false);
				return;
			}

			request.addHeader("Content-Type", "application/json");
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() != 200) {
				this.sender.sendMessage("ошибка в работе авторизации " + response.getStatusLine(), chatId, msgId, userId, false);
				return;
			}

			HttpEntity responseEntity = response.getEntity();
			Header encodingHeader = responseEntity.getContentEncoding();

			Charset encoding = encodingHeader == null ? StandardCharsets.UTF_8 : Charsets.toCharset(encodingHeader.getValue());

			String json = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);

			int firstIndex = json.indexOf(':') + 3;
			lastIMAKey = json.substring(firstIndex);
			lastIMAKey = lastIMAKey.substring(0, lastIMAKey.lastIndexOf("\"e") - 4);

			lastGetTime = curTime;
		}

		String REQUEST = "https://stt.api.cloud.yandex.net/speech/v1/stt:recognize/?folderId=";
		URL url = new URL(REQUEST);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "audio/ogg;codecs=opus");
		connection.setRequestProperty("User-Agent", "BoBa");
		connection.setRequestProperty("Host", "stt.api.cloud.yandex.net");
		connection.setRequestProperty("Content-Length", "" + file.length());
		connection.setRequestProperty("Transfer-Encoding", "chunked");
		connection.setRequestProperty("Authorization", "Bearer " + lastIMAKey);

		connection.setUseCaches(false);
		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		FileInputStream inputStream = new FileInputStream(file);
		byte[] buffer = new byte[4096];
		int bytesRead = -1;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			wr.write(buffer, 0, bytesRead);
		}
		wr.flush();
		inputStream.close();
		wr.flush();
		wr.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		String res = "";
		String decodedString;
		while ((decodedString = in.readLine()) != null) {
			res += decodedString;
		}
		connection.disconnect();
		in.close();

		String str = res.replace("{\"result\":\"", "").replace("\"}", "");

		if (str.length() == 0) {
			System.out.println("[Recognition] failed");
			this.sender.sendMessage("хуй пойми чо ты там высрал", chatId, msgId, userId, false);
			return;
		}

		int random = rand.nextInt(3);
		String out = "";
		if (random == 0) out = "вроде ты высрал ";
		else if (random == 1) out = "ты кажеца записал ";
		else if (random == 2) out = "наверн ты сказал ";

		out += str;
		// out += "\n" + neuro.newMessage(str);

		System.out.println("[Recognition] " + out);

		this.sender.sendMessage(out, chatId, msgId, userId, false);
		if (rand.nextInt(25) == 0) this.processAnser(str, chatId, msgId, userId);
	}

	long lastGetTime = 0;
	String lastIMAKey = "";

	public void attachVoiceMessage(String message, int chatId, int msgId, int userId) throws Exception {
		
		boolean isture = true;
		this.sender.sendMessage("функция отключена", chatId, msgId, userId, false);
		if(isture) return;
		
		String[] args = message.split(" ");

		String out = new String(message).substring(6);
		String speed = "1.0";
		String lang = "ru-RU";
		String speaker = "zahar";
		String emotion = "evil";
		if (args.length > 4) {
			try {
				float speed1 = Float.parseFloat(args[1]);
				speed = speed1 + "";
				out = out.replaceFirst(args[1], "");
			} catch (Exception e) {

			}
			switch (args[2]) {
			case "en-US":
				lang = "en-US";
				out = out.replaceFirst(lang, "");
				break;
			case "uk-UK":
				lang = "uk-UK";
				out = out.replaceFirst(lang, "");
				break;
			case "tr-TR":
				lang = "tr-TR";
				out = out.replaceFirst(lang, "");
				break;
			case "ru-RU":
				out = out.replaceFirst(lang, "");
				break;
			}
			switch (args[3]) {
			case "jane":
				speaker = "jane";
				out = out.replaceFirst(speaker, "");
				break;
			case "oksana":
				speaker = "oksana";
				out = out.replaceFirst(speaker, "");
				break;
			case "alyss":
				speaker = "alyss";
				out = out.replaceFirst(speaker, "");
				break;
			case "omazh":
				speaker = "omazh";
				out = out.replaceFirst(speaker, "");
				break;
			case "zahar":
				out = out.replaceFirst(speaker, "");
				break;
			case "ermil":
				speaker = "ermil";
				out = out.replaceFirst(speaker, "");
				break;
			}
			switch (args[4]) {
			case "good":
				emotion = "good";
				out = out.replaceFirst(emotion, "");
				break;
			case "neutral":
				emotion = "neutral";
				out = out.replaceFirst(emotion, "");
				break;
			case "evil":
				out = out.replaceFirst(emotion, "");
				break;
			}

		}

		String text = out;
		// text = text.replace(" ", "%20");
		// text = text.replace("&", "?");
		text = text.replace("\n", " ");

		if (text.length() > 4096) {
			this.sender.sendMessage("слишком дохуя высрал ", chatId, msgId, userId, false);
			return;
		}

		HttpClient client = HttpClientBuilder.create().build();

		long curTime = System.currentTimeMillis();
		if (lastGetTime == 0 || curTime - lastGetTime > 0) {
			String yandexOauth = "https://iam.api.cloud.yandex.net/iam/v1/tokens";
			StringEntity requestEntity = new StringEntity("{\"yandexPassportOauthToken\": \"AQAAAAAM9_ZnAATuwemZLR8V801Olui_99uOBs0\"}", ContentType.APPLICATION_JSON);

			HttpPost request = null;

			try {
				request = new HttpPost(yandexOauth);
				request.setEntity(requestEntity);
			} catch (Exception e) {
				this.sender.sendMessage("пиши норм текст", chatId, msgId, userId, false);
				return;
			}

			request.addHeader("Content-Type", "application/json");
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() != 200) {
				this.sender.sendMessage("ошибка в работе авторизации " + response.getStatusLine(), chatId, msgId, userId, false);
				return;
			}

			HttpEntity responseEntity = response.getEntity();
			Header encodingHeader = responseEntity.getContentEncoding();

			Charset encoding = encodingHeader == null ? StandardCharsets.UTF_8 : Charsets.toCharset(encodingHeader.getValue());

			String json = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
			System.out.println(json);
			int firstIndex = json.indexOf(':') + 3;
			lastIMAKey = json.substring(firstIndex);
			lastIMAKey = lastIMAKey.substring(0, lastIMAKey.lastIndexOf("\"e") - 4);
			System.out.println(lastIMAKey);
			lastGetTime = curTime;
		}

		List<NameValuePair> form = new ArrayList<>();
		form.add(new BasicNameValuePair("text", text));
		form.add(new BasicNameValuePair("quality", "ultrahigh"));
		form.add(new BasicNameValuePair("lang", lang));
		form.add(new BasicNameValuePair("voice", speaker));
		form.add(new BasicNameValuePair("speed", speed));
		form.add(new BasicNameValuePair("emotion", emotion));
		form.add(new BasicNameValuePair("folderId", "b1gbfv03vlokdtdn6dj4"));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);

		String yandexUrl = "https://tts.api.cloud.yandex.net/speech/v1/tts:synthesize";

		HttpPost request = null;

		try {
			request = new HttpPost(yandexUrl);
			request.setEntity(entity);
		} catch (Exception e) {
			this.sender.sendMessage("пиши норм текст", chatId, msgId, userId, false);
			return;
		}

		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		request.addHeader("Authorization", "Bearer " + lastIMAKey);
		HttpResponse response = client.execute(request);
		if (response.getStatusLine().getStatusCode() != 200) {
			this.sender.sendMessage("ошибка в работе tts " + response.getStatusLine(), chatId, msgId, userId, false);
			return;
		}
		InputStream in = response.getEntity().getContent();
		File file = new File(".", "test.ogg");
		FileUtils.copyInputStreamToFile(in, file);
		
		GetUploadServerResponse res;
		if(Main.isFromGroup) {
			res = vk.docs().getMessagesUploadServer(Main.INSTANCE.group).peerId(chatId != 0 ? chatId + 2000000000 : userId).type(DocsGetMessagesUploadServerType.AUDIO_MESSAGE).execute();
		} else {
			res = vk.docs().getMessagesUploadServer(actor).peerId(chatId != 0 ? chatId + 2000000000 : userId).type(DocsGetMessagesUploadServerType.AUDIO_MESSAGE).execute();
		}
		
		String utltoupload = res.getUploadUrl();
		String charset = "UTF-8";
		String boundary = "===" + System.currentTimeMillis() + "===";

		URL url = new URL(utltoupload);
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setUseCaches(false);
		httpConn.setDoOutput(true); // indicates POST method
		httpConn.setDoInput(true);
		httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		httpConn.setRequestProperty("User-Agent", "Suraan/1.0");

		OutputStream outputStream = null;
		try {
			outputStream = httpConn.getOutputStream();
		} catch (Exception e) {
			this.sender.sendMessage("чот дохуя ты высрал либо я уебался", chatId, msgId, userId, false);
			return;
		}

		String LINE_FEED = "\r\n";
		String fieldName = "file";
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
		writer.append("User-Agent" + ": " + "Suraan/1.0").append(LINE_FEED);
		writer.flush();

		String fileName = file.getName();
		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
		writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
		writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.flush();

		FileInputStream inputStream = new FileInputStream(file);
		byte[] buffer = new byte[4096];
		int bytesRead = -1;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		outputStream.flush();
		inputStream.close();

		writer.append(LINE_FEED);
		writer.flush();

		List<String> resp = new ArrayList<String>();

		writer.append(LINE_FEED).flush();
		writer.append("--" + boundary + "--").append(LINE_FEED);
		writer.close();

		// checks server's status code first
		int status = httpConn.getResponseCode();
		if (status == HttpURLConnection.HTTP_OK) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				resp.add(line);
			}
			reader.close();
			httpConn.disconnect();
		} else {
			throw new IOException("Server returned non-OK status: " + status);
		}
		// for(int i=0;i<resp.size();i++)
		// this.sender.sendMessage(resp.get(i).substring(9,
		// resp.get(i).length()-2),
		// chatId, msgId, userId, false);
		String docStr = resp.get(0).substring(9, resp.get(0).length() - 2);
		List<Doc> docs = null;
		try {
			if(Main.isFromGroup) docs = vk.docs().save(Main.INSTANCE.group, docStr).execute();
			else docs = vk.docs().save(actor, docStr).execute();
		} catch (ApiCaptchaException captcha) {
			System.out.println("[CAPTCHA] Error on saving voice msg");
			processCapcha(captcha);

			try {
				if(Main.isFromGroup) docs = vk.docs().save(Main.INSTANCE.group, docStr).captchaKey(capcha).captchaSid(capchaSid).execute();
				else docs = vk.docs().save(actor, docStr).captchaKey(capcha).captchaSid(capchaSid).execute();
				capcha = null;
				System.out.println("[CAPTCHA] Success saving voice msg");
			} catch (ApiCaptchaException captcha1) {
				System.out.println("[CAPTCHA] Saving voice msg failed");
				return;
			}
		}

		
		Doc doc = docs.get(0);
		List<String> atts = new ArrayList<String>();
		atts.add("doc" + doc.getOwnerId() + "_" + doc.getId());

		try {
			sender.sendMessage("", chatId, msgId, userId, false, atts, null);
			System.out.println("[Voice Message] " + message);
		} catch (Exception e) {
			this.sender.sendMessage("чот дохуя ты высрал либо я уебался", chatId, msgId, userId, false);
			return;
		}
	}

	private void processAdminCommands(Message msg, String message, int chatId, int msgId, int userId)throws Exception {
		if (message.startsWith("ылястоп")) {
			stopBot();
			this.sender.sendMessage("успешно", chatId, msgId, userId, false);
		} else if (message.startsWith("/дебаг")) this.processAnser("приф", 13, msgId, userId);
		else if (message.startsWith("ылякик")) {
			String[] s = message.split(" ");
			vk.messages().removeChatUser(actor, chatId, s[1]).execute();
		} else if (message.startsWith("ылябан")) {
			String[] s = message.split(" ");
			BlackList.addUser(Integer.parseInt(s[1]));
		} else if (message.startsWith("ыляинвайт")) {
			String[] s = message.split(" ");
			vk.messages().addChatUser(actor, chatId, Integer.parseInt(s[1])).execute();
		} else if (message.startsWith("whitelistadd")) {
			int id = Integer.parseInt(message.split(" ")[1]);
			WhiteList.addUser(id);
			this.sender.sendMessage("ну короч тя в белый список кинули вот, живи", 0, msgId, id, false);
		} else if (message.startsWith("whitelistremove")) {
			int id = Integer.parseInt(message.split(" ")[1]);
			WhiteList.addUser(id);
			this.sender.sendMessage("сори но ты больше не в белом списке", 0, msgId, id, false);
		} else if (message.startsWith("сохр")) {
			String id = message.split(" ")[1];
			List<MessageAttachment> atts = msg.getAttachments();
			MessageAttachment att = atts.get(0);
			Photo photo = att.getPhoto();
			Integer photoId = vk.photos().copy(actor, photo.getOwnerId(), photo.getId()).accessKey(photo.getAccessKey()).execute();
			
			if (StringUtils.equalsIgnoreCase(id, "этти")) {
				vk.photos().move(actor, 260391759, photoId).execute();
				
			} else if (StringUtils.equalsIgnoreCase(id, "хентай")) {
				vk.photos().move(actor, 260142294, photoId).execute();
				
			}
			this.sender.sendMessage("сохранил", chatId, msgId, userId, false);
		} else if(message.startsWith("тест")) {
			
//			this.sender.sendMessage("успешно", chatId, msgId, userId, false);
		}
	}
	
	public void processMsg(Message msg, String message, int chatId, int msgId, int userId, VkUser user, String action, int date) {
		try {
			if (chatId != 0) {
				VkChat chat = ChatHandler.INSTACNE.getChat(chatId);
				if (chat == null) return;
				int lastTime = chat.lastMsgDate;

				long time = System.currentTimeMillis();
				long dif = time - chat.lastBotMessage;
				long maxTime = 3000;
				// if (chatId != VkChat.LOCAL_CHAT && dif <= maxTime) { return;
				// }
				if (!chat.canTalk) chat.lastBotMessage = time;

				if (msg.getDate() < lastTime) { return; }
			}
			
			if (GameHandler.INSTANCE != null && GameHandler.INSTANCE.checkCommands(msg, message, user, chatId, msgId, userId)) {
				return;
			}

			for (BotCommand cmd1 : botCommands) {
				if (cmd1.checkCommand(msg, message, chatId, msgId, userId, user)) {
					break;
				}
			}


			if (StringUtils.startsWithIgnoreCase(message, "анигиф") && user.canCmd(true)) {
				int r = rand.nextInt(2);
				if (r == 0) attachRandomGif(chatId, userId, -39615703);
				else attachRandomGif(chatId, userId, -152567386);
			}
			else if (StringUtils.startsWithIgnoreCase(message, "гиф") && user.canCmd(true)) {
				int r = rand.nextInt(3);
				if (r == 0) attachRandomGif(chatId, userId, -39488246);
				else if (r == 1) attachRandomGif(chatId, userId, -65047210);
				else attachRandomGif(chatId, userId, -95927518);
			} else if(StringUtils.startsWithIgnoreCase(message, "ускоряем")) {
				attachRandomPhoto(chatId, userId, "wall", -123101833);
				
			} else if (StringUtils.startsWithIgnoreCase(message, "анимем") && user.canCmd(true)) {
				attachRandomPhoto(chatId, userId, "wall", -132364112);
			} else if (StringUtils.startsWithIgnoreCase(message, "сохрани") && user.canCmd(true)) {
				try {
					List<MessageAttachment> atts = msg.getAttachments();
					MessageAttachment att = atts.get(0);
					Photo photo = att.getPhoto();
					Integer photoId = vk.photos().copy(actor, photo.getOwnerId(), photo.getId()).accessKey(photo.getAccessKey()).execute();
					
					int rand1 = rand.nextInt(3);
					if (rand1 == 0) this.sender.sendMessage("сохранил твой высер", chatId, msgId, userId, false);
					else if (rand1 == 1) this.sender.sendMessage("добавил в сохранки твое дерьмо", chatId, msgId, userId, false);
					else if (rand1 == 2) this.sender.sendMessage("сохранил", chatId, msgId, userId, false);
				} catch (Exception e) {
					this.sender.sendMessage("в твоем говне нету пикчи", chatId, msgId, userId, false);
				}
			} else if (StringUtils.startsWithIgnoreCase(message, "сохра") && user.canCmd(true)) {
				if (rand.nextInt(2) == 0) attachRandomPhoto(chatId, userId, "saved", Main.BOT_ID);
				else attachRandomPhoto(chatId, userId, "saved", 66311705);
			} else if (StringUtils.startsWithIgnoreCase(message, "лоли") && user.canCmd(true)) {
				int r = rand.nextInt(2);
				if (r == 0) attachRandomPhoto(chatId, userId, "wall", -101072212);
				// else if(r == 1) attachRandomPhoto(chatId, userId, "wall",
				// -130148566);
				else if (r == 1) attachRandomPhoto(chatId, userId, "wall", -143737998);
			} else if (StringUtils.startsWithIgnoreCase(message, "татар") && user.canCmd(true)) {
				attachRandomPhoto(chatId, userId, "wall", -131248495);
			} else if (StringUtils.startsWithIgnoreCase(message, "рандом") && user.canCmd(true)) {
				processRandomPhotos(chatId, userId);
			} else if (StringUtils.startsWithIgnoreCase(message, "аниме") && user.canCmd(true)) {
				int r = rand.nextInt(3);
				if (r == 0) attachRandomPhoto(chatId, userId, "237688683", -53849257);
				else if (r == 1) attachRandomPhoto(chatId, userId, "wall", -52068842);
				else if (r == 2) attachRandomPhoto(chatId, userId, "wall", -129066268);
			} else if (StringUtils.startsWithIgnoreCase(message, "хентай") && user.canCmd(true)) {
//				attachRandomPhoto(chatId, userId, "260142294", 261894423);
			} else if (StringUtils.startsWithIgnoreCase(message, "этти") && user.canCmd(true)) {
				int r = rand.nextInt(5);
				// if (r == 0) attachRandomPhoto(chatId, userId, "wall",
				// -121863649);
				// else if (r == 1) attachRandomPhoto(chatId, userId, "wall",
				// -140829635);
				if (r == 0) attachRandomPhoto(chatId, userId, "260391759", 261894423);
				if (r == 1) attachRandomPhoto(chatId, userId, "wall", -129073009);
				else if (r == 2) attachRandomPhoto(chatId, userId, "wall", -127874606);
				else if (r == 3) attachRandomPhoto(chatId, userId, "wall", -143923832);
				else if (r == 4) attachRandomPhoto(chatId, userId, "wall", -132143173);
			} else if (StringUtils.startsWithIgnoreCase(message, "awebm") && user.canCmd(true)) {
				int r = rand.nextInt(2);
				if (r == 0) attachRandomVideoFromWall(chatId, msgId, userId, -102087446);
				else if (r == 1) attachRandomVideoFromWall(chatId, msgId, userId, -141407062);
			} else if (StringUtils.startsWithIgnoreCase(message, "webm") && user.canCmd(true)) {
				int r = rand.nextInt(2);
				if (r == 0) attachRandomVideoFromWall(chatId, msgId, userId, -30316056);
				else if (r == 1) attachRandomVideoFromWall(chatId, msgId, userId, -99126464);
			} else if (StringUtils.startsWithIgnoreCase(message, "videosos") && user.canCmd(true)) {
				attachRandomVideoFromWall(chatId, msgId, userId, -56263398);
			} else if (StringUtils.startsWithIgnoreCase(message, "геи") && user.canCmd(true)) {
//				int random = rand.nextInt(2);
//				if (random == 0) attachRandomPhoto(chatId, userId, "wall", -114090117);
//				else attachRandomPhoto(chatId, userId, "wall", -65653121);
			} else if (StringUtils.startsWithIgnoreCase(message, "евриван") && user.canCmd(true)) {
				// евриван сасать
				String[] msgs = message.split(" ");

				String addMsg = "";
				if (msgs.length > 1) {
					for (int i = 1; i < msgs.length; i++) {
						addMsg += msgs[i] + " ";
					}
				}
				
				if(chatId != 0) {
					VkChat chat = ChatHandler.INSTACNE.getChat(chatId);
					if(chat != null) {
						List<Integer> users = chat.users;
						List<String> usersString = new ArrayList<String>();
						for (Integer userId1 : users) {
							if (userId1.intValue() != Main.BOT_ID) usersString.add(userId1 + "");
						}
						
						String msg1 = "";

						List<UserXtrCounters> vkUsers = vk.users().get(actor).fields(UserField.SCREEN_NAME).userIds(usersString).execute();
						
						for (UserXtrCounters userXtr : vkUsers) {
							String nick = userXtr.getScreenName();
							msg1 += "@" + nick + " ";
						}

						msg1 += addMsg;

						sender.sendMessage(msg1, chatId, msgId, userId, false);
					}
				}

			}
			// else if(StringUtils.startsWithIgnoreCase(message, "тусин") &&
			// user.canCmd(true))
			// {
			// int random = rand.nextInt(1);
			// if(random == 0) attachRandomPhoto(chatId, userId, "wall",
			// -58113365);
			// // else attachRandomPhoto(chatId, userId, "wall", -151021471);
			// }
			else if (StringUtils.startsWithIgnoreCase(message, "скери") && user.canCmd(true)) {
				int random = rand.nextInt(10);
				if (random == 0) attachRandomGif(chatId, userId, -161037346);
				else attachRandomPhoto(chatId, userId, "251070058", Main.OWNER_ID);
			} else if (StringUtils.startsWithIgnoreCase(message, "донейшан") && user.canCmd(true)) {
				// if (chatId == 0) this.sender.sendMessage("на киви плиз
				// 79002292271, если задониш напиши команду \"задонил <смс
				// разрабу>\" и я сообщу о донате дибилу и отправлю твои
				// пожелания", chatId, msgId, userId, false);
			} else if (StringUtils.startsWithIgnoreCase(message, "запиши") && user.canCmd(true)) {
				// this.sender.sendMessage("функция не пашет, нету мани",
				// chatId, msgId, userId, false);
				if (WhiteList.isWhitelisted(userId)) {
					attachVoiceMessage(message, chatId, msgId, userId);
				} else {
					long time = System.currentTimeMillis();
					if (time - user.lastVoiceTime > 1000 * 60) {
						attachVoiceMessage(message, chatId, msgId, userId);
						user.lastVoiceTime = time;
					} else {
						int r = rand.nextInt(3);
						if (r == 0) this.sender.sendMessage("сори братан сейчас не могу", chatId, msgId, userId, false);
						else if (r == 1) this.sender.sendMessage("часто юзаеш отдохни", chatId, msgId, userId, false);
						else if (r == 2) this.sender.sendMessage("сори позже запишу", chatId, msgId, userId, false);
					}
				}
			} else if(StringUtils.startsWithIgnoreCase(message, "savealbum") && user.canCmd(true) && userId == Main.OWNER_ID) {
				PhotoAlbumFull album = vk.photos().createAlbum(actor, "тайтл").groupId(-Main.GROUP_ID).uploadByAdminsOnly(true).commentsDisabled(true).privacyView("only_me").execute();
				
				
				
				
			} else if (StringUtils.startsWithIgnoreCase(message, "инвайти") && user.canCmd(true)) {
				if (chatId != 0) {
					int r = rand.nextInt(3);
					user.setInviteStatus(chatId, true);
					if (r == 0) this.sender.sendMessage("терь ты буш сидеть тут.", chatId, msgId, userId, false);
					else if (r == 1) this.sender.sendMessage("ок сын.", chatId, msgId, userId, false);
					else if (r == 2) this.sender.sendMessage("правильный выбор.", chatId, msgId, userId, false);
				} else {
					this.processAnser("инвайти", chatId, msgId, userId);
				}
			} else if (StringUtils.startsWithIgnoreCase(message, "неинвайти") && user.canCmd(true)) {
				if (chatId != 0) {
					int r = rand.nextInt(3);
					user.setInviteStatus(chatId, false);
					if (r == 0) this.sender.sendMessage("ну и сычуй. инвайтить не буду.", chatId, msgId, userId, false);
					else if (r == 1) this.sender.sendMessage("ок сын.", chatId, msgId, userId, false);
					else if (r == 2) this.sender.sendMessage("ну и вали.", chatId, msgId, userId, false);
				} else {
					this.processAnser("неинвайти", chatId, msgId, userId);
				}
			} else if (StringUtils.startsWithIgnoreCase(message, "нераспознавай") && user.canCmd(true)) {
				if (user.canRecognize) {
					user.canRecognize = false;
					this.sender.sendMessage("наконец отъебался", chatId, msgId, userId, false);
				} else {
					this.sender.sendMessage("ты и так мне не нужон", chatId, msgId, userId, false);
				}
			} else if (StringUtils.startsWithIgnoreCase(message, "распознавай") && user.canCmd(true)) {
				// if (WhiteList.isWhitelisted(userId)) {
				user.canRecognize = true;
				this.sender.sendMessage("я буду слушать твои высеры", chatId, msgId, userId, false);
				// } else {
				// this.sender.sendMessage("сори платная функция, бесплатно раз
				// в 15 минут", chatId, msgId, userId, false);
				// }
			} else if (StringUtils.startsWithIgnoreCase(message, "задонил") && user.canCmd(true)) {
				sender.sendMessage(message.replace("задонил", "вам задонил " + userId), 0, msgId, Main.OWNER_ID, false);
				sender.sendMessage("я насрал дибилу о донате", chatId, msgId, userId, false);
			} else if (StringUtils.startsWithIgnoreCase(message, "высирай") && user.canCmd(true)) {
				if (chatId != 0) {
					enableTalking(message, chatId, msgId, userId);
				} else {
					this.sender.sendMessage("я и так тут с тобой свободно общаюсь", chatId, msgId, userId, false);
				}
			} else if (StringUtils.startsWithIgnoreCase(message, "некрякой") && user.canCmd(true)) {
				if (chatId != 0) {
					disableTalking(message, chatId, msgId, userId);
				} else this.sender.sendMessage("не сри мне в лс тада и не буду крякоть", chatId, msgId, userId, false);
			} else if (StringUtils.containsIgnoreCase(message, " или ") && user.canCmd(false)) {
				processOr(message, chatId, msgId, userId);
			} else if (RaidHandler.INSTANCE.checkCommands(message, user, chatId, msgId, userId)) {
				return;
			} else if (AnonymousChatHandler.INSTANCE.checkCommands(msg, message, user, chatId, msgId, userId)) {
				return;
			} else {
				if (!AnonymousChatHandler.INSTANCE.processMsg(msg, message, user, chatId, msgId, userId)) 
					processTalk(msg, message, action, chatId, msgId, userId);
				//if (chatId == VkChat.LOCAL_CHAT) saver.writeMsg(msg, user.firstName, user.lastName, chatId);
			}

			if (chatId != 0) {
				ChatHandler.INSTACNE.setLastMsg(msg, chatId);
			}

			if (userId == 66311705) {
				processAdminCommands(msg, message, chatId, msgId, userId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isBotName(String message) {
		botNameL = 0;
		if (StringUtils.startsWithIgnoreCase(message, "ыля ") || StringUtils.startsWithIgnoreCase(message, "иля ")) {
			if (message.length() > 3) botNameL = 4;
			else botNameL = 3;
			return true;
		} else if (StringUtils.startsWithIgnoreCase(message, "ыля,") || StringUtils.startsWithIgnoreCase(message, "иля,")) {
			if (message.length() > 4) botNameL = 5;
			else botNameL = 4;
			return true;
		} else if (StringUtils.startsWithIgnoreCase(message, "мыльный")) {
			if (message.length() > 7) botNameL = 8;
			else botNameL = 7;
			return true;
		} else if (StringUtils.startsWithIgnoreCase(message, "мыльный,")) {
			if (message.length() > 8) botNameL = 9;
			else botNameL = 8;
			return true;
		} else if (StringUtils.startsWithIgnoreCase(message, "мылья")) {
			if (message.length() > 5) botNameL = 6;
			else botNameL = 5;
			return true;
		} else if (StringUtils.startsWithIgnoreCase(message, "мылья,")) {
			if (message.length() > 6) botNameL = 7;
			else botNameL = 6;
			return true;
		} else if (StringUtils.startsWithIgnoreCase(message, "мыля")) {
			if (message.length() > 4) botNameL = 5;
			else botNameL = 4;
			return true;
		} else if (StringUtils.startsWithIgnoreCase(message, "мыля,")) {
			if (message.length() > 5) botNameL = 6;
			else botNameL = 5;
			return true;
		} else if (StringUtils.startsWithIgnoreCase(message, "илья")) {
			if (message.length() > 4) botNameL = 5;
			else botNameL = 4;
			return true;
		} else if (StringUtils.startsWithIgnoreCase(message, "илья,")) {
			if (message.length() > 5) botNameL = 6;
			else botNameL = 5;
			return true;
		} else if (StringUtils.startsWithIgnoreCase(message, "илюха")) {
			if (message.length() > 4) botNameL = 5;
			else botNameL = 4;
			return true;
		} else if (StringUtils.startsWithIgnoreCase(message, "илюха,")) {
			if (message.length() > 5) botNameL = 6;
			else botNameL = 5;
			return true;
		} else if (StringUtils.startsWithIgnoreCase(message, "илюша")) {
			if (message.length() > 4) botNameL = 5;
			else botNameL = 4;
			return true;
		} else if (StringUtils.startsWithIgnoreCase(message, "илюша,")) {
			if (message.length() > 5) botNameL = 6;
			else botNameL = 5;
			return true;
		} else if (StringUtils.containsIgnoreCase(message, "илюша") || StringUtils.containsIgnoreCase(message, "илюха") || StringUtils.containsIgnoreCase(message, "ыля") || StringUtils.containsIgnoreCase(message, "мыля") || StringUtils.containsIgnoreCase(message, "мылья") || StringUtils.containsIgnoreCase(message, "илья") || StringUtils.containsIgnoreCase(message, "мыльный") || StringUtils.containsIgnoreCase(message, "иля")) return true;

		return false;
	}

	public String processCapcha(ApiCaptchaException captcha) {
		String image = captcha.getImage();
		capchaSid = captcha.getSid();

		System.out.println("[CAPTCHA] Need captcha: " + captcha.getImage());
		needCaptcha = true;
		boolean reporting = false;

		String CAPCHA_ID;

		while (needCaptcha) {
			HttpClient httpclient = HttpClients.createDefault();
			HttpGet httppost = new HttpGet(captcha.getImage());
			File captchaFile = new File("image.jpg");
			try {
				System.out.println("[CAPTCHA] Staring download captcha image...");
				HttpResponse httpresponse = httpclient.execute(httppost);
				HttpEntity entity = httpresponse.getEntity();

				InputStream instream = entity.getContent();
				System.out.println("[CAPTCHA] Image downloaded");
				BufferedImage image1 = ImageIO.read(instream);
				ImageIO.write(image1, "jpg", captchaFile);
				instream.close();
				System.out.println("[CAPTCHA] Image writed");
				RuCaptcha.API_KEY = "";
				System.out.println("[CAPTCHA] Connect to rucaptcha...");
				String response = RuCaptcha.postCaptcha(captchaFile);

				if (response.startsWith("OK")) {
					long time = System.currentTimeMillis();
					CAPCHA_ID = response.substring(3);
					System.out.println("[CAPTCHA] Success, response: " + response);
					while (true) {
						System.out.println("[CAPTCHA] Rucaptcha gettings decryption...");
						response = RuCaptcha.getDecryption(CAPCHA_ID);
						if (response.equals(RuCaptcha.Responses.CAPCHA_NOT_READY.toString())) {
							System.out.println("[CAPTCHA] Captcha not ready...");
							Thread.sleep(5000);

							if (System.currentTimeMillis() - time > 5 * 60 * 1000) {
								capcha = "1234";
								break;
							} else

							continue;
						} else if (response.startsWith("OK")) {
							reporting = true;
							capcha = response.substring(3);
							System.out.println("[CAPTCHA] Sucess! Captcha is " + capcha);
							break;
						} else {
							System.out.println("[CAPTCHA] ERROR: " + response);
							Thread.sleep(5000);
						}
					}

					needCaptcha = false;
				}
			} catch (ApiCaptchaException e) {
				System.out.println("[CAPTCHA] ERROR bad captcha! Reporting...");
				if (reporting) {
					httppost = new HttpGet("http://rucaptcha.com/res.php?key=" + RuCaptcha.API_KEY + "&action=&id=CAPCHA_ID");
					try {
						HttpResponse httpresponse = httpclient.execute(httppost);
						HttpEntity entity = httpresponse.getEntity();
						String res = EntityUtils.toString(entity);
						if (res.startsWith("OK")) {
							System.out.println("[CAPTCHA] Bad cpatcha reported!");
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(5000);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}

		return capcha;
	}

	private void stopBot() {
		Main.isRun = false;
	}

}
