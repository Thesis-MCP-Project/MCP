package br.edu.ifpr.mcp_server.main;

import java.util.List;

import br.edu.ifpr.mcp_server.mcp.tools.ExcelReaderTool;
import br.edu.ifpr.mcp_server.mcp.tools.JsonDocTool;
import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.Implementation;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import tools.jackson.databind.json.JsonMapper;

public class McpServer01 {
   private McpSyncServer server;

   private void registerTools(
         SyncSpecification<SingleSessionSyncSpecification> mcp)
         throws Exception {
      // lista de ferramentas disponíveis no servidor
      List<McpToolDefinition> tools = List.of(
    		  new JsonDocTool(),
    		  new ExcelReaderTool()
    		  );
      // para cada ferramenta, executa seu registro para posterior execução
      for (McpToolDefinition tool : tools)
         if (tool != null)
            tool.register(mcp);
   }

   private SyncSpecification<SingleSessionSyncSpecification> configMcpServer() {
      // cria o objeto para comunicação entre servidor e cliente
      JacksonMcpJsonMapper jsonMapper = new JacksonMcpJsonMapper(new JsonMapper());
      // estabelece que a comunicação usará o transporte STDIO
      StdioServerTransportProvider transportProvider = new StdioServerTransportProvider(jsonMapper);
      // identifica quais "capacidades" o servidor disponibiliza
      ServerCapabilities capabilities = ServerCapabilities.builder()
            .tools(true)
            .logging()
            .build();
      // identificação
      Implementation implementation = new Implementation(
            "MCP Server - Exemplo 01",
            "MCP Server", "1.0.0");
      // Configura do transporte via STDIO
      return McpServer.sync(transportProvider)
            .serverInfo(implementation)
            .capabilities(capabilities);
   }

   public McpServer01() throws Exception {
      // configura/inicializa o servidor
      SyncSpecification<SingleSessionSyncSpecification> srv = configMcpServer();

      // registra as ferramentas disponíveis neste servidor
      registerTools(srv);
      // cria o servidor
      this.server = srv.build();

      System.err.println("MCP Server 01 iniciado via STDIO...");
   }

   public static void main(String[] args) throws Exception {
      new McpServer01();
      Thread.currentThread().join();
      // McpServer01 p = new McpServer01();
      //
      // Runtime.getRuntime().addShutdownHook(new Thread() {
      // public void run() {
      // synchronized (p) {
      // p.notify();
      // }
      // }
      // });
      // synchronized (p) {
      // p.wait();
      // server.closeGracefully();
      // }
   }
}
