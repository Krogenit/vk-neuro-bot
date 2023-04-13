package ru.krogenit.vkbot.command;

import com.vk.api.sdk.objects.messages.Message;

import ru.krogenit.vkbot.MessageSender;
import ru.krogenit.vkbot.user.VkUser;

public class Repeat extends BotCommand {


	public Repeat() {
		super(new String[] {"повтори", "скажи", "скажы", "скожы"}, false);
	}
	
	@Override
	public boolean checkCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		return super.checkCommand(msg, message, chatId, msgId, userId, user);
	}
	
	@Override
	public boolean processCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		try {
			int index = message.indexOf(" ");
			if(index != -1) {
				String s = message.substring(index);
				MessageSender.INSTANCE.sendMessage(s, chatId, msgId, userId, false, null, null);
				return true;
			} 
			return false;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
