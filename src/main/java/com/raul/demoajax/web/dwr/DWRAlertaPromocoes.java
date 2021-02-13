package com.raul.demoajax.web.dwr;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.directwebremoting.Browser;
import org.directwebremoting.ScriptSessions;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import com.raul.demoajax.repositories.PromocaoRepository;

@Component					//Transforma a classe em um bean gerenciado pelo spring
/*Essa anotação serve como uma configuração de proxy remoto q vai ser utilizado para comunicação entre 
 servidor e cliente via dwr*/
@RemoteProxy				
public class DWRAlertaPromocoes {

	@Autowired
	private PromocaoRepository repository;
	
	private Timer timer;				//Para utilizar no agendamento de tarefas na classe
	
	/*Quando o usuário abrir a pag, o método do lado cliente DWRAlertaPromocoes.init() será executado
	e cair nessa classe*/
	private LocalDateTime getDtCadastroByUltimaPromocao() {
		//pagina para consulta/ qtd de elementos na pag/ direção/ atributo para ordenação
		PageRequest pageRequest = PageRequest.of(0, 1, Direction.DESC, "dtCadastro");
		return repository.findUltimaDataDePromocao(pageRequest)
				.getContent()				//getContent retorna um obj java.util.List
				.get(0);					//Retorna a data mais recente da promoção cadastrada
	}
	
	/*@RemoteMethod - essa anotação faz a configuração de relação entre o método init do servidor e o init 
	 do cliente (DWRAlertaPromocoes.init() )*/
	@RemoteMethod
	public synchronized void init() {					//Trabalha com thread
		System.out.println("DWR está ativado!");
		
		LocalDateTime lastDate = getDtCadastroByUltimaPromocao();
		
		WebContext context = WebContextFactory.get();
		
		timer = new Timer();
		/*schedule é um método de agendamento de tarefas	
		 tempo de dalay/ tempo de cada execução da tarefa
		1° param de tempo - tempo de delay q é o tempo q a tarefa vai levar para ser executada pela 1°x
		2° param de tempo - significa q a cada 60s a tarefa (código no AlertTask) será executada e depois 
		de 60s é executado novamente. Isso é um agendamento de tarefa*/
		timer.schedule(new AlertTask(context, lastDate), 10000, 60000);
	}
	
	/*Conceito de classe interna
	TimerTask é a classe q trabalha junto com a classe Time para a realização da tarefa
	A classe Time serve para o agendamento e a classe TimerTask para a execução
	
	A cada 60s todo o bloco de código da classe AlertTask será executado*/
	class AlertTask extends TimerTask {

		private LocalDateTime lastDate;
		private WebContext context;
		private long count;
		
		public AlertTask(WebContext context, LocalDateTime lastDate) {
			super();
			this.lastDate = lastDate;
			this.context = context;
		}

		@Override				//Método do agendamento de tarefas
		public void run() {
			String session = context.getScriptSession().getId();		//Retorna o ID da sessão
			
			/*Cada user q abrir a pag terá um id diferente para a sua sessão. É por esse id q a dwr vai
			conseguir saber para onde ela precisa enviar as informações q o servidor qr att o cliente
			
			O ultimo param é uma thread pq esse método trabalha em forma de thread*/
			Browser.withSession(context, session, new Runnable() {
				
				@Override			//Thread da dwr para trabalhar com ajax reverso
				public void run() {
		//A query contém um count com o total de novas promoções cadastradas e a data da última promoção
					Map<String, Object> map = 
							repository.totalAndUltimaPromocaoByDataCadastro(lastDate);
					
					count = (Long) map.get("count");
					//Se tiver um nova data, substitui ela para atualizar
					//Se retornar null, então nenhuma nova promoção foi cadastrada
					lastDate = map.get("lastDate") == null 
							? lastDate 
							: (LocalDateTime) map.get("lastDate");
					
					//CÓDIGO INFORMATIVO=====================================
					//getLastAccessedTime retorna o hr da ult tentativa da dwr com o lado cliente
					Calendar time = Calendar.getInstance();
					time.setTimeInMillis(context.getScriptSession().getLastAccessedTime());
					System.out.println("count: " + count 
							+ ", lastDate: " + lastDate
							+ "<" + session + "> " + " <" + time.getTime() + ">");
					//CÓDIGO INFORMATIVO=====================================
					
		//Verifica se tem alguma promoção cadastrada a partir da ultima data atualizada no intervalo de 60s
					if (count > 0) {
					/* O primeiro parâmetro contém uma str com o nome de um método no JS q está esperando
					por esses valores. O 2° param é o valor a ser enviado para o JS*/
						ScriptSessions.addFunctionCall("showButton", count);
					}					
				}
			});			
		}		
	}
}
