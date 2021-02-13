package com.raul.demoajax.service;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.raul.demoajax.entities.SocialMetaTag;

@Service
public class SocialMetaTagService {
	
	private static Logger log = LoggerFactory.getLogger(SocialMetaTagService.class);

	public SocialMetaTag getSocialMetaTagByUrl(String url) {
		SocialMetaTag twitter = getTwitterCardByUrl(url);
		
		if (!isEmpty(twitter)) {
			return twitter;
		}
		
		SocialMetaTag openGraph = getTwitterCardByUrl(url);
		
		if (!isEmpty(openGraph)) {
			return openGraph;
		}
		
		return null;
	}
	
	/*Está usando dois tipos de meta tags (Twitter-Card e Open-Graph) pq espera-se q uma das duas opções se 
	encontrem na maioria das págs*/
	private SocialMetaTag getTwitterCardByUrl(String url) {
		SocialMetaTag tag = new SocialMetaTag();
		
		try {
			Document doc = Jsoup.connect(url).get();

			//doc.select("meta[name=twitter:title]").attr("content")		//Funcionou
			tag.setTitle(doc.head().select("meta[name=twitter:title]").attr("content"));
			tag.setSite(doc.head().select("meta[name=twitter:site]").attr("content"));
			tag.setImage(doc.head().select("meta[name=twitter:image]").attr("content"));
			tag.setUrl(doc.head().select("meta[name=twitter:url]").attr("content"));
			
		} catch (IOException e) {
			log.error(e.getMessage(), e.getCause());
		} 	
		return tag;		
	}
	
	private SocialMetaTag getOpenGraphByUrl(String url) {
		SocialMetaTag tag = new SocialMetaTag();		//Url de acesso à pag
		
		try {
			Document doc = Jsoup.connect(url).get();   //Recupera o obj Document com o conteúdo html dentro dele
			//Entra no cabeçalho e depois pega o atributo da tag "content"
			tag.setTitle(doc.head().select("meta[property=og:title]").attr("content"));
			tag.setSite(doc.head().select("meta[property=og:site_name]").attr("content"));
			tag.setImage(doc.head().select("meta[property=og:image]").attr("content"));
			tag.setUrl(doc.head().select("meta[property=og:url]").attr("content"));
			
		} catch (IOException e) {
			//Quando trabalha com aplicação em m.produção não é legal utilizar o recurso e.printStackTrace();
			log.error(e.getMessage(), e.getCause());			//Instruções de log
		}
		return tag;		
	}
	
	private boolean isEmpty(SocialMetaTag tag) {
		/*Se retornar true signigca q o obj meta tag não tem todas as informações esperadas. Então n irá usar
		este objeto e vai entrar usar outro método de captura de informações pelas meta tags*/
		if (tag.getImage().isEmpty()) return true;
		else if (tag.getSite().isEmpty()) return true;
		else if (tag.getTitle().isEmpty()) return true;
		else if (tag.getUrl().isEmpty()) return true;
		
		return false;
	}
}
