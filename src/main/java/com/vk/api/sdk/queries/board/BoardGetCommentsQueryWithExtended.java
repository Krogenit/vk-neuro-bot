package com.vk.api.sdk.queries.board;

import com.vk.api.sdk.client.AbstractQueryBuilder;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.objects.board.responses.GetCommentsExtendedResponse;

import java.util.Arrays;
import java.util.List;

/**
 * Query for Board.getComments method
 */
public class BoardGetCommentsQueryWithExtended extends AbstractQueryBuilder<BoardGetCommentsQueryWithExtended, GetCommentsExtendedResponse> {
    /**
     * Creates a AbstractQueryBuilder instance that can be used to build api request with various parameters
     *
     * @param client  VK API client
     * @param actor   actor with access token
     * @param groupId value of "group id" parameter. Minimum is 0.
     * @param topicId value of "topic id" parameter. Minimum is 0.
     */
    public BoardGetCommentsQueryWithExtended(VkApiClient client, UserActor actor, int groupId, int topicId) {
        super(client, "board.getComments", GetCommentsExtendedResponse.class);
        accessToken(actor.getAccessToken());
        groupId(groupId);
        topicId(topicId);
        extended(true);
    }

    /**
     * Creates a AbstractQueryBuilder instance that can be used to build api request with various parameters
     *
     * @param client  VK API client
     * @param groupId value of "group id" parameter. Minimum is 0.
     * @param topicId value of "topic id" parameter. Minimum is 0.
     */
    public BoardGetCommentsQueryWithExtended(VkApiClient client, ServiceActor actor, int groupId, int topicId) {
        super(client, "board.getComments", GetCommentsExtendedResponse.class);
        accessToken(actor.getAccessToken());
        clientSecret(actor.getClientSecret());
        groupId(groupId);
        topicId(topicId);
        extended(true);
    }

    /**
     * ID of the community that owns the discussion board.
     *
     * @param value value of "group id" parameter. Minimum is 0.
     * @return a reference to this {@code AbstractQueryBuilder} object to fulfill the "Builder" pattern.
     */
    protected BoardGetCommentsQueryWithExtended groupId(int value) {
        return unsafeParam("group_id", value);
    }

    /**
     * Topic ID.
     *
     * @param value value of "topic id" parameter. Minimum is 0.
     * @return a reference to this {@code AbstractQueryBuilder} object to fulfill the "Builder" pattern.
     */
    protected BoardGetCommentsQueryWithExtended topicId(int value) {
        return unsafeParam("topic_id", value);
    }

    /**
     * Return the "likes" field
     *
     * @param value value of "need likes" parameter.
     * @return a reference to this {@code AbstractQueryBuilder} object to fulfill the "Builder" pattern.
     */
    public BoardGetCommentsQueryWithExtended needLikes(Boolean value) {
        return unsafeParam("need_likes", value);
    }

    /**
     * Set start comment id
     *
     * @param value value of "start comment id" parameter. Minimum is 0.
     * @return a reference to this {@code AbstractQueryBuilder} object to fulfill the "Builder" pattern.
     */
    public BoardGetCommentsQueryWithExtended startCommentId(Integer value) {
        return unsafeParam("start_comment_id", value);
    }

    /**
     * Offset needed to return a specific subset of comments.
     *
     * @param value value of "offset" parameter.
     * @return a reference to this {@code AbstractQueryBuilder} object to fulfill the "Builder" pattern.
     */
    public BoardGetCommentsQueryWithExtended offset(Integer value) {
        return unsafeParam("offset", value);
    }

    /**
     * Number of comments to return.
     *
     * @param value value of "count" parameter. Maximum is 100. Minimum is 0. By default 20.
     * @return a reference to this {@code AbstractQueryBuilder} object to fulfill the "Builder" pattern.
     */
    public BoardGetCommentsQueryWithExtended count(Integer value) {
        return unsafeParam("count", value);
    }

    /**
     * Return information about users who posted comments
     *
     * @param value value of "extended" parameter.
     * @return a reference to this {@code AbstractQueryBuilder} object to fulfill the "Builder" pattern.
     */
    protected BoardGetCommentsQueryWithExtended extended(Boolean value) {
        return unsafeParam("extended", value);
    }

    /**
     * Sort order
     *
     * @param value value of "sort" parameter.
     * @return a reference to this {@code AbstractQueryBuilder} object to fulfill the "Builder" pattern.
     */
    public BoardGetCommentsQueryWithExtended sort(BoardGetCommentsSort value) {
        return unsafeParam("sort", value);
    }

    @Override
    protected BoardGetCommentsQueryWithExtended getThis() {
        return this;
    }

    @Override
    protected List<String> essentialKeys() {
        return Arrays.asList("group_id", "topic_id");
    }
}
