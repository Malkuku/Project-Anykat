package com.anyview.xiazihao.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathUtils {
    public static String normalize(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        // 移除连续的/
        path = path.replaceAll("/+", "/");

        // 移除结尾的/ (除非是整个路径就是/)
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    public static boolean match(String pattern, String path) {
        // 标准化路径
        pattern = normalize(pattern);
        path = normalize(path);

        // 完全相等
        if (pattern.equals(path)) {
            return true;
        }

        // 转换为正则表达式
        String regex = convertPatternToRegex(pattern);
        return Pattern.matches(regex, path);
    }

    /**
     * 将Ant风格模式转换为正则表达式
     */
    private static String convertPatternToRegex(String pattern) {
        StringBuilder regex = new StringBuilder();
        regex.append('^');

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '{') {
                // 处理路径变量 {var}
                int end = pattern.indexOf('}', i);
                if (end == -1) {
                    throw new IllegalArgumentException("Invalid path pattern: " + pattern);
                }
                regex.append("([^/]*)");
                i = end;
            }else{
                switch (c) {
                    case '?':
                        regex.append('.');
                        break;
                    case '*':
                        // 检查是否是**通配符
                        if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '*') {
                            regex.append(".*");
                            i++; // 跳过下一个*
                        } else {
                            regex.append("[^/]*");
                        }
                        break;
                    case '.':
                    case '$':
                    case '^':
                    case '[':
                    case ']':
                    case '(':
                    case ')':
                    case '|':
                    case '+':
                    case '\\':
                        // 转义正则特殊字符
                        regex.append('\\').append(c);
                        break;
                    default:
                        regex.append(c);
                }
            }
        }
        regex.append('$');
        return regex.toString();
    }

    /**
     * 提取路径变量
     * 例如: pattern="/users/{id}", path="/users/123" → 返回{"id": "123"}
     */
    public static Map<String, String> extractPathVariables(String pattern, String path) {
        Map<String, String> variables = new HashMap<>();
        pattern = normalize(pattern);
        path = normalize(path);

        // 将 {var} 转换为正则捕获组
        StringBuilder regex = new StringBuilder();
        regex.append('^');
        List<String> varNames = new ArrayList<>();

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '{') {
                int end = pattern.indexOf('}', i);
                if (end == -1) {
                    throw new IllegalArgumentException("Invalid path pattern: " + pattern);
                }
                String varName = pattern.substring(i + 1, end);
                varNames.add(varName);
                regex.append("([^/]*)");
                i = end;
            } else {
                // 转义特殊字符
                if (isRegexSpecialChar(c)) {
                    regex.append('\\');
                }
                regex.append(c);
            }
        }
        regex.append('$');

        Matcher matcher = Pattern.compile(regex.toString()).matcher(path);
        if (matcher.matches()) {
            for (int i = 0; i < varNames.size(); i++) {
                String varName = varNames.get(i);
                String value = matcher.group(i + 1);
                variables.put(varName, value);
            }
        }

        return variables;
    }

    private static boolean isRegexSpecialChar(char c) {
        return ".$^{[(|)*+?\\".indexOf(c) != -1;
    }

    /**
     * 从正则表达式中获取所有命名组
     */
    private static Set<String> getNamedGroups(String regex) {
        Set<String> namedGroups = new HashSet<>();
        Matcher m = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(regex);
        while (m.find()) {
            namedGroups.add(m.group(1));
        }
        return namedGroups;
    }
}
