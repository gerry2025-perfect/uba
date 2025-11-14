package com.iwhalecloud.bss.uba.file.operator;

import com.iwhalecloud.bss.uba.file.magic.resource.FileInfo;

/**根据File配置构造对应处理类*/
public class FileOperatorFactory {

    public static IFileOperator newFileOperator(FileInfo fileInfo){
        switch (fileInfo.getFileType()){
            case local:
                return new LocalFileOperator(fileInfo);
            case ftp:
                return new FtpFileOperator(fileInfo);
            case ftps:
                return new FtpsFileOperator(fileInfo);
            case sftp:
                return new SftpFileOperator(fileInfo);
        }
        throw new RuntimeException("current file type is not supported, fileType:" + fileInfo.getFileType());
    }

}
