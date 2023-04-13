package ru.krogenit.vkbot.command;

import java.util.List;

import com.vk.api.sdk.client.Lang;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.queries.users.UserField;
import com.vk.api.sdk.queries.users.UsersNameCase;

import ru.krogenit.vkbot.CommandHandler;
import ru.krogenit.vkbot.Main;
import ru.krogenit.vkbot.chat.ChatHandler;
import ru.krogenit.vkbot.chat.VkChat;
import ru.krogenit.vkbot.user.VkUser;

public class WhoCommand1 extends BotCommand {

	public WhoCommand1() {
		super(new String[] {"кого", "а кого"}, false);
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
			int random = CommandHandler.INSTANCE.rand.nextInt(6);
			if (random == 0) sender.sendMessage("хуле я без прав не могу узнать кто", chatId, msgId, userId, false);
			else if (random == 1) sender.sendMessage("дай права в чате для этой команды заебал", chatId, msgId, userId, false);
			else if (random == 2) sender.sendMessage("плиз дай права на чат", chatId, msgId, userId, false);
			else if (random == 3) sender.sendMessage("для этой команды мне права нужны", chatId, msgId, userId, false);
			return true;
		}
		Integer userId1 = users.get(CommandHandler.INSTANCE.rand.nextInt(users.size()));
		boolean allUsersIsBot = true;
		while (userId1 > 0 && userId1 == Main.BOT_ID && users.size() > 1) {
			userId1 = users.get(CommandHandler.INSTANCE.rand.nextInt(users.size()));
			for(int i=0;i<users.size();i++) {
				if(users.get(i) != Main.BOT_ID && users.get(i) > 0) {
					allUsersIsBot = false;
				}
			}
			
			if(allUsersIsBot) break;
		}
		
		List<UserXtrCounters> user1 = null;
		
		try {
			user1 = CommandHandler.INSTANCE.vk.users().get(CommandHandler.INSTANCE.actor).userIds("" + userId1).fields(UserField.FRIEND_STATUS).lang(Lang.RU).nameCase(UsersNameCase.GENITIVE).execute();
			
		} catch (Exception e) {
			System.out.println("ID: " + userId1);
			e.printStackTrace();
		}
		
		if(user1 != null) {
			UserXtrCounters us = user1.get(0);
			String firstName = us.getFirstName().toLowerCase();
			String lastName = us.getLastName().toLowerCase();
			int random = CommandHandler.INSTANCE.rand.nextInt(6);
			if (random == 0) sender.sendMessage("это короч " + firstName + " " + lastName, chatId, msgId, userId1, false, null, null);
			else if (random == 1) sender.sendMessage("ето " + firstName + " " + lastName, chatId, msgId, userId1, false, null, null);
			else if (random == 2) sender.sendMessage("полюбас это " + firstName + " " + lastName, chatId, msgId, userId1, false, null, null);
			else if (random == 3) sender.sendMessage("сран сказал что ето " + firstName + " " + lastName, chatId, msgId, userId1, false, null, null);
			else if (random == 4) sender.sendMessage("нидумою ето " + firstName + " " + lastName, chatId, msgId, userId1, false, null, null);
			else if (random == 5) sender.sendMessage("наверн " + firstName + " " + lastName, chatId, msgId, userId1, false, null, null);
			return true;
		}
		
		return false;
	}
}
