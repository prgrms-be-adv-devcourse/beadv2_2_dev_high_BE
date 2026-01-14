package com.dev_high.product.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.context.UserContext.UserInfo;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.exception.CustomException;
import java.util.Collections;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import com.dev_high.product.application.dto.FileGroupResponse;
import com.dev_high.product.application.dto.FileInfo;
import com.dev_high.product.config.AwsS3Properties;
import com.dev_high.product.domain.FileGroup;
import com.dev_high.product.domain.FileGroupRepository;
import com.dev_high.product.domain.StoredFile;
import com.dev_high.product.domain.StoredFileRepository;
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
    private final FileGroupRepository fileGroupRepository;
    private final StoredFileRepository storedFileRepository;

    @Transactional
    public ApiResponseDto<FileGroupResponse> upload(
            List<MultipartFile> multipartFiles
    ) {

        if (multipartFiles == null || multipartFiles.isEmpty()) {
            throw new CustomException("업로드할 파일이 없습니다.");
        }
        UserInfo userInfo = UserContext.get();
        if (userInfo == null || !StringUtils.hasText(userInfo.userId())) {
            throw new CustomException("업로더 정보가 없습니다.");
        }

        FileGroup fileGroup = fileGroupRepository.save(FileGroup.create());
        String fileGroupId = fileGroup.getId();
        String uploaderId = userInfo.userId();

        List<FileInfo> savedFiles = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if (multipartFile == null || multipartFile.isEmpty()) {
                continue;
            }

            String resolvedFileType = resolveFileType(null, multipartFile.getContentType());
            String key = buildObjectKey(multipartFile.getOriginalFilename(), fileGroupId);

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
                    .createdBy(uploaderId)
                    .fileGroupId(fileGroupId)
                    .build());

            savedFiles.add(FileInfo.from(saved));
        }

        return ApiResponseDto.success(new FileGroupResponse(fileGroupId, savedFiles));
    }

    @Transactional(readOnly = true)
    public ApiResponseDto<FileGroupResponse> findByFileGroupId(String fileGroupId) {
        if (!StringUtils.hasText(fileGroupId)) {
            throw new CustomException("파일 그룹 ID가 필요합니다.");
        }

        FileGroup fileGroup = fileGroupRepository.findById(fileGroupId)
                .orElseThrow(() -> new CustomException("파일 그룹을 찾을 수 없습니다."));

        List<FileInfo> files = fileGroup.getFiles() == null ? Collections.emptyList()
                : fileGroup.getFiles().stream()
                .map(FileInfo::from)
                .toList();

        return ApiResponseDto.success(new FileGroupResponse(fileGroupId, files));
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

    private String buildObjectKey(String originalFilename, String groupId) {
        String cleanName = resolveFileName(originalFilename).replace(" ", "_");
        int extensionStart = cleanName.lastIndexOf('.');

        String baseName = extensionStart > 0 ? cleanName.substring(0, extensionStart) : cleanName;
        String extension = extensionStart > -1 ? cleanName.substring(extensionStart) : "";

        String safeGroupId = groupId.replace("/", "_").replace("\\", "_");
        return safeGroupId + "/" + baseName + "-" + UUID.randomUUID() + extension;
    }

    public ApiResponseDto<List<FileGroupResponse>> findByFileGroupIds(List<String> fileGroupIds) {
        if (fileGroupIds == null || fileGroupIds.isEmpty()) {
            throw new CustomException("파일 그룹 ID가 필요합니다.");
        }

        

        List<FileGroup> fileGroups = fileGroupRepository.findByFileGroupIds(fileGroupIds);
        if (fileGroups.size() != fileGroupIds.size()) {
            throw new CustomException("파일 그룹을 찾을 수 없습니다.");
        }

        List<FileGroupResponse> responses = fileGroups.stream()
                .map(group -> {
                    List<FileInfo> files = group.getFiles() == null ? Collections.emptyList()
                            : group.getFiles().stream()
                            .map(FileInfo::from)
                            .toList();
                    return new FileGroupResponse(group.getId(), files);
                })
                .toList();

        return ApiResponseDto.success(responses);
    }
}
