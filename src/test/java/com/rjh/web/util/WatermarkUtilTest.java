package com.rjh.web.util;

import org.junit.Test;

import java.io.File;

/**
 * @author Null
 * @date 2020-05-20
 */
public class WatermarkUtilTest {

    @Test
    public void testWatermarkPdf(){
        File inFile = new File("D:/背包九讲.pdf");
        File outFile = new File("D:/背包九讲水印版.pdf");
        WatermarkUtil.watermarkPdf(inFile,outFile,"Good Good Study,Day Day Up",20,30,0.2f);
    }

    @Test
    public void testChineseWatermarkPdf(){
        File inFile = new File("D:/背包九讲.pdf");
        File outFile = new File("D:/背包九讲水印版.pdf");
        WatermarkUtil.watermarkPdf(inFile,outFile,"长洲宾客人数多，请试着从右向左读",20,30,0.2f);
    }

}
