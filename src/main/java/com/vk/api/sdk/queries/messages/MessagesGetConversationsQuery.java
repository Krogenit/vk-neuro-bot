package com.vk.api.sdk.queries.messages;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.vk.api.sdk.client.AbstractQueryBuilder;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.objects.messages.responses.GetConversationMembersResponse;
import com.vk.api.sdk.objects.messages.responses.GetConversationsResponse;
import com.vk.api.sdk.queries.EnumParam;

public class MessagesGetConversationsQuery extends AbstractQueryBuilder<MessagesGetConversationsQuery, GetConversationsResponse> {

	public MessagesGetConversationsQuery(VkApiClient client, UserActor actor) {
		super(client, "messages.getConversations", GetConversationsResponse.class);
        accessToken(actor.getAccessToken());
	}
	
	public MessagesGetConversationsQuery(VkApiClient client, GroupActor actor) {
		super(client, "messages.getConversations", GetConversationsResponse.class);
        accessToken(actor.getAccessToken());
	}
	
	public MessagesGetConversationsQuery offset(int value) {
        return unsafeParam("offset", value);
    }
	
	public MessagesGetConversationsQuery count(int value) {
        return unsafeParam("count", value);
    }
	
	public MessagesGetConversationsQuery filter(String value) {
        return unsafeParam("filter", value);
    }
	
	public MessagesGetConversationsQuery extended(int value) {
        return unsafeParam("extended", value);
    }
    
	public MessagesGetConversationsQuery startMessageId(int value) {
        return unsafeParam("start_message_id", value);
    }
    
    public MessagesGetConversationsQuery fields(EnumParam... value) {
        return unsafeParam("fields", value);
    }

    public MessagesGetConversationsQuery fields(List<EnumParam> value) {
        return unsafeParam("fields", value);
    }
    
	public MessagesGetConversationsQuery groupId(int value) {
        return unsafeParam("group_id", value);
    }

	@Override
	protected MessagesGetConversationsQuery getThis() {
		return this;
	}

	@Override
	protected Collection<String> essentialKeys() {
		return Arrays.asList("access_token");
	}
    
}
