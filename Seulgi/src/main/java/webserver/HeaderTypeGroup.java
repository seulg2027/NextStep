package webserver;

import java.util.Arrays;
import java.util.List;

public enum HeaderTypeGroup {
    CONTENT("Content-Type", Arrays.asList(HeaderType.HTML, HeaderType.CSS, HeaderType.JSON, HeaderType.JAVASCRIPT, HeaderType.ICO, HeaderType.TTF)),
    STATUS("status", Arrays.asList(HeaderType.STATUS_200, HeaderType.STATUS_302));

    private String key;
    private List<HeaderType> headerList;
    HeaderTypeGroup(String key, List<HeaderType> headerList) {
        this.key = key;
        this.headerList = headerList;
    }

    public static HeaderTypeGroup findByHeaderType(HeaderType headerType) {
        return Arrays.stream(HeaderTypeGroup.values())
                .filter(headerGroup -> headerGroup.hasHeaderType(headerType))
                .findAny()
                .orElse(null);
    }

    public boolean hasHeaderType(HeaderType headerType) {
        return headerList.stream()
                .anyMatch(header -> header == headerType);
    }
}
