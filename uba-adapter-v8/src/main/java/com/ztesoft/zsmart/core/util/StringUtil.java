package com.ztesoft.zsmart.core.util;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;

public class StringUtil {


    /**
     * 占位符格式化
     *
     * @param format 格式
     * @param args   参数
     * @return String
     */
    public static String format(String format, Object... args) {
        if (StringUtils.isEmpty(format)) {
            return null;
        }
        return String.format(format.replace("%", "%%").replace("{}", "%s"), args);
    }

    /**
     * split操作。
     *
     * @param line      String
     * @param separator String
     * @return String[]
     */
    public static final String[] split(String line, String separator) {
        LinkedList<String> list = new LinkedList<String>();
        if (line != null) {
            int start = 0;
            int end = 0;
            int separatorLen = separator.length();
            while ((end = line.indexOf(separator, start)) >= 0) {
                list.add(line.substring(start, end));
                start = end + separatorLen;
            }
            if (start < line.length()) {
                list.add(line.substring(start, line.length()));
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Value of string.
     *
     * @param obj the obj
     * @return the string
     */
    public static String valueOf(Object obj) {
        return (obj == null) ? null : obj.toString();
    }

    public static boolean isEmpty(CharSequence charSequence){
        charSequence = charSequence==null?"":charSequence;
        return charSequence.length()==0;
    }

}
