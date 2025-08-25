package com.tokkitalk.analysis;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tokkitalk.analysis.dto.AnalyzeRequest;
import com.tokkitalk.analysis.dto.AnalysisResult;
import com.tokkitalk.analysis.external.OpenAiClient;

@WebServlet("/AnalysisServlet")
//이 어노테이션을 추가하여 이미지 파일 업로드 크기를 설정합니다.
@MultipartConfig(
 fileSizeThreshold = 1024 * 1024, // 1MB
 maxFileSize = 1024 * 1024 * 10, // 10MB
 maxRequestSize = 1024 * 1024 * 50 // 50MB
)
public class AnalysisServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Gson GSON = new Gson();
    private final OpenAiClient openAiClient = new OpenAiClient();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        
        PrintWriter out = response.getWriter();
        AnalyzeRequest analyzeRequest = null;
        
        try {
            // JSON 요청 본문을 문자열로 읽어들임
            String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            
            if (requestBody == null || requestBody.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject error = new JsonObject();
                error.addProperty("error", "요청 데이터가 비어 있습니다.");
                out.print(error.toString());
                return;
            }
            
            // JSON 문자열을 객체로 파싱
            analyzeRequest = GSON.fromJson(requestBody, AnalyzeRequest.class);

            if (analyzeRequest == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject error = new JsonObject();
                error.addProperty("error", "요청 데이터 형식이 올바르지 않습니다.");
                out.print(error.toString());
                return;
            }

            // analyzeWithLLM 메서드에 맞게 두 개의 인자를 명확하게 전달
            AnalysisResult result = openAiClient.analyzeWithLLM(analyzeRequest.getText(), analyzeRequest.getImageBase64());            
            out.print(GSON.toJson(result));

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("error", "분석 중 오류가 발생했습니다: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(error.toString());
        }
    }
}