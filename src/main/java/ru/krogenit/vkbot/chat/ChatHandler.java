package ru.krogenit.vkbot.chat;

import java.util.HashMap;
import java.util.Iterator;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.objects.messages.Chat;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.responses.GetConversationMembersResponse;

import ru.krogenit.vkbot.Main;

public class ChatHandler {
	public static ChatHandler INSTACNE;
	VkApiClient vk;
	UserActor actor;

	public HashMap<Integer, VkChat> chats = new HashMap<Integer, VkChat>();
	public HashMap<Integer, VkChat> groupChats = new HashMap<Integer, VkChat>();

	public ChatHandler(VkApiClient vk, UserActor actor) {
		this.vk = vk;
		this.actor = actor;
		INSTACNE = this;
	}
	
	public void tryUpdateUsers(int chatId) {
		try {
			System.out.println("[CHAT HANDLER] Trying get users " + chatId);
			GetConversationMembersResponse membersResp = vk.messages().getConversationMembers(Main.INSTANCE.group).peerId(2000000000+chatId).execute();
				
			VkChat chat = new VkChat(membersResp.getItems(), chatId);
			groupChats.put(chatId, chat);
		} catch(Exception e) {
			System.out.println("[CHAT HANDLER] No access to chat " + chatId);
		}
		
		
	}

	public VkChat getChat(int chatId) {
		VkChat chat;
		if(Main.isFromGroup) chat = groupChats.get(chatId);
		else chat = chats.get(chatId);

		if (chat == null) {
			System.out.println("[CHAT HANDLER] Creating new chat " + chatId);
			try {
				if(!Main.isFromGroup) {
//					OLD VERSION
					Chat chat1 = vk.messages().getChat(actor).chatId(chatId).execute();
					chat = new VkChat(chat1.getUsers(), chat1.getAdminId(), chat1.getTitle(), chat1.getId());
					chats.put(chatId, chat);
				} else {
					GetConversationMembersResponse membersResp = vk.messages().getConversationMembers(Main.INSTANCE.group).peerId(2000000000+chatId).execute();
					
					System.out.println("[CHAT HANDLER] Got chat from vk " + membersResp);
					chat = new VkChat(membersResp.getItems(), chatId);
					groupChats.put(chatId, chat);
				}
			} catch (Exception e) {
				System.out.println("[CHAT HANDLER] No access to chat " + chatId);
				//e.printStackTrace();
				
				chat = new VkChat(chatId);
				groupChats.put(chatId, chat);
				System.out.println("[CHAT HANDLER] Created chat without users list " + chatId);
			}
		}

		return chat;
	}

	public void update() {
		Iterator<Integer> keys = chats.keySet().iterator();
		long time = System.currentTimeMillis();
		while (keys.hasNext()) {
			Integer key = keys.next();
			VkChat chat = chats.get(key);
			if (chat.canTalk) {
				Message lastMsg = chat.lastMsg;

				if (lastMsg != null && lastMsg.getUserId() != Main.BOT_ID && time - chat.lastMsgDate > 15 * 60 * 1000) {
					// CommandHandler.INSTANCE.processAnser(lastMsg.getBody(),
					// key, lastMsg.getId(), lastMsg.getUserId());
					chat.lastMsgDate = (int) (time / 1000);
				}
			}
		}
	}

	public void setLastMsg(Message msg, int chatId) {
		if (msg.getText() != null && msg.getText().length() > 0) {
			VkChat chat = getChat(chatId);
			chat.lastMsg = msg;
			chat.lastMsgDate = msg.getDate();
		}
	}
}
