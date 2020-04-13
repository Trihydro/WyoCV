package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.TimRsu;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class TimRsuServiceTest extends BaseServiceTest {
    @Mock
    protected ResponseEntity<TimRsu[]> mockResponseEntityTimRsuArray;
    @Mock
    protected ResponseEntity<TimRsu> mockResponseEntityTimRsu;
    @Mock
    protected CVRestServiceProps cVRestServiceProps;

    @InjectMocks
    private TimRsuService uut;

    @Test
    public void getTimRsusByTimId() {
        // Arrange
        Long timId = -1l;
        TimRsu[] timRsus = new TimRsu[1];
        TimRsu timRsu = new TimRsu();
        timRsus[0] = timRsu;
        HttpEntity<String> entity = getEntity(null, String.class);
        String url = String.format("null/tim-rsu/tim-id/%d", timId);
        when(mockResponseEntityTimRsuArray.getBody()).thenReturn(timRsus);
        when(mockRestTemplate.exchange(url, HttpMethod.GET, entity, TimRsu[].class))
                .thenReturn(mockResponseEntityTimRsuArray);

        // Act
        List<TimRsu> data = uut.getTimRsusByTimId(timId);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, entity, TimRsu[].class);
        assertEquals(1, data.size());
    }

    @Test
    public void getTimRsu() {
        // Arrange
        Long timId = -1l;
        int rsuId = -2;
        HttpEntity<String> entity = getEntity(null, String.class);
        String url = String.format("null/tim-rsu/tim-rsu/%d/%d", timId, rsuId);
        when(mockResponseEntityTimRsu.getBody()).thenReturn(new TimRsu());
        when(mockRestTemplate.exchange(url, HttpMethod.GET, entity, TimRsu.class)).thenReturn(mockResponseEntityTimRsu);

        // Act
        TimRsu data = uut.getTimRsu(timId, rsuId);

        // Assert
        assertNotNull(data);
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, entity, TimRsu.class);
    }
}