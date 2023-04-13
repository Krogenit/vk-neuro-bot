package ru.krogenit.vkbot.command;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.wall.WallPostFull;
import com.vk.api.sdk.objects.wall.responses.GetResponse;

import ru.krogenit.vkbot.user.VkUser;

public class AttachBssPost extends AttachPostCommand {

	public final static int bssGroupId = -99780931;

	public AttachBssPost() {
		super("бсс", bssGroupId);
	}

	private void attachBss24Post(String msg, int chatId, int msgId, int userId, int offset, int randomMax) {
		GetResponse resp = null;
		try {
			resp = vk.wall().get(actor).ownerId(bssGroupId).count(100).offset(offset).execute();
		} catch (ApiException | ClientException e) {
			e.printStackTrace();
		}

		List<WallPostFull> posts = resp.getItems();

		boolean postFound = false;
		while (posts.size() > 0) {
			WallPostFull post = posts.remove(rand.nextInt(posts.size()));

			if (post.getText() != null && StringUtils.startsWithIgnoreCase(post.getText(), "bss24")) {
				attachWallPost(post, chatId, msgId, userId);
				postFound = true;
				break;
			}
		}

		if (!postFound) {
			int random = rand.nextInt(randomMax);
			this.attachBss24Post(msg, chatId, msgId, userId, random, randomMax);
		}
	}

	private void attachBssSearchPost(String msg, int chatId, int msgId, int userId, int offset, String search) {
		GetResponse resp = null;
		try {
			resp = vk.wall().get(actor).ownerId(bssGroupId).count(100).offset(offset).execute();
		} catch (ApiException | ClientException e) {
			e.printStackTrace();
		}

		List<WallPostFull> posts = resp.getItems();

		boolean postFound = false;
		for (WallPostFull post : posts) {
			if (post.getText() != null && StringUtils.containsIgnoreCase(post.getText(), search)) {
				attachWallPost(post, chatId, msgId, userId);
				postFound = true;
				break;
			}
		}

		if (!postFound) {
			int newOffset = offset + 100;

			if (offset > 787) {
				sender.sendMessage("твой высер не найден в бсс", chatId, msgId, userId, false);
				return;
			}

			this.attachBssSearchPost(msg, chatId, msgId, userId, newOffset, search);
		}
	}

	private void attachRandomBssPost(String msg, int chatId, int msgId, int userId) {
		int offset1 = rand.nextInt(getWallSize(bssGroupId));
		GetResponse resp = null;
		try {
			resp = vk.wall().get(actor).ownerId(bssGroupId).count(1).offset(offset1).execute();	
		} catch (ApiException | ClientException e) {
			e.printStackTrace();
		}

		attachWallPost(resp.getItems().get(0), chatId, msgId, userId);
	}

	@Override
	public boolean processCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		String[] words = message.split(" ");
		if (words.length > 1) {
			String mt = words[1];
			if (StringUtils.equalsIgnoreCase(mt, "24")) {
				attachBss24Post(message, chatId, msgId, userId, 0, 1);
			} else {
				String search = "";

				for (int i = 1; i < words.length; i++) {
					search += (words[i] + " ");
				}
				search = search.substring(0, search.length() - 1);

				attachBssSearchPost(message, chatId, msgId, userId, 0, search);
			}
		} else {
			attachRandomBssPost(message, chatId, msgId, userId);
		}

		return true;
	}
}
