package com.raul.demoajax.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.raul.demoajax.entities.SocialMetaTag;
import com.raul.demoajax.service.SocialMetaTagService;

@Controller
@RequestMapping("/meta")
public class SocialMetaTagController {

	@Autowired
	private SocialMetaTagService service;
	
	//Post pq se enviar a requisição no formato Get td a url q colar no input do form vai ficar como param na url
	@PostMapping("/info")
	public ResponseEntity<SocialMetaTag> getDadosViaUrl(@RequestParam("url") String url){
		SocialMetaTag socialMetaTag = service.getSocialMetaTagByUrl(url);

		if (socialMetaTag != null) {
			return ResponseEntity.ok(socialMetaTag); 		//status 200
		}
		else {
			return ResponseEntity.notFound().build(); 		//status 404
		}
	}
}
