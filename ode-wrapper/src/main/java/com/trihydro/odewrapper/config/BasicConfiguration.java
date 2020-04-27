package com.trihydro.odewrapper.config;

import java.math.BigDecimal;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.RsuDataServiceProps;
import com.trihydro.library.model.SdwProps;
import com.trihydro.library.model.TmddProps;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;

@Component
@ConfigurationProperties("config")
public class BasicConfiguration implements SdwProps, RsuDataServiceProps, TmddProps, CVRestServiceProps {

    private TimeToLive sdwTtl;
    private BigDecimal defaultLaneWidth;
    private String cvRestService;
    private String[] rsuRoutes;
    private Integer httpLoggingMaxSize;
    private String odeUrl;
    private String sdwRestUrl;
    private String sdwApiKey;
    private String mailHost;
    private int mailPort;
    private String[] alertAddresses;
    private String fromEmail;
    private String rsuDataServiceUrl;
    private String tmddUrl;
    private String tmddUser;
    private String tmddPassword;

    private Double pointIncidentBufferMiles;

    public String getOdeUrl() {
        return odeUrl;
    }

    public Double getPointIncidentBufferMiles() {
        return pointIncidentBufferMiles;
    }

    public void setPointIncidentBufferMiles(Double pointIncidentBufferMiles) {
        this.pointIncidentBufferMiles = pointIncidentBufferMiles;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getSdwApiKey() {
        return sdwApiKey;
    }

    public void setSdwApiKey(String sdwApiKey) {
        this.sdwApiKey = sdwApiKey;
    }

    public int getMailPort() {
        return mailPort;
    }

    public void setMailPort(int mailPort) {
        this.mailPort = mailPort;
    }

    public String getMailHost() {
        return mailHost;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    public String[] getAlertAddresses() {
        return alertAddresses;
    }

    public void setAlertAddresses(String[] alertAddresses) {
        this.alertAddresses = alertAddresses;
    }

    public void setAlertAddresses(String alertAddresses) {
        this.alertAddresses = alertAddresses.split(",");
    }

    public String getSdwRestUrl() {
        return sdwRestUrl;
    }

    public void setSdwRestUrl(String sdwRestUrl) {
        this.sdwRestUrl = sdwRestUrl;
    }

    public void setOdeUrl(String odeUrl) {
        this.odeUrl = odeUrl;
    }

    public String getRsuDataServiceUrl() {
        return rsuDataServiceUrl;
    }

    public void setRsuDataServiceUrl(String rsuDataServiceUrl) {
        this.rsuDataServiceUrl = rsuDataServiceUrl;
    }

    public String getTmddUrl() {
        return tmddUrl;
    }

    public void setTmddUrl(String tmddUrl) {
        this.tmddUrl = tmddUrl;
    }

    public String getTmddUser() {
        return tmddUser;
    }

    public void setTmddUser(String tmddUser) {
        this.tmddUser = tmddUser;
    }

    public String getTmddPassword() {
        return tmddPassword;
    }

    public void setTmddPassword(String tmddPassword) {
        this.tmddPassword = tmddPassword;
    }

    public TimeToLive getSdwTtl() {
        return sdwTtl;
    }

    public Integer getHttpLoggingMaxSize() {
        return httpLoggingMaxSize;
    }

    public void setHttpLoggingMaxSize(Integer httpLoggingMaxSize) {
        this.httpLoggingMaxSize = httpLoggingMaxSize;
    }

    public String[] getRsuRoutes() {
        return rsuRoutes;
    }

    public void setRsuRoutes(String[] rsuRoutes) {
        this.rsuRoutes = rsuRoutes;
    }

    public String getCvRestService() {
        return cvRestService;
    }

    public void setCvRestService(String cvRestService) {
        this.cvRestService = cvRestService;
    }

    public BigDecimal getDefaultLaneWidth() {
        return defaultLaneWidth;
    }

    public void setDefaultLaneWidth(BigDecimal defaultLaneWidth) {
        this.defaultLaneWidth = defaultLaneWidth;
    }

    public void setSdwTtl(String sdwTtl) {
        this.sdwTtl = TimeToLive.valueOf(sdwTtl);
    }
}