package com.vk.api.sdk.objects.photos;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * PhotoSizes object
 */
public class PhotoSizes {
    /**
     * URL of the image
     */
    @SerializedName("url")
    private String url;

    /**
     * Width in px
     */
    @SerializedName("width")
    private Integer width;

    /**
     * Height in px
     */
    @SerializedName("height")
    private Integer height;

    /**
     * Size type
     */
    @SerializedName("type")
    private PhotoSizesType type;

    public String getUrl() {
        return url;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public PhotoSizesType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, width, type, height);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhotoSizes photoSizes = (PhotoSizes) o;
        return Objects.equals(url, photoSizes.url) &&
                Objects.equals(width, photoSizes.width) &&
                Objects.equals(height, photoSizes.height) &&
                Objects.equals(type, photoSizes.type);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhotoSizes{");
        sb.append("url='").append(url).append("'");
        sb.append(", width=").append(width);
        sb.append(", height=").append(height);
        sb.append(", type='").append(type).append("'");
        sb.append('}');
        return sb.toString();
    }
}
