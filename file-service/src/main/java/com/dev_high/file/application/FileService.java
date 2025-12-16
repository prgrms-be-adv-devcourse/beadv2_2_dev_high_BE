package com.dev_high.file.application;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.exception.CustomException;
import com.dev_high.file.application.dto.FileInfo;
import com.dev_high.file.application.dto.FileUploadCommand;
import com.dev_high.file.application.dto.FilePathListResponse;
import com.dev_high.file.presentation.dto.FileUploadRequest;
import com.dev_high.file.config.AwsS3Properties;
import com.dev_high.file.domain.StoredFile;
import com.dev_high.file.domain.StoredFileRepository;
import java.io.IOException;
import java.net.URI;
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
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
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
            FileUploadRequest uploadRequest,
            String productId,
            String userId
    ) {

        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new CustomException("업로드할 파일이 없습니다.");
        }
        if (uploadRequest == null || !StringUtils.hasText(productId)) {
            throw new CustomException("상품 ID가 필요합니다.");
        }
        if (!StringUtils.hasText(userId)) {
            throw new CustomException("업로더 정보가 없습니다.");
        }

        FileUploadCommand uploadCommand = uploadRequest.toCommand(productId, userId);

        String resolvedFileType = resolveFileType(uploadCommand.fileType(), multipartFile.getContentType());
        String key = buildObjectKey(multipartFile.getOriginalFilename(), uploadCommand.userId());

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

        String fileUrl = s3Client.utilities().getUrl(GetUrlRequest.builder()
                .bucket(awsS3Properties.getBucket())
                .key(key)
                .build()).toExternalForm();

        StoredFile saved = storedFileRepository.save(StoredFile.builder()
                .filePath(fileUrl)
                .fileType(resolvedFileType)
                .fileName(resolveFileName(multipartFile.getOriginalFilename()))
                .productId(uploadCommand.productId())
                .createdBy(uploadCommand.userId())
                .build());

        return ApiResponseDto.success(FileInfo.from(saved));
    }

    @Transactional(readOnly = true)
    public ApiResponseDto<FilePathListResponse> findByProductId(String productId) {
        if (!StringUtils.hasText(productId)) {
            throw new CustomException("상품 ID가 필요합니다.");
        }

        List<String> filePathList = storedFileRepository.findByProductId(productId).stream()
                .map(StoredFile::getFilePath)
                .toList();

        return ApiResponseDto.success(new FilePathListResponse(filePathList));
    }

    @Transactional
    public ApiResponseDto<Void> deleteByProductId(String productId) {
        if (!StringUtils.hasText(productId)) {
            throw new CustomException("상품 ID가 필요합니다.");
        }

        List<StoredFile> files = storedFileRepository.findByProductId(productId);

        files.forEach(file -> {
            String objectKey = extractObjectKey(file.getFilePath());
            try {
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(awsS3Properties.getBucket())
                        .key(objectKey)
                        .build());
            } catch (S3Exception e) {
                throw new CustomException("S3 객체 삭제에 실패했습니다.");
            }
        });

        storedFileRepository.deleteByProductId(productId);
        return ApiResponseDto.success(null);
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

    private String extractObjectKey(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            throw new CustomException("파일 경로가 잘못되었습니다.");
        }
        String path = URI.create(fileUrl).getPath(); // leading slash 포함
        if (!StringUtils.hasText(path)) {
            throw new CustomException("파일 경로가 잘못되었습니다.");
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
