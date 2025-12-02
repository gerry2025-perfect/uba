package com.iwhalecloud.bss.uba.file.excel;

import com.iwhalecloud.bss.uba.adapter.log.UbaLogger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**通过POI操作Excel文件*/
public class PoiExcelOperator implements IFileOperator {

    private static final UbaLogger logger = UbaLogger.getLogger(PoiExcelOperator.class);

    /**
     * 从excel文件中读取信息
     * @param fileName 文件名称
     * @param inputStream 文件内容
     * @return 返回解析之后的文件内容，包含每个sheet页的数据：
     * key为sheet页名称，
     * value为对应sheet表格中的内容，每一行的数据用map表达，默认第一行为表头，所以从第一行中解析key清单，从第二行开始解析出表格内容，用map来存储每一行的数据
     * */
    @Override
    public Map<String, List<Map<String,Object>>> read(InputStream inputStream, String fileName) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is require");
        }
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("fileName is require");
        }

        Workbook workbook = null;
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();

        try {
            workbook = getWorkbook(fileName, inputStream);
            for (Sheet sheet : workbook) {
                result.put(sheet.getSheetName(), readSheet(sheet));
            }
        } finally {
            closeResource(workbook, null, inputStream);
        }
        return result;
    }

    @Override
    public Map<String, List<Map<String, Object>>> read(byte[] fileContent, String fileName) throws IOException {
        return read(new ByteArrayInputStream(fileContent), fileName);
    }

    /**
     * 从excel文件中读取指定sheet名称的信息
     * @param inputStream 文件流
     * @param fileName 文件名
     * @param sheetName sheet名称
     * @return 返回指定sheet的内容
     * @throws IOException
     */
    @Override
    public List<Map<String, Object>> read(InputStream inputStream, String fileName, String sheetName) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is require");
        }
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("fileName is require");
        }
        if (sheetName == null || sheetName.isEmpty()) {
            throw new IllegalArgumentException("sheetName is require");
        }

        Workbook workbook = null;
        try {
            workbook = getWorkbook(fileName, inputStream);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                return Collections.emptyList();
            }
            return readSheet(sheet);
        } finally {
            closeResource(workbook, null, inputStream);
        }
    }

    @Override
    public List<Map<String, Object>> read(byte[] fileContent, String fileName, String sheetName) throws IOException {
        return read(new ByteArrayInputStream(fileContent), fileName, sheetName);
    }

    /**
     * 从excel文件中读取指定sheet序列号的信息
     * @param inputStream 文件流
     * @param fileName 文件名
     * @param sheetIdx sheet序列号 (0-based)
     * @return 返回指定sheet的内容
     * @throws IOException
     */
    @Override
    public List<Map<String, Object>> read(InputStream inputStream, String fileName, int sheetIdx) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is require");
        }
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("fileName is require");
        }
        if (sheetIdx < 0) {
            throw new IllegalArgumentException("sheetIdx cannot be negative");
        }

        Workbook workbook = null;
        try {
            workbook = getWorkbook(fileName, inputStream);
            if (sheetIdx >= workbook.getNumberOfSheets()) {
                throw new IllegalArgumentException("Sheet index (" + sheetIdx + ") is out of bounds (" + workbook.getNumberOfSheets() + ")");
            }
            Sheet sheet = workbook.getSheetAt(sheetIdx);
            return readSheet(sheet);
        } finally {
            closeResource(workbook, null, inputStream);
        }
    }

    @Override
    public List<Map<String, Object>> read(byte[] fileContent, String fileName, int sheetIdx) throws IOException {
        return read(new ByteArrayInputStream(fileContent), fileName, sheetIdx);
    }

    private List<Map<String, Object>> readSheet(Sheet sheet) {
        List<Map<String, Object>> sheetData = new ArrayList<>();
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return sheetData; // Return empty list for sheets without a header
        }

        List<Object> headers = getRow(headerRow);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            Map<String, Object> rowData = new LinkedHashMap<>();
            for (int j = 0; j < headers.size(); j++) {
                Object header = headers.get(j);
                if (header == null || header.toString().isEmpty()) {
                    continue;
                }
                Cell cell = row.getCell(j);
                rowData.put(String.valueOf(header), cell == null ? "" : getValue(cell));
            }
            sheetData.add(rowData);
        }
        return sheetData;
    }

    /**向文件中追加信息*/
    @Override
    public byte[] append(InputStream inputStream, String fileName, Map<String, List<Map<String,Object>>> dataList) throws IOException {
        // 校验输入参数
        if (dataList == null || dataList.isEmpty()) {
            throw new IllegalArgumentException("dataList is require");
        }
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is require");
        }
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("fileName is require");
        }

        Workbook workbook = null;
        ByteArrayOutputStream outputStream = null;

        try {

            workbook = getWorkbook(fileName, inputStream);
            Workbook finalWorkbook = workbook;
            dataList.forEach((key, value) -> {
                // 根据sheet名称获取工作表，如果不存在就新增
                Sheet sheet = finalWorkbook.getSheet(key);
                if (sheet == null) {
                    sheet = finalWorkbook.createSheet(key);
                }
                writeToSheet(finalWorkbook, sheet, value);
            });

            // 将Workbook写入ByteArrayOutputStream
            outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            outputStream.flush();

            return outputStream.toByteArray();

        } finally {
            closeResource(workbook, outputStream, inputStream);
        }
    }

    @Override
    public byte[] append(byte[] fileContent, String fileName, Map<String, List<Map<String, Object>>> dataList) throws IOException {
        return append(new ByteArrayInputStream(fileContent), fileName, dataList);
    }

    /**生成文件*/
    @Override
    public byte[] create(String fileName, Map<String, List<Map<String,Object>>> dataList) throws IOException {
        // 校验输入参数
        if (dataList == null || dataList.isEmpty()) {
            throw new IllegalArgumentException("dataList is require");
        }
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("fileName is require");
        }

        Workbook workbook = null;
        ByteArrayOutputStream outputStream = null;

        try {
            // 根据文件名创建对应的Workbook
            workbook = createWorkbook(fileName);

            // 遍历数据，创建sheet和数据
            for (Map.Entry<String, List<Map<String, Object>>> entry : dataList.entrySet()) {
                createSheet(workbook, entry.getKey(), entry.getValue());
            }

            // 将Workbook写入ByteArrayOutputStream
            outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            outputStream.flush();

            return outputStream.toByteArray();

        } finally {
            closeResource(workbook, outputStream, null);
        }
    }

    private void createSheet(Workbook workbook, String sheetName, List<Map<String, Object>> sheetData) {
        Sheet sheet = workbook.createSheet(sheetName);

        if (sheetData == null || sheetData.isEmpty()) {
            return; // just create an empty sheet
        }

        // 从第一行数据中获取表头
        List<String> headers = new ArrayList<>(sheetData.get(0).keySet());

        // 创建表头行
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }

        // 创建数据行
        CellStyle dataStyle = workbook.createCellStyle();
        int rowNum = 1;
        for (Map<String, Object> dataMap : sheetData) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                Object value = dataMap.get(header);
                Cell cell = row.createCell(i);
                setValue(cell, value);
                cell.setCellStyle(dataStyle);
            }
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private Workbook createWorkbook(String fileName) {
        if (fileName.endsWith(".xls")) {
            return new HSSFWorkbook();
        } else if (fileName.endsWith(".xlsx")) {
            return new XSSFWorkbook();
        } else {
            throw new IllegalArgumentException("file format not supported, must be xls or xlsx");
        }
    }

    private Workbook getWorkbook(String fileName, InputStream inputStream) throws IOException {
        // 根据文件名判断Excel版本并创建对应的Workbook
        if (fileName.endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        } else if (fileName.endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("file format not supported, must be xls or xlsx");
        }
    }

    private void closeResource(Workbook workbook, OutputStream outputStream, InputStream inputStream){
        // 关闭资源
        if (workbook != null) {
            try {
                workbook.close();
            } catch (IOException e) {
                // 记录关闭异常，但不影响主流程
                logger.warn("close workbook", e);
            }
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                logger.warn("close outputStream error", e);
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.warn("close inputSteam error", e);
            }
        }
    }

    private void writeToSheet(Workbook workbook, Sheet sheet, List<Map<String,Object>> rowData){
        // 读取表头（第一行）
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalStateException("can not found head row in current file");
        }

        // 获取表头信息
        List<Object> headers = getRow(headerRow);
        if (headers.isEmpty()) {
            throw new IllegalStateException("head row is empty, please check file content");
        }

        // 获取当前最后一行的索引，新行将从其后开始添加
        int lastRowNum = sheet.getLastRowNum();

        // 创建单元格样式（与表头保持一致）
        CellStyle cellStyle = createCellStyle(workbook, headerRow);

        // 遍历数据列表，追加到Excel中
        for (Map<String, Object> dataMap : rowData) {
            lastRowNum++;
            Row row = sheet.createRow(lastRowNum);

            // 按表头顺序添加数据
            for (int i = 0; i < headers.size(); i++) {
                Object header = headers.get(i);
                Cell cell = row.createCell(i);
                cell.setCellStyle(cellStyle);
                // 设置单元格值，如果Map中没有对应key则设为空
                Object value = dataMap.getOrDefault(String.valueOf(header), "");
                setValue(cell, value);
            }
        }
    }

    /**
     * 获取一行信息，放到list中，按照顺序放置
     */
    private List<Object> getRow(Row row) {
        List<Object> rowData = new ArrayList<>();
        int lastCellNum = row.getLastCellNum();
        for (int i = 0; i < lastCellNum; i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                rowData.add(getValue(cell));
            } else {
                rowData.add("");
            }
        }
        return rowData;
    }

    /**从单元格中获取值*/
    private Object getValue(Cell cell) {
        switch (cell.getCellType()) {
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            case STRING:
                return cell.getStringCellValue();
            case FORMULA:
                // Attempt to evaluate the formula and return the cached result.
                switch (cell.getCachedFormulaResultType()) {
                    case NUMERIC:
                        return cell.getNumericCellValue();
                    case STRING:
                        return cell.getStringCellValue();
                    case BOOLEAN:
                        return cell.getBooleanCellValue();
                    case ERROR:
                        return FormulaError.forInt(cell.getErrorCellValue()).getString();
                    default:
                        return ""; // Or handle as an error
                }
            case BLANK:
                return "";
            case ERROR:
                return FormulaError.forInt(cell.getErrorCellValue()).getString();
            default:
                return "";
        }
    }

    /**
     * 创建单元格样式，尽量与表头样式保持一致
     */
    private CellStyle createCellStyle(Workbook workbook, Row headerRow) {
        CellStyle cellStyle = workbook.createCellStyle();

        // 如果表头有样式，复制表头的样式
        if (headerRow.getLastCellNum() > 0) {
            Cell headerCell = headerRow.getCell(0);
            if (headerCell != null) {
                CellStyle headerStyle = headerCell.getCellStyle();
                if (headerStyle != null) {
                    cellStyle.cloneStyleFrom(headerStyle);
                }
            }
        }
        // 如果没有样式，设置默认样式
        if (cellStyle.getFontIndex() == 0) {
            Font font = workbook.createFont();
            cellStyle.setFont(font);
        }
        return cellStyle;
    }

    /**设置单元格的值*/
    private void setValue(Cell cell, Object value){
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if (value instanceof Calendar) {
            cell.setCellValue((Calendar) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof RichTextString) {
            cell.setCellValue((RichTextString) value);
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

}
