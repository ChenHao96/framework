package org.steven.chen.utils.jdbc;

import org.steven.chen.utils.CommonsUtil;

import java.sql.*;
import java.util.*;

public class JDBC {

    private Connection connection = null;

    public JDBC(String driverClass, String url, String userName, String password) {

        try {
            Class.forName(driverClass);
            connection = DriverManager.getConnection(url, userName, password);
            connection.setAutoCommit(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> querySingleton(String sql, Object... params) {

        List<Map<String, Object>> result = query(sql, params);
        if (result == null || result.size() == 0) return null;
        if (result.size() > 1) {
            throw new SqlManyResultException(String.format("Many Result size:%d", result.size()));
        }

        return result.get(0);
    }

    public List<Map<String, Object>> query(String sql, Object... params) {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = addPsParams(connection, sql, params);
            resultSet = preparedStatement.executeQuery();
            return getResultMaps(resultSet);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CommonsUtil.safeClose(resultSet, preparedStatement);
        }

        return null;
    }

    private List<Map<String, Object>> getResultMaps(ResultSet resultSet) throws SQLException {

        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        Set<String> columnNames = new HashSet<>(columnCount);
        for (int i = 0; i < columnCount; i++) {
            columnNames.add(resultSetMetaData.getColumnName(i + 1));
        }
        int listSize = resultSet.getFetchSize();
        List<Map<String, Object>> result = new ArrayList<>(listSize);

        while (resultSet.next()) {
            Map<String, Object> vo = new HashMap<>(columnCount);
            for (String column : columnNames) {
                vo.put(column, resultSet.getObject(column));
            }
            result.add(vo);
        }

        return result;
    }

    public int update(String sql, Object... params) {

        int result = 0;

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = addPsParams(connection, sql, params);
            result = preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CommonsUtil.safeClose(preparedStatement);
        }

        return result;
    }

    public int prepareCallUpdate(String sql, CallableParam... params) {

        int result = 0;
        CallableStatement callableStatement = null;

        try {
            callableStatement = addPsParams4Call(connection, sql, params);
            result = callableStatement.executeUpdate();
            returnPsParams4Call(callableStatement, params);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CommonsUtil.safeClose(callableStatement);
        }

        return result;
    }

    public List<List<Map<String, Object>>> prepareCallQuery(String sql, CallableParam... params) {

        List<List<Map<String, Object>>> result = null;
        CallableStatement callableStatement = null;

        try {
            callableStatement = addPsParams4Call(connection, sql, params);
            if (callableStatement.execute()) {
                result = new ArrayList<>(6);
                do {
                    ResultSet resultSet = callableStatement.getResultSet();
                    result.add(getResultMaps(resultSet));
                } while (callableStatement.getMoreResults());

                returnPsParams4Call(callableStatement, params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CommonsUtil.safeClose(callableStatement);
        }

        return result;
    }

    private void returnPsParams4Call(CallableStatement callableStatement, CallableParam... params) {

        if (callableStatement == null) return;
        if (params == null || params.length == 0) return;

        for (int i = 0; i < params.length; i++) {
            try {
                params[i].returnValue = callableStatement.getObject(i + 1);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static CallableStatement addPsParams4Call(Connection connection, String sql, CallableParam... params) throws SQLException {

        CallableStatement callableStatement = connection.prepareCall(sql);

        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                switch (params[i].isInOrOutAndInOut) {

                    case CallableParam.IN_PARAM:
                        callableStatement.setObject(i + 1, params[i].value);
                        break;

                    case CallableParam.IN_OUT_PARAM:
                        callableStatement.setObject(i + 1, params[i].value);

                    case CallableParam.OUT_PARAM:
                        callableStatement.registerOutParameter(i + 1, params[i].type);
                        break;
                }
            }
        }

        return callableStatement;
    }


    private static PreparedStatement addPsParams(Connection connection, String sql, Object... params) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
        }

        return preparedStatement;
    }

    public void commit() {
        try {
            if (connection != null) {
                connection.commit();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback() {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        CommonsUtil.safeClose(connection);
    }
}
