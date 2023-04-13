package com.vk.api.sdk.objects.messages;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.photos.Photo;

/**
 * Message action
 */
public class Action {
	
	@SerializedName("type")
	private ActionType type;

	@SerializedName("member_id")
	private Integer memberId;

	@SerializedName("text")
	private String text;

	@SerializedName("email")
	private String email;

	@SerializedName("photo")
	private Photo photo;

	public ActionType getType() {
		return type;
	}

	public Integer getMemberId() {
		return memberId;
	}

	public String getText() {
		return text;
	}

	public String getEmail() {
		return email;
	}

	public Photo getPhoto() {
		return photo;
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, memberId, photo, text, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		Action other = (Action) obj;
		return Objects.equals(email, other.email) && Objects.equals(memberId, other.memberId) && Objects.equals(photo, other.photo) && Objects.equals(text, other.text) && type == other.type;
	}

	@Override
	public String toString() {
		return "Action [type=" + type + ", memberId=" + memberId + ", text=" + text + ", email=" + email + ", photo=" + photo + "]";
	}

}
