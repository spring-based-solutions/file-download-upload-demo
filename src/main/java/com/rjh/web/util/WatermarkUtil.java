package com.rjh.web.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 水印工具类
 *
 * @author NULL
 * @date 2020-05-16
 */
@Slf4j
public class WatermarkUtil {
    /**
     * @param inputStream 输入流
     * @param outputStream 输出流
     * @param watermark 水印内容
     * @param fontSize 字体大小
     * @param theta 水印旋转角度
     * @param alpha 透明度
     */
    public static void watermarkPdf(InputStream inputStream,OutputStream outputStream,String watermark, float fontSize, float theta, float alpha) {
        PDDocument doc = null;
        try {
            doc = PDDocument.load(inputStream);
            watermarkPdf(doc, watermark, fontSize, theta, alpha);
            // 将修改后的文件转到输出流
            doc.save(outputStream);
            doc.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (doc != null) {
                try {
                    doc.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * @param infile    原始文件
     * @param outFile   修改后的文件
     * @param watermark 水印内容
     * @param fontSize 字体大小
     * @param theta 水印旋转角度
     * @param alpha 透明度
     */
    public static void watermarkPdf(File infile, File outFile, String watermark, float fontSize, float theta, float alpha) {
        PDDocument doc = null;
        try {
            doc = PDDocument.load(infile);
            watermarkPdf(doc, watermark, fontSize, theta, alpha);
            // 将修改后的文件转到输出流
            doc.save(outFile);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (doc != null) {
                try {
                    doc.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 水印处理，使用项目自带的字体来实现中文水印
     * @param doc       pdf对象
     * @param watermark 水印内容
     * @param fontSize  字体大小
     * @param theta     水印旋转角度
     * @param alpha     透明度，0-1之间,0为完全透明
     * @throws IOException
     */
    private static void watermarkPdf(PDDocument doc, String watermark, float fontSize, float theta, float alpha) throws IOException {
        // 移除PDF的安全模式
        doc.setAllSecurityToBeRemoved(true);
        // 获取每一页内容
        PDPageTree tree = doc.getPages();
        // 水印字体
//        PDFont font = PDType1Font.COURIER_OBLIQUE;
        // 获取角度对应的弧度值
        double radian = Math.toRadians(theta);
        // 加载微软雅黑字体
        Resource resource = new ClassPathResource("wryh.ttf");
        InputStream resourceInputStream = resource.getInputStream();
        // 加载项目下自己准备的字体库，解决中文乱码问题
        PDFont font = PDType0Font.load(doc, resourceInputStream);
        // 基准宽度
        float baseWidth = font.getStringWidth("A");
        // 实际宽度
        float strWidth = font.getStringWidth(watermark) / baseWidth;
        for (PDPage page : tree) {
            PDRectangle rectangle = page.getBBox();
            // 当前页面高度，单位为磅
            float height = rectangle.getHeight();
            // 当前页面宽度
            float width = rectangle.getWidth();
            PDPageContentStream stream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);
            // 设置图层
            PDExtendedGraphicsState state = new PDExtendedGraphicsState();
            // 设置透明度
            state.setNonStrokingAlphaConstant(alpha);
//            state.setAlphaSourceFlag(true);
            // 设置图层的参数
            stream.setGraphicsStateParameters(state);
            // 设置字体颜色
            stream.setNonStrokingColor(Color.RED);
            // 设置水印字体和大小
            stream.setFont(font, fontSize);
            // 计算出所有要添加的水印位置
            List<Location> list = getLocation(height, width, strWidth, fontSize, radian);
            // 开始追加文本水印
            stream.beginText();
            for (Location location : list) {
                // 设置水印位置
                stream.setTextMatrix(Matrix.getRotateInstance(radian, location.getX(), location.getY()));
                // 设置水印内容
                stream.showText(watermark);
            }
            // 结束文本处理
            stream.endText();
//            stream.restoreGraphicsState();
            // 关闭流
            stream.close();
        }
    }

    /**
     * 获取水印位置
     *
     * @param height   页面高度
     * @param width    页面宽度
     * @param strWidth 字符内容宽度
     * @param fontSize 字体大小
     * @param radian   水印旋转弧度
     * @return
     */
    private static List<Location> getLocation(float height, float width, float strWidth, float fontSize, double radian) {
        // 内容长度
        float strSize = strWidth * fontSize;
        // 根据角度自动调整水印间隔
        float heightInterval = (float) (strSize * Math.sin(radian));
        float widthInterval = (float) (strSize * Math.cos(radian));
        float x = fontSize;
        float y = fontSize;
        List<Location> list = new ArrayList<>();
        while (y < height) {
            while (x < width) {
                list.add(new Location(x, y));
                x += widthInterval;
            }
            y += heightInterval;
            x = fontSize;
        }
        return list;
    }

    /**
     * 坐标信息
     */
    @Data
    @AllArgsConstructor
    private static class Location {
        float x;
        float y;
    }

}
