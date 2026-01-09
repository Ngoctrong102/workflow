package com.notificationplatform.dto.response;

import java.util.List;

public class DiscordEmbed {

    private String title;
    private String description;
    private String url;
    private Integer color;
    private DiscordFooter footer;
    private DiscordImage image;
    private DiscordThumbnail thumbnail;
    private DiscordAuthor author;
    private List<DiscordField> fields;
    private String timestamp;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public DiscordFooter getFooter() {
        return footer;
    }

    public void setFooter(DiscordFooter footer) {
        this.footer = footer;
    }

    public DiscordImage getImage() {
        return image;
    }

    public void setImage(DiscordImage image) {
        this.image = image;
    }

    public DiscordThumbnail getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(DiscordThumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    public DiscordAuthor getAuthor() {
        return author;
    }

    public void setAuthor(DiscordAuthor author) {
        this.author = author;
    }

    public List<DiscordField> getFields() {
        return fields;
    }

    public void setFields(List<DiscordField> fields) {
        this.fields = fields;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public static class DiscordFooter {
        private String text;
        private String iconUrl;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }
    }

    public static class DiscordImage {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class DiscordThumbnail {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class DiscordAuthor {
        private String name;
        private String url;
        private String iconUrl;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }
    }

    public static class DiscordField {
        private String name;
        private String value;
        private Boolean inline;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Boolean getInline() {
            return inline;
        }

        public void setInline(Boolean inline) {
            this.inline = inline;
        }
    }
}

