package com.iwhalecloud.bss.uba.file.operator;

import com.iwhalecloud.bss.uba.common.releaser.IReleasable;
import com.iwhalecloud.bss.uba.file.magic.resource.FileInfo;
import com.ztesoft.zsmart.core.log.ZSmartLogger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地文件操作实现类
 */
public class LocalFileOperator implements IFileOperator {

    private static final ZSmartLogger logger = ZSmartLogger.getLogger(LocalFileOperator.class);

    private final FileInfo fileInfo;
    private String rootDir;

    public LocalFileOperator(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
        // 初始化根目录，如果未指定则使用当前工作目录
        this.rootDir = fileInfo.getRootDir();
        if (this.rootDir == null || this.rootDir.trim().isEmpty()) {
            this.rootDir = System.getProperty("user.dir");
        }
        // 确保根目录以文件分隔符结尾
        if (!this.rootDir.endsWith(File.separator)) {
            this.rootDir += File.separator;
        }
        logger.debug(String.format("current local file information is key : %s,rootDir: %s" , this.fileInfo.getKey() ,this.rootDir));
    }

    @Override
    public InputStream getFile(String filePath) {
        try {
            filePath = buildFullPath(rootDir, filePath);
            logger.debug(String.format("obtaining file stream, key : %s, filePath: %s" , this.fileInfo.getKey() ,filePath));
            return new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("obtaining file stream error: " + filePath, e);
        }
    }

    @Override
    public void removeTo(String sourceFilePath, String targetFilePath) {
        logger.debug(String.format("remove file, key:%s, filePath: %s to %s",
                fileInfo.getKey(), buildFullPath(rootDir, sourceFilePath), buildFullPath(rootDir, targetFilePath)));
        File sourceFile = new File(buildFullPath(rootDir, sourceFilePath));
        File targetFile = new File(buildFullPath(rootDir, targetFilePath));
        // 确保目标目录存在
        if (targetFile.getParentFile() != null && !targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }

        if (!sourceFile.renameTo(targetFile)) {
            throw new RuntimeException("文件移动失败: " + sourceFilePath + " -> " + targetFilePath);
        }
    }

    @Override
    public List<String> getFileNames(String fileDir) {
        logger.debug(String.format("get file names, key: %s, filePath: %s",
                fileInfo.getKey(), fileDir));
        File dir = new File(buildFullPath(rootDir, fileDir));
        if (!dir.exists() || !dir.isDirectory()) {
            throw new RuntimeException(String.format("current fileDir[%s] is not directory or is not directory" , fileDir));
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        List<String> fileNames = new ArrayList<>();
        for (File file : files) {
            fileNames.add(file.getName());
        }
        return fileNames;
    }

    @Override
    public void deleteFile(String filePath) {
        logger.debug(String.format("delete file, key: %s, filePath: %s",
                fileInfo.getKey(), buildFullPath(rootDir, filePath)));
        File file = new File(buildFullPath(rootDir, filePath));
        if (file.exists()) {
            if (!file.delete()) {
                throw new RuntimeException("delete file error: " + filePath);
            }
        }
    }

    @Override
    public void writeFile(byte[] data, String filePath) {
        logger.debug(String.format("write file, key: %s, filePath: %s, size: %d",
                fileInfo.getKey(), buildFullPath(rootDir, filePath), data.length));
        try (OutputStream os = Files.newOutputStream(Paths.get(buildFullPath(rootDir, filePath)))) {
            // 确保父目录存在
            File file = new File(buildFullPath(rootDir, filePath));
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            os.write(data);
        } catch (IOException e) {
            throw new RuntimeException("write to file error: " + filePath, e);
        }
    }

    @Override
    public void createDir(String dirName) {
        File dir = new File(buildFullPath(rootDir, dirName));
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("create directory error: " + dirName);
        }
    }

    @Override
    public boolean exists(String filePath, boolean throwException) {
        File file = new File(buildFullPath(rootDir, filePath));
        return file.exists();
    }

    @Override
    public void disconnect() {

    }

}
