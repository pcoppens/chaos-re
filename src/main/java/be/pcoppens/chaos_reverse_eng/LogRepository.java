package be.pcoppens.chaos_reverse_eng;

import be.pcoppens.generator.EndPoint;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.groupingBy;
import java.util.stream.Collectors;

import static be.pcoppens.chaos_reverse_eng.LogEntry.mapToLogEntry;

public class LogRepository {
    //saved lines
    private List<LogEntry> lines= new ArrayList<>();

    //corr. id and list of lines
    private Map<String, List<LogEntry>> callList ;

    //endpoints and number of call on it
    private Map<String, Long> endpoints;

    // list of Sv (List of endpoints) and number of use it
    private Map<Service, Long> services= new HashMap<>() ;


    public void read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            lines= buffer.lines().map(mapToLogEntry).collect(Collectors.toList());
            callList= lines.stream().collect(groupingBy(LogEntry::getCorrelationId));
            endpoints= lines.stream().collect(groupingBy(LogEntry::getUri, Collectors.counting()));
        }
    }

    private void buildServiceList(){
        if(callList==null)throw new IllegalArgumentException("callList must be initiaized. Call LogRepository.read() first.");
        for (List<LogEntry> list : callList.values()) {
            Service sv= new Service();
            for (LogEntry entry : list ) {
                sv.add(entry.getEndPointEntry());
            }
            services.put(sv,services.containsKey(sv)?services.get(sv)+1:1);
        }
    }

    public static void main(String args[])throws IOException {
        String fileName = "runDs.txt";
        LogRepository lr= new LogRepository();
        lr.read(new FileInputStream(fileName));
        lr.buildServiceList();
        lr.servicesToDotFile("discoverSv.dot");
        System.out.println(lr);

    }

    public void servicesToDotFile(String fileName) throws IOException {
        StringBuffer sb= new StringBuffer("digraph System{\n");
        // nodes
        endpoints.keySet().forEach(s->sb.append(String.format("\t \"%s\" ;\n",s)));

        //edges

        int listId=1;
        for (Service service: services.keySet()) {
            sb.append(String.format("Service_%d[shape=square];\n Service_%d->\"%s\"; ", listId, listId, service.get(0).toString()));
            listId++;
            sb.append(service.stream().map(f->f.toDot()).collect(Collectors.joining(" -> ")));
            sb.append(";\n");
        }
        sb.append("}");

        //write file
        Files.write(Paths.get(fileName), sb.toString().getBytes());

    }
    @Override
    public String toString() {
        String linesStr= lines.stream().map( l -> l.toString() ).collect( Collectors.joining( ", " ) );

        StringBuffer sb= new StringBuffer();
         callList.entrySet().stream().forEach(e-> sb.append("\t"+e.getKey()+" : "+e.getValue().size()));
        String callListStr=sb.toString();

        StringBuffer sb2= new StringBuffer();
        endpoints.entrySet().stream().forEach(e-> sb2.append("\t"+e.getKey()+" : "+e.getValue()));
        String endpointsStr= sb2.toString();

        StringBuffer sb3= new StringBuffer();
        services.entrySet().stream().forEach(e-> sb3.append("\t"+e.getKey()+" : "+e.getValue()+"\n"));
        String servicesStr= sb3.toString();

        return "LogRepository{\n" +
                "lines=" + linesStr +
                ",\n callList=" + callListStr +
                ",\n endpoints=" + endpointsStr +
                ",\n services=" + servicesStr +
                "\n}";
    }
}
