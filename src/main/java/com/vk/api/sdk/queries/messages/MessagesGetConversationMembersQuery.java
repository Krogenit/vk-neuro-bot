package com.vk.api.sdk.queries.messages;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.vk.api.sdk.client.AbstractQueryBuilder;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.objects.messages.responses.GetConversationMembersResponse;
import com.vk.api.sdk.queries.EnumParam;

public class MessagesGetConversationMembersQuery extends AbstractQueryBuilder<MessagesGetConversationMembersQuery, GetConversationMembersResponse> {

	public MessagesGetConversationMembersQuery(VkApiClient client, GroupActor actor) {
		super(client, "messages.getConversationMembers", GetConversationMembersResponse.class);
        accessToken(actor.getAccessToken());
	}
	
	public MessagesGetConversationMembersQuery peerId(int value) {
        return unsafeParam("peer_id", value);
    }
    
	public MessagesGetConversationMembersQuery groupId(int value) {
        return unsafeParam("group_id", value);
    }
    
    public MessagesGetConversationMembersQuery fields(EnumParam... value) {
        return unsafeParam("fields", value);
    }

    public MessagesGetConversationMembersQuery fields(List<EnumParam> value) {
        return unsafeParam("fields", value);
    }

	@Override
	protected MessagesGetConversationMembersQuery getThis() {
		return this;
	}

	@Override
	protected Collection<String> essentialKeys() {
		return Arrays.asList("peer_id", "access_token");
	}
    
}
