package com.trihydro.loggerkafkaconsumer.app.dataConverters;

import com.google.gson.Gson;
import com.trihydro.library.helpers.JsonToJavaConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeDriverAlertPayload;
import us.dot.its.jpo.ode.model.OdeLogMetadata;

@Component
public class DriverAlertDataConverter {

    public Gson gson = new Gson();
    private JsonToJavaConverter jsonToJava;

    @Autowired
    public void InjectDependencies(JsonToJavaConverter _jsonToJava) {
        jsonToJava = _jsonToJava;
    }

    public OdeData processDriverAlertJson(String value) {
		OdeData odeData = null;
		OdeLogMetadata odeDriverAlertMetadata = jsonToJava.convertDriverAlertMetadataJsonToJava(value);
		OdeDriverAlertPayload odeDriverAlertPayload = jsonToJava.convertDriverAlertPayloadJsonToJava(value);
		if (odeDriverAlertMetadata != null && odeDriverAlertPayload != null)
			odeData = new OdeData(odeDriverAlertMetadata, odeDriverAlertPayload);
		return odeData;
	}
}