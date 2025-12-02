package com.iwhalecloud.bss.uba.file.module;

import com.iwhalecloud.bss.uba.common.CommonUtils;
import com.iwhalecloud.bss.uba.common.prop.CommonConst;
import com.iwhalecloud.bss.uba.file.magic.resource.FileInfo;
import com.iwhalecloud.bss.uba.file.magic.resource.FileMagicDynamicRegistry;
import com.iwhalecloud.bss.uba.file.operator.*;
import com.iwhalecloud.bss.uba.adapter.log.UbaLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.iwhalecloud.bss.magic.magicapi.core.annotation.MagicModule;
import com.iwhalecloud.bss.magic.magicapi.modules.DynamicModule;
import com.iwhalecloud.bss.magic.script.MagicScriptContext;
import com.iwhalecloud.bss.magic.script.annotation.Comment;
import com.iwhalecloud.bss.magic.script.functions.DynamicAttribute;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
@MagicModule("file")
public class FileModule implements DynamicAttribute<FileModule, FileModule>,DynamicModule<FileModule> {

    private static final UbaLogger logger = UbaLogger.getLogger(FileModule.class);

    private IFileOperator fileOperator;
    private final FileMagicDynamicRegistry fileMagicDynamicRegistry;

    @Autowired
    public FileModule(FileMagicDynamicRegistry fileMagicDynamicRegistry){
        this.fileMagicDynamicRegistry = fileMagicDynamicRegistry;
    }

    public FileModule(IFileOperator fileOperator, FileMagicDynamicRegistry fileMagicDynamicRegistry){
        this.fileOperator = fileOperator;
        this.fileMagicDynamicRegistry = fileMagicDynamicRegistry;
    }

    private IFileOperator newFileOperator(FileInfo fileInfo){
        return FileOperatorFactory.newFileOperator(fileInfo);
    }

    @Override
    public FileModule getDynamicModule(MagicScriptContext context) {
        Map<String, FileInfo> operators = fileMagicDynamicRegistry.getFileInfos();
        FileInfo fileInfo = null;
        if(operators.isEmpty()){
            throw new RuntimeException("file configuration is empty, please check");
        }else if(operators.size()==1){
            fileInfo = operators.values().stream().findFirst().get();
        }else{
            String fileKey = context.getString(CommonConst.fileDefaultCode);
            if(fileKey!=null && !operators.containsKey(fileKey)){
                fileInfo = operators.get(fileKey);
            }else if(operators.containsKey(CommonConst.fileDefaultCode)){
                fileInfo = operators.get(CommonConst.fileDefaultCode);
            }
            if(fileInfo==null){
                throw new RuntimeException(String.format("cannot find default fileInfo by key: %s and %s" , fileKey,CommonConst.fileDefaultCode ));
            }
        }
        return new FileModule(newFileOperator(fileInfo), fileMagicDynamicRegistry);
    }

    @Comment("Reads file content")
    public byte[] getFileContent(String filePath){
        try {
            return CommonUtils.toByteArray(getFile(filePath));
        }catch (Exception e){
            logger.error("get file content except",e);
            return null;
        }
    }

    @Comment("Reads a file stream")
    public InputStream getFile(String filePath){
        return fileOperator.getFile(filePath);
    }

    @Comment("Moves a file from one file path to another; filenames are listed in the filePath")
    public void removeTo(String sourceFilePath, String targetFilePath){
        fileOperator.removeTo(sourceFilePath, targetFilePath);
    }

    @Comment("Gets a list of file names")
    public List<String> getFileNames(String fileDir){
        return fileOperator.getFileNames(fileDir, IFileOperator.FileType.ALL);
    }

    @Comment("Deletes a file")
    public void deleteFile(String filePath){
        fileOperator.deleteFile(filePath);
    }

    @Comment("Uploads a file")
    public void writeFile(byte[] data, String filePath){
        fileOperator.writeFile(data, filePath);
    }

    @Comment("Add directory")
    public void createDir(String dirName){
        fileOperator.createDir(dirName);
    }

    @Comment("Check if remote file exists")
    public boolean exists(String filePath, boolean throwException){
        return fileOperator.exists(filePath, throwException);
    }

    @Comment("Release connection. For FTP, FTPS, and SFTP, resources can be manually released. If not called, the system will automatically release them after a maximum of two minutes")
    public void disconnect(){
        fileOperator.disconnect();
    }

    @Override
    public FileModule getDynamicAttribute(String key) {
        FileInfo fileInfo = fileMagicDynamicRegistry.getFileInfo(key);
        if(fileInfo == null){
            throw new RuntimeException("cannot find dubbo operator by key:"+key);
        }
        return new FileModule(newFileOperator(fileInfo), fileMagicDynamicRegistry);
    }
}
