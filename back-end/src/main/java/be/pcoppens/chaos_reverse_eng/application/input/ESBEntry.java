package be.pcoppens.chaos_reverse_eng.application.input;

import be.pcoppens.chaos_reverse_eng.application.model.EndPointEntry;

import java.util.Objects;
import java.util.function.Function;

public class ESBEntry {

    //properties
    private String client;
    private EndPointEntry esbEndpoint;
    private EndPointEntry backendEntry;

    public ESBEntry(String client, EndPointEntry esbEndpoint, EndPointEntry backendEntry) {
        this.client = client;
        this.esbEndpoint = esbEndpoint;
        this.backendEntry = backendEntry;
    }

    public String getClient() {
        return client;
    }

    public EndPointEntry getEsbEndpoint() {
        return esbEndpoint;
    }

    public EndPointEntry getBackendEntry() {
        return backendEntry;
    }

    @Override
    public String toString() {
        return "ESBEntry{" +
                "client='" + client + '\'' +
                ", esbEndpoint=" + esbEndpoint +
                ", backendEntry=" + backendEntry +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ESBEntry esbEntry = (ESBEntry) o;
        return client.equalsIgnoreCase(esbEntry.client) &&
                esbEndpoint.equals(esbEntry.esbEndpoint) &&
                backendEntry.equals(esbEntry.backendEntry);
    }

    public static Function<String, ESBEntry> mapToESEntry = (line) -> {
        String[] p = line.split(",");
        // ignore line that contain less than 3 column (client, entry, backend)
        if(p.length<3)
            return null;
        String client= p[0];
        String start=null;
        String end=null;
        for (String s: p){
            if(s.startsWith("http")) {
                if (start == null)
                    start = s;
                else {
                    if (end == null)
                        end = s;
                    else
                    if(! end.contains("0.0.0.0"))
                        throw new RuntimeException("found more than 2 uri for the line !");
                    else
                        end=s;
                }
            }
        }
        if(start==null || end == null)
            return null;

        return new ESBEntry(client,
                EndPointEntry.parse(start),
                EndPointEntry.parse(end));
    };

    @Override
    public int hashCode() {
        return Objects.hash(client, esbEndpoint, backendEntry);
    }
}
