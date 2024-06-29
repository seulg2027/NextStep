package webserver.controller;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class CreateUserController extends AbstractController {

	@Override
	public void doGet(HttpRequest httpRequest, HttpResponse httpResponse) {

	}

	@Override
	public void doPost(HttpRequest httpRequest, HttpResponse httpResponse) {
		User user = new User(httpRequest.getParameter("userId"), httpRequest.getParameter("password"), httpRequest.getParameter("name"),
			httpRequest.getParameter("email"));
		DataBase.addUser(user);
		httpResponse.sendRedirect("/index.html");
	}
}
