package cz.muni.fi.model;

/**
 * Created by Michal on 4.11.13.
 */
public class RssFingerprint {

    private String bssid;
    private int level;
    private int orientation;
    private String rp;
    private int average;
    private int deviation;

    public RssFingerprint() {

    }

    public RssFingerprint(String bssid, int orientation, String rp, int average, int deviation) {
        this.bssid = bssid;
        this.orientation = orientation;
        this.rp = rp;
        this.average = average;
        this.deviation = deviation;
    }

    public RssFingerprint(String bssid, int level, int orientation, String rp, int average, int deviation) {
        this.bssid = bssid;
        this.level = level;
        this.orientation = orientation;
        this.rp = rp;
        this.average = average;
        this.deviation = deviation;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getBssid() {
        return bssid;
    }

    public int getOrientation() {
        return orientation;
    }

    public String getRp() {
        return rp;
    }

    public int getDeviation() {
        return deviation;
    }

    public int getAverage() {
        return average;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void setRp(String rp) {
        this.rp = rp;
    }

    public void setAverage(int average) {
        this.average = average;
    }

    public void setDeviation(int deviation) {
        this.deviation = deviation;
    }

    @Override
    public String toString() {
        return "RssFingerprint{" +
                "bssid='" + bssid + '\'' +
                ", orientation=" + orientation +
                ", rp='" + rp + '\'' +
                ", average=" + average +
                ", deviation=" + deviation +
                '}';
    }
}
