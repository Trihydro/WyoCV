package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTimHolding;
import com.trihydro.library.model.Coordinate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActiveTimHoldingService extends BaseService {
    Utility utility;

    @Autowired
    public ActiveTimHoldingService(Utility _utility) {
        utility = _utility;
    }

    public ActiveTimHolding getRsuActiveTimHolding(String clientId, String direction, String ipv4Address) {
        ActiveTimHolding activeTimHolding = null;
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            connection = dbInteractions.getConnectionPool();
            statement = connection.createStatement();
            String query = "select * from active_tim_holding";
            query += " where rsu_target = '" + ipv4Address;
            if (clientId != null) {
                query += "' and client_id = '" + clientId + "'";
            } else {
                query += "' and client_id is null";
            }
            query += " and direction = '" + direction + "'";

            rs = statement.executeQuery(query);

            // convert to ActiveTim object
            while (rs.next()) {
                activeTimHolding = new ActiveTimHolding();
                activeTimHolding.setActiveTimHoldingId(rs.getLong("ACTIVE_TIM_HOLDING_ID"));
                activeTimHolding.setClientId(rs.getString("CLIENT_ID"));
                activeTimHolding.setDirection(rs.getString("DIRECTION"));
                activeTimHolding.setRsuTargetId(rs.getString("RSU_TARGET"));
                activeTimHolding.setSatRecordId(rs.getString("SAT_RECORD_ID"));
                activeTimHolding.setStartPoint(
                        new Coordinate(rs.getBigDecimal("START_LATITUDE"), rs.getBigDecimal("START_LONGITUDE")));
                activeTimHolding.setEndPoint(
                        new Coordinate(rs.getBigDecimal("END_LATITUDE"), rs.getBigDecimal("END_LONGITUDE")));

                int projectKey = rs.getInt("PROJECT_KEY");
                if (!rs.wasNull()) {
                    activeTimHolding.setProjectKey(projectKey);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // close prepared statement
                if (statement != null)
                    statement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
                // close result set
                if (rs != null)
                    rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return activeTimHolding;
    }

    public ActiveTimHolding getSdxActiveTimHolding(String clientId, String direction, String satRecordId) {
        ActiveTimHolding activeTimHolding = null;
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            connection = dbInteractions.getConnectionPool();
            statement = connection.createStatement();
            String query = "select * from active_tim_holding";
            query += " where sat_record_id = '" + satRecordId;
            if (clientId != null) {
                query += "' and client_id = '" + clientId + "'";
            } else {
                query += "' and client_id is null";
            }
            query += " and direction = '" + direction + "'";

            rs = statement.executeQuery(query);

            // convert to ActiveTim object
            while (rs.next()) {
                activeTimHolding = new ActiveTimHolding();
                activeTimHolding.setActiveTimHoldingId(rs.getLong("ACTIVE_TIM_HOLDING_ID"));
                activeTimHolding.setClientId(rs.getString("CLIENT_ID"));
                activeTimHolding.setDirection(rs.getString("DIRECTION"));
                activeTimHolding.setRsuTargetId(rs.getString("RSU_TARGET"));
                activeTimHolding.setSatRecordId(rs.getString("SAT_RECORD_ID"));
                activeTimHolding.setStartPoint(
                        new Coordinate(rs.getBigDecimal("START_LATITUDE"), rs.getBigDecimal("START_LONGITUDE")));
                activeTimHolding.setEndPoint(
                        new Coordinate(rs.getBigDecimal("END_LATITUDE"), rs.getBigDecimal("END_LONGITUDE")));

                int projectKey = rs.getInt("PROJECT_KEY");
                if (!rs.wasNull()) {
                    activeTimHolding.setProjectKey(projectKey);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // close prepared statement
                if (statement != null)
                    statement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
                // close result set
                if (rs != null)
                    rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return activeTimHolding;
    }

    public Boolean deleteActiveTimHolding(Long activeTimHoldingId) {
        if (activeTimHoldingId == null || activeTimHoldingId < 0) {
            // if we don't have a valid pk, we can't delete
            return false;
        }

        String updateTableSQL = "DELETE FROM ACTIVE_TIM_HOLDING WHERE ACTIVE_TIM_HOLDING_ID = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(updateTableSQL);
            preparedStatement.setLong(1, activeTimHoldingId);
            var success = dbInteractions.updateOrDelete(preparedStatement);
            if (success) {
                utility.logWithDate("Deleted ACTIVE_TIM_HOLDING with ID: " + activeTimHoldingId);
            } else {
                utility.logWithDate("Failed to delete ACTIVE_TIM_HOLDING with ID: " + activeTimHoldingId);
            }
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                // close prepared statement
                if (preparedStatement != null)
                    preparedStatement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}