package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import db.DataBase;
import lombok.extern.slf4j.Slf4j;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

@Slf4j
public class RequestHandler extends Thread {

    private final Socket connection;
    private static final Map<String, String> headerContents = new ConcurrentHashMap<>();
    private int contentLength;
    private boolean logined;
    private String body;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            String[] requestLine = parseRequestLine(in);
            if (requestLine == null) return;

            String method = requestLine[0];
            String url = requestLine[1];

            if (url.endsWith(".css")) {
                cssProcess(out, url);
            }
            if (method.equals("GET")) {
                if (url.startsWith("/user/create")) {
                    registerGetProcess(url, out);
                } else {
                    responseResource(out, url);
                }
            } else if (method.equals("POST")) {
                if (url.equals("/user/create")) {
                    registerPostProcess(out);
                } else if (url.equals("/user/login")) {
                    loginProcess(out);
                }
            } else {
                responseResource(out, url);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String[] parseRequestLine(InputStream in) throws IOException {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String requestLine = buffer.readLine();
        if (requestLine == null) {
            return null;
        }

        String[] tokens = requestLine.split(" ");
        String line;
        while (!(line = buffer.readLine()).isEmpty()) {
            if (line.contains("Content-Length")) {
                contentLength = getContentLength(line);
            }
            if (line.contains("Cookie")) {
                logined = isLogin(line);
            }
        }

        if (tokens[0].equals("POST")) {
            body = IOUtils.readData(buffer, contentLength);
        }

        return tokens;
    }

    private int getContentLength(String line) {
        String[] headerTokens = line.split(":");
        return Integer.parseInt(headerTokens[1].trim());
    }

    private void addUser(String data) {
        Map<String, String> params = HttpRequestUtils.parseQueryString(data);
        User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
        DataBase.addUser(user);
        log.debug("User : {}", user);
    }

    private void registerGetProcess(String url, OutputStream out) {
        int index = url.indexOf("?");
        String queryString = url.substring(index + 1);
        addUser(queryString);
        response302RegisterSuccessHeader(new DataOutputStream(out));
    }

    private void registerPostProcess(OutputStream out) {
        addUser(body);
        response302RegisterSuccessHeader(new DataOutputStream(out));
    }

    private void loginProcess(OutputStream out) throws IOException {
        Map<String, String> params = HttpRequestUtils.parseQueryString(body);
        User userInfo = DataBase.findUserById(params.get("userId"));

        if (userInfo == null || !userInfo.getPassword().equals(params.get("password"))) {
            responseResource(out, "/user/login_failed.html");
        } else {
            response302LoginSuccessHeader(new DataOutputStream(out));
        }
    }

    private void cssProcess(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        response200CssHeader(dos, body.length);
        responseBody(dos, body);
    }

    private void response302RegisterSuccessHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect OK\r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302LoginSuccessHeader(DataOutputStream dos) throws IOException {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect OK\r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200CssHeader(DataOutputStream dos, int length) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css\r\n");
            dos.writeBytes("Content-Length: " + length + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isLogin(String line) {
        String[] headerTokens = line.split(":");
        Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());
        String value = cookies.get("logined");
        return value != null && Boolean.parseBoolean(value);
    }

    private void responseResource(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
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
