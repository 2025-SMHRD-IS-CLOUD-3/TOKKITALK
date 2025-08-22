package com.tokkitalk.analysis.external;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VisionOcrClient {

    public static String detectTextFromImage(File imageFile) throws IOException {
        // API 키 파일 경로를 환경 변수에 설정합니다.
        // ⚠️ 실제 서버에서는 이 경로를 직접 코드에 넣지 않고, 서버 환경변수로 관리합니다.
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "C:/Users/사용자이름/Downloads/서비스-계정-키.json");

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            // 이미지를 바이트 데이터로 변환합니다.
        	byte[] data;
        	try (FileInputStream fis = new FileInputStream(imageFile)) {
        	    data = new byte[(int) imageFile.length()];
        	    fis.read(data);
        	}
            ByteString imgBytes = ByteString.copyFrom(data);
            Image img = Image.newBuilder().setContent(imgBytes).build();

            // 이미지 분석 요청을 만듭니다. (텍스트 감지 기능 요청)
            Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();

            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);

            // API를 호출하고 응답을 받습니다.
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            
            // 응답에서 텍스트를 추출합니다.
            if (response.getResponsesCount() > 0) {
                AnnotateImageResponse firstResponse = response.getResponses(0);
                if (firstResponse.getTextAnnotationsCount() > 0) {
                    // 전체 텍스트를 반환합니다.
                    return firstResponse.getTextAnnotations(0).getDescription();
                }
            }
        }
        return ""; // 텍스트가 감지되지 않으면 빈 문자열 반환
    }
}