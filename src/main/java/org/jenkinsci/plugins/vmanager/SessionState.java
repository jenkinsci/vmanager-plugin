package org.jenkinsci.plugins.vmanager;

public class SessionState {

    private String id = "NA";
    private String status = "NA";
    private String name = "NA";
    private String totalRuns = "NA";
    private String passed = "NA";
    private String failed = "NA";
    private String running = "NA";
    private String waiting = "NA";
    private String other = "NA";
    private String owner = "NA";
    private String numOfSession = "NA";
    private String serverUrl = "NA";
    private String sessionCode = "NA";
    private String idNames = "NA";

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getNumOfSession() {
        return numOfSession;
    }

    public void setNumOfSession(String numOfSession) {
        this.numOfSession = numOfSession;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(String totalRuns) {
        this.totalRuns = totalRuns;
    }

    public String getPassed() {
        return passed;
    }

    public void setPassed(String passed) {
        this.passed = passed;
    }

    public String getFailed() {
        return failed;
    }

    public void setFailed(String failed) {
        this.failed = failed;
    }

    public String getRunning() {
        return running;
    }

    public void setRunning(String running) {
        this.running = running;
    }

    public String getWaiting() {
        return waiting;
    }

    public void setWaiting(String waiting) {
        this.waiting = waiting;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getIdNames() {
        return idNames;
    }

    public void setIdNames(String idNames) {
        this.idNames = idNames;
    }
}
