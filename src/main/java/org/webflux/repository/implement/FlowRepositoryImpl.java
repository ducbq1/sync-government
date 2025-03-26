package org.webflux.repository.implement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.webflux.domain.SyncFlow;
import org.webflux.enumerate.ScriptQuery;
import org.webflux.repository.FlowRepository;
import org.webflux.service.dto.MapStructureDTO;
import org.webflux.service.dto.MapStructureDetailDTO;
import org.webflux.service.dto.SyncOperatorDTO;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.*;

import static org.webflux.config.ClientFactory.getDataSource;
import static org.webflux.config.ClientFactory.getJdbcTemplate;

@Repository
public class FlowRepositoryImpl implements FlowRepository {
    private final JdbcTemplate jdbcTemplate;

    public FlowRepositoryImpl() throws ClassNotFoundException {
        this.jdbcTemplate = getJdbcTemplate(getDataSource("org.sqlite.JDBC", "jdbc:sqlite:chinook.sqlite", "", ""));
    }

    @Override
    public Long saveDataFlow(String dataFlow) {
        String sql = "INSERT INTO sync_flow (name, flow_data, is_activated) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, "flow 1");
            ps.setString(2, dataFlow);
            ps.setBoolean(3, true);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<SyncFlow> findAll() {
        String sqlQuery = ScriptQuery.getAllSyncFlow;
        return getSyncFlowByQuery(sqlQuery);
    }

    public List<SyncOperatorDTO> findAllSyncOperator(Long syncFlowId) {
        String sqlQuery = ScriptQuery.getAllSyncOperator;
        return jdbcTemplate.query(sqlQuery, new Object[]{syncFlowId}, new int[]{Types.INTEGER}, (rs, rowNum) -> SyncOperatorDTO.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .categoryId(rs.getLong("category_id"))
                .body(rs.getString("body"))
                .syncFlowId(rs.getLong("sync_flow_id"))
                .build());
    }

    public List<MapStructureDTO> findAllMapStructure(Long syncFlowId) {
        String sqlQuery = ScriptQuery.getAllMapStructure;
        return jdbcTemplate.query(sqlQuery, new Object[]{syncFlowId}, new int[]{Types.INTEGER}, (rs, rowNum) -> MapStructureDTO.builder()
                .id(rs.getLong("id"))
                .source(SyncOperatorDTO.builder().id(rs.getLong("source_id")).build())
                .destination(SyncOperatorDTO.builder().id(rs.getLong("destination_id")).build())
                .syncFlowId(rs.getLong("sync_flow_id"))
                .build());
    }

    public List<MapStructureDetailDTO> findAllMapStructureDetail(String mapStructureId) {
        String sqlQuery = String.format(ScriptQuery.getGetAllMapStructureDetail.replaceAll("\\?", "%s"), mapStructureId);
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> MapStructureDetailDTO.builder()
                .id(rs.getLong("id"))
                .sourceField(rs.getString("source_field"))
                .destinationField(rs.getString("destination_field"))
//                .function(rs.getString("function"))
                .mapStructureId(rs.getLong("map_structure_id"))
                .build());
    }

    public List<SyncFlow> getSyncFlowByQuery(String sqlQuery) {
        // Get list of category
        List<SyncFlow> lstSyncFLow = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> SyncFlow.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .flowData(rs.getString("flow_data"))
                .build());
        return lstSyncFLow;
    }

    @Override
    public Long saveMapStruct(MapStructureDTO mapStructureDTO) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("insert into  map_structure (source_id, destination_id, sync_flow_id) values (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setLong(1, mapStructureDTO.getSource().getId());
            ps.setLong(2, mapStructureDTO.getDestination().getId());
            ps.setLong(3, mapStructureDTO.getSyncFlowId());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Long saveMapDetail(MapStructureDetailDTO mapStructureDetailDTO) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("insert into  map_structure_detail (source_field, destination_field, map_structure_id) values (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, mapStructureDetailDTO.getSourceField());
            ps.setString(2, mapStructureDetailDTO.getDestinationField());
            ps.setLong(3, mapStructureDetailDTO.getMapStructureId());
//            ps.setString(4, mapStructureDetailDTO.getFunction());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Long saveSyncOperator(SyncOperatorDTO syncOperatorDTO) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("insert into  sync_operator (name, description, operator_data, category_id, sync_flow_id, body) values (?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, syncOperatorDTO.getName());
            ps.setString(2, syncOperatorDTO.getDescription());
            ps.setString(3, syncOperatorDTO.getOperatorData());
            ps.setLong(4, syncOperatorDTO.getCategoryId());
            ps.setLong(5, syncOperatorDTO.getSyncFlowId());
            ps.setString(6, syncOperatorDTO.getBody());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void deleteAllDataFlow() {
        String sql = "DELETE FROM sync_flow";
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            return ps;
        });
    }

    @Override
    public void deleteAllMapStruct() {
        String sql = "DELETE FROM map_structure";
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            return ps;
        });
    }

    @Override
    public void deleteAllMapDetail() {
        String sql = "DELETE FROM map_structure_detail";
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            return ps;
        });
    }

    @Override
    public void deleteAllSyncOperator() {
        String sql = "DELETE FROM sync_operator";
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            return ps;
        });
    }
}
