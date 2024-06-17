package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DataUtils;
import util.IOUtils;

import javax.xml.crypto.Data;

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
            BufferedReader bf = new BufferedReader(new InputStreamReader(in, "euc-kr"));
            String line = bf.readLine();

            if (!"".equals(line) && line != null) {
                DataOutputStream dos = new DataOutputStream(out);
                // 추가 : POST, GET 구분
                if (line.startsWith("GET")) {
                    String url = IOUtils.urlData(line);
                    // HTML 페이지 나타내기
                    if (line.matches(".*.html.*")) {
                        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                        response200Header(dos, body.length);
                        responseBody(dos, body);
                    } else if (line.matches(".*.css.*")) {
                        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                        responseCssHeader(dos, body.length);
                        responseBody(dos, body);
                    } else if (line.matches(".*/user/list.*")) {
                        boolean isAuth = DataUtils.loginAuth(bf);
                        if (isAuth) {
                            byte[] data = DataUtils.getUserAll().getBytes();
                            response200Header(dos, data.length);
                            responseBody(dos, data);
                        } else {
                            response302Header(dos, "/user/login.html");
                        }
                    }
                } else if (line.startsWith("POST")) {
                    // create user
                    if (line.matches(".*/user/create.*")) {
                        DataUtils.createUser(bf);
                        response302Header(dos, "/index.html");
                        // login user
                    } else if (line.matches(".*/user/login.*")) {
                        boolean setCookie;
                        String redirectUrl = "";
                        int result = DataUtils.loginUser(bf);

                        if (result == 1) {
                            redirectUrl = "/index.html";
                            setCookie = true;
                        } else {
                            redirectUrl = "/user/login_failed.html";
                            setCookie = false;
                        }
                        response302Header(dos, redirectUrl, setCookie);
                    }
                }
            }

            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
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

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url, boolean isLogined) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            dos.writeBytes("Set-Cookie: logined=" + isLogined + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseCssHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;\r\n");
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
