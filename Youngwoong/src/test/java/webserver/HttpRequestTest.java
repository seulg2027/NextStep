package webserver;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

class HttpRequestTest {
	private final String testDirectory = "./src/test/resources";

	@Test
	void get() throws FileNotFoundException {
		InputStream inputStream = new FileInputStream(testDirectory + "/HTTP_GET.txt");
		HttpRequest httpRequest = new HttpRequest(inputStream);
		assertEquals("GET", httpRequest.getMethod());
		assertEquals("/user/create", httpRequest.getPath());
		assertEquals("keep-alive", httpRequest.getHeader("Connection"));
		assertEquals("javajigi", httpRequest.getParameter("userId"));
	}

	@Test
	void getNoQuery() throws FileNotFoundException {
		InputStream inputStream = new FileInputStream(testDirectory + "/HTTP_GET_NO_QUERY.txt");
		HttpRequest httpRequest = new HttpRequest(inputStream);
		assertEquals("GET", httpRequest.getMethod());
		assertEquals("/user/create", httpRequest.getPath());
		assertEquals("keep-alive", httpRequest.getHeader("Connection"));
		assertThrows(NullPointerException.class, () -> httpRequest.getParameter("userId"));
	}

	@Test
	void post() throws FileNotFoundException {
		InputStream inputStream = new FileInputStream(testDirectory + "/HTTP_POST.txt");
		HttpRequest httpRequest = new HttpRequest(inputStream);
		assertEquals(HttpMethod.POST, httpRequest.getMethod());
		assertEquals("/user/create", httpRequest.getPath());
		assertEquals("javajigi", httpRequest.getParameter("userId"));
	}
}
