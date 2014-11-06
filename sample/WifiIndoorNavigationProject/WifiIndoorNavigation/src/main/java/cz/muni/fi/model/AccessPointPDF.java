package cz.muni.fi.model;

/**
 * Created by Michal on 15.3.14.
 */
public class AccessPointPDF {
    private String bssid;
    private String rp;
    private double pdf;

    public AccessPointPDF(String bssid, String rp, double pdf) {
        this.bssid = bssid;
        this.rp = rp;
        this.pdf = pdf;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getRp() {
        return rp;
    }

    public void setRp(String rp) {
        this.rp = rp;
    }

    public double getPdf() {
        return pdf;
    }

    public void setPdf(double pdf) {
        this.pdf = pdf;
    }

    @Override
    public String toString() {
        return "AccessPointPDF{" +
                "bssid='" + bssid + '\'' +
                ", rp='" + rp + '\'' +
                ", pdf=" + pdf +
                '}';
    }
}
