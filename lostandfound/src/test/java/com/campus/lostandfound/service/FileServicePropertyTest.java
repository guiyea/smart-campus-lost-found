package com.campus.lostandfound.service;

import com.campus.lostandfound.service.impl.FileServiceImpl;
import com.campus.lostandfound.util.OssUtil;
import net.jqwik.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 文件服务属性测试
 * 
 * Feature: smart-campus-lost-found, Property 7: 图片压缩阈值处理
 * Validates: Requirements 3.5
 */
class FileServicePropertyTest {

    // 压缩阈值：5MB
    private static final long COMPRESS_THRESHOLD = 5 * 1024 * 1024;

    /**
     * Property 7: 图片压缩阈值处理 - 大于5MB的图片压缩后应<=5MB
     * 
     * Feature: smart-campus-lost-found, Property 7: 图片压缩阈值处理
     * 测试: 对于任意大于5MB的图片，压缩后大小应<=5MB
     * Validates: Requirements 3.5
     */
    @Property(tries = 10)
    void largeImagesShouldBeCompressedToUnderThreshold(
            @ForAll("largeImageSizes") int targetSizeKB) throws IOException {
        
        // Setup
        OssUtil ossUtil = mock(OssUtil.class);
        FileServiceImpl fileService = new FileServiceImpl(ossUtil);
        
        // Generate a large image (> 5MB)
        MultipartFile largeImage = generateLargeImage(targetSizeKB);
        
        // Verify the generated image is > 5MB
        assertTrue(largeImage.getSize() > COMPRESS_THRESHOLD,
            "Generated image should be larger than 5MB, actual size: " + largeImage.getSize());
        
        // Compress the image
        MultipartFile compressedImage = fileService.compressImage(largeImage);
        
        // Verify compressed image is <= 5MB
        assertTrue(compressedImage.getSize() <= COMPRESS_THRESHOLD,
            "Compressed image should be <= 5MB, actual size: " + compressedImage.getSize() + 
            " bytes (" + (compressedImage.getSize() / 1024 / 1024.0) + " MB)");
    }

    /**
     * Property 7: 图片压缩阈值处理 - 小于5MB的图片不应进行压缩（保持原始质量）
     * 
     * Feature: smart-campus-lost-found, Property 7: 图片压缩阈值处理
     * 测试: 对于任意小于5MB的图片，不应进行压缩
     * Validates: Requirements 3.5
     * 
     * 注意：此测试验证uploadImage方法的行为，当图片小于5MB时不调用压缩
     */
    @Property(tries = 20)
    void smallImagesShouldNotTriggerCompression(
            @ForAll("smallImageSizes") int targetSizeKB) throws IOException {
        
        // Setup
        OssUtil ossUtil = mock(OssUtil.class);
        when(ossUtil.uploadFile(any(MultipartFile.class), anyString())).thenReturn("http://example.com/test.jpg");
        
        FileServiceImpl fileService = new FileServiceImpl(ossUtil);
        
        // Generate a small image (< 5MB)
        MultipartFile smallImage = generateSmallImage(targetSizeKB);
        
        // Verify the generated image is < 5MB
        assertTrue(smallImage.getSize() < COMPRESS_THRESHOLD,
            "Generated image should be smaller than 5MB, actual size: " + smallImage.getSize());
        
        long originalSize = smallImage.getSize();
        
        // Upload the image (which internally checks if compression is needed)
        fileService.uploadImage(smallImage);
        
        // Verify that the file uploaded to OSS has the same size as original
        // (meaning no compression was applied)
        verify(ossUtil).uploadFile(argThat(file -> {
            // The uploaded file should have the same size as original
            // (within a small tolerance for any metadata changes)
            return file.getSize() == originalSize;
        }), eq("images"));
    }

    /**
     * 验证压缩质量计算的确定性
     * 
     * Feature: smart-campus-lost-found, Property 7: 图片压缩阈值处理
     * 测试: 对于相同大小的图片，压缩质量计算应返回相同结果
     * Validates: Requirements 3.5
     */
    @Property(tries = 50)
    void compressionQualityCalculationShouldBeDeterministic(
            @ForAll("fileSizes") long fileSize) {
        
        // Setup
        OssUtil ossUtil = mock(OssUtil.class);
        FileServiceImpl fileService = new FileServiceImpl(ossUtil);
        
        // Use reflection to access private method
        double quality1 = invokeCalculateCompressionQuality(fileService, fileSize);
        double quality2 = invokeCalculateCompressionQuality(fileService, fileSize);
        
        // Verify same input produces same output
        assertEquals(quality1, quality2, 0.0001,
            "Compression quality calculation should be deterministic");
        
        // Verify quality is within valid range [0.5, 1.0]
        assertTrue(quality1 >= 0.5 && quality1 <= 1.0,
            "Compression quality should be between 0.5 and 1.0, actual: " + quality1);
    }

