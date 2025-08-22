package com.tokkitalk.analysis;

// 예시 서블릿 코드
import com.tokkitalk.analysis.external.VisionOcrClient;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

@WebServlet("/ocr-process") // 이 주소로 웹페이지에서 이미지를 보낼 것입니다.
@MultipartConfig
public class OcrServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // 웹페이지에서 전송된 이미지 파일을 받습니다.
            Part filePart = request.getPart("image");
            String fileName = filePart.getSubmittedFileName();
            File tempFile = Files.createTempFile(null, fileName).toFile();
            filePart.write(tempFile.getAbsolutePath());

            // 3단계에서 만든 OCR 클래스를 사용하여 텍스트를 분석합니다.
            String detectedText = VisionOcrClient.detectTextFromImage(tempFile);

            // 분석 결과를 JSON 형태로 웹페이지에 보냅니다.
            out.print("{\"success\":true, \"detected_text\":\"" + detectedText.replace("\n", "\\n") + "\"}");
            out.flush();

            // 임시 파일 삭제
            tempFile.delete();

        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"success\":false, \"error\":\"" + e.getMessage() + "\"}");
            out.flush();
        }
    }
}