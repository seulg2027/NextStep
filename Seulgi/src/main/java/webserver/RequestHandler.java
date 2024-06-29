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
            HttpResponse httpResponse = new HttpResponse(out);
            String line = bf.readLine();
            String status = "";
            String url = "";
            String ext = "";
            boolean isAuth;

            if (!"".equals(line) && line != null) {
                status = IOUtils.urlData(line)[0];
                url = IOUtils.urlData(line)[1];
                ext = IOUtils.extData(url);
            }

            // 추가 : POST, GET 구분
            if ("GET".equals(status)) {
                if ("/user/list.html".equals(url)) {
                    isAuth = DataUtils.loginAuth(bf);
                    if (isAuth) {
                        byte[] data = DataUtils.getUserAll().getBytes();
                        httpResponse.response200Header(data.length, "html");
                        httpResponse.responseBody(data);
                    } else {
                        httpResponse.sendRedirect("/user/login.html");
                    }
                } else if (ext.equals("html") || ext.equals("css") || ext.equals("js") || ext.equals("ico") || ext.equals("ttf")) {
                    httpResponse.responseUrlResource(url, ext);
                }
            }
            else if ("POST".equals(status)) {
                if ("/user/create".equals(url)) { // create user
                    DataUtils.createUser(IOUtils.bufferGetBody(bf));
                    httpResponse.sendRedirect("/index.html");
                } else if ("/user/login".equals(url)) { // login
                    String redirectUrl = "";
                    int result = DataUtils.loginUser(IOUtils.bufferGetBody(bf));

                    if (result == 1) {
                        redirectUrl = "/index.html";
                        httpResponse.addResponseHeader("Cookie", String.valueOf(true));
                    } else {
                        redirectUrl = "/user/login_failed.html";
                        httpResponse.addResponseHeader("Cookie", String.valueOf(false));
                    }
                    httpResponse.sendRedirect(redirectUrl);
                }
            }

            httpResponse.responseUrlResource("/index.html", HeaderType.HTML.getType());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
