package br.edu.ifpr.mcp_server.mcp.tools;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
	
	// Limite de segurança para evitar (OutOfMemmory)
	private static final int MAX_ROWS_LIMIT = 5000;

    @Override
    public void register(SyncSpecification<SingleSessionSyncSpecification> mcp) throws Exception {
        // Schema dos parâmetros: recebe o caminho do arquivo (obrigatorio)
        JsonSchema inputSchema = new JsonSchema("object",
            Map.of("filePath", Map.of(
                "type", "string",
                "description", "Caminho absoluto ou relativo para o arquivo Excel (.xlsx ou .xls)"
            ),
            	"sheetName", Map.of(
            			"type", "string",
            			"description", "Opcional. Nomes das abas separadas por vírgula. Por padrão lê todas as abas"
            			),
            	"maxRows", Map.of(
            			"type", "integer",
            			"description", "Limite opcional de linhas a serem lidas. (Padrão e máximo = 5000)"
            			)
            	),
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
            String sheetsParam = (String) request.arguments().get("sheets");
            
            int maxRows = MAX_ROWS_LIMIT;
            if (request.arguments().containsKey("maxRows") && request.arguments().get("maxRows") != null) {
            	maxRows = Math.min((int) request.arguments().get("maxRows"), MAX_ROWS_LIMIT);
            }
            
            File file = new File(filePath);

            if (!file.exists()) {
                return CallToolResult.builder()
                    .content(List.of(new TextContent(null, "Erro: Arquivo não encontrado em " + filePath)))
                    .isError(true)
                    .build();
            }

            // Lê o arquivo Excel via Apache POI
            // O método devolve um Map onde a CHAVE é o nome da aba e o VALOR é a matriz de dados daquela aba
            Map<String, List<List<String>>> excelData = readExcel(file, sheetsParam, maxRows);
            
            // Converte as linhas em formato JSON para enviar de volta ao MCP Client
            JsonMapper jsonMapper = new JsonMapper();
            String jsonOutput = jsonMapper.writeValueAsString(excelData);

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

    private Map<String, List<List<String>>> readExcel(File file, String sheetsParam, int maxRows) throws Exception {
        Map<String, List<List<String>>> resultMap = new LinkedHashMap<>();
        DataFormatter formatter = new DataFormatter();
        
        List<String> requestedSheets = new ArrayList<>();
        if (sheetsParam != null && !sheetsParam.trim().isEmpty()) {
        	for (String name : sheetsParam.split(",")) {
        		requestedSheets.add(name.trim().toLowerCase());
        	}
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            int numberOfSheets = workbook.getNumberOfSheets();
            
            for (int i = 0; i < numberOfSheets; i++) {
            	Sheet sheet = workbook.getSheetAt(i);
            	String sheetName = sheet.getSheetName();
            
            	//Verifica se a aba atual está na lista solicitada (Somente se o cliente específicou)
            	if (!requestedSheets.isEmpty() && !requestedSheets.contains(sheetName.toLowerCase())) {
            		continue;
            	}

            	List<List<String>> sheetData = new ArrayList<>();
            	int rowCount = 0;
            	for (Row row : sheet) {
            		// Trava de segurança pra não estourar a memória
            		if (rowCount >= maxRows) {
            			break;
            		}
            	
            		List<String> rowData = new ArrayList<>();
            		for (Cell cell : row) {
            			// Obtém o valor formatado como String de cada célula
            			rowData.add(formatter.formatCellValue(cell));
            		}
            		sheetData.add(rowData);
            		rowCount++;
            	}
                resultMap.put(sheetName, sheetData);
            }
        }
        return resultMap;
    }
}
