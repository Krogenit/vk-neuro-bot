package ru.krogenit.vkbot.command;

import com.vk.api.sdk.objects.messages.Message;

import ru.krogenit.vkbot.CommandHandler;
import ru.krogenit.vkbot.MessageSender;
import ru.krogenit.vkbot.user.VkUser;

public class SimpleAnswer extends BotCommand {

	private String[] answer;
	
	public SimpleAnswer(String[] cmd, String[] answer) {
		super(cmd, false);
		this.answer = answer;
	}
	
	public SimpleAnswer(String cmd, String[] answer) {
		super(cmd, false);
		this.answer = answer;
	}
	
	@Override
	public boolean checkCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		return super.checkCommand(msg, message, chatId, msgId, userId, user);
	}
	
	@Override
	public boolean processCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		MessageSender.INSTANCE.sendMessage(answer[CommandHandler.INSTANCE.rand.nextInt(answer.length)], chatId, msgId, userId, false, null, null);
		return true;
	}

}
