package com.trihydro.loggerkafkaconsumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.tables.TimOracleTables;
import com.trihydro.loggerkafkaconsumer.app.services.ActiveTimService;
import com.trihydro.loggerkafkaconsumer.app.services.TestBase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

public class ActiveTimServiceTest extends TestBase<ActiveTimService> {

    // public Long insertActiveTim(ActiveTim activeTim) {
    // public boolean updateActiveTim(ActiveTim activeTim) {
    // public ActiveTim getActiveSatTim(String satRecordId, String direction) {
    // public ActiveTim getActiveRsuTim(String clientId, String direction, String
    // ipv4Address) {
    @Spy
    private TimOracleTables mockTimOracleTables = new TimOracleTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    @Before
    public void setupSubTest() {
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
        // doReturn("").when(mockTimOracleTables).buildInsertQueryStatement(isA(String.class),
        // table)
        // doReturn(mockPreparedStatement).when(mockTimOracleTables).buildUpdateStatement(any(),
        // any(), any(), any(),
        // any());
    }

    @Test
    public void insertActiveTim_SUCCESS() throws SQLException {
        // Arrange
        ActiveTim activeTim = new ActiveTim();
        activeTim.setStartDateTime("2020-02-03T16:00:00.000Z");
        activeTim.setEndDateTime("2020-02-03T16:00:00.000Z");
        doReturn("").when(mockTimOracleTables).buildInsertQueryStatement(any(), any());
        doReturn(-1l).when(uut).log(mockPreparedStatement, "active tim");

        // Act
        Long data = uut.insertActiveTim(activeTim);

        // Assert
        assertEquals(new Long(-1), data);
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, activeTim.getTimId());// TIM_ID
        verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 2, activeTim.getMilepostStart());// MILEPOST_START
        verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 3, activeTim.getMilepostStop());// MILEPOST_STOP
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 4, activeTim.getDirection());// DIRECTION
        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 5, java.sql.Timestamp// TIM_START
                .valueOf(LocalDateTime.parse(activeTim.getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME)));
        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 6, java.sql.Timestamp// TIM_END
                .valueOf(LocalDateTime.parse(activeTim.getEndDateTime(), DateTimeFormatter.ISO_DATE_TIME)));
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 7, activeTim.getTimTypeId());// TIM_TYPE_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 8, activeTim.getRoute());// ROUTE
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 9, activeTim.getClientId());// CLIENT_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 10, activeTim.getSatRecordId());// SAT_RECORD_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 11, activeTim.getPk());// PK
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void insertActiveTim_FAIL() throws SQLException {
        // Arrange
        ActiveTim activeTim = new ActiveTim();
        activeTim.setStartDateTime("2020-02-03T16:00:00.000Z");
        activeTim.setEndDateTime("2020-02-03T16:00:00.000Z");
        doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, (Long) null);

        // Act
        Long data = uut.insertActiveTim(activeTim);

        // Assert
        assertEquals(new Long(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void updateActiveTim_SUCCESS() throws SQLException{
        // Arrange
        doReturn(true).when(uut).updateOrDelete(mockPreparedStatement);
        ActiveTim activeTim = new ActiveTim();
        activeTim.setActiveTimId(-1l);
        activeTim.setStartDateTime("2020-02-03T16:00:00.000Z");
        activeTim.setEndDateTime("2020-02-03T16:00:00.000Z");

        // Act
        boolean data = uut.updateActiveTim(activeTim);

        // Assert
        assertTrue("Failed to update activeTim", data);
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, activeTim.getTimId());
        verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 2, activeTim.getMilepostStart());
        verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 3, activeTim.getMilepostStop());
        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 4, java.sql.Timestamp
                .valueOf(LocalDateTime.parse(activeTim.getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME)));
        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 5, java.sql.Timestamp
                .valueOf(LocalDateTime.parse(activeTim.getEndDateTime(), DateTimeFormatter.ISO_DATE_TIME)));
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 6, activeTim.getPk());
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 7, activeTim.getActiveTimId());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void updateActiveTim_FAIL() throws SQLException{
       // Arrange
       doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, (Long) null);
       ActiveTim activeTim = new ActiveTim();
       activeTim.setActiveTimId(-1l);
       activeTim.setStartDateTime("2020-02-03T16:00:00.000Z");
       activeTim.setEndDateTime("2020-02-03T16:00:00.000Z");

       // Act
       boolean data = uut.updateActiveTim(activeTim);

       // Assert
       assertFalse("Success reported on failed update activeTim", data);
       verify(mockPreparedStatement).close();
       verify(mockConnection).close();
    }
}