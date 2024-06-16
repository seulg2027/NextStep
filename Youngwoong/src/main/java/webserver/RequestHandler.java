package webserver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            DataOutputStream dos = new DataOutputStream(out);

            // 요청
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line, requestBody = "", requestLine = bufferedReader.readLine(), contentType = "";

            if(requestLine == null) return;

            // 0 METHOD, 1 URL, 2 HTTP VERSION
            String[] request = requestLine.split(" ");

            HashMap<String, String> header = new HashMap<>();
            while(!(line = bufferedReader.readLine()).isEmpty()) {
                HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
                header.put(pair.getKey(), pair.getValue());
            }

            Map<String, String> cookie = new HashMap<>();
            if(header.get("Cookie") != null) {
                cookie = HttpRequestUtils.parseCookies(header.get("Cookie"));
            }

            if(request[0].equals("GET") && request[1].contains("?")) {
                requestBody = request[1].split("\\?")[1];

            } else if(request[0].equals("POST")) {
                // Content-Length 헤더를 통해 본문의 길이를 파악
                String contentLengthValue = header.get("Content-Length");
                if (contentLengthValue != null) {
                    int contentLength = Integer.parseInt(contentLengthValue);
                    char[] body = new char[contentLength];
                    bufferedReader.read(body, 0, contentLength);
                    requestBody = new String(body);
                }
            }

            // 요청에 따른 응답 변경

            File file = new File("requirements-7/webapp/index.html");

            if(header.get("Sec-Fetch-Dest").equals("document")) {

                Map<String, String> requestMap = HttpRequestUtils.parseQueryString(requestBody);

                if (request[1].equals("/")) {
                    request[1] = "/index.html";

                } else if(request[1].contains("/user/create") && (request[0].equals("GET") || request[0].equals("POST"))) {
                    User.create(requestMap);
                    response302Header(dos, "/");
                    return;

                } else if(request[1].contains("/user/login") && request[0].equals("POST")) {
                    User user = DataBase.findUserById(requestMap.getOrDefault("userId", ""));

                    if(user != null && user.getPassword().equals(requestMap.get("password"))) {
                        response302Header(dos, "/", "logined=true");
                        return;
                    }

                    response302Header(dos, "/user/login_failed.html", "logined=false");
                    return;
                } else if(request[1].contains("/user/list") && request[0].equals("GET")) {
                    if(cookie.get("logined") == null || cookie.get("logined").equals("false")) {
                        response302Header(dos, "/user/login.html");
                    }

                    request[1] = "/user/list.html";
                    file = new File("requirements-6/webapp" + request[1]);

                    String htmlContent = new String(Files.readAllBytes(file.toPath()));
                    StringBuilder stringBuilder = new StringBuilder();

                    AtomicInteger index = new AtomicInteger(1);
                    DataBase.findAll().forEach((k) -> {
                        int currentIndex = index.getAndIncrement();
                        stringBuilder.append("""
                                <tr>
                                    <th scope="row">%s</th>
                                    <td>%s</td>
                                    <td>%s</td>
                                    <td>%s</td>
                                    <td><a href="#" class="btn btn-success" role="button">수정</a></td>
                                </tr>
                            """.formatted(currentIndex, k.getUserId(), k.getName(), URLDecoder.decode(k.getEmail())));
                    });

                    // String 객체에서 replace 메소드를 호출하면 원본 문자열이 변경되지 않고, 변경된 새로운 문자열이 반환
                    htmlContent = htmlContent.replace("<!-- DATA_LIST -->", stringBuilder.toString());
                    byte[] bytes = htmlContent.getBytes();
                    response200Header(dos, bytes.length, "text/html;charset=utf-8");
                    responseBody(dos, bytes);
                    return;
                }

                file = new File("requirements-7/webapp" + request[1]);
                contentType = "text/html;charset=utf-8";

            } else if(header.get("Sec-Fetch-Dest").equals("style")) {
                file = new File("requirements-7/webapp" + request[1]);
                contentType = "text/css";

            } else if(header.get("Sec-Fetch-Dest").equals("script")) {
                file = new File("requirements-7/webapp" + request[1]);
                contentType = "text/javascript";

            }

             // 응답
            byte[] bytes = Files.readAllBytes(file.toPath());
            response200Header(dos, bytes.length, contentType);
            responseBody(dos, bytes);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        response200Header(dos, lengthOfBodyContent, null, contentType);
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String cookieValue, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + "\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            if(cookieValue != null) dos.writeBytes("Set-Cookie: " + cookieValue + "; Path=/ \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url) {
        response302Header(dos, url, null);
    }

    private void response302Header(DataOutputStream dos, String url, String cookieValue) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            if(cookieValue != null) dos.writeBytes("Set-Cookie: " + cookieValue + "; Path=/ \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
