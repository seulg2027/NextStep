package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private final ResponseHandler responseHandler = new ResponseHandler();
    private final RequestList requestList = new RequestList();

    private final String moduleName = "test-junit5";

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
	}

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            DataOutputStream dos = new DataOutputStream(out);

            // 요청
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String requestBody = "", requestLine = bufferedReader.readLine(), contentType = "";

            if(requestLine == null) return;

            // 0 METHOD, 1 URL, 2 HTTP VERSION
            String[] request = requestLine.split(" ");

            Map<String, String> header = HttpRequestUtils.parseHeader(bufferedReader);
            Map<String, String> cookie = header.get("Cookie") != null ? HttpRequestUtils.parseCookies(header.get("Cookie")) : new HashMap<>();

            if(request[0].equals("GET") && request[1].contains("?")) {
                requestBody = request[1].split("\\?")[1];

            } else if(request[0].equals("POST")) {
                // Content-Length 헤더를 통해 본문의 길이를 파악
                String contentLengthValue = header.get("Content-Length");
                if (contentLengthValue == null) return;

                int contentLength = Integer.parseInt(contentLengthValue);
                char[] body = new char[contentLength];
                bufferedReader.read(body, 0, contentLength);
                requestBody = new String(body);
            }

            // 요청에 따른 응답 변경
            contentType = ResponseContentType.getContentType(header.get("Sec-Fetch-Dest"));

            if(contentType.equals(ResponseContentType.HTML.contentType())) {
                Map<String, String> requestMap = HttpRequestUtils.parseQueryString(requestBody);
                for (Method method : RequestList.class.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        Annotation[] a = method.getDeclaredAnnotations();
                        for(Annotation annotation : a) {
                            if(annotation instanceof RequestMapping) {
                                RequestMapping metadata = (RequestMapping) annotation;
                                if(request[1].equals(metadata.value()) && request[0].equals(metadata.method())) {
									try {
                                        // The call of method.setAccessible(true) allows us to execute the private initNames() method.
                                        method.setAccessible(true);

                                        // object is not an instance of declaring class
                                        // 인스턴스를 넣어줘야하는데 클래스를 그대로 넣어줘서 생긴 오류
                                        method.invoke(requestList, cookie, requestMap, dos);
                                        return;
									} catch (IllegalAccessException | InvocationTargetException e) {
										throw new RuntimeException(e);
									}
								}
                            }
                        }
                    }
                }
            }

             // 응답
            File file = new File(moduleName + "/webapp" + request[1]);
            byte[] bytes = Files.readAllBytes(file.toPath());
            responseHandler.response200Header(dos, bytes.length, contentType);
            responseHandler.responseBody(dos, bytes);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
