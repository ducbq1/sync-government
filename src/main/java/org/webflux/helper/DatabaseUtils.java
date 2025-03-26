package org.webflux.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DatabaseUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabaseUtils.class);
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<?> findAll(String table, String sql) throws ClassNotFoundException {
        String moduleName = "org.thymeleaf.examples.springboot3.biglist.webflux";
        String packageName = "domain";
        var classLoader = findAllClassesUsingClassLoader(String.format("%s.%s", moduleName, packageName));
        Class<?> classDefine = Class.forName(classLoader.stream().filter(x -> x.getName().endsWith(table)).findFirst().get().getName());
        RowMapper<?> rowMapper = (rs, rowNum) -> {
            try {
                var instance = classDefine.getDeclaredConstructor().newInstance();
                Field[] fields = classDefine.getDeclaredFields();
                for (Field field : fields) {
                    Class t = field.getType();
                    if (t.getTypeName().equals(Set.class.getName())
                    || t.getTypeName().equals(List.class.getName())) {
                        continue;
                    }
                    if (t.getTypeName().equals(Long.class.getTypeName())) {
                        Object v = rs.getLong(StringUtils.camelToSnake(field.getName()));
                        field.setAccessible(true);
                        field.set(instance, v);
                    } else if (t.getTypeName().equals(Integer.class.getTypeName())) {
                        Object v = rs.getInt(StringUtils.camelToSnake(field.getName()));
                        field.setAccessible(true);
                        field.set(instance, v);
                    } else if (t.getTypeName().equals(Double.class.getTypeName())) {
                        Object v = rs.getDouble(StringUtils.camelToSnake(field.getName()));
                        field.setAccessible(true);
                        field.set(instance, v);
                    } else if (t.getTypeName().equals(Float.class.getTypeName())) {
                        Object v = rs.getFloat(StringUtils.camelToSnake(field.getName()));
                        field.setAccessible(true);
                        field.set(instance, v);
                    } else if (t.getTypeName().equals(Boolean.class.getTypeName())) {
                        Object v = rs.getInt(StringUtils.camelToSnake(field.getName())) == 1;
                        field.setAccessible(true);
                        field.set(instance, v);
                    } else if (t.getTypeName().equals(Instant.class.getTypeName())) {
                        Object v = rs.getDate(StringUtils.camelToSnake(field.getName()));
                        field.setAccessible(true);
                        field.set(instance, v);
                    } else {
                        Object v = rs.getString(StringUtils.camelToSnake(field.getName()));
                        field.setAccessible(true);
                        field.set(instance, v);
                    }
                }
                return instance;
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            }
        };
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Set<Class> findAllClassesUsingClassLoader(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(packageName.replaceAll("[.]", "/"));
        if (Objects.isNull(stream)) {
            System.out.println("Resource not found");
            return new HashSet<>();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines().filter(line -> line.endsWith(".class")).map(line -> getClass(line, packageName)).collect(Collectors.toSet());
    }

    private Class getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }

    public static void insert(JdbcTemplate jdbcTemplate, String table, List<Map<String, Object>> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            throw new IllegalArgumentException("rows is null or empty");
        }

        StringBuilder sql = new StringBuilder("insert into ").append(table).append(" (");
        Map<String, Object> firstRow = rows.get(0);
        String[] columns = firstRow.keySet().toArray(new String[firstRow.size()]);

        for (String column: columns) {
            sql.append(column).append(",");
        }

        sql.setLength(sql.length() - 1);
        sql.append(") values (");
        for (String column: columns) {
            sql.append(":").append(column).append(",");
        }

        sql.setLength(sql.length() - 1);
        sql.append(")");

        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        for (Map<String, Object> row: rows) {
            MapSqlParameterSource parameters = new MapSqlParameterSource(row);
            namedParameterJdbcTemplate.update(sql.toString(), parameters);
        }
    }
}
