package com.tokkitalk.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tokkitalk.analysis.dto.AnalyzeRequest;
import com.tokkitalk.analysis.dto.AnalysisResult;

@WebServlet(name = "AnalysisServlet", urlPatterns = {"/analyze"})
public class AnalysisServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final transient Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private final transient AnalysisService analysisService = new AnalysisService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        StringBuilder bodyBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                bodyBuilder.append(line);
            }
        }

        AnalyzeRequest analyzeRequest = gson.fromJson(bodyBuilder.toString(), AnalyzeRequest.class);
        if (analyzeRequest == null || analyzeRequest.input_type == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"invalid_request\"}");
            }
            return;
        }

        // Mask PII first
        AnalyzeRequest masked = PiiMasker.mask(analyzeRequest);

        // Generate ID early
        String analysisId = UUID.randomUUID().toString();

        AnalysisResult result = analysisService.analyze(analysisId, masked);

        String json = gson.toJson(result);
        try (PrintWriter out = response.getWriter()) {
            out.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        }
    }
}





