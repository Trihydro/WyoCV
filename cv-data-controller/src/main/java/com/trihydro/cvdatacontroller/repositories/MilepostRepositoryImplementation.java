package com.trihydro.cvdatacontroller.repositories;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.trihydro.cvdatacontroller.model.Milepost;

import org.neo4j.ogm.session.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class MilepostRepositoryImplementation implements MilepostRepository {
    private final Session session;

    public MilepostRepositoryImplementation(Session session) {
        this.session = session;
    }

    @Override
    public Collection<Milepost> getMilepostsByCommonNameWithLimit(String commonName, int limit) {
        String query = "MATCH (m:Milepost) WHERE m.CommonName = $commonName RETURN m LIMIT {limit}";
        Map<String, Object> map = new HashMap<>();
        map.put("commonName", commonName);
        map.put("limit", limit);
        Iterable<Milepost> mp = session.query(Milepost.class, query, map);
        return StreamSupport.stream(mp.spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public Collection<Milepost> getPathWithBuffer(String commonName, BigDecimal startLat, BigDecimal startLong,
            BigDecimal endLat, BigDecimal endLong, String direction) {
        String dirQuery = "[";
        if (!direction.equalsIgnoreCase("B")) {
            dirQuery += "'" + direction.toUpperCase() + "', ";
        }
        dirQuery += "'B']";

        String query = "match(startMp:Milepost{CommonName: $commonName})";
        query += " where startMp.Direction in " + dirQuery;
        query += " with startMp, distance(point({longitude:apoc.number.parseFloat($startLong),latitude:apoc.number.parseFloat($startLat)}), point({longitude:startMp.Longitude,latitude:startMp.Latitude})) as d1 ";

        query += " with startMp, d1 ORDER BY d1 ASC LIMIT 1";

        query += " match(endMp:Milepost{CommonName: $commonName})";
        query += " where endMp.Direction in " + dirQuery;

        query += " with startMp, endMp, distance(point({longitude:apoc.number.parseFloat($endLong),latitude:apoc.number.parseFloat($endLat)}), point({longitude:endMp.Longitude,latitude:endMp.Latitude})) as d2 ";

        query += " with startMp, endMp, d2 ORDER BY d2 ASC LIMIT 1";
        query += " call apoc.when(startMp.Milepost < endMp.Milepost, ";

        if (direction.equalsIgnoreCase("I")) {
            query += " 'optional match(tmp:Milepost)-->(startMp) where tmp.Milepost < startMp.Milepost return coalesce(tmp, startMp) as adjust',";
            query += " 'optional match(tmp:Milepost)-->(endMp) where tmp.Milepost < endMp.Milepost return coalesce(tmp, endMp) as adjust',";
        } else { // 'D' direction
            query += " 'optional match(tmp:Milepost)-->(endMp) where tmp.Milepost > endMp.Milepost return coalesce(tmp, endMp) as adjust',";
            query += " 'optional match(tmp:Milepost)-->(startMp) where tmp.Milepost > startMp.Milepost return coalesce(tmp, startMp) as adjust',";
        }
        query += " {startMp:startMp, endMp:endMp}) yield value";

        query += " with startMp, endMp, value.adjust as adjust";
        query += " call apoc.when(startMp.Milepost < endMp.Milepost,";
        if (direction.equalsIgnoreCase("I")) {
            query += " 'call algo.shortestPath.stream(adjust,endMp) yield nodeId match(other:Milepost) where id(other) = nodeId return other',";
            query += " 'call algo.shortestPath.stream(startMp,adjust) yield nodeId match(other:Milepost) where id(other) = nodeId return other',";
        } else {
            query += " 'call algo.shortestPath.stream(startMp,adjust) yield nodeId match(other:Milepost) where id(other) = nodeId return other',";
            query += " 'call algo.shortestPath.stream(adjust,endMp) yield nodeId match(other:Milepost) where id(other) = nodeId return other',";
        }
        query += " {adjust:adjust, startMp:startMp, endMp:endMp}) yield value";
        query += " return value.other order by value.other.Milepost";

        if (direction.equalsIgnoreCase("D")) {
            query += " desc";
        }

        Map<String, Object> map = new HashMap<>();
        map.put("commonName", commonName);
        map.put("startLat", startLat.toPlainString());
        map.put("startLong", startLong.toPlainString());
        map.put("endLat", endLat.toPlainString());
        map.put("endLong", endLong.toPlainString());
        Iterable<Milepost> mp = session.query(Milepost.class, query, map);
        return StreamSupport.stream(mp.spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public Collection<Milepost> getPathWithSpecifiedBuffer(String commonName, BigDecimal lat, BigDecimal lon,
            String direction, Double bufferInMiles) {
        /**
         * This function creates a statement such as the following
         * 
         * match(mp:Milepost{CommonName: 'WY 59'}) where mp.Direction in ['I', 'B'] with
         * min(mp) as extremeMp match(mp:Milepost{CommonName: 'WY 59'}) where
         * mp.Direction in ['I', 'B'] with extremeMp, mp,
         * distance(point({longitude:-105.259913,latitude:43.114181}),
         * point({longitude:mp.Longitude,latitude:mp.Latitude})) as d1 with extremeMp,
         * mp, d1 ORDER BY d1 ASC LIMIT 1 with mp, case when mp.Milepost -1 <
         * extremeMp.Milepost then extremeMp.Milepost else mp.Milepost -1 end as
         * startMpNum match(bufferStart:Milepost{CommonName:mp.CommonName,
         * Milepost:startMpNum}) where bufferStart.Direction in ['I', 'B'] with
         * bufferStart, mp call algo.shortestPath.stream(bufferStart,mp) yield nodeId
         * match(other:Milepost) where id(other) = nodeId return other
         */
        boolean increasing = direction.equalsIgnoreCase("I");
        String dirQuery = "[";
        if (!direction.equalsIgnoreCase("B")) {
            dirQuery += "'" + direction.toUpperCase() + "', ";
        }
        dirQuery += "'B']";

        String query = "match(mp:Milepost{CommonName: $commonName})";
        query += " where mp.Direction in " + dirQuery;
        if (increasing) {
            query += " with min(mp) as extremeMp";
        } else {
            query += " with max(mp) as extremeMp";
        }

        query += " match(mp:Milepost{CommonName: $commonName})";
        query += " where mp.Direction in " + dirQuery;
        query += " with extremeMp, mp, distance(point({longitude:apoc.number.parseFloat($lon),latitude:apoc.number.parseFloat($lat)}), point({longitude:mp.Longitude,latitude:mp.Latitude})) as d1 ";
        query += " with extremeMp, mp, d1 ORDER BY d1 ASC LIMIT 1";// here we have the closest point, now go back
                                                                   // bufferInMiles

        // get the buffered start
        // if 'I' direction, get bufferedMiles before
        // if 'D' direction, get bufferMiles after
        query += " with mp,";
        if (increasing) {
            query += " case when mp.Milepost - ";
            query += bufferInMiles;
            query += " < extremeMp.Milepost then extremeMp.Milepost else mp.Milepost - ";
        } else {
            query += " case when mp.Milepost + ";
            query += bufferInMiles;
            query += " > extremeMp.Milepost then extremeMp.Milepost else mp.Milepost + ";
        }
        query += bufferInMiles;
        query += " end as startMpNum";
        query += " match(bufferStart:Milepost{CommonName:mp.CommonName, Milepost:startMpNum})";
        query += " where bufferStart.Direction in " + dirQuery;

        query += " with bufferStart, mp";
        query += " call algo.shortestPath.stream(bufferStart,mp) yield nodeId";
        query += " match(other:Milepost)";
        query += " where id(other) = nodeId return other;";

        // Note that we do not worry about the 'B' case here, as it should be called
        // with 'I' or 'D'
        // it would be difficult to return 'B' since there are two sets of data, one I
        // and one D. These should be separate TIMs and this function called as such

        Map<String, Object> map = new HashMap<>();
        map.put("commonName", commonName);
        map.put("lat", lat.toPlainString());
        map.put("lon", lon.toPlainString());
        map.put("bufferMiles", bufferInMiles);
        Iterable<Milepost> mp = session.query(Milepost.class, query, map);
        return StreamSupport.stream(mp.spliterator(), false).collect(Collectors.toList());
    }
}