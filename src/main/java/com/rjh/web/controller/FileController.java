package com.rjh.web.controller;

import com.rjh.web.util.WatermarkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLEncoder;


/**
 * @author Null
 * @date 2020-01-03
 */
@Slf4j
@RestController
@RequestMapping("file")
public class FileController {

//    private static final String STATIC_FILE_PATH = "static";

    /**
     * 下载文件，直接生成输入流，不会在磁盘生成文件
     *
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
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + handleFileName(fileName))
                .body(resource);
    }

    /**
     * 上传单个文件
     *
     * @param file
     * @return
     */
    @PostMapping("upload")
    public boolean upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        log.info("文件名:" + file.getOriginalFilename());
        log.info("文件大小" + file.getSize() + "字节");
        return true;
    }

    /**
     * 给pdf文件加水印
     *
     * @param file      文件
     * @param watermark 水印内容
     * @return
     */
    @PostMapping("watermark")
    public ResponseEntity<Resource> upload(@RequestParam("file") MultipartFile file, @RequestParam("content") String watermark) {
        String fileName = file.getResource().getFilename();
        InputStream originStream = null;
        StringBuilder builder = new StringBuilder();
        try {
            originStream = file.getInputStream();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        InputStream inputStream;
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        ByteArrayOutputStream outputStream = null;
        // 判断上传的文件是否为pdf文件
        if ("pdf".equals(fileType)) {
            outputStream = new ByteArrayOutputStream();
            WatermarkUtil.watermarkPdf(originStream, outputStream, watermark, 30, 30, 0.2f);
            inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            builder.append(fileName, 0, fileName.lastIndexOf(".pdf")).append("_加水印版.pdf");
        } else if ("jpg".equals(fileType) || "png".equals(fileType)) {
            outputStream = new ByteArrayOutputStream();
            WatermarkUtil.watermarkImg(originStream, outputStream, watermark, 50, 0.2f, fileType);
            inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            builder.append(fileName, 0, fileName.lastIndexOf(".")).append("_加水印版").append(fileName, fileName.lastIndexOf("."), fileName.length());
        } else {
            inputStream = originStream;
            builder.append(fileName);
        }
        Resource resource = new InputStreamResource(inputStream);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + handleFileName(builder.toString()))
                .body(resource);
    }

    /**
     * 处理文件名，避免中文文件名出现乱码
     * @param fileName
     * @return
     */
    private String handleFileName(String fileName) {
        String newFileName;
        try {
            newFileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            newFileName = fileName;
        }
        return newFileName;
    }

}
