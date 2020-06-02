package com.trihydro.timrefresh.service;

import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.WydotOdeTravelerInformationMessage;
import com.trihydro.library.model.WydotTravelerInputData;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.timrefresh.config.TimRefreshConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW;
import us.dot.its.jpo.ode.plugin.j2735.OdeGeoRegion;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;

@Component
public class WydotTimService {

    protected TimRefreshConfiguration configuration;
    private RestTemplateProvider restTemplateProvider;
    private Gson gson = new Gson();

    @Autowired
    public void setConfiguration(TimRefreshConfiguration configurationRhs, RestTemplateProvider _restTemplateProvider) {
        configuration = configurationRhs;
        restTemplateProvider = _restTemplateProvider;
    }

    public void updateTimOnRsu(WydotTravelerInputData timToSend) {

        String timToSendJson = gson.toJson(timToSend);
        restTemplateProvider.GetRestTemplate().put(configuration.getOdeUrl() + "/tim", timToSendJson, String.class);
    }

    public void updateTimOnSdw(WydotTravelerInputData timToSend) {
        String timToSendJson = gson.toJson(timToSend);

        // send TIM
        try {
            restTemplateProvider.GetRestTemplate().postForObject(configuration.getOdeUrl() + "/tim", timToSendJson,
                    String.class);
        } catch (RuntimeException targetException) {
            System.out.println("exception");
        }
    }

    public void sendNewTimToSdw(WydotTravelerInputData timToSend, String recordId, List<Milepost> mps) {

        // set msgCnt to 1 and create new packetId
        timToSend.getTim().setMsgCnt(1);

        SDW sdw = new SDW();

        // calculate service region
        sdw.setServiceRegion(getServiceRegion(mps));

        // set time to live
        sdw.setTtl(configuration.getSdwTtl());
        // set new record id
        sdw.setRecordId(recordId);

        // set sdw block in TIM
        timToSend.getRequest().setSdw(sdw);

        // send to ODE
        String timToSendJson = gson.toJson(timToSend);

        try {
            restTemplateProvider.GetRestTemplate().postForObject(configuration.getOdeUrl() + "/tim", timToSendJson,
                    String.class);
            System.out.println("Successfully sent POST to ODE to send new TIM: " + timToSendJson);
        } catch (RuntimeException targetException) {
            System.out.println("Failed to POST new SDX TIM: " + timToSendJson);
            targetException.printStackTrace();
        }
    }

    public WydotTravelerInputData updateTim(WydotTravelerInputData timToSend, Long timId,
            WydotOdeTravelerInformationMessage tim) {

        // set TIM packetId
        timToSend.getTim().setPacketID(tim.getPacketID());

        // roll msgCnt over to 1 if at 127
        if (tim.getMsgCnt() == 127)
            timToSend.getTim().setMsgCnt(1);
        // else increment msgCnt
        else
            timToSend.getTim().setMsgCnt(tim.getMsgCnt() + 1);

        return timToSend;
    }

    public OdeGeoRegion getServiceRegion(List<Milepost> mileposts) {

        Comparator<Milepost> compLat = (l1, l2) -> l1.getLatitude().compareTo(l2.getLatitude());
        Comparator<Milepost> compLong = (l1, l2) -> l1.getLongitude().compareTo(l2.getLongitude());
        OdeGeoRegion serviceRegion = new OdeGeoRegion();

        if (mileposts.size() > 0) {

            Milepost maxLat = mileposts.stream().max(compLat).get();

            Milepost minLat = mileposts.stream().min(compLat).get();

            Milepost maxLong = mileposts.stream().max(compLong).get();

            Milepost minLong = mileposts.stream().min(compLong).get();

            OdePosition3D nwCorner = new OdePosition3D();
            nwCorner.setLatitude(maxLat.getLatitude());
            nwCorner.setLongitude(minLong.getLongitude());

            OdePosition3D seCorner = new OdePosition3D();
            seCorner.setLatitude(minLat.getLatitude());
            seCorner.setLongitude(maxLong.getLongitude());

            serviceRegion.setNwCorner(nwCorner);
            serviceRegion.setSeCorner(seCorner);
        } else {
            System.out.println("getServiceRegion fails due to no mileposts");
        }
        return serviceRegion;
    }
}