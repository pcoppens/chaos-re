package be.pcoppens.chaos_reverse_eng.application.input;

import be.pcoppens.chaos_reverse_eng.application.model.CallEntry;
import be.pcoppens.chaos_reverse_eng.application.model.EsbService;
import be.pcoppens.chaos_reverse_eng.application.model.ServiceGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static be.pcoppens.chaos_reverse_eng.application.input.ESBEntry.mapToESEntry;

public class LogESBEntry {
    private String src;
    private List<String> targets;

    public LogESBEntry() {
    }

    public LogESBEntry(String src, List<String> targets) {
        this.src = src;
        this.targets = targets;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public List<String> getTargets() {
        return targets;
    }

    public void setTargets(List<String> targets) {
        this.targets = targets;
    }

    @Override
    public String toString() {
        return "LogESBEntry{" +
                "src='" + src + '\'' +
                ", targets.size=" + (targets!=null?targets.size():0) +
                '}';
    }

    public static Function<String, LogESBEntry> mapToLogESBEntry = (line) -> {
        String[] p = line.split(",");
        return new LogESBEntry(p[0], p.length<2?null:Arrays.asList(p).subList(1, p.length-1));
    };

    public static ServiceGroup read(InputStream input, String name) throws IOException {
        Map<String, EsbService> services = new HashMap();

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            List<String> lines = buffer.lines().collect(Collectors.toList());
            List<ESBEntry> esbClient= lines.stream().map(mapToESEntry).filter(entry->entry!=null).collect(Collectors.toList());

            esbClient.forEach(esbEntry -> {
                EsbService sv=null;
                String client= esbEntry.getClient();
                if(! services.containsKey(client)){
                    sv= new EsbService(client);
                    services.put(client, sv);
                }
                else{
                    sv=services.get(client);
                }
                sv.add(new CallEntry(esbEntry.getEsbEndpoint(), esbEntry.getBackendEntry()));

            });
        }
        return new ServiceGroup(name, services.values());
    }
}