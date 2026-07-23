package br.edu.ifpr.mcp_server.mcp.tools;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import tools.jackson.databind.json.JsonMapper;

public class ExcelReaderTool implements McpToolDefinition {

    @Override
    public void register(SyncSpecification<SingleSessionSyncSpecification> mcp) throws Exception {
        // Schema dos parâmetros: recebe o caminho do arquivo (obrigatorio)
        JsonSchema inputSchema = new JsonSchema("object",
            Map.of("filePath", Map.of(
                "type", "string",
                "description", "Caminho absoluto ou relativo para o arquivo Excel (.xlsx ou .xls)"
            )),
            List.of("filePath"),
            false, null, null
        );

        Tool tool = Tool.builder()
            .name("ler_excel")
            .title("Ler Arquivo Excel")
            .description("Lê o conteúdo de uma planilha Excel e devolve em formato estruturado (JSON).")
            .inputSchema(inputSchema)
            .build();

        SyncToolSpecification toolSpec = SyncToolSpecification.builder()
            .tool(tool)
            .callHandler(this::run)
            .build();

        mcp.tools(toolSpec);
    }

    @Override
    public CallToolResult run(McpSyncServerExchange exchange, CallToolRequest request) {
        try {
            String filePath = (String) request.arguments().get("filePath");
            File file = new File(filePath);

            if (!file.exists()) {
                return CallToolResult.builder()
                    .content(List.of(new TextContent(null, "Erro: Arquivo não encontrado em " + filePath)))
                    .isError(true)
                    .build();
            }

            // Lê o arquivo Excel via Apache POI
            List<List<String>> rowsData = readExcel(file);

            // Converte as linhas em formato JSON para enviar de volta ao MCP Client
            JsonMapper jsonMapper = new JsonMapper();
            String jsonOutput = jsonMapper.writeValueAsString(rowsData);

            TextContent content = new TextContent(null, jsonOutput);

            return CallToolResult.builder()
                .content(List.of(content))
                .isError(false)
                .build();

        } catch (Exception ex) {
            return CallToolResult.builder()
                .content(List.of(new TextContent(null, "Erro ao processar arquivo Excel: " + ex.getMessage())))
                .isError(true)
                .build();
        }
    }

    private List<List<String>> readExcel(File file) throws Exception {
        List<List<String>> sheetData = new ArrayList<>();
        DataFormatter formatter = new DataFormatter(); // Evita formatações incorretas de datas/números

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            // Lê a primeira aba (sheet)
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    // Obtém o valor formatado como String de cada célula
                    rowData.add(formatter.formatCellValue(cell));
                }
                sheetData.add(rowData);
            }
        }
        return sheetData;
    }
}
