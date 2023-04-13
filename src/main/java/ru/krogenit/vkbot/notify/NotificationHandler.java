package ru.krogenit.vkbot.notify;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiCaptchaException;
import com.vk.api.sdk.objects.friends.RequestsXtrMessage;
import com.vk.api.sdk.objects.friends.responses.GetRequestsExtendedResponse;
import com.vk.api.sdk.objects.friends.responses.GetRequestsResponse;
import com.vk.api.sdk.objects.notifications.Feedback;
import com.vk.api.sdk.objects.notifications.Notification;
import com.vk.api.sdk.objects.notifications.Reply;
import com.vk.api.sdk.objects.notifications.responses.GetResponse;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.users.User;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostAttachmentType;
import com.vk.api.sdk.queries.users.UserField;

import ru.krogenit.vkbot.CommandHandler;
import ru.krogenit.vkbot.ImageDistortionQueue;
import ru.krogenit.vkbot.Main;
import ru.krogenit.vkbot.user.UserHandler;
import ru.krogenit.vkbot.user.VkUser;

public class NotificationHandler {
	public static NotificationHandler INSTANCE;

	VkApiClient vk;
	UserActor actor;

	int lastNotifyDate;

	public NotificationHandler(VkApiClient vk, UserActor actor) {
		this.vk = vk;
		this.actor = actor;
		INSTANCE = this;

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(".", "lastdate.txt")));
			lastNotifyDate = Integer.parseInt(reader.readLine());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void replyOnWallCaommentWithAttachments(String msg, int wallId, int replyId, int userId, List<String> atts) throws Exception {
		try {
			vk.wall().createComment(actor, wallId).ownerId(userId).attachments(atts).replyToComment(replyId).message(msg).execute();
		} catch (ApiCaptchaException captcha) {
			System.out.println("[CAPTCHA] Comment error");
			CommandHandler.INSTANCE.processCapcha(captcha);

			try {
				vk.wall().createComment(actor, wallId).ownerId(userId).attachments(atts).replyToComment(replyId).message(msg).captchaKey(CommandHandler.INSTANCE.capcha).captchaSid(CommandHandler.INSTANCE.capchaSid).execute();
				CommandHandler.INSTANCE.capcha = null;
				System.out.println("[CAPTCHA] Success comment");
			} catch (ApiCaptchaException captcha1) {
				System.out.println("[CAPTCHA] Upload photo failed");
				return;
			}
		}
	}

	public void replyOnWallComment(String msg, int wallId, int replyId, int userId) throws Exception {
		try {
			vk.wall().createComment(actor, wallId).ownerId(userId).replyToComment(replyId).message(msg).execute();
		} catch (ApiCaptchaException captcha) {
			System.out.println("[CAPTCHA] Comment error");
			CommandHandler.INSTANCE.processCapcha(captcha);

			try {
				vk.wall().createComment(actor, wallId).ownerId(userId).replyToComment(replyId).message(msg).captchaKey(CommandHandler.INSTANCE.capcha).captchaSid(CommandHandler.INSTANCE.capchaSid).execute();
				CommandHandler.INSTANCE.capcha = null;
				System.out.println("[CAPTCHA] Success comment");
			} catch (ApiCaptchaException captcha1) {
				System.out.println("[CAPTCHA] Upload photo failed");
				return;
			}
		}
	}

	public void sendCommentToWallWithAttachments(String msg, int wallId, int userId, List<String> atts) throws Exception {
		try {
			vk.wall().createComment(actor, wallId).ownerId(userId).attachments(atts).message(msg).execute();
		} catch (ApiCaptchaException captcha) {
			System.out.println("[CAPTCHA] Comment error");
			CommandHandler.INSTANCE.processCapcha(captcha);

			try {
				vk.wall().createComment(actor, wallId).ownerId(userId).message(msg).attachments(atts).captchaKey(CommandHandler.INSTANCE.capcha).captchaSid(CommandHandler.INSTANCE.capchaSid).execute();
				CommandHandler.INSTANCE.capcha = null;
				System.out.println("[CAPTCHA] Success comment");
			} catch (ApiCaptchaException captcha1) {
				System.out.println("[CAPTCHA] Upload photo failed");
				return;
			}
		}
	}

	public void sendCommentToWall(String msg, int wallId, int userId) throws Exception {
		try {
			vk.wall().createComment(actor, wallId).message(msg).ownerId(userId).execute();
		} catch (ApiCaptchaException captcha) {
			System.out.println("[CAPTCHA] Comment error");
			CommandHandler.INSTANCE.processCapcha(captcha);

			try {
				vk.wall().createComment(actor, wallId).ownerId(userId).message(msg).captchaKey(CommandHandler.INSTANCE.capcha).captchaSid(CommandHandler.INSTANCE.capchaSid).execute();
				CommandHandler.INSTANCE.capcha = null;
				System.out.println("[CAPTCHA] Success comment");
			} catch (ApiCaptchaException captcha1) {
				System.out.println("[CAPTCHA] Upload photo failed");
				return;
			}
		}
	}

	private void processDistortionWallPhoto(ImageDistortionQueue.ImageProcType type, Feedback feedBack, int wallId, int replyId, int userId) throws Exception {
		List<WallpostAttachment> atts = feedBack.getAttachments();
		if (atts.size() == 0) {
			sendCommentToWall("саси", wallId, userId);
			return;
		}

		WallpostAttachment att = atts.get(0);

		if (att.getType() != WallpostAttachmentType.PHOTO) {
			sendCommentToWall("саси", wallId, userId);
			return;
		}

		Photo photo = att.getPhoto();

		try {
			URL url = null;
			if (photo.getPhoto807() != null) url = new URL(photo.getPhoto807());
			else if (photo.getPhoto604() != null) url = new URL(photo.getPhoto604());

			Image image1 = ImageIO.read(url);
			ImageDistortionQueue.INSTANCE.addImageFromWall(type, image1, wallId, replyId, userId, false);
		} catch (ConnectException e1) {
			processDistortionWallPhoto(type, feedBack, wallId, replyId, userId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("[NOTIFICATIONS] Reply to new wall post command");
	}
	
	private long lastFriendsAndFollowersCheck;

	public void processNotifications() throws Exception {
		int newlastdate = 0;
		try {
			CommandHandler cmd = CommandHandler.INSTANCE;
			UserHandler userHandler = UserHandler.INSTACNE;

			long time = System.currentTimeMillis();
			if(time - lastFriendsAndFollowersCheck > 60*60*24*1000) {
				lastFriendsAndFollowersCheck = time;
				Thread t = new Thread() {
					public void run() {
						try {
							System.out.println("Removing blocked or not active users...");
							int offset = 0;
							int countFirends = 5000;
							while(true) {
								System.out.println("Gettings friends with offset " + offset);
								com.vk.api.sdk.objects.friends.responses.GetResponse response = vk.friends().get(actor).fields(UserField.LAST_SEEN, UserField.DEACTIVATED).offset(offset).count(countFirends).execute();
								Thread.sleep(333);
								List<UserXtrCounters> users1 = response.getItems();
								int count = 10;
								int curTime = (int) (System.currentTimeMillis() / 1000);
								if(users1 == null || response.getCount() == 0 || users1.size() == 0) break;
								System.out.println("Processing "+users1.size()+" users");
								for(UserXtrCounters user : users1) {
									if(user != null) {
										if(user.getDeactivated() != null) {
											count--;
											vk.friends().delete(actor, user.getId()).execute();
											Thread.sleep(333);
											System.out.println("Removed for deactivated reason " + user.getFirstName() + " " + user.getLastName());
										} else if(user.getLastSeen() != null) {
											int dif = curTime - user.getLastSeen().getTime();
											int maxTime = 60 * 60 * 24 * 30 * 2;//2 months
											if(dif > maxTime) {
												count--;
												vk.friends().delete(actor, user.getId()).execute();
												Thread.sleep(333);
												System.out.println("Removed for inactive page " + user.getFirstName() + " " + user.getLastName());
											}
										}
									}
									if(count <= 0) break;
								}
								offset += countFirends;
							}
							
							System.out.println("Checking followers...");
							offset = 0;
							int countToReturn = 1000;
							while(true) {
								System.out.println("Getting new requests with offset " + offset);
								GetRequestsExtendedResponse response = vk.friends().getRequestsExtended(actor).unsafeParam("need_viewed", 1).count(countToReturn).offset(offset).execute();
								Thread.sleep(333);
								List<RequestsXtrMessage> requests = response.getItems();
								if(requests == null || response.getCount() == 0 || requests.size() == 0) break;
								System.out.println("Processing " + requests.size() + " requests");
								for(RequestsXtrMessage request : requests) {
									Integer requestUserId = request.getUserId();
									List<UserXtrCounters> users = vk.users().get(actor).userIds(requestUserId.toString()).fields(UserField.DEACTIVATED, UserField.LAST_SEEN).execute();
									Thread.sleep(333);
									if(users != null && users.size() > 0) {
										UserXtrCounters userCounter = users.get(0);
										int curTime = (int) (System.currentTimeMillis() / 1000);
										if(userCounter != null) {
											if(userCounter.getDeactivated() != null) {
												
											} else if(userCounter.getLastSeen() != null) {
												int dif = curTime - userCounter.getLastSeen().getTime();
												int maxTime = 60 * 60 * 24 * 30 * 2;//2 months
												if(dif > maxTime) {
													
												} else {
													vk.friends().add(actor, requestUserId).execute();
													System.out.println("User " + userCounter.getFirstName() + " " + userCounter.getLastName() + " added to firends");
													Thread.sleep(333);
												}
											}
										}

									}
								}
								offset += countToReturn;
							}
						} catch(Exception e) {
							e.printStackTrace();
						}
					};
				};
				t.setName("Thread friends and followers");
				t.start();
			}
			
			System.out.println("Removing outgoing requests...");
			GetRequestsResponse resp = vk.friends().getRequests(actor).out(true).execute();
			List<Integer> outgoings = resp.getItems();
			for (Integer userId : outgoings) {
				vk.friends().delete(actor, userId).execute();
			}
			
			GetResponse notificationsresp = vk.notifications().get(actor).execute();
			List<Notification> notifications = notificationsresp.getItems();
			List<User> users = notificationsresp.getProfiles();

			for (Notification notify : notifications) {
				if (notify.getDate() <= lastNotifyDate) continue;

				String type = notify.getType();
				if(type == null) continue;
				Feedback feedBack = notify.getFeedback();
				Reply reply = notify.getReply();
				if (type.equals("wall")) {
					String text = feedBack.getText();
					int userId = feedBack.getFromId();
					VkUser user = UserHandler.INSTACNE.getUser(userId);

					if (text == null || text.length() == 0) {
						text = "я тебе тут на стену насрал";
					}

					int ownerId = Main.BOT_ID;

					if (StringUtils.startsWithIgnoreCase(text, "разъеби") && user.canCmd(true)) {
						processDistortionWallPhoto(ImageDistortionQueue.ImageProcType.Dist, feedBack, feedBack.getId(), 0, ownerId);
					} else if (StringUtils.startsWithIgnoreCase(text, "сожми") && user.canCmd(true)) {
						processDistortionWallPhoto(ImageDistortionQueue.ImageProcType.Jpeg, feedBack, feedBack.getId(), 0, ownerId);
					} else {
						String out = cmd.neuro.newMessage(text);
						sendCommentToWall(out, feedBack.getId(), ownerId);
						System.out.println("[NOTIFICATIONS] Reply to new wall post " + text + ": " + out);
					}
				} else if (type.equals("reply_comment")) {
					String text = feedBack.getText();
					int userId = feedBack.getFromId();
					VkUser user = UserHandler.INSTACNE.getUser(userId);

					if (text == null || text.length() == 0) {
						text = "я тебе тут на коммент ответил";
					}

					JsonObject json = notify.getParent();
					JsonObject post = (JsonObject) json.get("post");
					JsonElement postId = post.get("id");
					JsonElement ownerIdjson = post.get("to_id");

					int ownerId = ownerIdjson.getAsInt();

					if (StringUtils.startsWithIgnoreCase(text, "разъеби") && user.canCmd(true)) {
						processDistortionWallPhoto(ImageDistortionQueue.ImageProcType.Dist, feedBack, postId.getAsInt(), feedBack.getId(), ownerId);
					} else if (StringUtils.startsWithIgnoreCase(text, "сожми") && user.canCmd(true)) {
						processDistortionWallPhoto(ImageDistortionQueue.ImageProcType.Jpeg, feedBack, postId.getAsInt(), feedBack.getId(), ownerId);
					} else {
						String out = cmd.neuro.newMessage(text);
						replyOnWallComment(out, postId.getAsInt(), feedBack.getId(), ownerId);
						System.out.println("[NOTIFICATIONS] Reply to comment " + text + ": " + out);
					}
				} else if (type.equals("mention")) {
					String text = feedBack.getText();
					int userId = feedBack.getFromId();
					VkUser user = UserHandler.INSTACNE.getUser(userId);

					if (text == null || text.length() == 0) {
						text = "ыля";
					}

					int ownerId = feedBack.getToId();

					if (StringUtils.containsIgnoreCase(text, "разъеби") && user.canCmd(true)) {
						processDistortionWallPhoto(ImageDistortionQueue.ImageProcType.Dist, feedBack, feedBack.getId(), 0, ownerId);
					} else if (StringUtils.containsIgnoreCase(text, "сожми") && user.canCmd(true)) {
						processDistortionWallPhoto(ImageDistortionQueue.ImageProcType.Jpeg, feedBack, feedBack.getId(), 0, ownerId);
					} else {
						String out = cmd.neuro.newMessage(text);
						sendCommentToWall(out, feedBack.getId(), ownerId);
						System.out.println("[NOTIFICATIONS] Reply to mention wall " + text + ": " + out);
					}
				} else if (type.equals("mention_comments")) {
					String text = feedBack.getText();
					int userId = feedBack.getFromId();
					VkUser user = UserHandler.INSTACNE.getUser(userId);

					if (text == null || text.length() == 0) {
						text = "ыля";
					}

					JsonObject json = notify.getParent();
					JsonElement postId = json.get("id");
					JsonElement ownerIdjson = json.get("to_id");

					int ownerId = ownerIdjson.getAsInt();

					if (StringUtils.containsIgnoreCase(text, "разъеби") && user.canCmd(true)) {
						processDistortionWallPhoto(ImageDistortionQueue.ImageProcType.Dist, feedBack, postId.getAsInt(), feedBack.getId(), ownerId);
					} else if (StringUtils.containsIgnoreCase(text, "сожми") && user.canCmd(true)) {
						processDistortionWallPhoto(ImageDistortionQueue.ImageProcType.Jpeg, feedBack, postId.getAsInt(), feedBack.getId(), ownerId);
					} else {
						String out = cmd.neuro.newMessage(text);
						replyOnWallComment(out, postId.getAsInt(), feedBack.getId(), ownerId);
						System.out.println("[NOTIFICATIONS] Reply to mention comment " + text + ": " + out);
					}
				} else if (type.equals("reply_comment_photo")) {

				} else if (type.equals("reply_comment_video")) {

				} else if (type.equals("reply_topic")) {

				} else if (type.equals("comment_post")) {
					String text = feedBack.getText();
					if (text == null || text.length() == 0) {
						text = "комментарий к посту";
					}

					JsonObject json = notify.getParent();
					JsonElement id = json.get("id");
					JsonElement ownerIdjson = json.get("to_id");

					int ownerId = ownerIdjson.getAsInt();

					String out = cmd.neuro.newMessage(text);
					replyOnWallComment(out, id.getAsInt(), feedBack.getId(), ownerId);
					System.out.println("[NOTIFICATIONS] Reply to wall comment " + text + ": " + out);
				} else if (type.equals("comment_photo")) {
					String text = feedBack.getText();
					if (text == null || text.length() == 0) {
						text = "комментарий к фото";
					}

					JsonObject json = notify.getParent();
					JsonElement id = json.get("id");
					JsonElement ownerIdjson = json.get("owner_id");

					int ownerId = ownerIdjson.getAsInt();

					String out = cmd.neuro.newMessage(text);
					vk.photos().createComment(actor, id.getAsInt()).replyToComment(feedBack.getId()).message(out).execute();
					System.out.println("[NOTIFICATIONS] Reply to photo comment " + text + ": " + out);
				} else if (type.equals("comment_video")) {

				} else if (type.equals("follow")) {
					for (User user : users) {
						VkUser user1 = userHandler.updateUser(user.getId());
						if (user1.isFollower() && user.getId() != Main.BOT_ID) {
							user1.friend_status = 3;
							vk.friends().add(actor, user.getId()).execute();
							System.out.println("[NOTIFICATIONS] User " + user1.firstName + " " + user1.lastName + " added to friends");
						}
					}
				}

				if (notify.getDate() > newlastdate) newlastdate = notify.getDate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (newlastdate != 0) {
				saveLastNotify(newlastdate);
				Date d = new Date(newlastdate * 1000);
				System.out.println("[NOTIFY] SAVED NEW DATE " + d.toString());
			}
		}
	}

	private void saveLastNotify(int date) throws IOException {
		lastNotifyDate = date;
		FileWriter writer = new FileWriter(new File(".", "lastdate.txt"));
		writer.write("" + lastNotifyDate);
		writer.close();
	}
}
