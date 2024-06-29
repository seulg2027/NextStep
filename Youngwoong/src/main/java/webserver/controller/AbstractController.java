package webserver.controller;

import java.util.HashMap;
import java.util.Map;

import webserver.HttpRequest;
import webserver.HttpResponse;

public abstract class AbstractController implements Controller {

	public static Map<String, AbstractController> controllers = new HashMap<>();

	static {
		controllers.put("/user/create", new CreateUserController());
		controllers.put("/user/list", new ListUserController());
		controllers.put("/user/login", new LoginController());
	}

	@Override
	public void service(HttpRequest httpRequest, HttpResponse httpResponse) {
		if(!httpRequest.getMethod().isPost()) {
			controllers.get(httpRequest.getPath()).doGet(httpRequest, httpResponse);
		} else {
			controllers.get(httpRequest.getPath()).doPost(httpRequest, httpResponse);
		}
	};

	public abstract void doGet(HttpRequest httpRequest, HttpResponse httpResponse);

	public abstract void doPost(HttpRequest httpRequest, HttpResponse httpResponse);

}
