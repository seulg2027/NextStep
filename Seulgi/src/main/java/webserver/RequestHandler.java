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
            DataOutputStream dos = new DataOutputStream(out);
            String line = bf.readLine();
            String status = "";
            String url = "";
            String ext = "";

            if (!"".equals(line) && line != null) {
                status = IOUtils.urlData(line)[0];
                url = IOUtils.urlData(line)[1];
                ext = IOUtils.extData(url);
            }

            // 추가 : POST, GET 구분
            if ("GET".equals(status)) {
                if (line.matches(".*/user/list.*")) {
                    boolean isAuth = DataUtils.loginAuth(bf);
                    if (isAuth) {
                        byte[] data = DataUtils.getUserAll().getBytes();
                        response200Header(dos, data.length, "html");
                        responseBody(dos, data);
                    } else {
                        response302Header(dos, "/user/login.html");
                    }
                }

                if (ext.equals("html") || ext.equals("css") || ext.equals("js") || ext.equals("ico") || ext.equals("ttf")) {
                    byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                    response200Header(dos, body.length, ext);
                    responseBody(dos, body);
                }
            } else if ("POST".equals(status)) {
                // create user
                if (line.matches(".*/user/create.*")) {
                    DataUtils.createUser(IOUtils.bufferGetBody(bf));
                    response302Header(dos, "/index.html");
                    // login user
                } else if (line.matches(".*/user/login.*")) {
                    boolean setCookie;
                    String redirectUrl = "";
                    int result = DataUtils.loginUser(IOUtils.bufferGetBody(bf));

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

            byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length, HeaderType.HTML.getType());
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String ext) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + HeaderType.findBykey(ext).getText() + "charset=utf-8\r\n");
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
