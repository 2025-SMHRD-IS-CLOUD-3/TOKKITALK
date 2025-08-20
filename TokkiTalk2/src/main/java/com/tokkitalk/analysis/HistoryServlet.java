package com.tokkitalk.analysis;

import com.google.gson.Gson;
import com.tokkitalk.analysis.store.AnalysisDAO;
import com.tokkitalk.analysis.store.AnalysisRecord;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet(name = "HistoryServlet", urlPatterns = {"/history"})
public class HistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final transient AnalysisDAO dao = new AnalysisDAO();
    private final transient Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        // 우선 세션에서 사용자 ID 사용, 없으면 쿼리 파라미터 허용
        String userIdStr = null;
        Object v = req.getSession(false) != null ? req.getSession(false).getAttribute("userNumericId") : null;
        if (v instanceof Number) userIdStr = String.valueOf(((Number) v).longValue());
        else if (v instanceof String) userIdStr = (String) v;
        if (userIdStr == null) userIdStr = req.getParameter("userId");
        int offset = parseInt(req.getParameter("offset"), 0);
        int limit = parseInt(req.getParameter("limit"), 20);

        if (userIdStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = resp.getWriter()) {
                out.write("{\"error\":\"missing_userId\"}");
            }
            return;
        }

        long userId = Long.parseLong(userIdStr);
        List<AnalysisRecord> list = dao.selectByUser(userId, offset, limit);
        String json = gson.toJson(list);
        try (PrintWriter out = resp.getWriter()) {
            out.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        }
    }

    private int parseInt(String s, int def) {
        try { return s == null ? def : Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}


