package be.pcoppens.chaos_reverse_eng.output.vizceral;

public class Notice {
    public static final int DEFAULT=0;
    public static final int WARNING=1;
    public static final int ERROR=2;
    private String title;
    private String link;
    private int severity;

    public Notice() {
    }

    public Notice(String title, String link, int severity) {
        this.title = title;
        this.link = link;
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    @Override
    public String toString() {
        return "Notice{" +
                "title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", severity=" + severity +
                '}';
    }
}
