package br.edu.ifpr.mcp_server.mcp.tools;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.Annotations;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class JsonDocTool implements McpToolDefinition {
    public JsonDocTool() {
    }

    @Override
    public void register(SyncSpecification<SingleSessionSyncSpecification> mcp) throws Exception {
        // define o esquema para troca de dados
        JsonSchema inputSchema = new JsonSchema("object",
                Map.of("id", Map.of(
                        "type", "integer",
                        "description", "ID do documento")),
                List.of("id"),
                false,
                null,
                null);
        // define a ferramenta
        Tool tool = Tool.builder()
                .name("obter_documento")
                .title("Obter Documento JSON")
                .description("Retorna os dados de um documento JSON.")
                .inputSchema(inputSchema)
                .build();
        // vincula o objeto ferramenta (acima) à implementação da ferramenta
        SyncToolSpecification toolSpec = SyncToolSpecification.builder()
                .tool(tool)
                .callHandler(this::run)
                .build();
        // acrescenta a ferramenta ao servidor
        mcp.tools(toolSpec);
    }

    @Override
    public CallToolResult run(McpSyncServerExchange exchange,
            CallToolRequest request) {
        try {
            List<Role> roles = List.of(McpSchema.Role.USER);
            Annotations annotations = new Annotations(roles, 1.0);
            // recupera o(s) parâmetro(s) passados pelo cliente, se houver algum
            int id = (int) request.arguments().get("id");
            // realiza alguma operação, com ou sem, eventuais, parâmetros do
            // cliente
            String msg = String.format("id[%d], titulo['Relatório de " +
                    "Documentos'], status['finalizado'], " +
                    "data[%s]",
                    id, LocalDateTime.now().toString());
            // prepara o texto de retorno ao cliente
            TextContent content = new TextContent(annotations, msg);
            // prepara uma notificação ao cliente (opcional)
            LoggingMessageNotification lmn = new LoggingMessageNotification(LoggingLevel.INFO,
                    this.getClass().getSimpleName(),
                    String.valueOf(id));
            // envia uma notificação ao cliente
            exchange.loggingNotification(lmn);
            // retorna o resultado da ferramenta ('tool') ao cliente
            return CallToolResult.builder()
                    .content(List.of(content))
                    // NÃO houve erro na execução da ferramenta
                    .isError(false)
                    .build();
        } catch (Exception ex) {
            throw new RuntimeException("ERRO EXECUTANDO FERRAMENTA:\n" +
                    ex.getMessage());
        }
    }
}

