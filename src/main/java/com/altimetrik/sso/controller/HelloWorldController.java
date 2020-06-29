package com.altimetrik.sso.controller;

import java.util.HashMap;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

	@RequestMapping({ "/hello" })
	public HashMap<String, String> firstPage() {
		HashMap<String, String> map = new HashMap<>();
		map.put("message", "Hello Chetan");

		return map;
	}
}
