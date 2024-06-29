package webserver.controller;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class LoginController extends AbstractController {

	@Override
	public void doGet(HttpRequest httpRequest, HttpResponse httpResponse) {

	}

	@Override
	public void doPost(HttpRequest httpRequest, HttpResponse httpResponse) {
		User user = DataBase.findUserById(httpRequest.getParameter("userId"));
		if (user != null) {
			if (user.getPassword().equals(httpRequest.getParameter("password"))) {
				httpResponse.addHeader("Set-Cookie", "logined=true");
				httpResponse.sendRedirect("/index.html");
				return;
			}
		}

		httpResponse.addHeader("Set-Cookie", "logined=false");
		httpResponse.sendRedirect("/user/login_failed.html");
	}
}
