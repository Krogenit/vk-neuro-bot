package ru.krogenit.vkbot.chat;

import java.util.ArrayList;
import java.util.List;

import com.vk.api.sdk.objects.messages.ConversationMember;
import com.vk.api.sdk.objects.messages.Message;

import ru.krogenit.vkbot.CommandHandler;
import ru.krogenit.vkbot.Main;

public class VkChat {
	public final static int LOCAL_CHAT = 3;

	public List<Integer> users;
	public List<Integer> admin;
	public String title;
	public int chatId;

	public boolean canTalk;
	public int talkChance;

	public Message lastMsg;
	public int lastMsgDate;
	public long talkStartTime;

	public long lastBotMessage;
	public int sendsCountPerTime;
	
	public VkChat(int chatId) {
		this.chatId = chatId;
		this.admin = null;
		this.users = null;
	}

	public VkChat(List<ConversationMember> members, int chatId) {
		this.users = new ArrayList<Integer>();
		this.admin = new ArrayList<Integer>();
		for (ConversationMember member : members) {
			this.users.add(member.getMemberId());
			if (member.getIsAdmin() != null && member.getIsAdmin()) {
				this.admin.add(member.getMemberId());
			}
		}

		this.title = "unknown";
		this.chatId = chatId;
	}

	public VkChat(List<Integer> users, Integer admin, String title, int chatId) {
		this.users = new ArrayList<Integer>(users);
		this.admin = new ArrayList<Integer>();
		this.admin.add(admin);
		this.title = title;
		this.chatId = chatId;
	}
	
	public boolean isAdmin(Integer adminId) {
		for(Integer adminId1 : admin) {
			if(adminId1.equals(adminId)) return true;
		}
		
		return false;
	}
	
	public boolean isLocalChat() {
		int localChat = Main.isFromGroup ? 2 : LOCAL_CHAT;
		return chatId == localChat;
	}

	public boolean canTalk() {
		int maxUses = 2;
		long time = System.currentTimeMillis();
		int localChat = Main.isFromGroup ? 2 : LOCAL_CHAT;
		int maxTime = chatId == localChat ? 2000 : 4000;

		if (canTalk) {
			if (chatId != localChat && time - talkStartTime > 30 * 60 * 1000) {
				canTalk = false;
			}

			long dif = time - lastBotMessage;
			if (dif > maxTime) {
				lastBotMessage = time;
				return true;
			}
		} else {
			if(chatId == localChat && CommandHandler.INSTANCE.rand.nextInt(25) == 0) {
				return true;
			} else if(CommandHandler.INSTANCE.rand.nextInt(100) == 0) {
				return true;
			}
		}

		return false;

		// if(sendsCountPerTime > maxUses)
		// {
		// long dif = time - lastBotMessage;
		// if(dif > maxTime)
		// {
		// sendsCountPerTime -= dif/maxTime;
		//
		// if(sendsCountPerTime < 0)
		// sendsCountPerTime = 0;
		//
		// if(sendsCountPerTime > maxUses)
		// return canTalk;
		//
		// return false;
		// }
		//
		// return false;
		// }
		// else
		// {
		// if(!canTalk)
		// return false;
		//
		// if(time - lastBotMessage < maxTime)
		// sendsCountPerTime++;
		//
		// lastBotMessage = time;
		//
		// return true;
		// }
	}
}
