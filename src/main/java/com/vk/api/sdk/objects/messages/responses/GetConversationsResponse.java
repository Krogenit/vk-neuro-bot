package com.vk.api.sdk.objects.messages.responses;

import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.groups.Group;
import com.vk.api.sdk.objects.messages.ConversationMember;
import com.vk.api.sdk.objects.messages.ConversationWithLastMessage;
import com.vk.api.sdk.objects.users.User;

public class GetConversationsResponse {
    /**
     * Total number
     */
    @SerializedName("count")
    private Integer count;

    @SerializedName("items")
    private List<ConversationWithLastMessage> items;
    
    @SerializedName("profiles")
    private List<User> profiles;
    
    @SerializedName("groups")
    private List<Group> groups;

	public Integer getCount() {
		return count;
	}

	public List<ConversationWithLastMessage> getItems() {
		return items;
	}

	public List<User> getUsers() {
		return profiles;
	}

	public List<Group> getGroups() {
		return groups;
	}
    
	@Override
	public int hashCode() {
		return Objects.hash(count, items, profiles, groups);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GetConversationsResponse member = (GetConversationsResponse) o;
		return Objects.equals(count, member.count) &&
				Objects.equals(items, member.items) &&
				Objects.equals(profiles, member.profiles) &&
				Objects.equals(groups, member.groups);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("GetConversationMembersResponse{");
		sb.append("count=").append(count);
		sb.append(", items=").append(items);
		sb.append(", profiles=").append(profiles);
		sb.append(", groups=").append(groups);
		sb.append('}');
		return sb.toString();
	}
}
