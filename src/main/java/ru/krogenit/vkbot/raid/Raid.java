package ru.krogenit.vkbot.raid;

import java.util.ArrayList;
import java.util.List;

public class Raid {
	public int chatId, backupChatid;
	public int ownerId;
	public List<Integer> members = new ArrayList<Integer>();

	public Raid(int chatId, int ownerId) {
		this.chatId = chatId;
		this.ownerId = ownerId;
	}
}