    /**
     * 验证小于阈值的文件压缩质量为1.0（不压缩）
     * 
     * Feature: smart-campus-lost-found, Property 7: 图片压缩阈值处理
     * 测试: 对于小于5MB的文件，计算的压缩质量应为1.0
     * Validates: Requirements 3.5
     */
    @Property(tries = 50)
    void smallFilesShouldHaveFullQuality(
            @ForAll("smallFileSizes") long fileSize) {
        
        // Setup
        OssUtil ossUtil = mock(OssUtil.class);
        FileServiceImpl fileService = new FileServiceImpl(ossUtil);
        
        // Verify file size is below threshold
        assertTrue(fileSize <= COMPRESS_THRESHOLD,
            "File size should be <= 5MB for this test");
        
        // Calculate compression quality
        double quality = invokeCalculateCompressionQuality(fileService, fileSize);
        
        // Verify quality is 1.0 (no compression needed)
        assertEquals(1.0, quality, 0.0001,
            "Files <= 5MB should have compression quality of 1.0");
    }

    // ==================== Helper Methods ====================

    /**
     * 使用反射调用私有方法 calculateCompressionQuality
     */
    private double invokeCalculateCompressionQuality(FileServiceImpl fileService, long fileSize) {
        try {
            java.lang.reflect.Method method = FileServiceImpl.class.getDeclaredMethod(
                "calculateCompressionQuality", long.class);
            method.setAccessible(true);
            return (double) method.invoke(fileService, fileSize);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke calculateCompressionQuality", e);
        }
    }

    /**
     * 生成大于5MB的测试图片
     * 使用高分辨率和复杂图案来生成大文件
     */
    private MultipartFile generateLargeImage(int targetSizeKB) throws IOException {
        // 创建一个大的BufferedImage
        // 使用较大的尺寸和复杂的图案来生成大文件
        int width = 4000;
        int height = 3000;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 填充复杂的图案以增加文件大小
        for (int x = 0; x < width; x += 10) {
            for (int y = 0; y < height; y += 10) {
                // 使用随机颜色创建复杂图案，确保值在0-255范围内
                int r = Math.abs((x * y) % 256);
                int g = Math.abs((x + y) % 256);
                int b = Math.abs((x + y + 128) % 256);
                g2d.setColor(new Color(r, g, b));
                g2d.fillRect(x, y, 10, 10);
            }
        }
        g2d.dispose();
        
        // 转换为JPEG格式的字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // 使用高质量设置来生成大文件
        javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(1.0f); // 最高质量
        
        javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        writer.setOutput(ios);
        writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
        writer.dispose();
        ios.close();
        
        byte[] imageBytes = baos.toByteArray();
        
        // 如果生成的图片不够大，通过重复数据来增加大小
        if (imageBytes.length < COMPRESS_THRESHOLD + 1024 * 1024) {
            // 创建更大的图片
            width = 6000;
            height = 5000;
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            g2d = image.createGraphics();
            
            for (int x = 0; x < width; x += 5) {
                for (int y = 0; y < height; y += 5) {
                    // 确保颜色值在有效范围内
                    int r = Math.abs((x * y * 3) % 256);
                    int g = Math.abs((x + y * 2) % 256);
                    int b = Math.abs((x + y + 100) % 256);
                    g2d.setColor(new Color(r, g, b));
                    g2d.fillRect(x, y, 5, 5);
                }
            }
            g2d.dispose();
            
            baos = new ByteArrayOutputStream();
            ios = ImageIO.createImageOutputStream(baos);
            writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
            writer.dispose();
            ios.close();
            
            imageBytes = baos.toByteArray();
        }
        
        return new MockMultipartFile(
            "file",
            "large-test-image.jpg",
            "image/jpeg",
            imageBytes
        );
    }

    /**
     * 生成小于5MB的测试图片
     */
    private MultipartFile generateSmallImage(int targetSizeKB) throws IOException {
        // 创建一个小的BufferedImage
        int width = 800;
        int height = 600;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 简单的渐变填充
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int r = (x * 255 / width);
                int g = (y * 255 / height);
                int b = 128;
                image.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        g2d.dispose();
        
        // 转换为JPEG格式
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", baos);
        
        byte[] imageBytes = baos.toByteArray();
        
        return new MockMultipartFile(
            "file",
            "small-test-image.jpg",
            "image/jpeg",
            imageBytes
        );
    }

    // ==================== Generators ====================

    /**
     * 生成大于5MB的文件大小（以KB为单位）
     * 范围：5.5MB - 9MB
     */
    @Provide
    Arbitrary<Integer> largeImageSizes() {
        return Arbitraries.integers().between(5632, 9216); // 5.5MB to 9MB in KB
    }

    /**
     * 生成小于5MB的文件大小（以KB为单位）
     * 范围：100KB - 4.5MB
     */
    @Provide
    Arbitrary<Integer> smallImageSizes() {
        return Arbitraries.integers().between(100, 4608); // 100KB to 4.5MB in KB
    }

    /**
     * 生成各种文件大小（字节）
     * 范围：1MB - 10MB
     */
    @Provide
    Arbitrary<Long> fileSizes() {
        return Arbitraries.longs().between(1024 * 1024, 10 * 1024 * 1024);
    }

    /**
     * 生成小于等于5MB的文件大小（字节）
     */
    @Provide
    Arbitrary<Long> smallFileSizes() {
        return Arbitraries.longs().between(1024, COMPRESS_THRESHOLD);
    }
}
