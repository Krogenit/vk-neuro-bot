package ru.krogenit.vkbot.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.objects.messages.Message;

import ru.krogenit.vkbot.CommandHandler;
import ru.krogenit.vkbot.MessageSender;
import ru.krogenit.vkbot.user.VkUser;

public class BotCommand {
	
	protected boolean friendsOnly;
	protected List<String> commandNames;
	protected UserActor actor = CommandHandler.INSTANCE.actor;
	protected GroupActor group = CommandHandler.INSTANCE.group;
	protected VkApiClient vk = CommandHandler.INSTANCE.vk;
	protected MessageSender sender = MessageSender.INSTANCE;
	protected Random rand = CommandHandler.INSTANCE.rand;
	
	protected String usedCommand;
	
	public BotCommand(String commandName, boolean friendsOnly) {
		this.commandNames = new ArrayList<String>();
		this.commandNames.add(commandName);
		this.friendsOnly = friendsOnly;
	}
	
	public BotCommand(String[] commandNames, boolean friendsOnly) {
		this.commandNames = new ArrayList<String>();
		for(int i=0;i<commandNames.length;i++) this.commandNames.add(commandNames[i]);
		this.friendsOnly = friendsOnly;
	}
	
	public boolean checkCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		boolean findCommand = false;
		for(String command : commandNames) {
			if(StringUtils.startsWithIgnoreCase(message, command)) {
				usedCommand = command;
				findCommand = true;
				break;
			}
		}
		
		return findCommand && user.canCmd(friendsOnly) && processCommand(msg, message, chatId, msgId, userId, user);
	}
	
	public boolean processCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		return true;
	}
}
