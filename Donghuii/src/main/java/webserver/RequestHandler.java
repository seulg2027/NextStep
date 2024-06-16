package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import model.User;
import util.IOUtils;

@Slf4j
public class RequestHandler extends Thread {

    private final Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            /**
             * GET / HTTP/1.1
             * Host: localhost:8080
             * Connection: keep-alive
             * sec-ch-ua: "Google Chrome";v="123", "Not:A-Brand";v="8", "Chromium";v="123"
             * sec-ch-ua-mobile: ?0
             * sec-ch-ua-platform: "macOS"
             * Upgrade-Insecure-Requests: 1
             * User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36
             * Sec-Purpose: prefetch;prerender
             * Purpose: prefetch
             * Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng;
             * Cookie:JSESSIONID = 6F ADBCE16B68412F08CD19264E23E66B
             */

            try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
                BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
                String readLine;
                String requestLine = null;
                Map<String, String> headers = new HashMap<>();
                while ((readLine = buffer.readLine()) != null && !readLine.isEmpty()) {
                    if (requestLine == null) {
                        requestLine = readLine;
                    } else {
                        String[] header = readLine.split(": ");
                        if (header.length == 2) {
                            headers.put(header[0], header[1]);
                        }
                    }
                }
                log.debug("Request Header : {}", headers);
                if (requestLine != null) {
                    String[] methodUrl = requestLine.split(" ");
                    String method = methodUrl[0];
                    String url = methodUrl[1];

                    if (method.equals("GET")) {
                        if (url.equals("/")) {
                            DataOutputStream dos = new DataOutputStream(out);
                            byte[] body = "Hello World".getBytes();
                            response200Header(dos, body.length);
                            responseBody(dos, body);
                        } else if (url.equals("/index.html")) {
                            handleIndexRequest(out, "/index.html");
                        } else if (url.startsWith("/user/create")) {
                            int contentLength = headers.size();
                            handleGetUserCreate(url, contentLength, out);
                        } else if(url.startsWith("/user/")) {
                            handleHtmlLogin(url, out);
                        }
                    } else if (method.equals("POST") && url.equals("/user/create")) {
                        handlePostUserCreate(buffer, headers, out);
                    }
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }

        private void handleIndexRequest(OutputStream out, String filePath) throws IOException {
            String indexPath = "/Users/proxy/Next-Step/webapp" + filePath;
            File file = new File(indexPath);
            if (file.exists()) {
                Path path = file.toPath();
                byte[] bytes = Files.readAllBytes(path);
                DataOutputStream dos = new DataOutputStream(out);
                response200Header(dos, bytes.length);
                responseBody(dos, bytes);
            }
        }

        private void handleGetUserCreate(String url, int contentLength, OutputStream out) throws IOException {
            String[] urlSplit = url.split("\\?");
            if (urlSplit.length > 1) {
                String[] params = urlSplit[1].split("&");

                Map<String, String> param = new HashMap<>();
                for (String str : params) {
                    String[] keyValue = str.split("=");
                    if (keyValue.length == 2) {
                        param.put(keyValue[0], keyValue[1]);
                    }
                }
                log.debug(param.toString());

                User user = new User();
                user.setUserId(param.get("userId"));
                user.setName(param.get("name"));
                user.setEmail(param.get("email"));
                user.setPassword(param.get("password"));
                log.debug(user.toString());

                String responseBody = "User created: " + user.getUserId();
                byte[] body = responseBody.getBytes();

                DataOutputStream dos = new DataOutputStream(out);
                response200Header(dos, body.length);
                responseBody(dos, body);
            }
        }

        private void handlePostUserCreate(BufferedReader buffer, Map<String, String> headers, OutputStream out) throws IOException {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            String body = IOUtils.readData(buffer, contentLength);

            Map<String, String> param = new HashMap<>();
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    param.put(keyValue[0], keyValue[1]);
                }
            }
            log.debug(param.toString());

            User user = new User();
            user.setUserId(param.get("userId"));
            user.setName(param.get("name"));
            user.setEmail(param.get("email"));
            user.setPassword(param.get("password"));

            DataOutputStream dos = new DataOutputStream(out);
            response302Header(dos, "/login.html");
        }

        private void handleHtmlLogin(String url, OutputStream out) throws IOException {
            String webappPath = "webapp" + url;

            File file = new File(webappPath);

            if(file.exists() && file.isFile()) {
                FileInputStream fis = new FileInputStream(file);
                byte[] body = fis.readAllBytes();
                fis.close();

                DataOutputStream dos = new DataOutputStream(out);
                response200Header(dos, body.length);
                responseBody(dos, body);
            } else {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = "404 Not Found".getBytes();
                response404Header(dos, body.length);
                responseBody(dos, body);
            }
        }

        private void response200Header( DataOutputStream dos, int lengthOfBodyContent) {
            try {
                dos.writeBytes("HTTP/1.1 200 OK \r\n");
                dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
                dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
                dos.writeBytes("\r\n");
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }

        private void response302Header(DataOutputStream dos, String location) {
            try {
                dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
                dos.writeBytes("Content-Type: text/html; charset=utf-8\r\n");
                dos.writeBytes("Location: " + location + "\r\n");
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }

        private void response404Header(DataOutputStream dos, int contentLength) throws IOException {
            try {
                dos.writeBytes("HTTP/1.1 404 Not Found\r\n");
                dos.writeBytes("Content-Type: text/html; charset=utf-8\r\n");
                dos.writeBytes("Content-Length: " + contentLength + "\r\n");
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
