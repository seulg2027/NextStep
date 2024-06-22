package webserver;

import java.util.Arrays;

public enum ResponseContentType {
	HTML("document", "text/html;charset=utf-8"),
	CSS("style", "text/css"),
	SCRIPT("script", "text/javascript"),
	WOFF("font", "font/woff"),
	IMAGE("image", "image/png"),;

	private final String secFetchDest;
	private final String contentType;

	ResponseContentType(String secFetchDest, String contentType) {
		this.secFetchDest = secFetchDest;
		this.contentType = contentType;
	}

	public static String getContentType(String secFetchDest) {
		return Arrays.stream(ResponseContentType.values())
			.filter(e -> e.secFetchDest.equals(secFetchDest))
			.map(e -> e.contentType)
			.findFirst()
			.orElse(null);
	}

	public String contentType() {
		return this.contentType;
	}
}
