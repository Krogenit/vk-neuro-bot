package com.vk.api.sdk.queries.messages;

import java.util.Arrays;
import java.util.List;

import com.vk.api.sdk.client.AbstractQueryBuilder;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.objects.base.BoolInt;
import com.vk.api.sdk.objects.messages.responses.GetByConversationMessageIdResponse;

/**
 * Query for Messages.getHistory method
 */
public class MessagesGetByConversationMessageId extends AbstractQueryBuilder<MessagesGetByConversationMessageId, GetByConversationMessageIdResponse> {

    public MessagesGetByConversationMessageId(VkApiClient client, UserActor actor) {
        super(client, "messages.getByConversationMessageId", GetByConversationMessageIdResponse.class);
        accessToken(actor.getAccessToken());
    }

    public MessagesGetByConversationMessageId(VkApiClient client, GroupActor actor) {
        super(client, "messages.getByConversationMessageId", GetByConversationMessageIdResponse.class);
        accessToken(actor.getAccessToken());
    }
    
    public MessagesGetByConversationMessageId peerId(List<Integer> value) {
        return unsafeParam("peer_id", value);
    }

    public MessagesGetByConversationMessageId peerId(Integer value) {
        return unsafeParam("peer_id", value);
    }

    public MessagesGetByConversationMessageId conversationMessageIds(List<Integer> value) {
        return unsafeParam("conversation_message_ids", value);
    }

    public MessagesGetByConversationMessageId extended(BoolInt value) {
        return unsafeParam("extended", value);
    }

    public MessagesGetByConversationMessageId fields(List<String> value) {
        return unsafeParam("fields", value);
    }

    public MessagesGetByConversationMessageId groupId(Integer value) {
        return unsafeParam("group_id", value);
    }

    @Override
    protected MessagesGetByConversationMessageId getThis() {
        return this;
    }

    @Override
    protected List<String> essentialKeys() {
        return Arrays.asList("peer_id", "conversation_message_ids", "access_token");
    }
}
