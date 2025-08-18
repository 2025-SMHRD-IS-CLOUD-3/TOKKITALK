package com.tokkitalk.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tokkitalk.analysis.dto.SuggestRequest;
import com.tokkitalk.analysis.dto.SuggestResult;

@WebServlet(name = "SuggestServlet", urlPatterns = {"/suggest"})
public class SuggestServlet extends HttpServlet {
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

        SuggestRequest suggestRequest = gson.fromJson(bodyBuilder.toString(), SuggestRequest.class);
        if (suggestRequest == null || suggestRequest.analysis_id == null || suggestRequest.analysis_id.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"invalid_request\"}");
            }
            return;
        }

        SuggestResult result = analysisService.regenerateSuggestion(suggestRequest);

        String json = gson.toJson(result);
        try (PrintWriter out = response.getWriter()) {
            out.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        }
    }
}





