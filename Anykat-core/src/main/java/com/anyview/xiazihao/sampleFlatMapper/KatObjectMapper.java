package com.anyview.xiazihao.sampleFlatMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface KatObjectMapper {
    <T> List<T> map(ResultSet rs, Class<T> targetClass) throws SQLException;
}
