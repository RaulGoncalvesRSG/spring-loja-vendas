package com.raul.demoajax.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.raul.demoajax.entities.Promocao;

public interface PromocaoRepository extends JpaRepository<Promocao, Long>{
	
	/*Pega o total de promoções referentes à data passada como parâmetro
	É preciso nomear o retorno, nesse caso "count", pq o nome desse retorno será a chave do map
	O max signigica a data maior, a mais recente. as lastDate para pegar o último horário da data*/
	@Query("select count(p.id) as count, max(p.dtCadastro) as lastDate "
			+ "from Promocao p where p.dtCadastro > :data")
	Map<String, Object> totalAndUltimaPromocaoByDataCadastro(@Param("data") LocalDateTime data);
	
	@Query("select p.dtCadastro from Promocao p")
	Page<LocalDateTime> findUltimaDataDePromocao(Pageable pageable);
	
	//Localiza a promoção a partir do preço					Pageable para o processo de paginação
	@Query("select p from Promocao p where p.preco = :preco")
	Page<Promocao> findByPreco(@Param("preco") BigDecimal preco, Pageable pageable);
	
	//Pesquisa por 3 colunas: titulo, site ou categoria. A pesquisa é feita em um único campo de input 
	@Query("select p from Promocao p where p.titulo like %:search% "
			+ "or p.site like %:search% "
			+ "or p.categoria.titulo like %:search%")
	Page<Promocao> findByTituloOrSiteOrCategoria(@Param("search") String search, Pageable pageable);

	//Page é um obj do Spring Data para trabalhar com paginação de dados
	@Query("select p from Promocao p where p.site like :site")
	Page<Promocao> findBySite(@Param("site") String site, Pageable pageable);
	
	/*distinc pq vai retornar apenas uma ocorrência do nome
	Retorna a lista de strings de acordo com o que digitar no campo de pesquisa*/
	@Query("select distinct p.site from Promocao p where p.site like %:site%")
	List<String> findSitesByTermo(@Param("site") String site);
	
	/*Quando trabalha com Spring Data JPA, a anotação Query é para consultas, então para realizar uma operação
	 de escrita é preciso adicionar as notações: @Transactional e @Modifying*/
	@Transactional(readOnly = false)
	@Modifying
	@Query("update Promocao p set p.likes = p.likes + 1 where p.id = :id")
	void updateSomarLikes(@Param("id") Long id);
	
	@Query("select p.likes from Promocao p where p.id = :id")
	int findLikesById(@Param("id") Long id);
}
