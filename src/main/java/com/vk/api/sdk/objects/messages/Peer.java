package com.vk.api.sdk.objects.messages;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class Peer {
	
    @SerializedName("id")
    private Integer id;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("local_id")
    private Integer localId;

	@Override
	public String toString() {
		return "Peer [id=" + id + ", type=" + type + ", localId=" + localId + "]";
	}

	public Integer getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public Integer getLocalId() {
		return localId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, localId, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		Peer other = (Peer) obj;
		return Objects.equals(id, other.id) && Objects.equals(localId, other.localId) && Objects.equals(type, other.type);
	}
}
