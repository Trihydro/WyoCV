package com.trihydro.cvlogger.app.services;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import us.dot.its.jpo.ode.model.OdeLogMetadataReceived;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import com.trihydro.library.service.tracmessage.TracMessageSentService;
import com.trihydro.library.service.tracmessage.TracMessageTypeService;
import com.trihydro.cvlogger.app.converters.JsonToJavaConverter;
import com.trihydro.library.model.TracMessageSent;
import com.trihydro.library.model.TracMessageType;


public class TracManager {
    
    public static TracMessageSent isDnMsgInTrac(Connection connection, String packetId){
        
        List<TracMessageSent> tracMessagesSent = TracMessageSentService.selectAll(connection);

        TracMessageSent tracMessageSent = tracMessagesSent.stream()
            .filter(x -> x.getPacketId().equals(packetId))
            .findFirst()
            .orElse(null);

        return tracMessageSent;
    }

    public static Long logNewDistressNotification(Connection connection, String packetId, String messageText){
               
        // get trac message types
        List<TracMessageType> tracMessageTypes = TracMessageTypeService.selectAll(connection);

        // get message type equal to distress notification
        TracMessageType tracMessageType = tracMessageTypes.stream()
        .filter(x -> x.getTracMessageType().equals("DN"))
        .findFirst()
        .orElse(null);
        
        TracMessageSent tracMessageSent = new TracMessageSent();
		tracMessageSent.setTracMessageTypeId(tracMessageType.getTracMessageTypeId());
		tracMessageSent.setDateTimeSent(new Timestamp(System.currentTimeMillis()));
		tracMessageSent.setMessageText(messageText);
        tracMessageSent.setPacketId(packetId);
        System.out.println("packet id: " + packetId);
        // log in db
        Long tracMessageSentId = TracMessageSentService.insertTracMessageSent(tracMessageSent, connection);
        return tracMessageSentId;
    }

    public static void submitDNMsgToTrac(String value, Connection connection) {
		
		OdeLogMetadataReceived metadata = JsonToJavaConverter.convertTimMetadataJsonToJava(value);
		OdeTimPayload payload = JsonToJavaConverter.convertTimPayloadJsonToJava(value);		

		// check if packetId is in trac message sent table
		if(TracManager.isDnMsgInTrac(connection, payload.getTim().getPacketID()) != null ){
			// if so, return	
			return;
		}

		// if not, add to db and sent to trac

		RestTemplate restTemplate = new RestTemplate();
		String url = "http://gisd01:8070/trac/newtask.text";	
		
		String latitude = null;
		String longitude = null;	 
			
		
		latitude = metadata.getReceivedMessageDetails().getLocationData().getLatitude();
		longitude = metadata.getReceivedMessageDetails().getLocationData().getLongitude();			

		String descUrl = "<b>";
		descUrl += "Distress Notification Issued at ";		
		descUrl += "<a href='https://www.google.com/maps/place/" + latitude + "," + longitude + "'>" + latitude + "/" + longitude + "</a></b>";		
	
		// Query parameters
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
			.queryParam("priority", "1")
			.queryParam("description", descUrl)
			.queryParam("createdBy", "Connected Vehicle Emergency Notification")
			.queryParam("source", "CV System");
			
		System.out.println(builder.buildAndExpand().toUri());
		
		HttpHeaders responseHeaders = new HttpHeaders();
		//String result = restTemplate.postForObject(builder.buildAndExpand().toUri(), new HttpEntity<String>(null, responseHeaders), String.class);		
		TracManager.logNewDistressNotification(connection, payload.getTim().getPacketID(), "Distress Notification Issued at " + latitude + "/" + longitude);
	}

}