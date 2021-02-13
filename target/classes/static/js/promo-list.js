var pageNumber = 0;

//(document).ready serve para q quando abra a pag, td q tem dentro dessa função, seja executado logo no início
$(document).ready(function(){
	$("#loader-img").hide();				//Esconde o loader e o btn
	$("#fim-btn").hide();
});

// efeito infinte scroll - chamada de paginação pela barra de rolagem
$(window).scroll(function() {		//scroll verifica quando está rolando a barra de rolagem ou o mouse
	
	//this é ferente ao window. scrollTop pega o valor atual da altura da barra de acordo com a tela
	var scrollTop = $(this).scrollTop();			//Pega a altura do momento	  
	//Valor da altura referente à pag pq tem uma parte da tela é ocupada pelo rodapé e cabeçalho do brower
	var conteudo = $(document).height() - $(window).height();		
	
	//console.log('scrollTop: ', scrollTop, ' | ', 'conteudo', conteudo);
	
	if (scrollTop >= conteudo) {
		pageNumber++;				//Incrementa a pág quando chegar no ponto da chamada
		setTimeout(function(){ 
			loadByScrollBar(pageNumber);			//Chama a função e acessa a url list/ajax
		}, 200);
	}
	
});

//Função para a paginação
function loadByScrollBar(pageNumber) {
	var site = $("#autocomplete-input").val(); //Sempre q fizer a paginação, captura o valor do campo de input
	
	$.ajax({
		method: "GET",
		url: "/promocao/list/ajax",
		data: {
			//page - recebe o valor da pág (param q será enviado junto com a url)
			page: pageNumber,
			site: site
		},
	//Pode usa o param de 2 formas: ?page=pageNumber na url ou utiliza o data q envia como param da solicitação
		
		beforeSend: function() {
			$("#loader-img").show();				//Mostra a img de load quando tiver uma requisição ajax
		},
		success: function( response ) {
			//console.log("resposta > ", response);
			console.log("lista > ", response.length);
			
			if (response.length > 150) {				//150 é o tamanho do obj de resposta
			
				$(".row").fadeIn(250, function() {
					$(this).append(response);
				});
			
			} else {
				$("#fim-btn").show();						//Mostra o btn
				$("#loader-img").removeClass("loader");		//Remove pq n possui mais promoções para carregar
			}
		},
		error: function(xhr) {
			alert("Ops, ocorreu um erro: " + xhr.status + " - " + xhr.statusText);
		},
		complete: function() {
			$("#loader-img").hide();					//Remove a img
		}
	})  
}

//autocomplete é um método q obtém a partir do JS q foi adicionado na pag layout
$("#autocomplete-input").autocomplete({
	source: function(request, response) {		//Argumentos para enviar e receber resposta da solicitação
		$.ajax({
			method: "GET",
			url: "/promocao/site",
			data: {								//data é o parâmetro enviado para o servidor
				termo: request.term				//request.term pega o valor digitado no campo
			},
			success: function(result) {
				response(result);
			}
		});
	}
});

$("#autocomplete-submit").on("click", function() {
	var site = $("#autocomplete-input").val();				//Recupera o valor do campo de entrada
	$.ajax({
		method: "GET",
		url: "/promocao/site/list",
		data: {
			site : site
		},
		beforeSend: function() {
			pageNumber = 0;									//Restaura o valor da paginação
			$("#fim-btn").hide();							//Esconde o btn
			$(".row").fadeOut(400, function(){				//Esconde os cards
				$(this).empty();		 //Faz refência ao div da classe ".row" o empty limpa td dentro do div
			});
		},
		success: function(response) {
			$(".row").fadeIn(250, function(){
				$(this).append(response);					//Mostra os novos cards na pag
			});
		},
		error: function(xhr) {
			alert("Ops, algo deu errado: " + xhr.status + ", " + xhr.statusText);
		}
	});
});

