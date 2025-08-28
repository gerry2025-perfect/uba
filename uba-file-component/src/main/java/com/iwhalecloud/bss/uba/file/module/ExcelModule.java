package com.iwhalecloud.bss.uba.file.module;

import com.iwhalecloud.bss.uba.file.excel.CsvOperator;
import com.iwhalecloud.bss.uba.file.excel.IFileOperator;
import com.iwhalecloud.bss.uba.file.excel.PoiExcelOperator;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.modules.DynamicModule;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.functions.DynamicAttribute;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
@MagicModule("excel")
public class ExcelModule implements DynamicAttribute<ExcelModule, ExcelModule>, DynamicModule<ExcelModule> {

    private static final ZSmartLogger logger = ZSmartLogger.getLogger(ExcelModule.class);

    @Override
    public ExcelModule getDynamicModule(MagicScriptContext context) {
        return new ExcelModule();
    }

    @Override
    public ExcelModule getDynamicAttribute(String key) {
        return new ExcelModule();
    }

    @Comment("读取excel文件的全量数据，文件内容通过数据流表达")
    public Map<String, List<Map<String,Object>>> read(InputStream inputStream, String fileName){
        try {
            return getFileOperator(fileName).read(inputStream, fileName);
        } catch (IOException e) {
            logger.error(String.format("read excel file except, fileName: %s", fileName), e);
            return null;
        }
    }

    @Comment("读取excel文件中特定sheet的数据，用sheet名称来标识读取的sheet，文件内容通过数据流表达")
    public List<Map<String, Object>> read(InputStream inputStream, String fileName, String sheetName){
        try {
            return getFileOperator(fileName).read(inputStream, fileName, sheetName);
        } catch (IOException e) {
            logger.error(String.format("read excel file except, fileName: %s , sheetName: %s", fileName, sheetName), e);
            return null;
        }
    }

    @Comment("读取excel文件中特定sheet的数据，用sheet 序列号来标识读取的sheet，文件内容通过数据流表达")
    public List<Map<String, Object>> read(InputStream inputStream, String fileName, Integer sheetIdx) {
        try {
            return getFileOperator(fileName).read(inputStream, fileName, sheetIdx);
        } catch (IOException e) {
            logger.error(String.format("read excel file except, fileName: %s , sheetIdx: %s", fileName, sheetIdx), e);
            return null;
        }
    }

    @Comment("读取excel文件的全量数据，文件内容通过字节数组表达")
    public Map<String, List<Map<String,Object>>> read(byte[] fileContent, String fileName){
        try {
            return getFileOperator(fileName).read(fileContent, fileName);
        } catch (IOException e) {
            logger.error(String.format("read excel file except, fileName: %s", fileName), e);
            return null;
        }
    }

    @Comment("读取excel文件中特定sheet的数据，用sheet名称来标识读取的sheet，文件内容通过字节数组表达")
    public List<Map<String, Object>> read(byte[] fileContent, String fileName, String sheetName){
        try {
            return getFileOperator(fileName).read(fileContent, fileName, sheetName);
        } catch (IOException e) {
            logger.error(String.format("read excel file except, fileName: %s , sheetName: %s", fileName, sheetName), e);
            return null;
        }
    }

    @Comment("读取excel文件中特定sheet的数据，用sheet 序列号来标识读取的sheet，文件内容通过字节数组表达")
    public List<Map<String, Object>> read(byte[] fileContent, String fileName, Integer sheetIdx) {
        try {
            return getFileOperator(fileName).read(fileContent, fileName, sheetIdx);
        } catch (IOException e) {
            logger.error(String.format("read excel file except, fileName: %s , sheetIdx: %s", fileName, sheetIdx), e);
            return null;
        }
    }

    @Comment("创建excel文件，文件内容通过字节数组表达")
    public byte[] create(String fileName, Map<String, List<Map<String,Object>>> dataList) {
        try {
            return getFileOperator(fileName).create(fileName, dataList);
        } catch (IOException e) {
            logger.error(String.format("create excel file except, fileName: %s", fileName), e);
            return null;
        }
    }

    @Comment("向excel文件中追加数据，文件内容通过数据流表达")
    public byte[] append(InputStream inputStream, String fileName, Map<String, List<Map<String,Object>>> dataList) {
        try {
            return getFileOperator(fileName).append(inputStream, fileName, dataList);
        } catch (IOException e) {
            logger.error(String.format("append excel file except, fileName: %s", fileName), e);
            return null;
        }
    }

    @Comment("向excel文件中追加数据，文件内容通过字节数组表达")
    public byte[] append(byte[] fileContent, String fileName, Map<String, List<Map<String,Object>>> dataList) {
        try {
            return getFileOperator(fileName).append(fileContent, fileName, dataList);
        } catch (IOException e) {
            logger.error(String.format("append excel file except, fileName: %s", fileName), e);
            return null;
        }
    }

    private IFileOperator getFileOperator(String fileName){
        if(fileName==null || fileName.isEmpty()) throw new RuntimeException("fileName is empty");
        if(fileName.endsWith(".csv") || fileName.endsWith(".CSV")){
            return new CsvOperator();
        }else{
            return new PoiExcelOperator();
        }
    }

}
