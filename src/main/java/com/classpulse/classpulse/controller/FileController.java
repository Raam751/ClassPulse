package com.classpulse.classpulse.controller;

import com.classpulse.classpulse.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "File upload and download APIs")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Operation(summary = "Upload a file", description = "Upload an image or PDF file (max 5MB)")
    @PostMapping("/upload")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String filename = fileStorageService.storeFile(file);
        return ResponseEntity.ok(Map.of(
                "filename", filename,
                "message", "File uploaded successfully"));
    }

    @Operation(summary = "Download a file", description = "Download a file by filename")
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        byte[] fileData = fileStorageService.loadFile(filename);
        ByteArrayResource resource = new ByteArrayResource(fileData);

        String contentType = getContentType(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @Operation(summary = "Delete a file", description = "Delete a file by filename")
    @DeleteMapping("/{filename}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String filename) {
        fileStorageService.deleteFile(filename);
        return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
    }

    private String getContentType(String filename) {
        if (filename.endsWith(".pdf"))
            return "application/pdf";
        if (filename.endsWith(".png"))
            return "image/png";
        if (filename.endsWith(".gif"))
            return "image/gif";
        return "image/jpeg";
    }
}
