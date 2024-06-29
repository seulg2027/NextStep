package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponse {

	private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);

	DataOutputStream dos;
	private Map<String, String> header = new HashMap<String, String>();

	HttpResponse(OutputStream dos) {
		this.dos = new DataOutputStream(dos);
	}

	public void addHeader(String key, String value) {
		header.put(key, value);
	}

	public void forward(String url) throws IOException {
		byte[] body = Files.readAllBytes(new File("refactor-3/webapp/" + url).toPath());
		addHeader("Content-Type", "text/html; charset=utf-8");
		response200Header(dos, body.length);
		responseBody(dos, body);
	}

	public void cssForward(String url) throws IOException {
		byte[] body = Files.readAllBytes(new File("refactor-3/webapp/" + url).toPath());
		addHeader("Content-Type", "text/css;charset=utf-8\r\n");
		response200Header(dos, body.length);
		responseBody(dos, body);
	}

	public void forward(byte[] body) {
		response200Header(dos, body.length);
		responseBody(dos, body);
	}

	public void sendRedirect(String url) {
		response302Header(dos, url);
	}

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			header.forEach((k,v) -> {
				try {
					dos.writeBytes(k + ": " + v + "\r\n");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response302Header(DataOutputStream dos, String url) {
		try {
			dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
			dos.writeBytes("Location: " + url + "\r\n");
			header.forEach((k,v) -> {
				try {
					dos.writeBytes(k + ": " + v + "\r\n");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.writeBytes("\r\n");
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}