package be.pcoppens.chaos_reverse_eng.input;

import be.pcoppens.chaos_reverse_eng.model.EndPointEntry;

import java.time.ZonedDateTime;
import java.util.function.Function;

public class LogEntry {
    private String svId;
    private EndPointEntry endPointEntry;
    private ZonedDateTime timeStamp;
    private String correlationId;

    public LogEntry(String svId, ZonedDateTime timeStamp, String host, String verb, String path, String correlationId) {
        this.svId = svId;
        this.timeStamp = timeStamp;
        this.endPointEntry= new EndPointEntry(verb, host, path);

        this.correlationId = correlationId;
    }

    public static Function<String, LogEntry> mapToLogEntry = (line) -> {
        String[] p = line.split("\t");
        String dateTime= p[1].substring(1,p[1].length()-1);
        return new LogEntry(p[0], ZonedDateTime.parse(dateTime), p[2], p[3], p[4], p[5]);
    };

    public String getUri(){
        return endPointEntry.toString();
    }

    public String getSvId() {
        return svId;
    }

    public ZonedDateTime getTimeStamp() {
        return timeStamp;
    }

    public EndPointEntry getEndPointEntry() {
        return endPointEntry;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "svId='" + svId + '\'' +
                ", timeStamp=" + timeStamp +
                ", endPointEntry='" + endPointEntry + '\'' +
                ", correlationId='" + correlationId + '\'' +
                '}';
    }
}
