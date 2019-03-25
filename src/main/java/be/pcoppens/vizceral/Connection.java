package be.pcoppens.vizceral;

import java.util.List;

public class Connection {

    private String source;
    private String target;
    private Metrics metrics;
    private List<Object> notices = null;
    private String _class;

    public Connection() {
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    public List<Object> getNotices() {
        return notices;
    }

    public void setNotices(List<Object> notices) {
        this.notices = notices;
    }

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }
}
