package com.trihydro.odewrapper.service;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import com.trihydro.odewrapper.model.ControllerResult;

import com.trihydro.library.model.WydotRsu;
import com.trihydro.odewrapper.model.WydotTravelerInputData;
import com.trihydro.odewrapper.helpers.util.CreateBaseTimUtil;
import com.trihydro.library.model.IncidentChoice;
import com.trihydro.odewrapper.model.TimQuery;
import com.trihydro.library.model.TimType;
import com.trihydro.odewrapper.model.WydotTim;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.apache.commons.lang3.StringUtils;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import us.dot.its.jpo.ode.plugin.SNMP;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW;
import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeGeoRegion;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;

import com.google.gson.Gson;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.IncidentChoicesService;
import com.trihydro.library.service.ItisCodeService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.service.TimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.TimRsu;

import org.springframework.http.MediaType;
import static java.lang.Math.toIntExact;

@Component
public class WydotTimService
{    
    @Autowired
    public static Environment env;
    public static RestTemplate restTemplate = new RestTemplate();         
    public static Gson gson = new Gson();
    private List<ItisCode> itisCodes;
    private List<IncidentChoice> incidentProblems;
    private List<IncidentChoice> incidentEffects;
    private List<IncidentChoice> incidentActions;
    private ArrayList<WydotRsu> rsus;    
    private List<TimType> timTypes;    
    private static String odeUrl = "https://ode.wyoroad.info:8443";
    WydotRsu[] rsuArr = new WydotRsu[1];    
    DateTimeFormatter utcformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");      

    public void createUpdateTim(String timTypeStr, WydotTim wydotTim, String direction) {
                 
        // for each tim in wydot's request        
        System.out.println(timTypeStr + " TIM");
        System.out.println("direction: " + wydotTim.getDirection());
        String route = wydotTim.getRoute().replaceAll("\\D+","");
        System.out.println("route: " + route);
        System.out.println("fromRm: " + wydotTim.getFromRm());
        System.out.println("toRm: " + wydotTim.getToRm());

        // FIND ALL RSUS TO SEND TO     
        List<WydotRsu> rsus = getRsusInBuffer(direction, Math.min(wydotTim.getToRm(), wydotTim.getFromRm()), Math.max(wydotTim.getToRm(), wydotTim.getFromRm()), route);       

        // build base TIM                
        WydotTravelerInputData timToSend = CreateBaseTimUtil.buildTim(wydotTim, direction, route);

        // set itis codes
        List<String> items = null;
        if(timTypeStr.equals("CC"))
            items = setItisCodesFromAdvisoryArray(wydotTim);   
        else if(timTypeStr.equals("RC"))
            items = setItisCodesRc(wydotTim);  
        else if(timTypeStr.equals("VSL"))
            items = setItisCodesVsl(wydotTim);   
        else if(timTypeStr.equals("I"))
            items = setItisCodesIncident(wydotTim);   
        else if(timTypeStr.equals("P"))
            items = setItisCodesFromAvailability(wydotTim);   
        else if(timTypeStr.equals("RW"))
            items = setItisCodesFromAdvisoryArray(wydotTim);   

        // add itis codes to tim
        timToSend.getTim().getDataframes()[0].setItems(items.toArray(new String[items.size()]));

        // get tim type            
        TimType timType = getTimType(timTypeStr);
        
        // overwrite start date/time if one is provided (start date/time has been set to the current time in base tim creation)
        if(wydotTim.getStartDateTime() != null){          
            timToSend.getTim().getDataframes()[0].setStartDateTime(wydotTim.getStartDateTime());
        }      

        // set the duration if there is an enddate
        if(wydotTim.getEndDateTime() != null){               
            long durationTime = getMinutesDurationBetweenTwoDates(wydotTim.getStartDateTime(), wydotTim.getEndDateTime());
            timToSend.getTim().getDataframes()[0].setDurationTime(toIntExact(durationTime));
        }    

        // if parking TIM
        if(timTypeStr.equals("P")){
            // set duration for two hours
            timToSend.getTim().getDataframes()[0].setDurationTime(120);      
        }

        // build region name for active tim logger to use            
        String regionNamePrev = direction + "_" + wydotTim.getRoute() + "_" + wydotTim.getFromRm() + "_" + wydotTim.getToRm();   
                                
        // query database for rsus that active tims could be on
        List<ActiveTim> activeTims = null;

        // for each rsu in range
        for (WydotRsu rsu : rsus) {

            // add rsu to tim
            rsuArr[0] = rsu;
            timToSend.setRsus(rsuArr);            
            
            // update region name for active tim logger
            String regionNameTemp = regionNamePrev + "_RSU-" + rsu.getRsuTarget() + "_" + timTypeStr;

            // add clientId to region name
            if(wydotTim.getClientId() != null)
                regionNameTemp += "_" + wydotTim.getClientId();
            
            // add on wydot primary key to region name if it exists
            if(wydotTim.getPk() != null)
                regionNameTemp += "_" + wydotTim.getPk();
            
            // set region name -- used for active tim logging
            timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNameTemp);
            
            // if client ID exists, get all active tims of same type with that client id
            if(wydotTim.getClientId() != null && !timTypeStr.equals("P"))
                activeTims = ActiveTimService.getActiveTimsOnRsuByClientId(rsu.getRsuTarget(), wydotTim.getClientId(), timType.getTimTypeId(), direction);    
            // if not, query active tims by road segment
            else 
                activeTims = ActiveTimService.getActiveTimsOnRsuByRoadSegment(rsu.getRsuTarget(), timType.getTimTypeId(), wydotTim.getFromRm(), wydotTim.getToRm(), direction);       
               
            // if active tims exist, update tim        
            if(activeTims != null && activeTims.size() > 0){                                            
                // update TIM rsu
                updateTimOnRsu(timToSend, activeTims.get(0).getTimId());  
            }              
            else{     
                // send new tim to rsu                    
                sendNewTimToRsu(timToSend, rsu);  
            }
        }

