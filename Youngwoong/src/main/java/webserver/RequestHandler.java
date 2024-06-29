package webserver;

import static webserver.controller.AbstractController.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest httpRequest = new HttpRequest(in);
            HttpResponse httpResponse = new HttpResponse(out);

            String url = httpRequest.getPath();

            if(controllers.get(url) != null) {
                controllers.get(url).service(httpRequest, httpResponse);
                return;
            }

            if (url.endsWith(".css")) {
                httpResponse.cssForward(url);
            } else {
                if(url.equals("/")) url = "index.html";
                httpResponse.forward(url);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
