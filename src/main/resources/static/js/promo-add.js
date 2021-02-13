//Todo JS q pertercer a pag "promo-add" estará aq

/*form-add-promo (ID do form). O método submit vai simular o btn de submit q teríamos em uma pag html q fosse 
fazer uma requisição normal (sem ajax)*/
$("#form-add-promo").submit(function(evt) {			//submit do formulario para o controller
	
	evt.preventDefault()    //evt(evento). preventDefault: bloquear o comportamento padrão do submit (refresh)
	
	var promo = {};	   //obj q representa promocao. As variáveis precisam ter o msmo nome da class Promocao
	promo.linkPromocao = $("#linkPromocao").val();
	promo.descricao = $("#descricao").val();
	promo.preco = $("#preco").val();
	promo.titulo = $("#titulo").val();
	promo.categoria = $("#categoria").val();
	promo.linkImagem = $("#linkImagem").attr("src");		//attr para recuperar o valor da url da img
	promo.site = $("#site").text();				//text paraa recuperar o valor da tag h5
	
	console.log('promo > ', promo);				//Console do navegador
	
	// data é qm recebe os valores que vc qr enviar para o lado servidor
	$.ajax({
		method: "POST",
		url: "/promocao/save",
		data: promo,
		beforeSend: function() {
			// removendo as mensagens do campo span criado no statusCode 422
			$("span").closest('.error-span').remove();
			
			//remover as bordas vermelhas
			$("#categoria").removeClass("is-invalid");
			$("#preco").removeClass("is-invalid");
			$("#linkPromocao").removeClass("is-invalid");
			$("#titulo").removeClass("is-invalid");
		
	//hide é um método para esconder algo na página. Qnd o beforeSend for executado, o form será escondido
			$("#form-add-promo").hide();
			//Depois q o form é escondido, mostra o loader na tela
			$("#loader-form").addClass("loader").show();
		},
		success: function() {
			//each trata cada um dos componentes html dentro da tag form
			$("#form-add-promo").each(function() {
				this.reset();		 //Limpa todo o form (tag de entrada de dados: input, select, textArea)
			});
			$("#linkImagem").attr("src", "/images/promo-dark.png");			//Volta para a img padrão
			$("#site").text("");			//Limpando o nome do site
			//Adiciona a msg no div. alert-success fax com q o componente fique com a cor verde
			$("#alert").removeClass("alert alert-danger")
						.addClass("alert alert-success")
						.text("OK! Promoção cadastrada com sucesso.");
		},
		statusCode: {
			422: function(xhr) {
				console.log('status error:', xhr.status);		//status mostra o cod de erro no console
				var errors = $.parseJSON(xhr.responseText);		//Pega a resposta (Map) e converte em Json
				
			 //errors é a lista				val contém o valor da msg q foi adicionada da classe da entidade
				$.each(errors, function(key, val){				//Valores q n passaram na regra de validação
					$("#" + key).addClass("is-invalid");		//Ex: # + 'titulo'. Adiciona a borda veremlha. 
					//invalid-feedback adiciona a msg já com a cor vermelha
					//append add algo dentro do campo trabalho, nesse caso será colocado entre as tags do div
					$("#error-" + key)
						.addClass("invalid-feedback")
						.append("<span class='error-span'>" + val + "</span>")
				});
			}
		},
		error: function(xhr) {
			//xhr parâmetro q traz a msg referente à um erro q pode ocorrer durante a requsição
			console.log("> error: ", xhr.responseText);
			$("#alert").addClass("alert alert-danger").text("Não foi possível salvar esta promoção.");
		},
		complete: function() {
		//fadeOut parece com o "hide", mas o componente é escondido aos poucos de acordo com o tempo em ms
			$("#loader-form").fadeOut(800, function() {
				$("#form-add-promo").fadeIn(250);			//Traz oq estava escondido suavemente em ms
				$("#loader-form").removeClass("loader");	//Remove a img de loader
			});
		}
	});
});

/*funcao para capturar as meta tags
$("#linkPromocao"): Essa instrução indica q quer acessar o componente na pag html q possui o id "linkPromocao"
A função on change é disparada quando há uma alteração no componente e ele perde o foco*/
$("#linkPromocao").on('change', function() {

	//Pega o valor da str q estiver no campo do input	
	var url = $(this).val();
	
	//Td url tem "http://"
	if (url.length > 7) {
		
		$.ajax({
			//Mesmo método do lado do servidor
			method:"POST",
			url: "/meta/info?url=" + url,
			cache: false,
			//Antes de qualquer requisição, limpa os campos do formulário
			beforeSend: function() {
				$("#alert").removeClass("alert alert-danger").text('');
				$("#titulo").val("");
				$("#site").text("");
				//Apaga a img para q entre o loader (carregamento)
				$("#linkImagem").attr("src", "");
				$("#loader-img").addClass("loader");
			},
			success: function( data ) {
				//console.log(data) - resultado da operação mostrado no log do navegador (pode remover se quiser)
				console.log(data);
				//title é o atributo da classe SocialMetaTag. Coloca o valor em um input, por isso usa o "val"
				$("#titulo").val(data.title);
				//O site é um "h5" pq o h5 n tem um atributo do tipo value, então usa o text
				$("#site").text(data.site.replace("@", ""));		//Tbm pode usar o replace no lado servidor
				//image é o atributo da classe SocialMetaTag
				$("#linkImagem").attr("src", data.image);
			},
			//Erros esperados, pode tratar quantos erros quiser
			statusCode: {
				404: function() {
					//alert está em um div da pág promo-add. addClass adiciona uma classe css no componente trabalhado
					$("#alert").addClass("alert alert-danger").text("Nenhuma informação pode ser recuperada dessa url.");
					$("#linkImagem").attr("src", "/images/promo-dark.png");
				}
			},
			//Erros genéricos
			error: function() {
				$("#alert").addClass("alert alert-danger").text("Ops... algo deu errado, tente mais tarde.");
				$("#linkImagem").attr("src", "/images/promo-dark.png");	  //Adiciona a imagem padrão de promoção
			},
			//Função executada depois de um sucesso ou erro
			complete: function() {
				$("#loader-img").removeClass("loader");			//Encerra o loader
			}
		});
	}
});