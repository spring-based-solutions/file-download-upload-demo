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

    public static void watermarkPdf(InputStream inputStream, String watermark, OutputStream outputStream) {
        try {
            PDDocument doc = PDDocument.load(inputStream);
            watermarkPdf(doc, watermark);
            // 将修改后的文件转到输出流
            doc.save(outputStream);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void watermarkPdf(File infile, String watermark, File outFile) {
        try {
            PDDocument doc = PDDocument.load(infile);
            watermarkPdf(doc, watermark);
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
     * @param doc
     * @param watermark
     * @throws IOException
     */
    public static void watermarkPdf(PDDocument doc, String watermark) throws IOException {
        // 移除PDF的安全模式
        doc.setAllSecurityToBeRemoved(true);
        // 获取每一页内容
        PDPageTree tree = doc.getPages();
        // 水印字体
        PDFont font = PDType1Font.COURIER_OBLIQUE;
        // TODO 加载项目下自己准备的字体库，解决中文乱码问题
//        PDFont font = PDType0Font.load(doc,new FileInputStream("/Library/Fonts/Arial Unicode.ttf"),false);
        // 字体大小
        float fontSize = 36;
        // 基准宽度
        float baseWidth = font.getStringWidth("A");
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
            state.setNonStrokingAlphaConstant(0.2f);
            state.setAlphaSourceFlag(true);
            // 设置图层的参数
            stream.setGraphicsStateParameters(state);
            // 设置字体颜色
            stream.setNonStrokingColor(Color.RED);
            // 设置水印字体和大小
            stream.setFont(font, fontSize);
            List<Location> list = getLocation(height, width, strWidth, fontSize);
            // 开始追加文本水印
            stream.beginText();
            for (Location location : list) {
                // 设置水印位置
                stream.setTextMatrix(Matrix.getRotateInstance(45, location.getX(), location.getY()));
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
     * @param height
     * @param width
     * @param strWidth
     * @param fontSize
     * @return
     */
    private static List<Location> getLocation(float height, float width, float strWidth, float fontSize) {
        // 计算水印间隔 TODO 长文本内容，水印间隔较大
        float interval = (float) Math.sqrt((strWidth * fontSize * strWidth * fontSize / 2));
        float x = fontSize;
        float y = fontSize;
        List<Location> list = new ArrayList<>();
        while (y < height) {
            while (x < width) {
                list.add(new Location(x,y));
                x += interval;
            }
            y += interval;
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
