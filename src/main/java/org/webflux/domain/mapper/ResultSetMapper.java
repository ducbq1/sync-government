package org.webflux.domain.mapper;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ResultSetMapper {

    public static RowMapper<Map<String, Object>> mapRowToMap() {
        return (rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = rs.getMetaData().getColumnName(i);
                if (Types.BLOB == metaData.getColumnType(i)) {
                    row.put(columnName, Base64.getEncoder().encodeToString(rs.getBytes(i)));
                } else {
                    row.put(columnName, rs.getObject(i));
                }
            }
            return row;
        };
    }
}
