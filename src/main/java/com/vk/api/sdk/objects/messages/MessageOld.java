package com.vk.api.sdk.objects.messages;

import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.base.Geo;

/**
 * Message object
 */
public class MessageOld {
    /**
     * Message ID
     */
    @SerializedName("id")
    private Integer id;
    
    @SerializedName("user_id")
    private Integer userId;
    
    @SerializedName("chat_id")
    private Integer chatId;

    /**
     * Date when the message has been sent in Unixtime
     */
    @SerializedName("date")
    private Integer date;

    @SerializedName("peer_id")
    private Integer peerId;

    @SerializedName("from_id")
    private Integer fromId;

    @SerializedName("text")
    private String text;
    
    @SerializedName("body")
    private String body;
    
    @SerializedName("out")
    private Integer out;
    
    @SerializedName("in")
    private Integer in;
    
    @SerializedName("read_state")
    private Integer readState;
    
    @SerializedName("conversation_message_id")
    private Integer conversationMessageId;
    
    @SerializedName("random_id")
    private Integer randomId;

    @SerializedName("attachments")
    private List<MessageAttachment> attachments;

    @SerializedName("important")
    private Boolean important;

    @SerializedName("geo")
    private GeoOld geo;

    @SerializedName("payload")
    private String payload;

    @SerializedName("fwd_messages")
    private List<MessageOld> fwdMessages;

    @SerializedName("action")
    private String action;
    
    //@SerializedName("is_hidden")
    //private Boolean isHidden;
    
    @SerializedName("action_mid")
    private Integer actionMid;
    
    @SerializedName("action_email")
    private String actionEmail;
    
    @SerializedName("action_text")
    private String actionText;
    
	public Integer getActionMid() {
		return actionMid;
	}

	public String getActionEmail() {
		return actionEmail;
	}

	public String getActionText() {
		return actionText;
	}

	public Integer getId() {
		return id;
	}

	public Integer getDate() {
		return date;
	}

	public Integer getPeerId() {
		return peerId;
	}

	public Integer getFromId() {
		return fromId;
	}

	public String getText() {
		return text != null ? text : body;
	}

	public Integer getRandomId() {
		return randomId;
	}

	public List<MessageAttachment> getAttachments() {
		return attachments;
	}

	public Boolean getImportant() {
		return important;
	}

	public GeoOld getGeo() {
		return geo;
	}

	public String getPayload() {
		return payload;
	}

	public List<MessageOld> getFwdMessages() {
		return fwdMessages;
	}

	public String getAction() {
		return action;
	}
	
	public String getBody() {
		return body;
	}

	public Integer getOut() {
		return out;
	}

	public Integer getIn() {
		return in;
	}

	public Integer getReadState() {
		return readState;
	}

	public Integer getConversationMessageId() {
		return conversationMessageId;
	}

//	public Boolean getIsHidden() {
//		return isHidden;
//	}

	public int getChatId() {
		if(getPeerId() != null && getPeerId() > 2000000000) {
			return getPeerId() - 2000000000;
		}
		
		if(chatId != null) return chatId;
		
		return 0;
	}
	
	public int getUserId() {
		if(getFromId() == null) return userId;
		return getFromId();
	}

	@Override
	public int hashCode() {
		return Objects.hash(action, attachments, body, conversationMessageId, date, fromId, fwdMessages, geo, id, important, in, out, payload, peerId, randomId, readState, text);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		MessageOld other = (MessageOld) obj;
		return Objects.equals(action, other.action) && Objects.equals(attachments, other.attachments) && Objects.equals(body, other.body) && Objects.equals(conversationMessageId, other.conversationMessageId) && Objects.equals(date, other.date) && Objects.equals(fromId, other.fromId) && Objects.equals(fwdMessages, other.fwdMessages) && Objects.equals(geo, other.geo) && Objects.equals(id, other.id) && Objects.equals(important, other.important) && Objects.equals(in, other.in) && Objects.equals(out, other.out) && Objects.equals(payload, other.payload) && Objects.equals(peerId, other.peerId) && Objects.equals(randomId, other.randomId) && Objects.equals(readState, other.readState) && Objects.equals(text, other.text);
	}

	@Override
	public String toString() {
		return "Message [id=" + id + ", date=" + date + ", peerId=" + peerId + ", fromId=" + fromId + ", text=" + text + ", body=" + body + ", out=" + out + ", in=" + in + ", readState=" + readState + ", conversationMessageId=" + conversationMessageId + ", randomId=" + randomId + ", attachments=" + attachments + ", important=" + important + ", geo=" + geo + ", payload=" + payload + ", fwdMessages=" + fwdMessages + ", action=" + action + "]";
	}


}
