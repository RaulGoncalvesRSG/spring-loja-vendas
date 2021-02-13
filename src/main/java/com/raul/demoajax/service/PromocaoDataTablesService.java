package com.raul.demoajax.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.raul.demoajax.entities.Promocao;
import com.raul.demoajax.repositories.PromocaoRepository;

public class PromocaoDataTablesService {

	/*Nome dos atributos da classe Promocao seguindo exatamente a msma ordem da pag html e das colunas do JS
	Isso é para relacionar as colunas da tabela com os atributos da classe Promocao*/
	private String[] cols = {
		"id", "titulo", "site", "linkPromocao", "descricao", "linkImagem",
		"preco", "likes", "dtCadastro", "categoria"
	};
	
	//O request é para recuperar as informações q são enviadas pelo cliente para o lado servidor
	public Map<String, Object> execute(PromocaoRepository repository, HttpServletRequest request) {
		//Os nomes dos parâmetros foram retirados da documentação
		int start = Integer.parseInt(request.getParameter("start"));		//Informação com o n° da pag
		int lenght = Integer.parseInt(request.getParameter("length"));		//Qtd de itens por pag na tabela
		int draw = Integer.parseInt(request.getParameter("draw"));			//Incirementa quando passar de pag
		
		int current = currentPage(start, lenght);			//Valor da pag que qr recuperar
		
		String column = columnName(request);				//Coluna q ordena a tabela
		Sort.Direction direction = orderBy(request);		//Ordem asc ou desc	
		String search = searchBy(request);					//Valor digitado no campo de pesquisa
		
		Pageable pageable = PageRequest.of(current, lenght, direction, column);
		
		Page<Promocao> page = queryBy(search, repository, pageable);		//Pega o retorno da consulta
		
		Map<String, Object> json = new LinkedHashMap<>();		//Inclui os dados q serão enviados como resposta
		json.put("draw", draw);
		json.put("recordsTotal", page.getTotalElements());
		json.put("recordsFiltered", page.getTotalElements());
		json.put("data", page.getContent());					//Lista de Promocoes
		
		return json;
	}

	//Método de consulta
	private Page<Promocao> queryBy(String search, PromocaoRepository repository, Pageable pageable) {		
		
		if (search.isEmpty()) {
			return repository.findAll(pageable);				//Usuário nao digitou no campo
		}

		/*Expressão regular para verificar se a pesquisa feita no campo é um valor monetário
		https://regex101.com/						digito (. ou ,) dois dígitos*/
		if (search.matches("^[0-9]+([.,][0-9]{2})?$")) {			
			search = search.replace(",", ".");						//No BD o valor número é com ponto
			return repository.findByPreco(new BigDecimal(search), pageable);
		}
		
		return repository.findByTituloOrSiteOrCategoria(search, pageable);
	}
	
	//Retorna o que foi digitado no campo de pesquisa
	private String searchBy(HttpServletRequest request) {
		//request.getParameter("search[value]") recupera o valor digitado no campo
		return request.getParameter("search[value]").isEmpty() ? "" : request.getParameter("search[value]");
	}	

	//Recupera o tipo de ordenação: asc ou desc
	private Direction orderBy(HttpServletRequest request) {
		String order = request.getParameter("order[0][dir]");
		Sort.Direction sort = Sort.Direction.ASC;
		
		if (order.equalsIgnoreCase("desc")) {
			sort = Sort.Direction.DESC;
		}
		return sort;
	}

	//Recupera qual coluna está ordenando os dados na tabela
	private String columnName(HttpServletRequest request) {
		//0 pq por padrão a coluna de ordenção é sempre a primeira coluna (ID nesse caso)
		int iCol = Integer.parseInt(request.getParameter("order[0][column]"));		//Posição da coluna da tabela
		return cols[iCol];
	}

	//start contém informação da primeira linha de cada pag e lenght é a qtd de itens de cada pag
	private int currentPage(int start, int lenght) {
		//0			1			2			Representa o n° da pag
		//0-9 |	10-19 	| 20-29				2° pag -> 10 / 10 = 1			3° pag -> 20 / 10 = 2 	
		return start / lenght;				//Assim consegue saber qual pag precisa consultar
	}
	
}
