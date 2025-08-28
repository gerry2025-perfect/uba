package com.iwhalecloud.bss.uba.file.excel;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CsvOperator implements IFileOperator {

    @Override
    public Map<String, List<Map<String, Object>>> read(InputStream inputStream, String fileName) throws IOException {
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        result.put(getSheetName(fileName), readSheet(inputStream));
        return result;
    }

    @Override
    public List<Map<String, Object>> read(InputStream inputStream, String fileName, String sheetName) throws IOException {
        if (getSheetName(fileName).equals(sheetName)) {
            return readSheet(inputStream);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> read(InputStream inputStream, String fileName, int sheetIdx) throws IOException {
        if (sheetIdx == 0) {
            return readSheet(inputStream);
        }
        throw new IllegalArgumentException("CSV files only have one sheet");
    }

    @Override
    public Map<String, List<Map<String, Object>>> read(byte[] fileContent, String fileName) throws IOException {
        return read(new ByteArrayInputStream(fileContent), fileName);
    }

    @Override
    public List<Map<String, Object>> read(byte[] fileContent, String fileName, String sheetName) throws IOException {
        return read(new ByteArrayInputStream(fileContent), fileName, sheetName);
    }

    @Override
    public List<Map<String, Object>> read(byte[] fileContent, String fileName, int sheetIdx) throws IOException {
        return read(new ByteArrayInputStream(fileContent), fileName, sheetIdx);
    }

    private List<Map<String, Object>> readSheet(InputStream inputStream) throws IOException {
        List<Map<String, Object>> sheetData = new ArrayList<>();
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            // Use older API to be compatible with the runtime library
            CSVFormat format = CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord();
            try (CSVParser csvParser = new CSVParser(reader, format)) {
                for (CSVRecord csvRecord : csvParser) {
                    sheetData.add(new LinkedHashMap<>(csvRecord.toMap()));
                }
            }
        }
        return sheetData;
    }

    @Override
    public byte[] create(String fileName, Map<String, List<Map<String, Object>>> dataList) throws IOException {
        if (dataList == null || dataList.isEmpty()) {
            return new byte[0];
        }

        // For CSV, we only consider the first sheet in the dataList
        List<Map<String, Object>> data = dataList.values().iterator().next();

        if (data == null || data.isEmpty()) {
            return new byte[0];
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            String[] headers = data.get(0).keySet().toArray(new String[0]);

            // Use older API to be compatible with the runtime library
            CSVFormat format = CSVFormat.DEFAULT.withHeader(headers);

            try (CSVPrinter csvPrinter = new CSVPrinter(writer, format)) {
                for (Map<String, Object> row : data) {
                    List<Object> values = new ArrayList<>();
                    for (String header : headers) {
                        values.add(row.get(header));
                    }
                    csvPrinter.printRecord(values);
                }
            }
        }
        return out.toByteArray();
    }

    @Override
    public byte[] append(InputStream inputStream, String fileName, Map<String, List<Map<String, Object>>> dataList) throws IOException {
        // This is a simplistic implementation. A more robust solution would need to handle mismatched headers.
        List<Map<String, Object>> existingData = readSheet(inputStream);
        if (dataList != null && !dataList.isEmpty()) {
            existingData.addAll(dataList.values().iterator().next());
        }

        Map<String, List<Map<String, Object>>> combinedData = new HashMap<>();
        combinedData.put(getSheetName(fileName), existingData);

        return create(fileName, combinedData);
    }

    @Override
    public byte[] append(byte[] fileContent, String fileName, Map<String, List<Map<String, Object>>> dataList) throws IOException {
        return append(new ByteArrayInputStream(fileContent), fileName, dataList);
    }

    private String getSheetName(String fileName) {
        if (fileName != null && fileName.lastIndexOf('.') > 0) {
            return fileName.substring(0, fileName.lastIndexOf('.'));
        }
        return "Sheet1"; // Default sheet name
    }
}
