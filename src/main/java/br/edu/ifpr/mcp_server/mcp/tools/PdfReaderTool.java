package br.edu.ifpr.mcp_server.mcp.tools;

import java.util.List;
import java.util.Map;

import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.Annotations;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class PdfReaderTool implements McpToolDefinition {

    @Override
    public CallToolResult run(McpSyncServerExchange exchange, CallToolRequest request) {
        return null;
    }

    @Override
    public void register(SyncSpecification<SingleSessionSyncSpecification> mcp) throws Exception {
        JsonSchema inputSchema = new JsonSchema(
                "object",
                Map.of("caminho_arquivo", Map.of("type", "string", "description", "Caminho absoluto do arquivo PDF no sistema"),
                        "pagina_inicio", Map.of("type", "integer","description", "Página inicial (1-indexed). Se omitido, inicia da primeira página."),
                        "pagina_fim", Map.of("type", "integer","description","Página final (1-indexed, inclusiva). Se omitido, vai até a última página."),
                        "incluir_imagens", Map.of("type", "boolean", "description", "Se true, extrai informações sobre imagens embutidas nas páginas selecionadas.")),
                List.of("caminho_arquivo"), // apenas o caminho é obrigatório
                false,
                null,
                null);
        /*
         * Tool tool = Tool.builder()
         * .name("obter_documento")
         * .title("Obter Documento JSON")
         * .description("Retorna os dados de um documento JSON.")
         * .inputSchema(inputSchema)
         * .build();
         * // vincula o objeto ferramenta (acima) à implementação da ferramenta
         * SyncToolSpecification toolSpec = SyncToolSpecification.builder()
         * .tool(tool)
         * .callHandler(this::run)
         * .build();
         * // acrescenta a ferramenta ao servidor
         * mcp.tools(toolSpec);
         */
    }

}