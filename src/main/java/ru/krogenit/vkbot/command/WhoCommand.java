package ru.krogenit.vkbot.command;

import java.util.List;

import com.vk.api.sdk.client.Lang;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.queries.users.UserField;

import ru.krogenit.vkbot.CommandHandler;
import ru.krogenit.vkbot.Main;
import ru.krogenit.vkbot.MessageSender;
import ru.krogenit.vkbot.chat.ChatHandler;
import ru.krogenit.vkbot.chat.VkChat;
import ru.krogenit.vkbot.user.VkUser;

public class WhoCommand extends BotCommand {

	public WhoCommand() {
		super(new String[] {"кто", "а кто"}, false);
	}
	
	@Override
	public boolean checkCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		return super.checkCommand(msg, message, chatId, msgId, userId, user);
	}
	
	@Override
	public boolean processCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		if(chatId == 0) return false;
		VkChat chat = ChatHandler.INSTACNE.getChat(chatId);
		List<Integer> users = chat.users;
		
		if(users == null) {
			ChatHandler.INSTACNE.tryUpdateUsers(chatId);
		}
		
		if(users == null) {
			int random = rand.nextInt(6);
			if (random == 0) sender.sendMessage("хуле я без прав не могу узнать кто", chatId, msgId, userId, false);
			else if (random == 1) sender.sendMessage("дай права в чате для этой команды заебал", chatId, msgId, userId, false);
			else if (random == 2) sender.sendMessage("плиз дай права на чат", chatId, msgId, userId, false);
			else if (random == 3) sender.sendMessage("для этой команды мне права нужны", chatId, msgId, userId, false);
			return true;
		}
		Integer userId1 = users.get(rand.nextInt(users.size()));
		while (userId1 == -Main.GROUP_ID && users.size() > 1) userId1 = users.get(rand.nextInt(users.size()));
		List<UserXtrCounters> user1 = null;
		
		try {
			user1 = vk.users().get(actor).userIds("" + userId1).fields(UserField.FRIEND_STATUS).lang(Lang.RU).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(user1 != null) {
			UserXtrCounters us = user1.get(0);
			String firstName = us.getFirstName().toLowerCase();
			String lastName = us.getLastName().toLowerCase();
			int random = CommandHandler.INSTANCE.rand.nextInt(6);
			if (random == 0) MessageSender.INSTANCE.sendMessage("это короч " + firstName + " " + lastName, chatId, msgId, userId1, false, null, null);
			else if (random == 1) MessageSender.INSTANCE.sendMessage("ето " + firstName + " " + lastName, chatId, msgId, userId1, false, null, null);
			else if (random == 2) MessageSender.INSTANCE.sendMessage("полюбас это " + firstName + " " + lastName, chatId, msgId, userId1, false, null, null);
			else if (random == 3) MessageSender.INSTANCE.sendMessage("сран сказал что ето " + firstName + " " + lastName, chatId, msgId, userId1, false, null, null);
			else if (random == 4) MessageSender.INSTANCE.sendMessage("нидумою ето " + firstName + " " + lastName, chatId, msgId, userId1, false, null, null);
			else if (random == 5) MessageSender.INSTANCE.sendMessage("наверн " + firstName + " " + lastName, chatId, msgId, userId1, false, null, null);

			return true;
		}
		
		return false;
	}
}
