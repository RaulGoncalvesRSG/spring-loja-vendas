package com.raul.demoajax.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;

@Entity
public class Promocao implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank(message = "O título é requerido.")
	@Column(nullable = false)
	private String titulo;
	
	@NotBlank(message = "O link da promoção é requerido.")
	@Column(name = "link_promocao", nullable = false)
	private String linkPromocao;
	
	@Column(name = "site_promocao", nullable = false)
	private String site;
	
	private String descricao;
	
	@Column(name = "link_imagem", nullable = false)
	private String linkImagem;
	
	//Como o preço n é Str, então usa o NotNull		CURRENCY é o tipo moeda
	@NotNull(message = "O preço da promoção é requerido.")
	@NumberFormat(style = Style.CURRENCY, pattern = "#,##0.00")
	@Column(name = "preco_promocao", nullable = false)
	private BigDecimal preco;
	
	@Column(name = "total_likes")
	private Integer likes;
	
	@Column(name = "data_cadastro", nullable = false)
	private LocalDateTime dtCadastro;
	
	@NotNull(message = "Uma categoria é requerida.")
	@ManyToOne
	private Categoria categoria;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getLinkPromocao() {
		return linkPromocao;
	}

	public void setLinkPromocao(String linkPromocao) {
		this.linkPromocao = linkPromocao;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getLinkImagem() {
		return linkImagem;
	}

	public void setLinkImagem(String linkImagem) {
		this.linkImagem = linkImagem;
	}

	public BigDecimal getPreco() {
		return preco;
	}

	public void setPreco(BigDecimal preco) {
		this.preco = preco;
	}

	public Integer getLikes() {
		return likes;
	}

	public void setLikes(Integer likes) {
		this.likes = likes;
	}

	public LocalDateTime getDtCadastro() {
		return dtCadastro;
	}

	public void setDtCadastro(LocalDateTime dtCadastro) {
		this.dtCadastro = dtCadastro;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Promocao other = (Promocao) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Promocao [id=" + id + ", titulo=" + titulo + ", linkPromocao=" + linkPromocao + ", site=" + site
				+ ", descricao=" + descricao + ", linkImagem=" + linkImagem + ", preco=" + preco + ", likes=" + likes
				+ ", dtCadastro=" + dtCadastro + ", categoria=" + categoria + "]";
	}
}
