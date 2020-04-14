package com.trihydro.loggerkafkaconsumer.app.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.ArrayList;

import com.trihydro.library.model.WydotRsu;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RsuServiceTest extends TestBase<RsuService> {

    @Test
    public void getRsus_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ArrayList<WydotRsu> data = uut.getRsus();

        // Assert
        assertEquals(1, data.size());
        verify(mockStatement).executeQuery(
                "select * from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid order by milepost asc");
        verify(mockRs).getInt("RSU_ID");
        verify(mockRs).getString("IPV4_ADDRESS");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }

    @Test
    public void getRsus_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockRs).getInt("RSU_ID");
        // Act
        ArrayList<WydotRsu> data = uut.getRsus();

        // Assert
        assertEquals(0, data.size());
        verify(mockStatement).executeQuery(
                "select * from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid order by milepost asc");
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }
}