        // satellite
        List<ActiveTim> activeSatTims = null;
        // if client ID exists, get all active tims of same type with that client id
        if(wydotTim.getClientId() != null && !timTypeStr.equals("P"))
            activeSatTims = ActiveTimService.getActiveSatTimsByClientIdDirection(wydotTim.getClientId(), timType.getTimTypeId(), direction);    
        // if not, query active tims by road segment
        else 
            activeSatTims = ActiveTimService.getActiveSatTimsBySegmentDirection(wydotTim.getFromRm(), wydotTim.getToRm(), timType.getTimTypeId(), direction);                

        if(activeSatTims != null && activeSatTims.size() > 0){
            String regionNameTemp = regionNamePrev + "_SAT-" + activeSatTims.get(0).getSatRecordId() + "_" + timTypeStr;
            if(wydotTim.getClientId() != null)
                regionNameTemp += "_" + wydotTim.getClientId();
            
            // add on wydot primary key if it exists
            if(wydotTim.getPk() != null)
                regionNameTemp += "_" + wydotTim.getPk();

            timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNameTemp);  
            updateTimOnSdw(timToSend, activeSatTims.get(0).getTimId(), activeSatTims.get(0).getSatRecordId());         
        }
        else{
            String recordId = getNewRecordId();    
            String regionNameTemp = regionNamePrev + "_SAT-" + recordId + "_" + timTypeStr;

            if(wydotTim.getClientId() != null)
                regionNameTemp += "_" + wydotTim.getClientId();

            // add on wydot primary key if it exists
            if(wydotTim.getPk() != null)
                regionNameTemp += "_" + wydotTim.getPk();
            
            timToSend.getTim().getDataframes()[0].getRegions()[0].setName(regionNameTemp);
            sendNewTimToSdw(timToSend, recordId);
        }        

    }

    public ControllerResult clearTimsByRoadSegment(String timTypeStr, WydotTim wydotTim, String direction){        
        
        WydotRsu rsu = null;

        ControllerResult result = new ControllerResult();
        List<String> resultsMessages = new ArrayList<String>();
        result.setDirection(direction);

        String route = wydotTim.getRoute().replaceAll("\\D+","");

        // get tim type            
        TimType timType = getTimType(timTypeStr);

        // get all RC active tims
        List<ActiveTim> activeTims = new ArrayList<ActiveTim>();            
        if(timType != null)
            activeTims = ActiveTimService.getAllActiveTimsBySegment(wydotTim.getFromRm(), wydotTim.getToRm(), timType.getTimTypeId(), direction);            

        if(activeTims.size() == 0){
            resultsMessages.add("No active TIMs found");
        }
        else{
            resultsMessages.add("success");
        }        
        deleteTimsFromRsusAndSdw(activeTims);
       
        result.setResultMessages(resultsMessages);
        return result;
    }

    public void deleteTimsFromRsusAndSdw(List<ActiveTim> activeTims){

        WydotRsu rsu = null;
        WydotTim wydotTim = new WydotTim();

        for (ActiveTim activeTim : activeTims) {

            wydotTim.setFromRm(activeTim.getMilepostStart());
            wydotTim.setToRm(activeTim.getMilepostStop());

            // get all tims
            J2735TravelerInformationMessage tim = TimService.getTim(activeTim.getTimId());                    
            // get RSU TIM is on
            List<TimRsu> timRsus = TimRsuService.getTimRsusByTimId(activeTim.getTimId());
                // get full RSU
                       
                if(timRsus.size() == 1){
                rsu = getRsu(timRsus.get(0).getRsuId());
                // delete tim off rsu           
                deleteTimFromRsu(rsu, tim.getIndex());                 
            }
            else{
                // is satellite tim
                String route = activeTim.getRoute().replaceAll("\\D+","");
                WydotTravelerInputData timToSend = CreateBaseTimUtil.buildTim(wydotTim, activeTim.getDirection(), route);
                String[] items = new String[1];
                items[0] = "4868";
                timToSend.getTim().getDataframes()[0].setItems(items);                    
                deleteTimFromSdw(timToSend, activeTim.getSatRecordId(), activeTim.getTimId());                    
            }

            // delete active tim                
            ActiveTimService.deleteActiveTim(activeTim.getActiveTimId());           
        }  
    }

    public boolean clearTimsById(String timTypeStr, String clientId){                
     
        WydotTim wydotTim = new WydotTim();
     
        List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
        WydotRsu rsu = null;
        activeTims = ActiveTimService.getActiveTimsByClientId(clientId);   
      
        deleteTimsFromRsusAndSdw(activeTims);
      
        return true;
    }

    public List<ActiveTim> selectTimByClientId(String timTypeStr, String clientId){

        TimType timType = getTimType(timTypeStr);
        
        List<ActiveTim> activeTims = ActiveTimService.getActivesTimByClientId(clientId, timType.getTimTypeId());

        return activeTims;
    }

    public List<ActiveTim> selectTimsByType(String timTypeStr){

        TimType timType = getTimType(timTypeStr);
        
        List<ActiveTim> activeTims = ActiveTimService.getActivesTimByType(timType.getTimTypeId());

        return activeTims;
    }

    public List<String> setItisCodesFromAdvisoryArray(WydotTim wydotTim) {    
        
        // check to see if code exists
        
        List<String> items = new ArrayList<String>();               
        for (Integer item : wydotTim.getAdvisory()){

            ItisCode code = getItisCodes().stream()
            .filter(x -> x.getItisCode().equals(item))
            .findFirst()
            .orElse(null);

            if(code != null)
                items.add(item.toString());                       
        }                            
        return items;
    }

    public List<String> setItisCodesFromAvailability(WydotTim wydotTim) {    
        
        // check to see if code exists        
        List<String> items = new ArrayList<String>();               
        
        ItisCode code = getItisCodes().stream()
        .filter(x -> x.getItisCode().equals(wydotTim.getAvailability()))
        .findFirst()
        .orElse(null);        

        if(code != null)
            items.add(wydotTim.getAvailability().toString());                       

        if(wydotTim.getExit() != null){
            items.add("11794");
            items.add(wydotTim.getExit());
        }
        else{
            items.add("7986");
        }

        return items;
    }

    public List<ItisCode> getItisCodes() {
        if(itisCodes != null)
            return itisCodes;
        else{
            itisCodes = ItisCodeService.selectAll(); 
            return itisCodes;
        }
    }

    public List<IncidentChoice> getIncidentProblems(){
        if(incidentProblems != null)
            return incidentProblems;
        else{
            incidentProblems = IncidentChoicesService.selectAllIncidentProblems(); 
            return incidentProblems;
        }
    }

    public List<IncidentChoice> getIncidentEffects(){
        if(incidentEffects != null)
            return incidentEffects;
        else{
            incidentEffects = IncidentChoicesService.selectAllIncidentEffects(); 
            return incidentEffects;
        }
    }

    public List<IncidentChoice> getIncidentActions(){
        if(incidentActions != null)
            return incidentActions;
        else{
            incidentActions = IncidentChoicesService.selectAllIncidentActions(); 
            return incidentActions;
        }
    }

    public List<String> setItisCodesRc(WydotTim wydotTim) {
        
        List<String> items = new ArrayList<String>();   

        ItisCode code = null;

        for (Integer item : wydotTim.getAdvisory()){

            // map "closed" itis code
            if(item == 769){
                code = getItisCodes().stream()
                .filter(x -> x.getItisCode().equals(770))
                .findFirst()
                .orElse(null);
            }
            else{
                code = getItisCodes().stream()
                .filter(x -> x.getItisCode().equals(item))
                .findFirst()
                .orElse(null);                        
            }

            if(code != null)
                items.add(code.getItisCode().toString());    
        }                   

        return items;
    }

    public List<String> setItisCodesVsl(WydotTim wydotTim) {
        
        List<String> items = new ArrayList<String>();        
        
        ItisCode speed = getItisCodes().stream()
            .filter(x -> x.getDescription().equals(wydotTim.getSpeed().toString()))
            .findFirst()
            .orElse(null);
        if(speed != null) {
            items.add(speed.getItisCode().toString());   
        }
        else
            return items;

        ItisCode speedLimit = getItisCodes().stream()
            .filter(x -> x.getDescription().equals("speed limit"))
            .findFirst()
            .orElse(null);
        if(speedLimit != null) {
            items.add(speedLimit.getItisCode().toString());           
        }

        ItisCode mph = getItisCodes().stream()
            .filter(x -> x.getDescription().equals("mph"))
            .findFirst()
            .orElse(null);
        if(mph != null){
            items.add(mph.getItisCode().toString());  
        }

        return items;
    }

    public List<String> setItisCodesRw(WydotTim wydotTim){

        List<String> items = new ArrayList<String>();      
        
        items.add("1025");           
       
        return items;
    }

    public List<String> setItisCodesParking(WydotTim wydotTim) {
        
        // check to see if code exists        
        List<String> items = new ArrayList<String>();               
    
        ItisCode code = getItisCodes().stream()
        .filter(x -> x.getItisCode().equals(wydotTim.getAvailability()))
        .findFirst()
        .orElse(null);        

        if(code != null)
            items.add(wydotTim.getAvailability().toString());                       

        if(wydotTim.getExit() != null){
            items.add("11794");
            List<String> list = splitExitNumberFromLetter(wydotTim.getExit());
            int exitItisCodeNumber = convertNumberToItisCode(Integer.parseInt(list.get(0)));
            items.add(String.valueOf(exitItisCodeNumber));
            if(list.size() > 1)
                items.add(list.get(1));
        }
        else{
            items.add("7986");
        }

        return items;
    }

    public List<String> splitExitNumberFromLetter(String exit){
        
        List<String> list = new ArrayList<String>();
        String exitNumber = ""; 
        String exitLetter = "";
        for (int i = 0; i < exit.length(); i++) {
            if(StringUtils.isNumeric(String.valueOf(exit.charAt(i)))){
                exitNumber += exit.charAt(i);
            }
            else {
                exitLetter += exit.charAt(i);
            }
        }

        list.add(exitNumber);
        if(exitLetter.length() > 0)
            list.add(exitLetter);

        return list;
    }

    public List<String> setItisCodesIncident(WydotTim wydotTim) {        
        List<String> items = new ArrayList<String>(); 

        // action
        IncidentChoice incidentAction = getIncidentActions().stream()
            .filter(x -> x.getCode().equals(wydotTim.getAction()))
            .findFirst()
            .orElse(null);
        
        // if action is not null and action itis code exists
        if(incidentAction != null && incidentAction.getItisCodeId() != null){
            ItisCode actionItisCode = getItisCodes().stream()
                .filter(x -> x.getItisCodeId().equals(incidentAction.getItisCodeId()))
                .findFirst()
                .orElse(null);
            if(actionItisCode != null){
                items.add(actionItisCode.getItisCode().toString());  
            }
        }

        // effect
        IncidentChoice incidentEffect = getIncidentEffects().stream()
            .filter(x -> x.getCode().equals(wydotTim.getEffect()))
            .findFirst()
            .orElse(null);
        
        // if effect is not null and effect itis code exists
        if(incidentEffect != null && incidentEffect.getItisCodeId() != null){
            ItisCode effectItisCode = getItisCodes().stream()
                .filter(x -> x.getItisCodeId().equals(incidentEffect.getItisCodeId()))
                .findFirst()
                .orElse(null);
            if(effectItisCode != null){
                items.add(effectItisCode.getItisCode().toString());  
            }
        }

        // problem
        IncidentChoice incidentProblem = getIncidentProblems().stream()
            .filter(x -> x.getCode().equals(wydotTim.getProblem()))
            .findFirst()
            .orElse(null);
        
        // if problem is not null and problem itis code exists
        if(incidentProblem != null && incidentProblem.getItisCodeId() != null){
            ItisCode problemItisCode = getItisCodes().stream()
                .filter(x -> x.getItisCodeId().equals(incidentProblem.getItisCodeId()))
                .findFirst()
                .orElse(null);
            if(problemItisCode != null){
                items.add(problemItisCode.getItisCode().toString());  
            }
        }

        if(items.size() == 0)
            items.add("531");

        return items;
    }

    public long getMinutesDurationBetweenTwoDates(String startDateTime, String endDateTime){

        ZonedDateTime zdtStart = ZonedDateTime.parse(startDateTime);
        ZonedDateTime zdtEnd = ZonedDateTime.parse(endDateTime);

        java.time.Duration dateDuration = java.time.Duration.between(zdtStart, zdtEnd);
        Math.abs(dateDuration.toMinutes());
        long durationTime = Math.abs(dateDuration.toMinutes());
       
        return durationTime;
    }

    public ArrayList<WydotRsu> getRsus() {
        if(rsus != null)
            return rsus;
        else{
            rsus = RsuService.selectAll(); 
            for (WydotRsu rsu : rsus) {
                rsu.setRsuRetries(3);
                rsu.setRsuTimeout(5000);
            }
            return rsus;
        }
    }    

    public ArrayList<WydotRsu> getRsusByRoute(String route){
        if(rsus != null)
            return rsus;
        else{
            rsus = RsuService.selectRsusByRoute(route); 
            for (WydotRsu rsu : rsus) {
                rsu.setRsuRetries(3);
                rsu.setRsuTimeout(5000);
            }
            return rsus;
        }
    }    

    public List<TimType> getTimTypes(){
        if(timTypes != null)
            return timTypes;
        else{
            timTypes = TimTypeService.selectAll();            
            return timTypes;
        }
    }    

    public List<WydotRsu> getRsusInBuffer(String direction, Double lowerMilepost, Double higherMilepost, String route){

        List<WydotRsu> rsus = new ArrayList<>();

        int closestIndexOutsideRange = 0;
        int i;

        // if there are no rsus on this route
        if(getRsusByRoute(route).size() == 0)
            return rsus;            

        if(direction.equals("eastbound")){
            // get rsus at mileposts less than your milepost 
            List<WydotRsu> rsusLower =  getRsusByRoute(route).stream()
                .filter(x -> x.getMilepost() < lowerMilepost)
                .collect(Collectors.toList());

            // get max from that list
            Comparator<WydotRsu> compMilepost = (l1, l2) -> Double.compare(l1.getMilepost(), l2.getMilepost());
            WydotRsu rsuLower =  rsusLower.stream()
                .max(compMilepost)
                .get();
                
            closestIndexOutsideRange = getRsusByRoute(route).indexOf(rsuLower);                        
        }
        else{
             // get rsus at mileposts greater than your milepost 
             List<WydotRsu> rsusLower =  getRsusByRoute(route).stream()
                .filter(x -> x.getMilepost() > lowerMilepost)
                .collect(Collectors.toList());
 
             // get min from that list
             Comparator<WydotRsu> compMilepost = (l1, l2) -> Double.compare(l1.getMilepost(), l2.getMilepost());
             WydotRsu rsuHigher =  rsusLower.stream()
                .min(compMilepost)
                .get();
             
             closestIndexOutsideRange = getRsusByRoute(route).indexOf(rsuHigher);         
        }

        for(i = 0; i < getRsusByRoute(route).size(); i++){                   
            if(getRsusByRoute(route).get(i).getMilepost() >= lowerMilepost && getRsusByRoute(route).get(i).getMilepost() <= higherMilepost)     
                rsus.add(getRsusByRoute(route).get(i));                
        }

        // add RSU closest in range
        rsus.add(getRsusByRoute(route).get(closestIndexOutsideRange));
        
        return rsus;
    }

    public WydotRsu getRsu(Long rsuId){
                
        WydotRsu wydotRsu = null;
		try {
			wydotRsu = getRsus().stream()
            .filter(x -> x.getRsuId() == rsuId.intValue())
            .findFirst()
            .orElse(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return wydotRsu;
    }

    public static void sendNewTimToRsu(WydotTravelerInputData timToSend, WydotRsu rsu) {
        
        // add snmp
        SNMP snmp = new SNMP();
        snmp.setChannel(178);
        snmp.setRsuid("00000083");
        snmp.setMsgid(31);
        snmp.setMode(1);
        snmp.setChannel(178);
        snmp.setInterval(2);
        snmp.setDeliverystart("2018-01-01T00:00:00-06:00");
        snmp.setDeliverystop("2019-01-01T00:00:00-06:00");
        snmp.setEnable(1);
        snmp.setStatus(4);
        timToSend.setSnmp(snmp);

        // set msgCnt to 1 and create new packetId
        timToSend.getTim().setMsgCnt(1);

        Random rand = new Random();            
        int randomNum = rand.nextInt(1000000) + 100000;            
        String packetIdHexString = Integer.toHexString(randomNum);
        packetIdHexString = String.join("", Collections.nCopies(18 - packetIdHexString.length(), "0")) + packetIdHexString;
        timToSend.getTim().setPacketID(packetIdHexString);

        // set RSU index and send TIM if query is successful 
        if(DbUtility.getConnectionEnvironment().equals("test")){
            timToSend.getTim().setIndex(0);
            return;
        }                  

        TimQuery timQuery = submitTimQuery(rsu, 0);
        if(timQuery != null){
            timToSend.getTim().setIndex(findFirstAvailableIndex(timQuery.getIndicies_set()));
            String timToSendJson = gson.toJson(timToSend); 
            // send TIM if not a test
            restTemplate.postForObject(odeUrl + "/tim", timToSendJson, String.class);
        }
        else
            timToSend.getTim().setIndex(0);                                            
    }

    public static void sendNewTimToSdw(WydotTravelerInputData timToSend, String recordId){

        // set msgCnt to 1 and create new packetId
        timToSend.getTim().setMsgCnt(1);
        
        Random rand = new Random();            
        int randomNum = rand.nextInt(1000000) + 100000;            
        String packetIdHexString = Integer.toHexString(randomNum);
        packetIdHexString = String.join("", Collections.nCopies(18 - packetIdHexString.length(), "0")) + packetIdHexString;
        timToSend.getTim().setPacketID(packetIdHexString);

        SDW sdw = new SDW();

        // calculate service region
        sdw.setServiceRegion(getServiceRegion(timToSend.getMileposts()));

        // set time to live
        sdw.setTtl(TimeToLive.oneday);
        // set new record id
        sdw.setRecordId(recordId);

        // set sdw block in TIM
        timToSend.setSdw(sdw);

        // send to ODE
        String timToSendJson = gson.toJson(timToSend);

        if(!DbUtility.getConnectionEnvironment().equals("test"))
            restTemplate.postForObject(odeUrl + "/tim", timToSendJson, String.class);
    }
    
    protected static String getNewRecordId() {
        String hexChars = "ABCDEF1234567890";
        StringBuilder hexStrB = new StringBuilder();
        Random rnd = new Random();
        while (hexStrB.length() < 8) { // length of the random string.
            int index = (int) (rnd.nextFloat() * hexChars.length());
            hexStrB.append(hexChars.charAt(index));
        }
        String hexStr = hexStrB.toString();
        return hexStr;
    }

    public String convertUtcDateTimeToLocal(String utcDateTime){  

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");            
        LocalDateTime startDate = LocalDateTime.parse(utcDateTime, formatter);        
        ZoneId mstZoneId = ZoneId.of("America/Denver");              
        ZonedDateTime mstZonedDateTime = startDate.atZone(mstZoneId);      
        String startDateTime = mstZonedDateTime.toLocalDateTime().toString() + "-06:00";
      
        return startDateTime;
    }

    public static void updateTimOnRsu(WydotTravelerInputData timToSend, Long timId){

        WydotTravelerInputData updatedTim = updateTim(timToSend, timId);

        SNMP snmp = new SNMP();
        snmp.setChannel(178);
        snmp.setRsuid("00000083");
        snmp.setMsgid(31);
        snmp.setMode(1);
        snmp.setChannel(178);
        snmp.setInterval(2);
        snmp.setDeliverystart("2018-01-01T00:00:00-06:00");
        snmp.setDeliverystop("2019-01-01T00:00:00-06:00");
        snmp.setEnable(1);
        snmp.setStatus(4);
        timToSend.setSnmp(snmp);

        String timToSendJson = gson.toJson(updatedTim); 

        if(!DbUtility.getConnectionEnvironment().equals("test"))
            restTemplate.put(odeUrl + "/tim", timToSendJson, String.class);        
    } 

    public static void updateTimOnSdw(WydotTravelerInputData timToSend, Long timId, String recordId){

        WydotTravelerInputData updatedTim = updateTim(timToSend, timId);

        SDW sdw = new SDW();
        
        // calculate service region
        sdw.setServiceRegion(getServiceRegion(timToSend.getMileposts()));

        // set time to live
        sdw.setTtl(TimeToLive.oneday);
        // set new record id
        sdw.setRecordId(recordId);

        // set sdw block in TIM
        updatedTim.setSdw(sdw);

        String timToSendJson = gson.toJson(updatedTim); 

        // send TIM to ODE if not a test
        if(!DbUtility.getConnectionEnvironment().equals("test"))
            restTemplate.postForObject(odeUrl + "/tim", timToSendJson, String.class);        
    } 

    public static WydotTravelerInputData updateTim(WydotTravelerInputData timToSend, Long timId){
        // get existing TIM
        J2735TravelerInformationMessage tim = TimService.getTim(timId);                    
        // set TIM packetId 
        timToSend.getTim().setPacketID(tim.getPacketID());
        // get RSU index
        timToSend.getTim().setIndex(tim.getIndex());

        // roll msgCnt over to 0 if at 127
        if(tim.getMsgCnt() == 127)
            timToSend.getTim().setMsgCnt(0);
        // else increment msgCnt
        else
            timToSend.getTim().setMsgCnt(tim.getMsgCnt() + 1);        
               
        return timToSend;
    }
    
    public static void updateActiveTims(ActiveTim activeTim, List<Integer> itisCodeIds, Long timId, String endDateTime){
        // update Active TIM table TIM Id
        ActiveTimService.updateActiveTimTimId(activeTim.getActiveTimId(), timId);

        if(endDateTime != null)
            ActiveTimService.updateActiveTimEndDate(activeTim.getActiveTimId(), endDateTime);        
    }    

    protected static TimQuery submitTimQuery(WydotRsu rsu, int counter){
        
        // stop if this fails five times
        if(counter == 2)
            return null;

        // tim query to ODE      
        String rsuJson = gson.toJson(rsu);
           
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);                
        HttpEntity<String> entity = new HttpEntity<String>(rsuJson, headers);
        
        String responseStr = null;

        try{           
            responseStr = restTemplate.postForObject(odeUrl + "/tim/query", entity, String.class);          
        }
        catch(RestClientException e){
            return submitTimQuery(rsu, counter + 1);
        }

        String[] items = responseStr.replaceAll("\\\"", "").replaceAll("\\:", "").replaceAll("indicies_set", "").replaceAll("\\{", "").replaceAll("\\}", "").replaceAll("\\[", "").replaceAll(" ", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
        
        int[] results = new int[items.length];
        
        for (int i = 0; i < items.length; i++) {
            try {
                results[i] = Integer.parseInt(items[i]);
            } catch (NumberFormatException nfe) {
                //NOTE: write something here if you need to recover from formatting errors
            };
        }

        Arrays.sort(results);

        TimQuery timQuery = new TimQuery();
        timQuery.setIndicies_set(results);
      //  TimQuery timQuery = gson.fromJson(responseStr, TimQuery.class);
        
        return timQuery;
    }

    public static void deleteTimFromRsu(WydotRsu rsu, Integer index){

        String rsuJson = gson.toJson(rsu);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);                
        HttpEntity<String> entity = new HttpEntity<String>(rsuJson, headers);

        if(DbUtility.getConnectionEnvironment().equals("test"))
            return;

        try{
            restTemplate.exchange(odeUrl + "/tim?index=" + index.toString(), HttpMethod.DELETE, entity, String.class);              
        }        
        catch(HttpClientErrorException e ){
            System.out.println(e.getMessage());
        }
    }

    public static void deleteTimFromSdw(WydotTravelerInputData timToSend, String recordId, Long timId){
        
        WydotTravelerInputData updatedTim = updateTim(timToSend, timId);         

        SDW sdw = new SDW();
        
        // calculate service region
        sdw.setServiceRegion(getServiceRegion(timToSend.getMileposts()));

        // set time to live
        sdw.setTtl(TimeToLive.oneminute);
        // set new record id
        sdw.setRecordId(recordId);

        // set sdw block in TIM
        updatedTim.setSdw(sdw);

        String timToSendJson = gson.toJson(updatedTim); 
       
        if(!DbUtility.getConnectionEnvironment().equals("test"))
            restTemplate.postForObject(odeUrl + "/tim", timToSendJson, String.class);               
    }

    public TimType getTimType(String timTypeName){
        
        // get tim type       
        TimType timType = getTimTypes().stream()
        .filter(x -> x.getType().equals(timTypeName))
        .findFirst()
        .orElse(null);

        return timType;
    } 
    
    protected static OdeGeoRegion getServiceRegion(List<Milepost> mileposts){
        
        Comparator<Milepost> compLat = (l1, l2) -> Double.compare( l1.getLatitude(), l2.getLatitude());
        Comparator<Milepost> compLong = (l1, l2) -> Double.compare( l1.getLongitude(), l2.getLongitude());

        Milepost maxLat = mileposts.stream()
            .max(compLat)
            .get();

        Milepost minLat = mileposts.stream()
            .min(compLat)
            .get();
        
        Milepost maxLong = mileposts.stream()
            .max(compLong)
            .get();

        Milepost minLong = mileposts.stream()
            .min(compLong)
            .get();

        OdePosition3D nwCorner = new OdePosition3D();
        nwCorner.setLatitude(new BigDecimal(maxLat.getLatitude()));
        nwCorner.setLongitude(new BigDecimal(minLong.getLongitude()));
        
        OdePosition3D seCorner = new OdePosition3D();
        seCorner.setLatitude(new BigDecimal(minLat.getLatitude()));
        seCorner.setLongitude(new BigDecimal(maxLong.getLongitude()));

        OdeGeoRegion serviceRegion = new OdeGeoRegion();
        serviceRegion.setNwCorner(nwCorner);
        serviceRegion.setSeCorner(seCorner);
        return serviceRegion;
    } 

    protected static int findFirstAvailableIndex(int[] indicies){
        for (int i = 2; i < 100; i++) {
            if(!contains(indicies, i)) {
                return i;
            }
        }
        return 0;
    }

    public static boolean contains(final int[] array, final int v) {
        boolean result = false;
        for(int i : array){
            if(i == v){
                result = true;
                break;
            }
        }
        return result;
    }

    public Integer[] setBufferItisCodes(String action){

        Integer[] codes = null;

        if(action.equals("leftClosed")){
            codes = new Integer[2];
            codes[0] = 777;
            codes[1] = 13580;
        }                       
        else if(action.equals("rightClosed")){
            codes = new Integer[2];
            codes[0] = 777;
            codes[1] = 13579;
        }
        else if(action.equals("workers")){
            codes = new Integer[1];
            codes[0] = 224;
        }
        else if(action.equals("surfaceGravel")){
            codes = new Integer[1];
            codes[0] = 5933;
        }
        else if(action.equals("surfaceMilled")){
            codes = new Integer[1];
            codes[0] = 6017;
        }
        else if(action.equals("surfaceDirt")){
            codes = new Integer[1];
            codes[0] = 6016;
        }
        else if(action.contains("delay_")){
            codes = new Integer[1];
            codes[0] = 1537;
        }
        else if(action.equals("prepareStop")){
            codes = new Integer[1];
            codes[0] = 7186;
        }
        else if(action.contains("reduceSpeed_")){
            codes = new Integer[1];
            codes[0] = 7443;
        }
               
        return codes;
    }

    private int convertNumberToItisCode(int number){
        int itisCode = 1 + 12544;
        return itisCode;
    }
}