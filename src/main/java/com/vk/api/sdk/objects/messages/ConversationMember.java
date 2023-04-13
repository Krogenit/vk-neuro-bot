package com.vk.api.sdk.objects.messages;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.base.BoolInt;

public class ConversationMember {
    /**
     * Member ID
     */
    @SerializedName("member_id")
    private Integer memberId ;
    
    /**
     * Invited by
     */
    @SerializedName("invited_by")
    private Integer invitedBy;
    
    /**
     * Join date
     */
    @SerializedName("join_date")
    private Integer joinDate;
    
    /**
     * Is admin
     */
    @SerializedName("is_admin")
    private Boolean isAdmin;
    
    @SerializedName("can_kick")
    private Boolean canKick;

	public Integer getMemberId() {
		return memberId;
	}

	public Integer getInvitedBy() {
		return invitedBy;
	}

	public Integer getJoinDate() {
		return joinDate;
	}

	public Boolean getIsAdmin() {
		return isAdmin;
	}
	
	public Boolean getCanKick() {
		return canKick;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(memberId, invitedBy, joinDate, isAdmin, canKick);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ConversationMember member = (ConversationMember) o;
		return Objects.equals(memberId, member.memberId) &&
				Objects.equals(invitedBy, member.invitedBy) &&
				Objects.equals(joinDate, member.joinDate) &&
				Objects.equals(isAdmin, member.isAdmin) &&
				Objects.equals(canKick, member.canKick);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ConversationMember{");
		sb.append("memberId=").append(memberId);
		sb.append(", invitedBy=").append(invitedBy);
		sb.append(", joinDate=").append(joinDate);
		sb.append(", isAdmin=").append(isAdmin);
		sb.append(", canKick=").append(canKick);
		sb.append('}');
		return sb.toString();
	}
}
