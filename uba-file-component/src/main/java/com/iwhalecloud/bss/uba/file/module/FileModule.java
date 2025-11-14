package com.iwhalecloud.bss.uba.file.module;

import com.iwhalecloud.bss.uba.common.CommonUtils;
import com.iwhalecloud.bss.uba.common.prop.CommonConst;
import com.iwhalecloud.bss.uba.file.magic.resource.FileInfo;
import com.iwhalecloud.bss.uba.file.magic.resource.FileMagicDynamicRegistry;
import com.iwhalecloud.bss.uba.file.operator.*;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.modules.DynamicModule;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.functions.DynamicAttribute;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
@MagicModule("file")
public class FileModule implements DynamicAttribute<FileModule, FileModule>,DynamicModule<FileModule> {

    private static final ZSmartLogger logger = ZSmartLogger.getLogger(FileModule.class);

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
            }else if(!operators.containsKey(CommonConst.fileDefaultCode)){
                fileInfo = operators.get(CommonConst.fileDefaultCode);
            }
            if(fileInfo==null){
                throw new RuntimeException(String.format("cannot find default fileInfo by key: %s and %s" , fileKey,CommonConst.fileDefaultCode ));
            }
        }
        return new FileModule(newFileOperator(fileInfo), fileMagicDynamicRegistry);
    }

    @Comment("读取文件内容")
    public byte[] getFileContent(String filePath){
        try {
            return CommonUtils.toByteArray(getFile(filePath));
        }catch (Exception e){
            logger.error("get file content except",e);
            return null;
        }
    }

    @Comment("读取文件流")
    public InputStream getFile(String filePath){
        return fileOperator.getFile(filePath);
    }

    @Comment("文件移动，从一个文件路径移动到另外一个文件路径，文件名都在filePath上")
    public void removeTo(String sourceFilePath, String targetFilePath){
        fileOperator.removeTo(sourceFilePath, targetFilePath);
    }

    @Comment("获取文件名称清单")
    public List<String> getFileNames(String fileDir){
        return fileOperator.getFileNames(fileDir, IFileOperator.FileType.ALL);
    }

    @Comment("删除文件")
    public void deleteFile(String filePath){
        fileOperator.deleteFile(filePath);
    }

    @Comment("上传文件")
    public void writeFile(byte[] data, String filePath){
        fileOperator.writeFile(data, filePath);
    }

    @Comment("新增目录")
    public void createDir(String dirName){
        fileOperator.createDir(dirName);
    }

    @Comment("校验远程文件是否存在")
    public boolean exists(String filePath, boolean throwException){
        return fileOperator.exists(filePath, throwException);
    }

    @Comment("释放连接，对于ftp、ftps和sftp可以手动释放资源，如果没有调用，最长两分钟之后系统会自动释放")
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
