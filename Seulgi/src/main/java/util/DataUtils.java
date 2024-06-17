package util;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.RequestHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class DataUtils {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    /**
     * User 모델 생성
     * @param br
     * @return User
     */
    public static void createUser(BufferedReader br) throws IOException {
        String body = IOUtils.bufferGetBody(br);
        log.debug("request : {}", body);

        String[] userArr = body.split("&");
        String userId = userArr[0].split("=")[1];
        String password = userArr[1].split("=")[1];
        String name = userArr[2].split("=")[1];
        String email = userArr[3].split("=")[1];

        User user = new User(userId, password, name, email);

        DataBase.addUser(user);
    }

    /**
     * User 로그인
     * @param br
     * @return Integer
     */
    public static Integer loginUser(BufferedReader br) throws IOException {
        String body = IOUtils.bufferGetBody(br);

        String[] userArr = body.split("&");
        String userId = userArr[0].split("=")[1];
        String password = userArr[1].split("=")[1];

        User user = DataBase.findUserById(userId);
        if (user != null) {
            if (password.equals(user.getPassword())) {
                return 1;
            }
            return -2;
        }
        return -1;
    }

    /**
     * 로그인 상태인지 확인하기
     * @param br
     * @return
     */
    public static Boolean loginAuth(BufferedReader br) throws IOException {
        String Cookies = IOUtils.bufferGetHeader(br, "Cookie");
        Map<String, String> cookies = HttpRequestUtils.parseCookies(Cookies);

        if (!cookies.isEmpty()) {
            if ("true".equals(cookies.get("logined"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 모든 User 출력하기
     * @return
     */
    public static String getUserAll() {
        StringBuilder userStr = new StringBuilder("user list ! 🐳").append("\n");
        Collection<User> allUsers = DataBase.findAll();

        for (User user : allUsers) {
            userStr.append("user name : " + user.getName());
            userStr.append("user email : " + user.getEmail());
            userStr.append("\n ----------------");
        }
        return String.valueOf(userStr);
    }
}
