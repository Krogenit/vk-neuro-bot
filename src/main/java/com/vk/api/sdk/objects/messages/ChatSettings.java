package com.vk.api.sdk.objects.messages;

import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.photos.Photo;

public class ChatSettings {

	@SerializedName("members_count")
	private Integer membersCount;

	@SerializedName("title")
	private String title;

	@SerializedName("pinned_message")
	private Message pinned_message;

	@SerializedName("state")
	private String state;

	@SerializedName("photo")
	private Photo photo;

	@SerializedName("active_ids")
	private List<Integer> activeIds;

	public Integer getMembersCount() {
		return membersCount;
	}

	public String getTitle() {
		return title;
	}

	public Message getPinned_message() {
		return pinned_message;
	}

	public String getState() {
		return state;
	}

	public Photo getPhoto() {
		return photo;
	}

	public List<Integer> getActiveIds() {
		return activeIds;
	}

	@Override
	public int hashCode() {
		return Objects.hash(activeIds, membersCount, photo, pinned_message, state, title);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		ChatSettings other = (ChatSettings) obj;
		return Objects.equals(activeIds, other.activeIds) && Objects.equals(membersCount, other.membersCount) && Objects.equals(photo, other.photo) && Objects.equals(pinned_message, other.pinned_message) && Objects.equals(state, other.state) && Objects.equals(title, other.title);
	}

	@Override
	public String toString() {
		return "ChatSettings [membersCount=" + membersCount + ", title=" + title + ", pinned_message=" + pinned_message + ", state=" + state + ", photo=" + photo + ", activeIds=" + activeIds + "]";
	}
	
}
