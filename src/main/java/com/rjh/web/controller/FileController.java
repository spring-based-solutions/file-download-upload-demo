package com.rjh.web.controller;

import com.rjh.web.util.WatermarkUtil;
import jdk.internal.util.xml.impl.Input;
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
import java.net.URLEncoder;
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

    /**
     * 给pdf文件加水印
     * @param file pdf文件
     * @param watermark 水印内容
     * @return
     */
    @PostMapping("pdf/watermark")
    public ResponseEntity<Resource> upload(@RequestParam("file") MultipartFile file,@RequestParam("content") String watermark) {
        String fileName = file.getResource().getFilename();
        InputStream originStream = null;
        StringBuilder builder = new StringBuilder();
        try {
            originStream = file.getInputStream();
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
        InputStream inputStream;
        // 判断上传的文件是否为pdf文件
        if(fileName.endsWith(".pdf")){
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            WatermarkUtil.watermarkPdf(originStream,outputStream,watermark,30,30,0.2f);
            inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            builder.append(fileName, 0, fileName.lastIndexOf(".pdf")).append("_加水印版.pdf");
        }else{
            inputStream=originStream;
            builder.append(fileName);
        }
        Resource resource = new InputStreamResource(inputStream);
        String newFileName;
        try {
            newFileName = URLEncoder.encode(builder.toString(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(),e);
            newFileName = builder.toString();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + newFileName)
                .body(resource);
    }

}
