package ru.krogenit.vkbot.command;

import com.vk.api.sdk.objects.messages.Message;

import ru.krogenit.vkbot.user.VkUser;

public class ChanceCommand extends BotCommand {
	
	public ChanceCommand() {
		super(new String[] {"шанс", "инфа"}, false);
	}
	
	@Override
	public boolean processCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		int random = rand.nextInt(25);
		int r = 0;
		if(random == 0) {
			r = rand.nextInt(150);
		} else {
			r = rand.nextInt(100);
		}

		int r1 = rand.nextInt(3);
		if (r1 == 0) sender.sendMessage("инфа " + r + "%", chatId, msgId, userId, false);
		else if (r1 == 1) sender.sendMessage("шанс " + r + "%", chatId, msgId, userId, false);
		else if (r1 == 2) sender.sendMessage("вероятность " + r + "%", chatId, msgId, userId, false);
		
		return true;
	}
}
