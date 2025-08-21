package com.tokkitalk.analysis;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/getHistory")
public class GetHistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // 세션에서 사용자 ID 가져오기
            HttpSession session = request.getSession();
            Long sessionUserId = (Long) session.getAttribute("userId");
            
            // 테스트용: 로그인 안 되어 있으면 임시 userId 사용
            final Long userId;
            if (sessionUserId == null) {
                userId = 1L;
                System.out.println("테스트용 userId 사용: " + userId);
            } else {
                userId = sessionUserId;
            }
            
            System.out.println("히스토리 조회 시작 - User ID: " + userId);
            
            // 히스토리 로그 디렉토리 경로 (절대 경로)
            String projectRoot = System.getProperty("user.dir");
            Path historyDir = Paths.get(projectRoot + "/history_logs");
            System.out.println("히스토리 디렉토리 경로: " + historyDir.toAbsolutePath());
            System.out.println("프로젝트 루트: " + projectRoot);
            
            if (!Files.exists(historyDir)) {
                System.out.println("히스토리 디렉토리가 존재하지 않습니다.");
                System.out.println("생성 시도 중...");
                try {
                    Files.createDirectories(historyDir);
                    System.out.println("히스토리 디렉토리 생성 완료");
                } catch (Exception e) {
                    System.out.println("디렉토리 생성 실패: " + e.getMessage());
                }
                // 디렉토리가 없으면 빈 배열 반환
                out.println("[]");
                return;
            }
            
            // 해당 사용자의 히스토리 파일들 찾기
            List<HistoryItem> historyItems = new ArrayList<>();
            
            try {
                List<Path> userFiles = new ArrayList<>();
                
                // 사용자 파일들 찾기
                Files.list(historyDir)
                    .filter(path -> path.toString().contains("_" + userId + "_"))
                    .forEach(userFiles::add);
                
                System.out.println("찾은 파일 개수: " + userFiles.size());
                
                // 디버깅: 모든 파일 목록 출력
                System.out.println("=== 디렉토리 내 모든 파일 ===");
                try {
                    Files.list(historyDir).forEach(path -> {
                        System.out.println("파일: " + path.getFileName());
                    });
                } catch (Exception e) {
                    System.out.println("파일 목록 조회 실패: " + e.getMessage());
                }
                System.out.println("==========================");
                
                // 최신순 정렬
                userFiles.sort(Comparator.reverseOrder());
                
                for (Path filePath : userFiles) {
                    try {
                        String fileName = filePath.getFileName().toString();
                        System.out.println("처리 중인 파일: " + fileName);
                        
                        String content = new String(Files.readAllBytes(filePath), "UTF-8");
                        
                        // 파일명에서 정보 추출 (예: 20241201_143022_1_user.txt)
                        String[] parts = fileName.replace(".txt", "").split("_");
                        if (parts.length >= 4) {
                            String date = parts[0] + "-" + parts[1].substring(0, 2) + "-" + parts[1].substring(2, 4);
                            String time = parts[1].substring(4, 6) + ":" + parts[1].substring(6, 8) + ":" + parts[1].substring(8, 10);
                            String role = parts[3];
                            
                            HistoryItem item = new HistoryItem();
                            item.id = fileName;
                            item.date = date;
                            item.time = time;
                            item.role = role;
                            item.content = content;
                            
                            historyItems.add(item);
                            System.out.println("히스토리 아이템 추가: " + fileName);
                        }
                    } catch (Exception e) {
                        System.err.println("파일 읽기 오류: " + filePath + " - " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                System.err.println("파일 목록 조회 오류: " + e.getMessage());
                e.printStackTrace();
            }
            
            // JSON 응답 생성 (Gson 없이 수동으로)
            String jsonResponse = convertToJson(historyItems);
            System.out.println("응답 전송: " + historyItems.size() + "개 아이템");
            out.println(jsonResponse);
            
        } catch (Exception e) {
            System.err.println("GetHistoryServlet 오류: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"히스토리 조회 중 오류가 발생했습니다: " + e.getMessage() + "\"}");
        }
    }
    
    // JSON 변환 메서드 (Gson 없이)
    private String convertToJson(List<HistoryItem> items) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        
        for (int i = 0; i < items.size(); i++) {
            HistoryItem item = items.get(i);
            json.append("{");
            json.append("\"id\":\"").append(escapeJson(item.id)).append("\",");
            json.append("\"date\":\"").append(escapeJson(item.date)).append("\",");
            json.append("\"time\":\"").append(escapeJson(item.time)).append("\",");
            json.append("\"role\":\"").append(escapeJson(item.role)).append("\",");
            json.append("\"content\":\"").append(escapeJson(item.content)).append("\"");
            json.append("}");
            
            if (i < items.size() - 1) {
                json.append(",");
            }
        }
        
        json.append("]");
        return json.toString();
    }
    
    // JSON 이스케이프
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    // 히스토리 아이템 클래스
    public static class HistoryItem {
        public String id;
        public String date;
        public String time;
        public String role;
        public String content;
    }
}
