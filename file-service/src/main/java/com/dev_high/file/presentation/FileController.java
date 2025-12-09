package com.dev_high.file.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.file.application.FileService;
import com.dev_high.file.application.dto.FileInfo;
import com.dev_high.file.presentation.dto.FileUploadRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDto<FileInfo> upload(
            @RequestPart("file") MultipartFile file, //file 데이터
            @RequestPart("request") FileUploadRequest request
    ) {
        return fileService.upload(file, request.toCommand());
    }

    @GetMapping
    public ApiResponseDto<List<FileInfo>> getAll() {
        return fileService.findAll();
    }
}
