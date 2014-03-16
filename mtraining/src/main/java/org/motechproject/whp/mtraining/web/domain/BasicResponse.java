package org.motechproject.whp.mtraining.web.domain;

public class BasicResponse implements MotechResponse {
    private Long callerId;
    private String sessionId;
    private String uniqueId;
    private int responseCode;
    private String responseMessage;

    public BasicResponse() {
    }

    public BasicResponse(Long callerId, String sessionId, String uniqueId, ResponseStatus responseStatus) {
        this.callerId = callerId;
        this.sessionId = sessionId;
        this.uniqueId = uniqueId;
        this.responseCode = responseStatus.getCode();
        this.responseMessage = responseStatus.getMessage();
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public String getResponseMessage() {
        return responseMessage;
    }

    @Override
    public Long getCallerId() {
        return callerId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }


}