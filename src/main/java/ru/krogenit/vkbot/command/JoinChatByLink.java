package ru.krogenit.vkbot.command;

import java.util.ArrayList;
import java.util.List;

import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.MessageAttachment;
import com.vk.api.sdk.objects.messages.MessageAttachmentType;

import ru.krogenit.vkbot.CommandHandler;
import ru.krogenit.vkbot.Main;
import ru.krogenit.vkbot.user.VkUser;

public class JoinChatByLink extends BotCommand {
	
	List<String> links;

	public JoinChatByLink() {
		super("", false);
		links = new ArrayList<String>();
	}

	@Override
	public boolean checkCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		if (!Main.isFromGroup && msg.getAttachments() != null && msg.getAttachments().size() > 0) {
			for (MessageAttachment att : msg.getAttachments()) {
				if (att.getType() == MessageAttachmentType.LINK && att.getLink().getUrl().contains("vk.me/join") && !links.contains(att.getLink().getUrl())) {
					try {
						CommandHandler.INSTANCE.vk.messages().joinChatByInviteLink(actor, att.getLink().getUrl()).execute();
						links.add(att.getLink().getUrl());
						
					} catch (Exception e) {

					}
				}
			}
		}

		return false;
	}

	@Override
	public boolean processCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		return false;
	}

}
