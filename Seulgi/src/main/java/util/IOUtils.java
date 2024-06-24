package util;

import java.io.BufferedReader;
import java.io.IOException;

public class IOUtils {
    /**
     * @param BufferedReader는
     *            Request Body를 시작하는 시점이어야
     * @param contentLength는
     *            Request Header의 Content-Length 값이다.
     * @return
     * @throws IOException
     */
    public static String readData(BufferedReader br, int contentLength) throws IOException {
        char[] body = new char[contentLength];
        br.read(body, 0, contentLength);
        return String.copyValueOf(body);
    }

    /**
     * BufferedReader body 찾기
     * @param br
     * @return
     * @throws IOException
     */
    public static String bufferGetBody(BufferedReader br) throws IOException {
        String body = null;
        int contentLength = 0;
        String read = null;
        while ((read = br.readLine()) != null) {
            if (read != null && read.contains("Content-Length")) {
                contentLength = Integer.parseInt(bodyData(read));
            }

            if (read.length() == 0) {
                body = IOUtils.readData(br, contentLength);
                break;
            }
        }
        return body;
    }

    /**
     * BufferedReader 특정 header 찾기
     * @param br
     * @param header
     * @return
     * @throws IOException
     */
    public static String bufferGetHeader(BufferedReader br, String header) throws IOException {
        String response = null;
        String read = null;
        while ((read = br.readLine()) != null) {
            if (read != null && read.contains(header)) {
                response = bodyData(read);
                break;
            }
        }
        return response;
    }

    /**
     * URL 반환
     * @param line
     * @return
     */
    public static String urlData(String line) {
        String[] url = line.split(" ");
        return url[1];
    }

    public static String extData(String line) {
        String[] arr = urlData(line).split("[.]");
        if (arr.length > 1) {
            return arr[arr.length-1];
        } else {
            return "";
        }
    }

    /**
     * key value -> value값 반환
     * @param line
     * @return
     */
    public static String bodyData(String line) {
        String[] l = line.replaceAll(" ", "").split(":");
        return l[1];
    }
}
