package com.enation.app.shop.mobile.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成二维码
 */
public class QrCodeUtils {

    /**
     * 生成二维码
     * @param content
     * @param savePath
     */
    public static void create(String content, String savePath) {
        try {
            BufferedImage bim = create(content,
                    BarcodeFormat.QR_CODE, 300, 300);
            ImageIO.write(bim, "jpg", new File(savePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成二维码bufferedImage图片
     *
     * @param content       编码内容
     * @param barcodeFormat 编码类型
     * @param width         图片宽度
     * @param height        图片高度
     * @return
     */
    public static BufferedImage create(String content,
                                                 BarcodeFormat barcodeFormat, int width, int height) {
        MultiFormatWriter multiFormatWriter = null;
        BitMatrix bm = null;
        BufferedImage image = null;
        try {
            multiFormatWriter = new MultiFormatWriter();

            // 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
            bm = multiFormatWriter.encode(content, barcodeFormat, width,
                    height, getDecodeHintType());

            int w = bm.getWidth();
            int h = bm.getHeight();
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

            // 开始利用二维码数据创建Bitmap图片，分别设为黑（0xFFFFFFFF）白（0xFF000000）两色
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    image.setRGB(x, y, bm.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * 设置二维码的格式参数
     *
     * @return
     */
    private static Map<EncodeHintType, Object> getDecodeHintType() {
        // 用于设置QR二维码参数
        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        // 设置QR二维码的纠错级别（H为最高级别）具体级别信息
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 设置编码方式
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.MAX_SIZE, 350);
        hints.put(EncodeHintType.MIN_SIZE, 100);
        hints.put(EncodeHintType.MARGIN, 0);
        return hints;
    }

    public static void main(String[] args){
        create("http://www.baidu.com", "/Users/Dawei/Desktop/test_code.jpg");
    }
}
