package com.vk.api.sdk.objects.messages;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class ConversationWithLastMessage {

    @SerializedName("conversation")
    private Chat conversation;
    
    @SerializedName("last_message")
    private Message lastMessage;

	public Chat getConversation() {
		return conversation;
	}

	public Message getLastMessage() {
		return lastMessage;
	}

	@Override
	public int hashCode() {
		return Objects.hash(conversation, lastMessage);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		ConversationWithLastMessage other = (ConversationWithLastMessage) obj;
		return Objects.equals(conversation, other.conversation) && Objects.equals(lastMessage, other.lastMessage);
	}

	@Override
	public String toString() {
		return "ConversationWithLastMessage [conversation=" + conversation + ", lastMessage=" + lastMessage + "]";
	}
    
    
}
