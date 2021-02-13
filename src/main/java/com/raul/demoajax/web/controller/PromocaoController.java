package com.raul.demoajax.web.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.raul.demoajax.dto.PromocaoDTO;
import com.raul.demoajax.entities.Categoria;
import com.raul.demoajax.entities.Promocao;
import com.raul.demoajax.repositories.CategoriaRepository;
import com.raul.demoajax.repositories.PromocaoRepository;
import com.raul.demoajax.service.PromocaoDataTablesService;

@Controller
@RequestMapping("/promocao")
public class PromocaoController {
	
	private static Logger log = LoggerFactory.getLogger(PromocaoController.class);
	
	@Autowired
	private PromocaoRepository promocaoRepository;
	
	@Autowired
	private CategoriaRepository categoriaRepository;
	
	//DATATABLES===================================================================================
	
	@GetMapping("/tabela")						//Método responsável por abrir a pag
	public String showTabela() {
		return "promo-datatables";
	}
	
	//Método responsável por receber e responder a requisição do JS
	@GetMapping("/datatables/server")			//Request para ter acesso às variáveis q estão na solicitação
	public ResponseEntity<?> datatables(HttpServletRequest request){
		Map<String, Object> data = new PromocaoDataTablesService().execute(promocaoRepository, request);
		return ResponseEntity.ok(data);
	}
	
	@GetMapping("/delete/{id}")
	public ResponseEntity<?> excluirPromocao(@PathVariable("id") Long id){
		promocaoRepository.deleteById(id);
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("/edit/{id}")				//Método para preencher os campos do form q será editado
	public ResponseEntity<?> preEditar(@PathVariable("id") Long id){
		Promocao promocao = promocaoRepository.findById(id).get();
		return ResponseEntity.ok(promocao);
	}
	
	@PostMapping("/edit")
	public ResponseEntity<?> editarPromocao(@Valid PromocaoDTO dto, BindingResult result){
		if (result.hasErrors()) {
			Map<String, String> errors = new HashMap<>();
			
			for (FieldError error: result.getFieldErrors()) {
				errors.put(error.getField(), error.getDefaultMessage());
			}
			
			return ResponseEntity.unprocessableEntity().body(errors);
		}
		
		Promocao promocao = promocaoRepository.findById(dto.getId()).get();
		promocao.setCategoria(dto.getCategoria());
		promocao.setDescricao(dto.getDescricao());
		promocao.setLinkImagem(dto.getLinkImagem());
		promocao.setPreco(dto.getPreco());
		promocao.setTitulo(dto.getTitulo());
		
		promocaoRepository.save(promocao);
		
		return ResponseEntity.ok().build();
	}
	
	//AUTOCOMPLETE===================================================================================
	
	@GetMapping("/site")					//Retorna para a barra de pesquisa a lista de strings
	public ResponseEntity<?> autocompleteByTermo(@RequestParam("termo") String termo){
		List<String> sites = promocaoRepository.findSitesByTermo(termo);
		return ResponseEntity.ok(sites);
	}
	
	@GetMapping("/site/list")		//Usa o Model para adicionar um valor em uma determinada variável da pag
	public String listarPorSite(@RequestParam("site") String site, ModelMap model) {
		Sort sort = Sort.by(Sort.Direction.DESC, "dtCadastro");
		
		PageRequest pageRequest = PageRequest.of(0, 8, sort);		//N° pag, tamanho da pag, ordenação
		model.addAttribute("promocoes", promocaoRepository.findBySite(site, pageRequest));
		
		return "promo-card";
	}
	
	//ADD LIKES===================================================================================
	
	@PostMapping("/like/{id}")					//PathVariable para recuperar o valor do parâmetro
	public ResponseEntity<?> adicionarLikes(@PathVariable("id") Long id){
		promocaoRepository.updateSomarLikes(id);
		int likes = promocaoRepository.findLikesById(id);
				
		return ResponseEntity.ok(likes);
	}
	
	//LISTAR OFERTAS==============================================================================
	@GetMapping("/list")
	public String listarOfertas(ModelMap model) {
		Sort sort = Sort.by(Sort.Direction.DESC, "dtCadastro");
		
		//N° da pag, tamanho da pag, ordenação
		PageRequest pageRequest = PageRequest.of(0, 8, sort);		//Aparece 8 registros de vez	
		
		model.addAttribute("promocoes", promocaoRepository.findAll(pageRequest));
		
		return "promo-list";
	}
	
	//Para setar um valor padrão quando o parâmetro n for enviado
	@GetMapping("/list/ajax")
	public String listarCards(@RequestParam(name = "page", defaultValue = "1") int page, 
							  @RequestParam(name = "site", defaultValue = "") String site, 
							  ModelMap model) {
		Sort sort = Sort.by(Sort.Direction.DESC, "dtCadastro");
		
		PageRequest pageRequest = PageRequest.of(page, 8, sort);	
		
		if (site.isEmpty()) {
			model.addAttribute("promocoes", promocaoRepository.findAll(pageRequest));
		} else {
			model.addAttribute("promocoes", promocaoRepository.findBySite(site, pageRequest));
		}
		return "promo-card";
	}

	// ======================================ADD OFERTAS=============================================
	@PostMapping("/save")		//Retorno genérico para poder retornar os erros tbm
	public ResponseEntity<?> salvarPromocao(@Valid Promocao promocao, BindingResult result){
		/*Monta um obj do tipo Map com chave e valor pq quando fizer o retorno da resposta com o erro
		referente às validações, precisamos enviar para o jQuery o obj no formato Json, q é formado
		 por chave e valor*/
		
		if (result.hasErrors()) {
			Map<String, String> errors = new HashMap<>();
			
			for (FieldError error: result.getFieldErrors()) {
				errors.put(error.getField(), error.getDefaultMessage());
			}
			
			//Método com stts 422 - dados do cliente vão para o servidor n condizem com o esperado
			return ResponseEntity.unprocessableEntity().body(errors);
		}
		
		promocao.setDtCadastro(LocalDateTime.now());
		promocao.setLikes(0);
		promocaoRepository.save(promocao);
		log.info("Promocao {}", promocao.toString());		//Para logar as instruções no console
		
		return ResponseEntity.ok().build();
	}

	@ModelAttribute("categorias")	//Nome da variável q vai conter a lista e q vai levar a lista para a pag
	public List<Categoria> getCategorias(){
		return categoriaRepository.findAll();
	}
	
	@GetMapping("/add")
	public String abrirCadastro(){
		return "promo-add";
	}
}
