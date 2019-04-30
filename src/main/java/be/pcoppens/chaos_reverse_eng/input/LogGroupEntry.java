package be.pcoppens.chaos_reverse_eng.input;


import be.pcoppens.chaos_reverse_eng.model.EsbService;
import be.pcoppens.chaos_reverse_eng.model.ServiceGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static be.pcoppens.chaos_reverse_eng.model.EsbService.mapToService;

public class LogGroupEntry {
    private String name;
    private List<String> childs;

    public LogGroupEntry(String name, List<String> childs) {
        this.name = name;
        this.childs = childs;
    }

    public String getName() {
        return name;
    }

    public List<String> getChilds() {
        return Collections.unmodifiableList(childs);
    }


    public List<EsbService> getChildsAsServices() {
        return childs.stream().map(mapToService).collect(Collectors.toList());
    }

    public List<ServiceGroup> getChildsAsGroup() {
        return childs.stream().map(ServiceGroup.mapToServiceGroup).collect(Collectors.toList());
    }

    public static Function<String, LogGroupEntry> mapToLogGroupEntry = (line) -> {
        String[] p = line.split(",");
        return new LogGroupEntry(p[0], p.length<2?null: Arrays.asList(p).subList(1, p.length-1));
    };

    public static Function<LogGroupEntry, ServiceGroup> mapToServiceGroup = (logGroupEntry) -> {
        return ServiceGroup.create(logGroupEntry.getName(), logGroupEntry.getChildsAsGroup());
    };

    public static Function<LogGroupEntry, ServiceGroup> mapToServiceGroupSv = (logGroupEntry) -> {
        return new ServiceGroup(logGroupEntry.getName(), logGroupEntry.getChildsAsServices());
    };

    @Override
    public String toString() {
        String childsStr= childs.stream().collect(Collectors.joining( ", " ) );
        return "LogGroupEntry{" +
                "name='" + name + '\'' +
                ", childs=[" + childs +"]"+
                '}';
    }
    public static ServiceGroup readGroup(InputStream input, String name, boolean isGroup) throws IOException {

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            List<String> lines = buffer.lines().collect(Collectors.toList());
            List<LogGroupEntry> entries= lines.stream().map(mapToLogGroupEntry).filter(entry->entry!=null).collect(Collectors.toList());

            List<ServiceGroup> groups= entries.stream().map(isGroup?mapToServiceGroup:mapToServiceGroupSv).collect(Collectors.toList());

            return ServiceGroup.create(name, groups);
        }
    }
}