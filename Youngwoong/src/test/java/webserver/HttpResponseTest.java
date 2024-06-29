package webserver;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

class HttpResponseTest {
	private final String testDirectory = "./src/test/resources";

	@Test
	public void responseForward() throws IOException {
		HttpResponse httpResponse = new HttpResponse(createOutputDirectory("/HTTP_RESPONSE_FORWARD.txt"));
		httpResponse.forward("/index.html");
	}

	@Test
	public void responseRedirect() throws IOException {
		HttpResponse httpResponse = new HttpResponse(createOutputDirectory("/HTTP_RESPONSE_REDIRECT.txt"));
		httpResponse.sendRedirect("/index.html");
	}

	@Test
	public void responseCookie() throws IOException {
		HttpResponse httpResponse = new HttpResponse(createOutputDirectory("/HTTP_RESPONSE_COOKIE.txt"));
		httpResponse.addHeader("Set-Cookie", "logined=true");
		httpResponse.forward("/index.html");
	}

	private FileOutputStream createOutputDirectory(String fileName) throws FileNotFoundException {
		return new FileOutputStream(testDirectory + "/" + fileName);
	}
}