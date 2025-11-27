package com.iwhalecloud.bss.uba.file.module;

import com.iwhalecloud.bss.uba.file.excel.CsvOperator;
import com.iwhalecloud.bss.uba.file.excel.IFileOperator;
import com.iwhalecloud.bss.uba.file.excel.PoiExcelOperator;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import org.springframework.stereotype.Component;
import com.iwhalecloud.bss.magic.magicapi.core.annotation.MagicModule;
import com.iwhalecloud.bss.magic.magicapi.modules.DynamicModule;
import com.iwhalecloud.bss.magic.script.MagicScriptContext;
import com.iwhalecloud.bss.magic.script.annotation.Comment;
import com.iwhalecloud.bss.magic.script.functions.DynamicAttribute;

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

    @Comment("Reads the full data of an Excel file, the file content is expressed through a data stream")
    public Map<String, List<Map<String,Object>>> read(InputStream inputStream, String fileName){
        try {
            return getFileOperator(fileName).read(inputStream, fileName);
        } catch (IOException e) {
            logger.error(String.format("read excel file except, fileName: %s", fileName), e);
            return null;
        }
    }

    @Comment("Reads data from a specific sheet in an Excel file, using the sheet name to identify the read sheet, the file content is expressed through a data stream")
    public List<Map<String, Object>> read(InputStream inputStream, String fileName, String sheetName){
        try {
            return getFileOperator(fileName).read(inputStream, fileName, sheetName);
        } catch (IOException e) {
            logger.error(String.format("read excel file except, fileName: %s , sheetName: %s", fileName, sheetName), e);
            return null;
        }
    }

    @Comment("Reads data from a specific sheet in an Excel file, using the sheet sequence number to identify the read sheet, the file content is expressed through a data stream")
    public List<Map<String, Object>> read(InputStream inputStream, String fileName, Integer sheetIdx) {
        try {
            return getFileOperator(fileName).read(inputStream, fileName, sheetIdx);
        } catch (IOException e) {
            logger.error(String.format("read excel file except, fileName: %s , sheetIdx: %s", fileName, sheetIdx), e);
            return null;
        }
    }

    @Comment("Reads the entire data from an Excel file; the file content is represented by a byte array")
    public Map<String, List<Map<String,Object>>> read(byte[] fileContent, String fileName){
        try {
            return getFileOperator(fileName).read(fileContent, fileName);
        } catch (IOException e) {
            logger.error(String.format("read excel file except, fileName: %s", fileName), e);
            return null;
        }
    }

    @Comment("Reads data from a specific sheet in an Excel file, identifying the sheet by its name; the file content is represented by a byte array")
    public List<Map<String, Object>> read(byte[] fileContent, String fileName, String sheetName){
        try {
            return getFileOperator(fileName).read(fileContent, fileName, sheetName);
        } catch (IOException e) {
            logger.error(String.format("read excel file except, fileName: %s , sheetName: %s", fileName, sheetName), e);
            return null;
        }
    }

    @Comment("Reads data from a specific sheet in an Excel file, identifying the sheet by its sequence number; the file content is represented by a byte array")
    public List<Map<String, Object>> read(byte[] fileContent, String fileName, Integer sheetIdx) {
        try {
            return getFileOperator(fileName).read(fileContent, fileName, sheetIdx);
        } catch (IOException e) {
            logger.error(String.format("read excel file except, fileName: %s , sheetIdx: %s", fileName, sheetIdx), e);
            return null;
        }
    }

    @Comment("Creates an Excel file; the file content is represented by a byte array")
    public byte[] create(String fileName, Map<String, List<Map<String,Object>>> dataList) {
        try {
            return getFileOperator(fileName).create(fileName, dataList);
        } catch (IOException e) {
            logger.error(String.format("create excel file except, fileName: %s", fileName), e);
            return null;
        }
    }

    @Comment("Appends data to an Excel file; the file content is represented by a data stream")
    public byte[] append(InputStream inputStream, String fileName, Map<String, List<Map<String,Object>>> dataList) {
        try {
            return getFileOperator(fileName).append(inputStream, fileName, dataList);
        } catch (IOException e) {
            logger.error(String.format("append excel file except, fileName: %s", fileName), e);
            return null;
        }
    }

    @Comment("Appends data to an Excel file; the file content is represented by a byte array")
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
