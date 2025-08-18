package com.tokkitalk.analysis;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "DeleteAnalysisServlet", urlPatterns = {"/analysis/*"})
public class DeleteAnalysisServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final transient AnalysisService analysisService = new AnalysisService();

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        String pathInfo = request.getPathInfo();
        String id = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            id = pathInfo.substring(1);
        }

        if (id == null || id.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"invalid_id\"}");
            }
            return;
        }

        boolean deleted = analysisService.deleteAnalysis(id.trim());
        if (!deleted) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"not_found\"}");
            }
            return;
        }

        try (PrintWriter out = response.getWriter()) {
            out.write("{\"ok\":true}");
        }
    }
}





