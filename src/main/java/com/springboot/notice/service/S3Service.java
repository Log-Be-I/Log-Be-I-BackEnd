package com.springboot.notice.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

//S3 파일 업로드하고, 업로드된 파일의 URL을 반환해주는 핵심 서비스
@Service
@RequiredArgsConstructor
public class S3Service {
    //AmazonS3 클라이언트를 DI로 주입받음 (Spring cloud AWS 설정 필요)
    private final AmazonS3 amazonS3;
    //application.yml에 설정된 S3 버킷 이름을 주입
    @Value("${cloud.aws.s3.bucket.images}")
    private String bucket;

    //S3에 파일을 업로드하고 해당 파일의 URL을 반환하는 메서드
    public String upload(MultipartFile file, String dirName) {
        //저장할 S3 경로를 고유하게 생성 (디렉토리명/UUID_파일명)
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        //파일 메타데이터 설정 (길이, MIME 타입 등)
        ObjectMetadata metadata =  new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        //파일을 S3에 업로드
        try (InputStream inputStream = file.getInputStream()) {
            amazonS3.putObject(
                    new PutObjectRequest(bucket, fileName, inputStream, metadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead) // 공개 읽기 권한
            );
        } catch (IOException e) {
            //업로드 중 예외가 발생하면 RuntimeException 으로 래핑해 던짐
            throw new RuntimeException("S3 업로드 실패", e);
        }
        //업로드된 파일의 S3 URL을 반환 (PublicRead 이므로 직접 접근 가능)
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    //DELETED_NOTICE 상태인 notice 의 file 이동
    public void moveDeletedFile(String sourceKey, String targetKey) {
        //images S3의 파일 복사 -> deletedImages S3로 이동
        amazonS3.copyObject(bucket, sourceKey, bucket, targetKey);
        //복사 후 images S3의 파일 삭제
        amazonS3.deleteObject(bucket, sourceKey);
    }

}
