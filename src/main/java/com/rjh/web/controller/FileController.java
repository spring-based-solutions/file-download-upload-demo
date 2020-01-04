package com.rjh.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Null
 * @date 2020-01-03
 */
@Slf4j
@RestController
@RequestMapping("file")
public class FileController {

    private static final String STATIC_FILE_PATH = "static";

    /**
     * 下载文件，直接生成输入流，不会在磁盘生成文件
     * @param fileName
     * @return
     */
    @GetMapping("download")
    public ResponseEntity<Resource> download(@RequestParam String fileName) {
        // 直接创建输入流，不会生成文件
        InputStream inputStream = new ByteArrayInputStream(fileName.getBytes());
        Resource resource = new InputStreamResource(inputStream);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(resource);
    }

//    @GetMapping("download/{fileName}")
//    public ResponseEntity<Resource> downloadExistFile(@PathVariable String fileName) {
//        Resource resource = null;
//        File file =new File(STATIC_FILE_PATH+File.separator+fileName);
//        if(file.exists()){
//            resource = new FileSystemResource(file);
//        }
//        if (resource != null) {
//            return ResponseEntity.ok()
//                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
//                    .body(resource);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }

    /**
     * 上传单个文件
     * @param file
     * @return
     */
    @PostMapping("upload")
    public boolean upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        boolean result = false;
        log.info("文件名:" + file.getResource().getFilename());
        log.info("文件大小" + file.getSize() + "字节");
//        Path path=Paths.get(STATIC_FILE_PATH);
//        File newFile = new File("");
//        try {
//            result = newFile.createNewFile();
//        } catch (IOException e) {
//            log.error("文件上传失败",e);
//        }
        return result;
    }

}
