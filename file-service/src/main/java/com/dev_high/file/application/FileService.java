package com.dev_high.file.application;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.exception.CustomException;
import com.dev_high.file.application.dto.FileInfo;
import com.dev_high.file.application.dto.FileUploadCommand;
import com.dev_high.file.config.AwsS3Properties;
import com.dev_high.file.domain.StoredFile;
import com.dev_high.file.domain.StoredFileRepository;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
public class FileService {

    private final S3Client s3Client;
    private final AwsS3Properties awsS3Properties;
    private final StoredFileRepository storedFileRepository;

    @Transactional
    public ApiResponseDto<FileInfo> upload(
            MultipartFile multipartFile,
            FileUploadCommand uploadCommand
    ) {

        //파일 유무 검사
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new CustomException("업로드할 파일이 없습니다.");
        }
        if (uploadCommand == null || !StringUtils.hasText(uploadCommand.userId())) {
            throw new CustomException("업로더 정보가 없습니다.");
        }

        String resolvedFileType = resolveFileType(uploadCommand.fileType(), multipartFile.getContentType());
        String key = buildObjectKey(multipartFile.getOriginalFilename(), uploadCommand.userId());

        // 바이너리를 S3에 저장
        try (var inputStream = multipartFile.getInputStream()) {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(awsS3Properties.getBucket())
                    .key(key)
                    .contentType(resolvedFileType)
                    .build();

            s3Client.putObject(putRequest,
                    RequestBody.fromInputStream(inputStream, multipartFile.getSize()));

        } catch (IOException e) {
            throw new CustomException("파일을 읽는 도중 오류가 발생했습니다.");
        } catch (S3Exception e) {
            throw new CustomException("S3 업로드에 실패했습니다.");
        }

        // 접근가능한 url 생성
        String fileUrl = s3Client.utilities().getUrl(GetUrlRequest.builder()
                .bucket(awsS3Properties.getBucket())
                .key(key)
                .build()).toExternalForm();

        // 업로드 결과를 DB에 기록해 조회 가능하도록 유지
        StoredFile saved = storedFileRepository.save(StoredFile.builder()
                .filePath(fileUrl)
                .fileType(resolvedFileType)
                .fileName(resolveFileName(multipartFile.getOriginalFilename()))
                .createdBy(uploadCommand.userId())
                .build());

        return ApiResponseDto.success(FileInfo.from(saved));
    }

    @Transactional(readOnly = true)
    public ApiResponseDto<List<FileInfo>> findAll() {
        List<FileInfo> files = storedFileRepository.findAll().stream()
                .map(FileInfo::from)
                .toList();
        return ApiResponseDto.success(files);
    }

    private String resolveFileType(String fileType, String contentType) {
        if (StringUtils.hasText(fileType)) {
            return fileType;
        }
        if (StringUtils.hasText(contentType)) {
            return contentType;
        }
        return "unknown";
    }

    private String resolveFileName(String originalFilename) {
        if (StringUtils.hasText(originalFilename)) {
            return Paths.get(originalFilename).getFileName().toString();
        }
        return "file-" + UUID.randomUUID();
    }

    private String buildObjectKey(String originalFilename, String uploaderId) {
        String cleanName = resolveFileName(originalFilename).replace(" ", "_");
        int extensionStart = cleanName.lastIndexOf('.');

        String baseName = extensionStart > 0 ? cleanName.substring(0, extensionStart) : cleanName;
        String extension = extensionStart > -1 ? cleanName.substring(extensionStart) : "";

        String safeUploader = uploaderId.replace("/", "_").replace("\\", "_");
        return safeUploader + "/" + LocalDate.now() + "/" + baseName + "-" + UUID.randomUUID() + extension;
    }
}
