//package com.github.houbb.cache.core.mytest;
//
//import org.junit.Test;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.RandomAccessFile;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//
//public class qq {
//    @Test
//    public void testChunk() throws IOException {
//        // 源文件
//        File sourceFile = new File("1.aof");
//        // 块文件路径
//        String chunkPath = "../";
//        File chunkFolder = new File(chunkPath);
//        if (!chunkFolder.exists()) {
//            chunkFolder.mkdirs();
//        }
//        // 分块大小 1M
//        long chunkSize = 1024 * 1024 * 1;
//        // 计算块数，向上取整
//        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
//        // 缓冲区大小
//        byte[] buffer = new byte[1024];
//        // 使用RandomAccessFile访问文件
//        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
//        // 遍历分块，依次向每一个分块写入数据
//        for (int i = 0; i < chunkNum; i++) {
//            // 创建分块文件，默认文件名 path + i，例如chunk\1  chunk\2
//            File file = new File(chunkPath + i);
//            if (file.exists()){
//                file.delete();
//            }
//            boolean newFile = file.createNewFile();
//            if (newFile) {
//                int len;
//                RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
//                // 向分块文件写入数据
//                while ((len = raf_read.read(buffer)) != -1) {
//                    raf_write.write(buffer, 0, len);
//                    // 写满就停
//                    if (file.length() >= chunkSize)
//                        break;
//                }
//                raf_write.close();
//            }
//        }
//        raf_read.close();
//        System.out.println("写入分块完毕");
//    }
//    @Test
//    public void testMerge() throws IOException {
//        // 块文件目录
//        File chunkFolder = new File("D:\\BaiduNetdiskDownload\\星际牛仔1998\\chunk\\");
//        // 源文件
//        File sourceFile = new File("D:\\BaiduNetdiskDownload\\星际牛仔1998\\星际牛仔1.mp4");
//        // 合并文件
//        File mergeFile = new File("D:\\BaiduNetdiskDownload\\星际牛仔1998\\星际牛仔1-1.mp4");
//        mergeFile.createNewFile();
//        // 用于写文件
//        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
//        // 缓冲区
//        byte[] buffer = new byte[1024];
//        // 文件名升序排序
//        File[] files = chunkFolder.listFiles();
//        List<File> fileList = Arrays.asList(files);
//        Collections.sort(fileList, Comparator.comparingInt(o -> Integer.parseInt(o.getName())));
//        // 合并文件
//        for (File chunkFile : fileList) {
//            RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");
//            int len;
//            while ((len = raf_read.read(buffer)) != -1) {
//                raf_write.write(buffer, 0, len);
//            }
//            raf_read.close();
//        }
//        raf_write.close();
//        // 判断合并后的文件是否与源文件相同
//        FileInputStream fileInputStream = new FileInputStream(sourceFile);
//        FileInputStream mergeFileStream = new FileInputStream(mergeFile);
//        //取出原始文件的md5
//        String originalMd5 = DigestUtils.md5Hex(fileInputStream);
//        //取出合并文件的md5进行比较
//        String mergeFileMd5 = DigestUtils.md5Hex(mergeFileStream);
//        if (originalMd5.equals(mergeFileMd5)) {
//            System.out.println("合并文件成功");
//        } else {
//            System.out.println("合并文件失败");
//        }
//    }
//}
