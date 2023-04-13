package ru.krogenit.vkbot.command;

import com.vk.api.sdk.objects.messages.Message;

import ru.krogenit.vkbot.user.VkUser;

public class GimnBSS extends BotCommand {

	public GimnBSS() {
		super(new String[] {"гимнбсс"}, false);
	}
	
	@Override
	public boolean processCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		//sender.sendMessage("", chatId, msgId, userId, false, null, "436429");
		
		return true;
	}
}
