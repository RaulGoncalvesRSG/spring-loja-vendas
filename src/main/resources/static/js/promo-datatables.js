$(document).ready(function(){
	moment.locale('pt-br');						//Declaração do locale
	
	var table = $("#table-server").DataTable({
		"language": {
			"url": "http://cdn.datatables.net/plug-ins/9dcbecd42ad/i18n/Portuguese-Brasil.json"
		},
		//Quando estiver paginando a tabela, aparece um barrinha no meio da tabela "processing", tipo loading
		processing: true,			
		serverSide: true,						//Habilita datable do lado do servidor para trabalhar com ela
		responsive: true,						//Comportamento responsivo
		lengthMenu: [ 10, 15, 20, 25 ],			//n° de itens para aparecer em cada página
		ajax: {
			url: "/promocao/datatables/server",
			data: "data"				//Parâmetro q recebe os dados, obj retorado para o cliente pelo servidor
		},
		columns: [
			{data: 'id'},
			{data: 'titulo'},
			{data: 'site'},
			{data: 'linkPromocao'},
			{data: 'descricao'},
			{data: 'linkImagem'},
			/*Instrução para formatar o preço, dentro dos () fica a formatação desejada. 
			A vírgula está substituindo o ponto e depois é a qtd de casas decimais*/
			{data: 'preco', render: $.fn.dataTable.render.number('.', ',', 2, 'R$')},
			{data: 'likes'},
			{data: 'dtCadastro', render: 
			//O arg da função é o valor q vai recuperar a partir do -> data: 'dtCadastro'
					function(dtCadastro) {			//Formatação tirada da pag: 	https://momentjs.com/
						return moment( dtCadastro ).format('LLL'); 
					}
			},
			{data: 'categoria.titulo'}
		],
		dom: 'Bfrtip',					//Acessa todo o conteúdo html da pag. "Bfrtip" está na documentação
		buttons: [
			{
				text: 'Editar',
				attr: {
					id: 'btn-editar',
					type: 'button'					
				},
				//Desabilita o btn qnd abrir a pag, ele so pode ser habilitado quando uma linha for selecionada
				enabled: false
			},
			{
				text: 'Excluir',
				attr: {
					id: 'btn-excluir',
					type: 'button'
				},
				enabled: false
			}
		]
	});
	
	// acao para marcar/desmarcar botoes ao clicar na ordenacao 
	$("#table-server thead").on('click', 'tr', function() {				//thead tem as colunas de ordenação
		//Sempre q clicar em alguma col de ordenação, como a linha perde a seleção, precisa desabilitar os botões
		table.buttons().disable();
	});
	
	/* acao para marcar/desmarcar linhas clicadas 
	table-server tbody é o ID 				tbody são as linhas*/
	$("#table-server tbody").on('click', 'tr', function() {		   //O click é em uma tag do tipo "tr" (linha)
		if ($(this).hasClass('selected')) {
			$(this).removeClass('selected');				//Desmarca pq está clicando em uma linha já selecionada
			table.buttons().disable();						//Desabilita os botões
		} else {			
			$('tr.selected').removeClass('selected');		//Remove a seleção anterior de alguma linha	
			$(this).addClass('selected');					//Seleciona a linha
			table.buttons().enable();						//Habilita os botões
		}
	});
	
	// acao do botao de editar (abrir modal)
	$("#btn-editar").on('click', function() {		
		if ( isSelectedRow() ) {				//Se a linha foi selecionada
			
			var id = getPromoId();
			$.ajax({
				method: "GET",
				url: "/promocao/edit/" + id,				//O id será um path na url e n um parâmetro
				
				//beforeSend para abrir abrir o modal depois do click do btn
				beforeSend: function() {							//Antes de abrir o modal, as msgs são limpadas
					$("span").closest('.error-span').remove();			//Removendo as mensagens		
					$(".is-invalid").removeClass("is-invalid");			//Remover as bordas vermelhas
					$("#modal-form").modal('show');						//Abre o modal
				},
				//O arg data traz os dados da promoção selecionada. Os valore são setados no campo
				success: function( data ) {
					$("#edt_id").val(data.id);					//"#edt_id" é o ID do campo html
					$("#edt_site").text(data.site);		  	 //text pq o valor será setado n é em um campo de input				
					$("#edt_titulo").val(data.titulo);
					$("#edt_descricao").val(data.descricao);
					//Por padrão, pega a formatação q está no BD, então formata para o form
					$("#edt_preco").val(data.preco.toLocaleString('pt-BR', {
						minimumFractionDigits: 2,				//Dígitos para os centavos
						maximumFractionDigits: 2
					}));
					//Pega o ID da categoria pq é a partir dele q irá preencher o comboBox com a lista
					$("#edt_categoria").val(data.categoria.id);
					$("#edt_linkImagem").val(data.linkImagem);
					//attr pq é um campo de img					src é o nome do atributo q será manipulado
					$("#edt_imagem").attr('src', data.linkImagem);
				},
				error: function() {
					alert("Ops... algum erro ocorreu, tente novamente.");
				}
			});			
		}
	});
	
	// submit do formulario para editar (edita e manda o obj para o servidor para salvar)
	$("#btn-edit-modal").on("click", function() {
		var promo = {};
		promo.descricao = $("#edt_descricao").val();
		promo.preco = $("#edt_preco").val();
		promo.titulo = $("#edt_titulo").val();
		promo.categoria = $("#edt_categoria").val();
		promo.linkImagem = $("#edt_linkImagem").val();
		promo.id = $("#edt_id").val();
		
		$.ajax({
			method: "POST",
			url: "/promocao/edit",
			data: promo,					//Parâmetro data responsável por enviar a var promo para o servidor
			beforeSend: function() {
				$("span").closest('.error-span').remove();		//Remove as mensagens			
				$(".is-invalid").removeClass("is-invalid");		//Remove as bordas vermelhas
			},
			success: function() {
				$("#modal-form").modal("hide");				//modal-form é o ID do modal
				table.buttons().disable();					//Desabilita os botões
				table.ajax.reload();						//Atualoza a tabela
			},
			statusCode: {
				422: function(xhr) {						//422 é quando o form n passa na validação
					console.log('status error:', xhr.status);
					var errors = $.parseJSON(xhr.responseText);
					$.each(errors, function(key, val){
						$("#edt_" + key).addClass("is-invalid");			//Borda avermelhada
						$("#error-" + key)
							.addClass("invalid-feedback")
							.append("<span class='error-span'>" + val + "</span>")
					});
				}
			}
		});
	});
	
	// alterar a imagem no componente <img> do modal
	$("#edt_linkImagem").on("change", function() {
		var link = $(this).val();					//Recupera o valor da url da img 
		$("#edt_imagem").attr("src", link);			//Atualiza a img de acordo com o link
	});
	
	// acao do botao de excluir (abrir modal)
	$("#btn-excluir").on('click', function() {
		if ( isSelectedRow() ) {
			$("#modal-delete").modal('show');				//modal-delete é o ID do modal      Abre o modal
		}
	});
	
	// exclusao de uma promocao
	$("#btn-del-modal").on('click', function() {			//Acessa o botão de exclusão do modal
		var id = getPromoId();
		
		/*O jQuery n sugere o uso do DELETE, ele aceita perfeitamente o POST, GET e PUT, mas o DELETE pode n 
		funcionar as vezes, pq alguns navegadores podem não ter suporte a ele. Evite usar -> method: "DELETE"*/
		$.ajax({
			method: "GET",
			url: "/promocao/delete/" + id,					//N passa um param ID, passa um path (caminho) na url
			success: function() {
				$("#modal-delete").modal('hide');			//Fecha o modal
				table.buttons().disable();
				table.ajax.reload();						//Atualiza a tabela com o btn excluído sem refresh
			},
			error: function() {
				alert("Ops... Ocorreu um erro, tente mais tarde.");
			}
		});
	});
	
	function getPromoId() {
		return table.row(table.$('tr.selected')).data().id;		//data é o obj promocao com todos os atributos		
	}
	
	function isSelectedRow() {
		var trow = table.row(table.$('tr.selected'));			//Pega a linha selecionada
		//trow.data() é o obj q deveria ter em uma linha selecionada
		return trow.data() !== undefined;						//Verifica se a linha está selecionada
	}
});