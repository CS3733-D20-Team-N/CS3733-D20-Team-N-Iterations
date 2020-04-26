package edu.wpi.N.entities.request;

import java.util.GregorianCalendar;

public class FlowerRequest extends Request {

    private String patientName;
    private String visitorName;
    private String flowerName;
    private double creditNum;
    FlowerRequest(int requestID, int emp_assigned, String reqNotes, String compNotes, String nodeID, GregorianCalendar timeRequested, GregorianCalendar timeCompleted, String status, String patientName, String visitorName, String flowerName, int creditNum) {
        super(requestID, emp_assigned, reqNotes, compNotes, nodeID, timeRequested, timeCompleted, status);
        this.patientName = patientName;
        this.visitorName = visitorName;
        this.flowerName = flowerName;
        this.creditNum = creditNum;
    }

    @Override
    public String getServiceType() {
        return "Flower";
    }

    public String getPatientName() {
        return patientName;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public String getFlowerName() {
        return flowerName;
    }

    public double getCreditNum() {
        return creditNum;
    }
}
