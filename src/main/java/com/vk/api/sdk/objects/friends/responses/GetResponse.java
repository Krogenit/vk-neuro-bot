package com.vk.api.sdk.objects.friends.responses;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.users.UserXtrCounters;

import java.util.List;
import java.util.Objects;

/**
 * GetResponse object
 */
public class GetResponse {
    /**
     * Total friends number
     */
    @SerializedName("count")
    private Integer count;

    @SerializedName("items")
    private List<UserXtrCounters> items;

    public Integer getCount() {
        return count;
    }

    public List<UserXtrCounters> getItems() {
        return items;
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, items);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetResponse getResponse = (GetResponse) o;
        return Objects.equals(count, getResponse.count) &&
                Objects.equals(items, getResponse.items);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GetResponse{");
        sb.append("count=").append(count);
        sb.append(", items=").append(items);
        sb.append('}');
        return sb.toString();
    }
}
