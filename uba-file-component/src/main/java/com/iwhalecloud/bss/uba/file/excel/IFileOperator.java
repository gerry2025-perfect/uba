package com.iwhalecloud.bss.uba.file.excel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface IFileOperator {

    Map<String, List<Map<String, Object>>> read(InputStream inputStream, String fileName) throws IOException;

    List<Map<String, Object>> read(InputStream inputStream, String fileName, String sheetName) throws IOException;

    List<Map<String, Object>> read(InputStream inputStream, String fileName, int sheetIdx) throws IOException;

    Map<String, List<Map<String, Object>>> read(byte[] fileContent, String fileName) throws IOException;

    List<Map<String, Object>> read(byte[] fileContent, String fileName, String sheetName) throws IOException;

    List<Map<String, Object>> read(byte[] fileContent, String fileName, int sheetIdx) throws IOException;

    byte[] create(String fileName, Map<String, List<Map<String, Object>>> dataList) throws IOException;

    byte[] append(InputStream inputStream, String fileName, Map<String, List<Map<String, Object>>> dataList) throws IOException;

    byte[] append(byte[] fileContent, String fileName, Map<String, List<Map<String, Object>>> dataList) throws IOException;

}
