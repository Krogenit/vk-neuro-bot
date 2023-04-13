package com.vk.api.sdk.objects.messages;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.base.BoolInt;

public class CanWrite {
	
    @SerializedName("allowed")
    private BoolInt allowed;
    
    @SerializedName("reason")
    private Integer reason;

	public BoolInt getAllowed() {
		return allowed;
	}

	public Integer getReason() {
		return reason;
	}

	@Override
	public String toString() {
		return "CanWrite [allowed=" + allowed + ", reason=" + reason + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(allowed, reason);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		CanWrite other = (CanWrite) obj;
		return allowed == other.allowed && Objects.equals(reason, other.reason);
	}
    
    
}
