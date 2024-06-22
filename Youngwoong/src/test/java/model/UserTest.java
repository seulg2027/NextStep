package model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import db.DataBase;

public class UserTest {

	@Test
	@DisplayName("회원가입")
	public void create() {
		Map<String, String> user = new HashMap<>() {
			{
				this.put("userId", "id");
				this.put("password", "password");
				this.put("name", "황만득");
				this.put("email", "nextstep@gmail.com");
			}
		};
		User newUser = User.create(user);
		assertEquals(DataBase.findUserById("id"), newUser);
	}
}
