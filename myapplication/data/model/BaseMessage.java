package fr.myapplication.dc.myapplication.data.model;

/**
 * Created by Crono on 04/12/16.
 */

public class BaseMessage extends Message {

    public String from;
    public String to;
    public String zone;

    public BaseMessage(){}

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    @Override
    public String toString(){

        StringBuilder b = new StringBuilder();

        b.append("from " + from);
        b.append(" to " + to);
        b.append(" zone " + zone);

        return b.toString();
    }

}

