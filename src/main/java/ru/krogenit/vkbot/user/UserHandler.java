package ru.krogenit.vkbot.user;

import java.util.HashMap;
import java.util.List;

import com.vk.api.sdk.client.Lang;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.queries.users.UserField;

public class UserHandler {
	public static UserHandler INSTACNE;
	VkApiClient vk;
	UserActor actor;

	public HashMap<Integer, VkUser> users = new HashMap<Integer, VkUser>();

	public UserHandler(VkApiClient vk, UserActor actor) {
		this.vk = vk;
		this.actor = actor;
		INSTACNE = this;
	}

	public VkUser updateUser(int userId) {
		if(userId == 0) return null;
		
		VkUser user = null;

		try {
			List<UserXtrCounters> user1 = vk.users().get(actor).userIds("" + userId).fields(UserField.FRIEND_STATUS, UserField.SCREEN_NAME).lang(Lang.RU).execute();

			UserXtrCounters us = user1.get(0);

			String firstName = us.getFirstName().toLowerCase();
			String lastName = us.getLastName().toLowerCase();
			Integer isFriend = us.getFriendStatus();
			String displayName = us.getScreenName();

			user = new VkUser(userId, firstName, lastName, isFriend, displayName);
			users.put(userId, user);
		} catch (Exception e) {
			System.out.println("USERID INVALID " + userId);
			e.printStackTrace();
		}

		return user;
	}

	public VkUser getUser(int userId) {
		VkUser user = users.get(userId);

		if (user == null) { return updateUser(userId); }

		return user;
	}
}
