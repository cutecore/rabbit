package com.cutec.utils;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ToolBox {

    public static String getFileName(String path) {
        if (org.springframework.util.StringUtils.hasText(path)) {
            return Paths.get(path).getFileName().toString();
        }
        return null;
    }

    public static String getFileNameWithoutExtension(String path) {
        String fileName = getFileName(path);
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(0, fileName.lastIndexOf('.'));
        }
        return fileName;
    }

    public static boolean isNumeric(String str) {
        return str != null && str.matches("-?\\d+(\\.\\d+)?");
    }

    public static String getNumberByFileName(String title) {
        String number = title.substring(7, 14);
        if (isNumeric(number)) {
            return number;
        }
        number = title.substring(7, 13);
        if (isNumeric(number)) {
            return number;
        }
        return null;
    }

    public static String getNumber(String title) {
        String number = "";
        if (title.indexOf("【") > 0) {
            number = title.split("【")[0];
        } else {
            number = title.split(" ")[0];
        }
        number = number.toUpperCase().replace("FC2PPV-", "");
        if (number.length() > 7) {
            number = number.substring(0, 7);
        }
        return number;
    }

    public static Integer getNumberReg(String text) {
        String number = null;
        if (StringUtils.isBlank(text)) {
            return null;
        }
        text = text.toLowerCase();
        if (text.contains("musume")) {
            return null;
        }
        if (text.contains("pacopacomama")) {
            return null;
        }
        if (text.contains("h4610")) {
            return null;
        }
        if (text.contains("h0930")) {
            return null;
        }
        if (text.contains("c0930")) {
            return null;
        }
        if (text.contains("caribbeancom")) {
            return null;
        }
        if (text.contains("carib")) {
            return null;
        }
        if (text.contains("1pondo")) {
            return null;
        }
//        String pattern = "\\b\\d{6,7}\\b"; // 匹配6到7位数字
        String pattern = "\\d{6,7}"; // 匹配6到7位数字
        // 编译正则表达式模式
        Pattern regex = Pattern.compile(pattern);
        // 创建匹配器对象
        Matcher matcher = regex.matcher(text);
        // 查找匹配的数字并输出
        if (matcher.find()) {
            number = matcher.group();
        }
        if (number == null) {
            return null;
        } else {
            return Integer.valueOf(number);
        }
    }

    public static boolean containsJapanese(String text) {
        // 匹配日语字符的正则表达式
        String regex = "[\\p{InHiragana}\\p{InKatakana}\\p{InCJKUnifiedIdeographs}]";
        return text.matches(".*" + regex + ".*");
    }

    public static boolean containsChinese(String text) {
        // 匹配中文字符的正则表达式
        String regex = "[\\u4e00-\\u9fa5]";
        return text.matches(".*" + regex + ".*");
    }

    // 正则表达式用于检测常见的SQL注入模式
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(?:'|\"|;|--|\\b(SELECT|UPDATE|DELETE|INSERT|DROP|ALTER|UNION|EXEC|SCRIPT)\\b)"
    );

    /**
     * 检查字符串是否包含潜在的SQL注入风险。
     *
     * @param input 要检查的字符串
     * @return 如果输入安全返回true，否则返回false
     */
    public static boolean isInputSafe(String input) {
        if (input == null || input.isEmpty()) {
            return true;
        }
        return !SQL_INJECTION_PATTERN.matcher(input).find();
    }

    public static String getIpAddr(HttpServletRequest request) {
        String ipAddress = null;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (ipAddress.equals("127.0.0.1")) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        log.info("{}", e.getMessage());
                    }
                    if (inet != null) {
                        ipAddress = inet.getHostAddress();
                    }
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
                // = 15
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "";
        }
        return ipAddress;
    }

}