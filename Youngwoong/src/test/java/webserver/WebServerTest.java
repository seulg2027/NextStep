package webserver;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WebServerTest {
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private RequestHandler requestHandler;
	private InputStream in;

	@BeforeEach
	public void setUp() throws Exception {
		serverSocket = new ServerSocket(8080);
		clientSocket = new Socket("localhost", 8080);
		in = clientSocket.getInputStream();
		requestHandler = new RequestHandler(clientSocket);
		requestHandler.start();
	}

	@Test
	void testSocketConnection() {
		assertNotNull(clientSocket);
		assertTrue(clientSocket.isConnected());
	}
}
