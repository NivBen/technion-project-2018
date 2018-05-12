import java.io.Serializable;

public class Event implements Serializable {
    private int eventID;
    private String journeyID;
    private int autoID;
    private int engineSpeed;
    private int vehicleSpeed;
    private int roadSpeedLimit;
    private int transmissionGear;
    private int beamStatus;
    private String timestamp;

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public String getJourneyID() {
        return journeyID;
    }

    public void setJourneyID(String journeyID) {
        this.journeyID = journeyID;
    }

    public int getAutoID() {
        return autoID;
    }

    public void setAutoID(int autoID) {
        this.autoID = autoID;
    }

    public int getEngineSpeed() {
        return engineSpeed;
    }

    public void setEngineSpeed(int engineSpeed) {
        this.engineSpeed = engineSpeed;
    }

    public int getVehicleSpeed() {
        return vehicleSpeed;
    }

    public void setVehicleSpeed(int vehicleSpeed) {
        this.vehicleSpeed = vehicleSpeed;
    }

    public int getRoadSpeedLimit() {
        return roadSpeedLimit;
    }

    public void setRoadSpeedLimit(int roadSpeedLimit) {
        this.roadSpeedLimit = roadSpeedLimit;
    }

    public int getTransmissionGear() {
        return transmissionGear;
    }

    public void setTransmissionGear(int transmissionGear) {
        this.transmissionGear = transmissionGear;
    }

    public int getBeamStatus() {
        return beamStatus;
    }

    public void setBeamStatus(int beamStatus) {
        this.beamStatus = beamStatus;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
