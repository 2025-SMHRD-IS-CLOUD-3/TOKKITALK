package com.tokkitalk.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tokkitalk.analysis.dto.FeedbackRequest;

@WebServlet(name = "FeedbackServlet", urlPatterns = {"/feedback"})
public class FeedbackServlet extends HttpServlet {
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

        FeedbackRequest feedbackRequest = gson.fromJson(bodyBuilder.toString(), FeedbackRequest.class);
        if (feedbackRequest == null || feedbackRequest.analysis_id == null || feedbackRequest.analysis_id.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"invalid_request\"}");
            }
            return;
        }

        analysisService.saveFeedback(feedbackRequest);
        try (PrintWriter out = response.getWriter()) {
            out.write("{\"ok\":true}");
        }
    }
}