$("#autocomplete-input").keypress(function(event){
	if (event.which == 13) {							//Tecla Enter
		event.preventDefault();
		
		var site = $("#autocomplete-input").val();
		
		$.ajax({
			method: "GET",
			url: "/promocao/site/list",
			data: {
				site : site
			},
			beforeSend: function() {
				pageNumber = 0;									//Restaura o valor da paginação
				$("#fim-btn").hide();							//Esconde o btn
				$(".row").fadeOut(400, function(){				//Esconde os cards
					$(this).empty();		 //Faz refência ao div da classe ".row" o empty limpa td dentro do div
				});
			},
			success: function(response) {
				$(".row").fadeIn(250, function(){
					$(this).append(response);					//Mostra os novos cards na pag
				});
			},
			error: function(xhr) {
				alert("Ops, algo deu errado: " + xhr.status + ", " + xhr.statusText);
			}
		});
	}
});

/*Método de adicionar likes		
Recupera o id do btn q foi clicado				button é o componente
id*: o * signfica q o jQuery deve esperar por um click de qlqr btn q tem a instrução "likes-btn-"*/
$(document).on("click", "button[id*='likes-btn-']", function() {
	/*this faz referência ao tbn clicado
	attr("id") para dizer q qr recuperar algo do componente id do componente 
	split("-"): [0]=likes	[1]=btn		[2]=id*/
	var id = $(this).attr("id").split("-")[2];
	console.log("id: ", id);				//Testa se pega o id correto
	
	$.ajax({
		method: "POST",
		url: "/promocao/like/" + id,
		success: function(response) {
			$("#likes-count-" + id).text(response);			//response recebe a resposta do servidor
		},
		error: function(xhr) {
			alert("Ops, ocorreu um erro: " + xhr.status + ", " + xhr.statusText);
		}
	});
});

//AJAX REVERSE===========================================================================================
//Recebe do lado servidor o total de novas promoções para visualizar. Qnd clicar no btn, zera a variável
var totalOfertas = 0;				

$(document).ready(function() {
	init();
});

/*Função q inicia e abre o canal de comunicação entre cliente e servidor. Esse canal deve ser aberto logo
quando a pag for aberta no navegador*/
function init() {				
	console.log("dwr init...");
	
	dwr.engine.setActiveReverseAjax(true);			//Habilita o reverse ajax no lado cliente
	//Instrução q captura msg de erro. Se tiver algum erro, o método setErrorHandler é chamado
	dwr.engine.setErrorHandler(error);				
	
	//DWRAlertaPromocoes é uma classe do lado servidor q trabalha com ajax reverso
	DWRAlertaPromocoes.init();      //Método da classe q abre o canal de comunicação entre cliente e servidor	
}

function error(excpetion) {
	console.log("dwr error: ", excpetion);
}

/*Função responsável por receber as informações q o lado servidor está enviando para o cliente
showButton recebe o valor do count através do método run da  classe interna AlertTask*/
function showButton(count) {					
	totalOfertas = totalOfertas + count;
	
	$("#btn-alert").show(function() {		//attr pq vai trabalhar com atributo
		//"display: block;" faz q o btn apareça na pag
		$(this)
			.attr("style", "display: block;")
			.text("Veja " + totalOfertas + " nova(s) oferta(s)!");
	});
}

//Botão de alerta para novas promoções - a tela é atualizada com a promoção cadastrada
$("#btn-alert").on("click", function() {
	
	$.ajax({
		method: "GET",
		url: "/promocao/list/ajax",
	/*0 pq td vez q for att a pag, vai buscar as ultimas 8 promoções no BD e essas 8 fazem parte da pag 0
	Mesmo se tiver apenas 1 promoção nova, será retornado as 8 utlimas	*/
		data: {
			page : 0		 
		},
		beforeSend: function() {
			pageNumber = 0;
			totalOfertas = 0;
			$("#fim-btn").hide();
			$("#loader-img").addClass("loader");
			$("#btn-alert").attr("style", "display: none;");		//Remove da tela o btn de alerta
			$(".row").fadeOut(400, function(){
				$(this).empty();
			});
		},
		success: function(response) {
			$("#loader-img").removeClass("loader");
			$(".row").fadeIn(250, function(){
				$(this).append(response);
			});
		},
		error: function(xhr) {
			alert("Ops, algo deu errado: " + xhr.status + ", " + xhr.statusText);
		}
	});
});