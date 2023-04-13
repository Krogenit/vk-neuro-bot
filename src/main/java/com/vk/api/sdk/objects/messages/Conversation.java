package com.vk.api.sdk.objects.messages;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.account.PushSettings;
import com.vk.api.sdk.objects.base.BoolInt;

/**
 * Conversation object
 */
public class Conversation {

	@SerializedName("peer")
	private Peer peer;

	@SerializedName("in_read")
	private Integer inRead;

	@SerializedName("out_read")
	private Integer outRead;

	@SerializedName("unread_count")
	private Integer unreadCount;

	@SerializedName("important")
	private BoolInt important;

	@SerializedName("unanswered")
	private BoolInt unanswered;

	@SerializedName("push_settings")
	private PushSettings pushSettings;

	@SerializedName("can_write")
	private CanWrite canWrite;

	@SerializedName("chat_settings")
	private ChatSettings chatSettings;

	public Peer getPeer() {
		return peer;
	}

	public Integer getInRead() {
		return inRead;
	}

	public Integer getOutRead() {
		return outRead;
	}

	public Integer getUnreadCount() {
		return unreadCount;
	}

	public BoolInt getImportant() {
		return important;
	}

	public BoolInt getUnanswered() {
		return unanswered;
	}

	public PushSettings getPushSettings() {
		return pushSettings;
	}

	public CanWrite getCanWrite() {
		return canWrite;
	}

	public ChatSettings getChatSettings() {
		return chatSettings;
	}

	@Override
	public int hashCode() {
		return Objects.hash(canWrite, chatSettings, important, inRead, outRead, peer, pushSettings, unanswered, unreadCount);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		Conversation other = (Conversation) obj;
		return Objects.equals(canWrite, other.canWrite) && Objects.equals(chatSettings, other.chatSettings) && important == other.important && Objects.equals(inRead, other.inRead) && Objects.equals(outRead, other.outRead) && Objects.equals(peer, other.peer) && Objects.equals(pushSettings, other.pushSettings) && unanswered == other.unanswered && Objects.equals(unreadCount, other.unreadCount);
	}

	@Override
	public String toString() {
		return "Conversation [peer=" + peer + ", inRead=" + inRead + ", outRead=" + outRead + ", unreadCount=" + unreadCount + ", important=" + important + ", unanswered=" + unanswered + ", pushSettings=" + pushSettings + ", canWrite=" + canWrite + ", chatSettings=" + chatSettings + "]";
	}

}
