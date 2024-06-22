package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import db.DataBase;
import model.User;

public class RequestList {

	private final ResponseHandler responseHandler = new ResponseHandler();
	private final String moduleName = "test-junit5";

	@RequestMapping(value = "/", method = "GET")
	public void index(HashMap<String, String> cookie, HashMap<String, String> requestMap, DataOutputStream dos) {
		File file = new File(moduleName + "/webapp/index.html");
		byte[] bytes;
		try {
			bytes = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		responseHandler.response200Header(dos, bytes.length, ResponseContentType.HTML.contentType());
		responseHandler.responseBody(dos, bytes);
	}

	@RequestMapping(value = "/user/create", method = "POST")
	public void createUser(Map<String, String> cookie, HashMap<String, String> requestMap, DataOutputStream dos) {
		User.create(requestMap);
		responseHandler.response302Header(dos, "/");
	}

	@RequestMapping(value = "/user/login", method = "POST")
	public void login(Map<String, String> cookie, HashMap<String, String> requestMap, DataOutputStream dos) {
		User user = DataBase.findUserById(requestMap.getOrDefault("userId", ""));

		if(user != null && user.getPassword().equals(requestMap.get("password"))) {
			responseHandler.response302Header(dos, "/", "logined=true");
		}
		responseHandler.response302Header(dos, "/user/login_failed.html", "logined=false");
	}

	@RequestMapping(value = "/user/list", method = "GET")
	public void list(HashMap<String, String> cookie, HashMap<String, String> requestMap, DataOutputStream dos) {
		if(cookie.get("logined") == null || cookie.get("logined").equals("false")) {
			responseHandler.response302Header(dos, "/user/login.html");
		}

		String htmlContent = null;
		try {
			htmlContent = new String(Files.readAllBytes(new File(moduleName + "/webapp/user/list.html").toPath()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

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
							""".formatted(currentIndex, k.getUserId(), k.getName(), URLDecoder.decode(k.getEmail(),
				StandardCharsets.UTF_8)));
		});

		// String 객체에서 replace 메소드를 호출하면 원본 문자열이 변경되지 않고, 변경된 새로운 문자열이 반환
		htmlContent = htmlContent.replace("<!-- DATA_LIST -->", stringBuilder.toString());
		byte[] bytes = htmlContent.getBytes();
		responseHandler.response200Header(dos, bytes.length, ResponseContentType.HTML.contentType());
		responseHandler.responseBody(dos, bytes);
	}
}