package com.iwhalecloud.bss.uba.file.operator;

import com.iwhalecloud.bss.uba.comm.CommonUtils;

import java.io.InputStream;
import java.util.List;

/**用来定义文件操作者接口，具体操作不同的文件会有不同的实现类*/
public interface IFileOperator {

    public enum FileType {
        FILE,
        DIRECTORY,
        ALL
    }

    /**读取文件流*/
    public InputStream getFile(String filePath);

    /**文件移动，从一个文件路径移动到另外一个文件路径，文件名都在filePath上*/
    public void removeTo(String sourceFilePath, String targetFilePath);

    /**获取文件名称清单*/
    public List<String> getFileNames(String fileDir, FileType fileType);

    /**删除文件*/
    public void deleteFile(String filePath);

    /**上传文件*/
    public void writeFile(byte[] data, String filePath);

    /**新增目录*/
    public void createDir(String dirName);

    /**校验远程文件是否存在*/
    public boolean exists(String filePath, boolean throwException);

    /**释放连接，对于不需要释放的置空即可*/
    public void disconnect();

    /**
     * 构建完整的文件路径
     */
    public default String buildFullPath(String rootDir, String filePath) {
        return CommonUtils.buildFullPath(rootDir, filePath);
    }

}
