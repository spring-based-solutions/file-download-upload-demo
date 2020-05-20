package com.rjh.web.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 水印工具类
 *
 * @author NULL
 * @date 2020-05-16
 */
@Slf4j
public class WatermarkUtil {

    public static void watermarkPdf(InputStream inputStream, String watermark,float fontSize,float theta, OutputStream outputStream) {
        try {
            PDDocument doc = PDDocument.load(inputStream);
            watermarkPdf(doc, watermark,fontSize,theta);
            // 将修改后的文件转到输出流
            doc.save(outputStream);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     *
     * @param infile 原始文件
     * @param outFile 修改后的文件
     * @param watermark 水印内容
     * @param fontSize 字体大小
     * @param theta 水印倾斜角度
     */
    public static void watermarkPdf(File infile,File outFile,String watermark,float fontSize,float theta) {
        try {
            PDDocument doc = PDDocument.load(infile);
            watermarkPdf(doc, watermark,fontSize,theta);
            // 将修改后的文件转到输出流
            doc.save(outFile);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 水印处理
     * TODO 增加中文水印支持
     *
     * @param doc pdf对象
     * @param watermark 水印内容
     * @param fontSize 字体大小
     * @param theta 水印旋转角度
     * @throws IOException
     */
    public static void watermarkPdf(PDDocument doc, String watermark,float fontSize,float theta) throws IOException {
        // 移除PDF的安全模式
        doc.setAllSecurityToBeRemoved(true);
        // 获取每一页内容
        PDPageTree tree = doc.getPages();
        // 水印字体
        PDFont font = PDType1Font.COURIER_OBLIQUE;
        // 获取角度对应的弧度值
        double angle = Math.toRadians(theta);
        // TODO 加载项目下自己准备的字体库，解决中文乱码问题
//        PDFont font = PDType0Font.load(doc,new FileInputStream("/Library/Fonts/Arial Unicode.ttf"),false);
        // 基准宽度
        float baseWidth = font.getStringWidth("A");
        // 实际宽度
        float strWidth = font.getStringWidth(watermark)/baseWidth;
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
            state.setNonStrokingAlphaConstant(0.1f);
//            state.setAlphaSourceFlag(true);
            // 设置图层的参数
            stream.setGraphicsStateParameters(state);
            // 设置字体颜色
            stream.setNonStrokingColor(Color.RED);
            // 设置水印字体和大小
            stream.setFont(font, fontSize);
            List<Location> list = getLocation(height, width, strWidth, fontSize, angle);
            // 开始追加文本水印
            stream.beginText();
            for (Location location : list) {
                // 设置水印位置
                stream.setTextMatrix(Matrix.getRotateInstance(angle, location.getX(), location.getY()));
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
     * @param height 页面高度
     * @param width 页面宽度
     * @param strWidth 字符内容宽度
     * @param fontSize 字体大小
     * @param theta 水印旋转角度
     * @return
     */
    private static List<Location> getLocation(float height, float width, float strWidth, float fontSize,double theta) {
        // 内容长度
        float strSize = strWidth * fontSize;
        // 根据角度自动调整水印间隔
        float heightInterval = (float) (strSize*Math.sin(theta));
        float widthInterval = (float) (strSize*Math.cos(theta));
        float x = fontSize;
        float y = fontSize;
        List<Location> list = new ArrayList<>();
        while (y < height) {
            while (x < width) {
                list.add(new Location(x,y));
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
    private static class Location{
        float x;
        float y;
    }

}
