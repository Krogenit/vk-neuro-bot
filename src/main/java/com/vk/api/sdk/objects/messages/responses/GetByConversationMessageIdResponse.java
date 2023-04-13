package com.vk.api.sdk.objects.messages.responses;

import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.messages.Message;

public class GetByConversationMessageIdResponse {
	
    @SerializedName("count")
    private Integer count;
    
    @SerializedName("items")
    private List<Message> items;

	public Integer getCount() {
		return count;
	}

	public List<Message> getItems() {
		return items;
	}

	@Override
	public int hashCode() {
		return Objects.hash(count, items);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		GetByConversationMessageIdResponse other = (GetByConversationMessageIdResponse) obj;
		return Objects.equals(count, other.count) && Objects.equals(items, other.items);
	}

	@Override
	public String toString() {
		return "GetByConversationMessageIdResponse [count=" + count + ", items=" + items + "]";
	}
    
    
}
