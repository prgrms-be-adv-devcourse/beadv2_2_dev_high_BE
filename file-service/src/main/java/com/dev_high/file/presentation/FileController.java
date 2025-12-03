package com.dev_high.file.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.file.application.FileService;
import com.dev_high.file.application.dto.FileGroupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
// metadata는 프론트에서 요청 시, 꼭 Content-Type을 application/json으로 지정
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDto<FileGroupResponse> upload(
            @RequestPart("files") List<MultipartFile> files
    ) {
        return fileService.upload(files);
    }

    @GetMapping("/groups/{fileGroupId}")
    public ApiResponseDto<FileGroupResponse> findByFileGroup(@PathVariable String fileGroupId) {
        return fileService.findByFileGroupId(fileGroupId);
    }
}
