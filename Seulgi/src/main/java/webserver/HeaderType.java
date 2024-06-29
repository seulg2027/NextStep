package webserver;

import java.util.Arrays;
import java.util.stream.Stream;

public enum HeaderType {
    HTML("html", "text/html;"),
    CSS("css", "text/css;"),
    JSON("json", "application/json;"),
    JAVASCRIPT("js", "text/javascript;"),
    ICO("ico", "image/png"),
    TTF("ttf", "font/ttf"),
    STATUS_200("200", "200 OK"),
    STATUS_302("302", "302 Found");

    private String type;
    private String text;
    HeaderType(String type, String text) {
        this.type = type;
        this.text = text;
    }

    public static HeaderType findBykey(String type) {
        return Stream.of(values()).filter(h -> h.type.equals(type)).findFirst().orElse(null);
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }
}
