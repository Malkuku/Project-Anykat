package com.anyview.xiazihao.utils;

import com.anyview.xiazihao.entity.result.Result;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ServletUtils {
    // 封装返回方法
    public static void sendResponse(HttpServletResponse resp, Result result) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.print(JsonUtils.toJson(result));
        log.info("返回结果:{}", JsonUtils.toJson(result));
        out.flush();
    }
    // 封装获取请求体方法
    public static String getRequestBody(HttpServletRequest request) throws IOException {
        request.setCharacterEncoding("UTF-8");
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }
    // 封装将请求头参数转成List的方法
    public static <T> List<T> parseStringToList(String value, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        if (value != null) {
            String[] idArray = value.split(",");
            for (String id_str : idArray) {
                log.debug("处理ID:{}", id_str);
                try {
                    if (clazz == Integer.class) {
                        list.add(clazz.cast(Integer.parseInt(id_str)));
                    } else {
                        log.error("Unsupported class type: {}", clazz);
                    }
                } catch (NumberFormatException e) {
                    log.error("ID格式错误:{}", id_str, e);
                }
            }
        }
        return list;
    }

    //TODO实现一个RequestParamBinder类？？
}
