package org.webflux.enumerate;

public class ScriptQuery {
    public static final String getAppName = "select name from source_config";
    public static final String getUserByName = "select id, user_name userName, password, first_name firstName, last_name lastName, image, gender from user where user_name = ?";
    public static final String getRoleByName = "select id, name, display_name displayName from role where name = ?";
    public static final String getUserIdByName = "select id from user where user_name = ?";
    public static final String getAuthByUserId = """
        select distinct name from
        (select role.name from user
        left join user_role on user.id = user_role.user_id
        left join role on role.id = user_role.role_id
        where user.id = 1 and ifnull(role.name, '') <> ''
        union
        select privilege.Name from user
        left join user_role on user.id = user_role.user_id
        left join role on role.id = user_role.role_id
        left join role_privilege on role.id = role_privilege.role_id
        left join privilege on privilege.id = role_privilege.privilege_id
        where user.id = ? and ifnull(privilege.name, '') <> '')
    """;

    public static final String getMenuByUserId = """
        select distinct menu.* from user
        left join user_role on user.id = user_role.user_id
        left join role on role.id = user_role.role_id
        left join role_privilege on role.id = role_privilege.role_id
        left join privilege on privilege.id = role_privilege.privilege_id
        left join role_menu on role.id = role_menu.role_id
        left join menu on menu.id = role_menu.menu_id
        where user.id = ? and ifnull(menu.id, '') <> '' and ifnull(menu.is_activated, 0) = 1
        order by menu.ordinal
    """;

    public static final String insertUser = """
        insert into user (user_name, password)
        values (?, ?)
    """;

    public static final String insertUserRole = """
        insert into user_role (user_id, role_id)
        values (?, ?)
    """;

    public static final String getAllCategory = """
        select * from category
    """;

    public static final String getAllAuditLog = """
        select * from audit_log
    """;

    public static final String getSingleCategory = """
        select * from category where id = ?
    """;

    public static final String getSingleAuditLog = """
        select * from audit_log where id = ?
    """;

    public static final String countAllCategory = """
        select count(*) from category
    """;

    public static final String getSystemDate = """
        select datetime('now', 'localtime')
    """;

    public static final String insertAuditLog = """
        insert into audit_log (action, content, detail, created_by, created_at, is_error)
        values (?, ?, ?, ?, ?, ?)
    """;

    public static final String insertCategory = """
        insert into category (name, type, content, description, updated_by, updated_at, created_by, created_at, is_deleted, is_activated, database_config_id)
        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    public static final String updateCategory = """
        update category
        set name = ?, type = ?, content = ?, description = ?, updated_at = ?, updated_by = ?, is_activated = ?, is_deleted = ?
        where id = ?
    """;

    public static final String deleteCategory = """
        update category
        set is_deleted = 1, updated_by = ?, updated_at = ?
        where id = ?
    """;

    public static final String deleteMultipleCategories = """
        update category
        set is_deleted = 1, updated_by = ?, updated_at = ?
    """;

    public static final String getAllSyncFlow = """
        select * from sync_flow where is_activated = 1
    """;

    public static final String getAllSyncOperator = """
        select * from sync_operator where sync_flow_id = ?
    """;

    public static final String getAllMapStructure = """
        select * from map_structure where sync_flow_id = ?
    """;

    public static final String getGetAllMapStructureDetail = """
        select * from map_structure_detail where map_structure_id in (?)
    """;

    public static final String getSingleDatabaseConfig = """
        select id, name, description, url, user_name, password, driver, port, service, is_connected, is_activated from database_config where id = ?
    """;

    public static final String getDatabaseConfig = """
        select id, url, user_name, password, driver, port, service, name from database_config
    """;

    public static final String getAllSyncFlowStatic = """
        SELECT a.*, b.name as source_name, c.name as destination_name FROM sync_flow_static a
        LEFT JOIN category b ON a.source_id = b.id
        LEFT JOIN database_config c ON a.destination_id = c.id
    """;

    public static final String getAllSyncFlowStaticConfig = """
        select sfs.id, sfs.payload, sfs.proxy, sfs.save_file_path, sfs.is_get_synced_again, c.content, c.token, dc.url, dc.port, dc.user_name, dc.password, dc.driver, dc.service from sync_flow_static sfs
        left join category c on sfs.source_id = c.id and sfs.is_activated = c.is_activated
        left join database_config dc on sfs.destination_id = dc.id and sfs.is_activated = dc.is_activated
        where sfs.is_activated = 1
    """;

    public static final String countAllSyncFlowStatic = """
        SELECT COUNT(*) FROM sync_flow_static a
        LEFT JOIN category b ON a.source_id = b.id
        LEFT JOIN database_config c ON a.destination_id = c.id
    """;

    public static final String getSingleSyncFlowStatic = """
        SELECT * FROM sync_flow_static
        WHERE id = ?
    """;

    public static final String getDestinationConfig = """
        SELECT id, name FROM database_config
    """;

    public static final String getSourceConfig = """
        SELECT id, name FROM category WHERE type = 'API_BASE'
    """;

    public static final String insertSyncFlowStatic = """
        insert into sync_flow_static (name, description, is_activated, source_id, destination_id, proxy, payload, save_file_path, is_get_synced_again)
        values (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    public static final String updateSyncFlowStatic = """
        update sync_flow_static
        set name = ?, description = ?, is_activated = ?, source_id = ?, destination_id = ?, updated_at = ?, updated_by = ?, proxy = ?, payload = ?, save_file_path = ?, is_get_synced_again = ?
        where id = ?
    """;

    public static final String updateFlowStatus = """
        update sync_flow_static
        set is_activated = ?, updated_by = ?, updated_at = ?
    """;

    public static final String getAllSource = """
        SELECT * FROM category WHERE type = 'API_BASE'
    """;

    public static final String countAllSource = """
        SELECT COUNT(*) FROM category WHERE type = 'API_BASE'
    """;

    public static final String updateSource = """
        update category
        set name = ?, type = ?, content = ?, description = ?, updated_at = ?, updated_by = ?, is_activated = ?, type = ?, token = ?
        where id = ?
    """;

    public static final String insertSource = """
        insert into category (name, type, content, description, created_by, created_at, is_activated)
        values (?, ?, ?, ?, ?, ?, ?)
    """;

    public static final String updateSourceStatus = """
        update category
        set is_activated = ?, updated_by = ?, updated_at = ?
    """;

    public static final String getAllDestination = """
        SELECT id, name, description, url, is_activated, is_connected FROM database_config
    """;

    public static final String countAllDestination = """
        SELECT COUNT(*) FROM database_config
    """;

    public static final String countAllAuditLog = """
        SELECT COUNT(*) FROM audit_log
    """;

    public static final String updateDatabaseConfig = """
        update database_config
        set name = ?, description = ?, updated_at = ?, updated_by = ?, is_activated = ?, url = ?, user_name = ?, password = ?, driver = ?, port = ?, service = ?
        where id = ?
    """;

    public static final String updateDatabaseConfigConnection = """
        update database_config
        set updated_at = ?, updated_by = ?, is_connected = ?
        where id = ?
    """;

    public static final String updateConfigStatus = """
        update database_config
        set is_activated = ?, updated_by = ?, updated_at = ?
    """;

    public static final String insertDatabaseConfig = """
        insert into database_config (name, description, url, user_name, password, driver, port, service, created_by, created_at, is_connected, is_activated, is_deleted)
        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

}