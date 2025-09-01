package com.tokkitalk.analysis;

import com.google.gson.Gson;
import com.tokkitalk.analysis.store.AnalysisDAO;
import com.tokkitalk.model.MevenMember;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/deleteHistory")
public class DeleteHistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();

    // 내부 요청 클래스
    private static class DeleteRequest {
        List<Integer> chatIds;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);

        // 1. 로그인 상태 확인
        if (session == null || session.getAttribute("member") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"로그인이 필요합니다.\"}");
            return;
        }

        try {
            MevenMember member = (MevenMember) session.getAttribute("member");
            String userId = member.getUser_id();

            // 2. 요청 본문에서 chatIds 읽기
            DeleteRequest deleteRequest = gson.fromJson(request.getReader(), DeleteRequest.class);
            List<Integer> chatIds = deleteRequest.chatIds;

            if (chatIds == null || chatIds.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"삭제할 ID가 없습니다.\"}");
                return;
            }

            // 3. DAO를 통해 DB에서 삭제
            AnalysisDAO analysisDAO = new AnalysisDAO();
            analysisDAO.deleteChatHistoryByIds(userId, chatIds);
         // ✅ 삭제 요청 로그 출력
            System.out.println("[INFO] 사용자 " + userId + " 의 chat_history "
                               + chatIds.size() + "건 삭제 요청 처리됨.");
            // 4. 성공 응답 전송
            out.print("{\"success\": true}");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"삭제 중 서버 오류가 발생했습니다.\"}");
        }
    }
}