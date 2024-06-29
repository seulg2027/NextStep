package webserver.controller;

import java.util.Collection;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class ListUserController extends AbstractController {

	@Override
	public void doGet(HttpRequest httpRequest, HttpResponse httpResponse) {

		System.out.println(logined(httpRequest));
		if (!logined(httpRequest)) {
			httpResponse.sendRedirect("/user/login.html");
			return;
		}

		Collection<User> users = DataBase.findAll();
		StringBuilder sb = new StringBuilder();
		sb.append("<table border='1'>");
		for (User user : users) {
			sb.append("<tr>");
			sb.append("<td>" + user.getUserId() + "</td>");
			sb.append("<td>" + user.getName() + "</td>");
			sb.append("<td>" + user.getEmail() + "</td>");
			sb.append("</tr>");
		}
		sb.append("</table>");
		byte[] body = sb.toString().getBytes();
		httpResponse.forward(body);
	}

	@Override
	public void doPost(HttpRequest httpRequest, HttpResponse httpResponse) {

	}

	private boolean logined(HttpRequest httpRequest) {
		return httpRequest.getCookie("logined") != null && httpRequest.getCookie("logined").equals("true");
	}
}
