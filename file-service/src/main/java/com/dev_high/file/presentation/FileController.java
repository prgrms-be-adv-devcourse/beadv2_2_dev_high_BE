package com.dev_high.file.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.file.application.FileService;
import com.dev_high.file.application.dto.FileInfo;
import com.dev_high.file.application.dto.FilePathListResponse;
import com.dev_high.file.presentation.dto.FileSearchRequest;
import com.dev_high.file.presentation.dto.FileUploadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
// metadata는 프론트에서 요청 시, 꼭 Content-Type을 application/json으로 지정
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDto<FileInfo> upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart("metadata") FileUploadRequest request,
            @RequestPart("productId") String productId,
            @RequestPart("userId") String userId
    ) {
        return fileService.upload(file, request, productId, userId);
    }

    @PostMapping("/search")
    public ApiResponseDto<FilePathListResponse> findByProduct(@RequestBody FileSearchRequest request) {
        return fileService.findByProductId(request.productId());
    }

    @DeleteMapping("/{productId}")
    public ApiResponseDto<Void> deleteByProduct(@PathVariable String productId) {
        return fileService.deleteByProductId(productId);
    }
}
