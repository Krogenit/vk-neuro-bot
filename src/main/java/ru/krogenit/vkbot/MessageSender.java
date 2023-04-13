package ru.krogenit.vkbot;

import java.util.List;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiCaptchaException;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ApiMessagesDenySendException;
import com.vk.api.sdk.exceptions.ApiMessagesPrivacyException;
import com.vk.api.sdk.exceptions.ApiPermissionException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.queries.messages.MessagesSendQuery;
import com.vk.api.sdk.queries.messages.MessagesSetActivityType;

public class MessageSender {

	public static MessageSender INSTANCE;
	public VkApiClient vk;
	public UserActor actor;
	public GroupActor group;
	
	public MessageSender(VkApiClient vk, UserActor actor, GroupActor group) {
		INSTANCE = this;
		this.vk = vk;
		this.actor = actor;
		this.group = group;
	}
	
	public boolean sendMessage(String msg, int chatId, int msgId, int userId, boolean foward) {
		if (msg.length() > 4096) {
			String msg1 = msg.substring(0, 4096);
			sendMessage(msg1, chatId, msgId, userId, foward, null, null);
			msg = msg.substring(4096);
			return sendMessage(msg, chatId, msgId, userId, foward, null, null);
		} else {
			return sendMessageLover4096(msg, chatId, msgId, userId, foward, null, null);
		}
	}

	public boolean sendMessage(String msg, int chatId, int msgId, int userId, boolean foward, List<String> att, String forwardMessage) {
		if (msg.length() > 4096) {
			String msg1 = msg.substring(0, 4096);
			sendMessage(msg1, chatId, msgId, userId, foward, att, forwardMessage);
			msg = msg.substring(4096);
			return sendMessage(msg, chatId, msgId, userId, foward, att, forwardMessage);
		} else {
			return sendMessageLover4096(msg, chatId, msgId, userId, foward, att, forwardMessage);
		}
	}

	public boolean sendMessageLover4096(String msg, int chatId, int msgId, int userId, boolean foward, List<String> att, String forwardMessage) {
		if (chatId == 0) {
			return sendMessageToUser(msg, msgId, userId, foward, att, forwardMessage);
		} else {
			return sendMessageToChat(msg, msgId, chatId, foward, att, forwardMessage);
		}
	}

	public boolean sendMessageToUser(String msg, int msgId, int userId, boolean foward, List<String> att, String forwardMessage) {
		MessagesSendQuery query;
		if(Main.isFromGroup) query = vk.messages().send(group).peerId(userId).randomId(CommandHandler.INSTANCE.rand.nextInt());
		else {
			try {
				vk.messages().setActivity(actor).peerId(userId).type(MessagesSetActivityType.TYPING).execute();
			} catch (ApiException e) {
				return false;
			} catch (ClientException e) {
				return false;
			}
			query = vk.messages().send(actor).peerId(userId).randomId(CommandHandler.INSTANCE.rand.nextInt());
		}
		if (foward) query = query.forwardMessages("" + msgId);
		if (att != null && att.size() > 0) query = query.attachment(att);
		if (forwardMessage != null) query = query.forwardMessages(forwardMessage);
		if(msg != null && msg.length() > 0) query = query.message(msg);

		return executeSendMessage(query, msg, att, userId);
	}

	public boolean sendMessageToChat(String msg, int msgId, int chatId, boolean foward, List<String> att, String forwardMessage) {
		MessagesSendQuery query;
		if(Main.isFromGroup) {
			query = vk.messages().send(group).peerId(chatId + 2000000000).randomId(CommandHandler.INSTANCE.rand.nextInt());
		} else {
			try {
				vk.messages().setActivity(actor).peerId(chatId + 2000000000).type(MessagesSetActivityType.TYPING).execute();
			} catch(ApiPermissionException e) {
				System.out.println("[Sender] No permissions to send message");
			} catch (ApiMessagesDenySendException | ApiMessagesPrivacyException e) {
				System.out.println("[Sender] No access to send message");
			} catch (Exception e) {
				return false;
			}
			query = vk.messages().send(actor).peerId(chatId + 2000000000).randomId(CommandHandler.INSTANCE.rand.nextInt());
		}
		if (foward) query = query.forwardMessages("" + msgId);
		if (att != null && att.size() > 0) query = query.attachment(att);
		if (forwardMessage != null) query = query.forwardMessages(forwardMessage);
		if(msg != null && msg.length() > 0) query = query.message(msg);

		return executeSendMessage(query, msg, att, chatId);
	}

	public boolean executeSendMessage(MessagesSendQuery query, String msg, List<String> att, int chatId) {
		try {
			query.execute();
			logMessage(msg, att, chatId);
			
			return true;
		} catch (ApiCaptchaException ecap) {
			String capcha = CommandHandler.INSTANCE.processCapcha(ecap);
			try {
				query.captchaKey(capcha).captchaSid(ecap.getSid()).execute();
				logMessage(msg, att, chatId);
				
				return true;
			} catch (Exception e) {
			}
		} catch(ApiPermissionException e) {
			System.out.println("[Sender] No permissions to send message");
		}catch (ApiMessagesDenySendException | ApiMessagesPrivacyException e) {
			System.out.println("[Sender] No access to send message");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private void logMessage(String msg, List<String> att, int chatId) {
		String atts = "";
		String out = msg;
		if(out == null) out = "";
		if(att != null) {
			atts = " [Atts: ";
			for(String s : att) {
				atts += s + " ";
			}
			atts += "]";
		}
		
		if(chatId == 0) {
			System.out.println("[Message] [UserId " + chatId + "] " + out + atts);
		} else {
			System.out.println("[Message] [ChatId " + chatId + "] " + out + atts);
		}
	}
}
