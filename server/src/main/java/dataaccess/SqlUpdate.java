package dataaccess;

import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class SqlUpdate {
    public void executeUpdate(String statement, Object... params) throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            try(var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for(int i = 0; i < params.length; i++) {
                    var param = params[i];
                    if(param instanceof String p) {ps.setString(i + 1, p);}
                    if(param == null) {ps.setString(i + 1, null);}
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if(rs.next()) {
                    rs.getInt(1);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("unable to read data: %s", e.getMessage()));
        }
    }

    public void configureDatabase(String[] createStatements) throws DataAccessException {
        DatabaseManager.createDatabase();
        try(var conn = DatabaseManager.getConnection()) {
            for(var statement : createStatements) {
                try(var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
