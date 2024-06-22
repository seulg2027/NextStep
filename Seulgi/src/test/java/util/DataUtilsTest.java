package util;

import db.DataBase;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class DataUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(DataUtilsTest.class);

    @Test
    @Before
    public void createUser() throws IOException {
        String body = "Content-Length: 50\n\nuserId=kk&password=kk&name=kk&email=kk%40naver.com";
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(body.getBytes())));

        DataUtils.createUser(br);
        assertThat(DataBase.findUserById("kk").getUserId(), is("kk"));
        assertThat(DataBase.findUserById("kk").getName(), is("kk"));
        assertThat(DataBase.findUserById("kk").getPassword(), is("kk"));
        assertThat(DataBase.findUserById("kk").getEmail(), is("kk%40naver.com"));
    }

    @Test
    public void loginUser() throws IOException {
        String body1 = "Content-Length: 50\n\nuserId=kk&password=kk&name=kk&email=kk%40naver.com";
        String body2 = "Content-Length: 50\n\nuserId=km&password=km&name=km&email=km%40naver.com";
        BufferedReader br1 = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(body1.getBytes())));
        BufferedReader br2 = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(body2.getBytes())));

        assertThat(DataUtils.loginUser(br1), is(1)); // 로그인 성공 (회원가입한 사용자)
        assertThat(DataUtils.loginUser(br2), is(-1)); // 로그인 실패 (회원에 없는 사용자)
    }

    @Test
    public void loginAuth() throws IOException {
        String header = "Cookie: logined=true";
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(header.getBytes())));

        assertTrue(DataUtils.loginAuth(br));
    }

    @Test
    public void getUserAll() {
        String result = DataUtils.getUserAll();

        assertThat(result, is("user list ! 🐳\nuser name : kkuser email : kk%40naver.com\n ----------------"));
    }
}
