package ru.krogenit.vkbot.user;

import java.util.HashMap;

import org.apache.commons.collections4.map.HashedMap;

public class VkUser {
	private int id;
	public String firstName, lastName, displayName;
	public int friend_status;
	public boolean isDeactivated;
	public int last_seen;

	public long lastCommandTime, lastVoiceTime, lastRecogTime;
	public int countCmdUses;

	public boolean canRecognize = true;

	public HashMap<Integer, Boolean> inviteMap = new HashMap<Integer, Boolean>();

	public VkUser(int id, String firstName, String lastName, int isFriend, String displayName) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.friend_status = isFriend;
		this.countCmdUses = 0;
		this.lastCommandTime = 0;
		this.displayName = displayName;
	}

	public boolean isFriend() {
		return friend_status == 3;
	}

	public boolean isFollower() {
		return friend_status == 2;
	}

	public void setInviteStatus(int chatId, boolean can) {
		this.inviteMap.put(chatId, !can);
	}

	public boolean canInviteInChat(int chatId) {
		Boolean b = this.inviteMap.get(chatId);
		boolean canInvite = false;
		if (b != null) canInvite = b.booleanValue();

		return this.isFriend() && !canInvite;
	}

	public boolean canCmd(boolean needFriend) {
		int maxUses = 5;
		long time = System.currentTimeMillis();
		int maxTime = 2500;

		//if (needFriend && !isFriend()) return false;

		long dif = time - lastCommandTime;
		if (dif > maxTime) {
			lastCommandTime = time;
			return true;
		}

		return false;

		// if(countCmdUses > maxUses)
		// {
		// long dif = time - lastCommandTime;
		// if(dif > maxTime)
		// {
		// countCmdUses -= dif/maxTime;
		//
		// if(countCmdUses < 0)
		// countCmdUses = 0;
		//
		// if(countCmdUses > maxUses)
		// return needFriend ? isFriend() : false;
		//
		// return false;
		// }
		//
		// return false;
		// }
		// else
		// {
		// if(needFriend && !isFriend())
		// return false;
		//
		// if(time - lastCommandTime < maxTime)
		// countCmdUses++;
		//
		// // System.out.println(this.firstName + " " + this.lastName + ": " +
		// countCmdUses);
		//
		// lastCommandTime = time;
		//
		// return true;
		// }
	}

	public int getId() {
		return id;
	}
	
	
}
