package br.edu.ifpr.mcp_server.main;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import tools.jackson.databind.json.JsonMapper;

public class McpClient01 {
   private static final Logger LOGGER =
                                  LoggerFactory.getLogger(McpClient01.class);

   private static final String SERVER_PATH =
                                         "../../server/mcp_server01_server.jar";

   private void loggingMessageNotification(
                                      LoggingMessageNotification notification) {
      LOGGER.info("[NOTIFICAÇÃO MCP] Origem: {} | Nível: {} | Mensagem: {}", 
                  notification.logger(), 
                  notification.level(), 
                  notification.data());
   }

   public McpClient01() {
      // identifica o servidor a ser chamado/executado
      String           jarPath = (new File(SERVER_PATH)).getAbsolutePath();
      LOGGER.info("\njarPath[" + jarPath + "]\n");
      ServerParameters params  = ServerParameters.builder("java")
                                                 .args("-jar",jarPath)
                                                 .build();
      // cria o objeto para comunicação entre cliente e servidor
      JacksonMcpJsonMapper jsonMapper = new JacksonMcpJsonMapper(
                                                            new JsonMapper());
      // estabelece que a comunicação usará o transporte STDIO
      McpClientTransport   transport  = new StdioClientTransport(params,
                                                                 jsonMapper);
      // constrói o objeto cliente
      McpSyncClient client = McpClient.sync(transport)
                                       // CONFIGURAÇÃO DO LISTENER DE LOGGING:
                                       // Registra o handler para capturar as
                                       // notificações enviadas pelo servidor
                                      .loggingConsumer(notification ->
                                       loggingMessageNotification(notification))
                                      .build();

      // inicializa o cliente com o transporte STDIO para o servidor
      client.initialize();
      while (!client.isInitialized())
         LOGGER.info("Aguardando servidor MCP...");

      // solicita as ferramentas disponíveis ao servidor
      ListToolsResult tools = client.listTools();
      LOGGER.info("'Tools' disponibilizadas pelo servidor:");
      // existe as ferramenas existentes
      tools.tools().forEach(tool -> System.out.println(" - " + tool.name()));

      LOGGER.info("\nExecutando ferramenta 'obter_documento'...");
      CallToolRequest request = new CallToolRequest("obter_documento",
                                      Map.of("id",123));
      // executa a ferramenta 'logPrompt' passando a ela o parâmetro de entrada
      CallToolResult  result  = client.callTool(request);
      LOGGER.info("Result: " + result.content());
      // encerra o cliente
      client.closeGracefully();
   }

   public static void main(String... args) {
      new McpClient01();
   }
}
