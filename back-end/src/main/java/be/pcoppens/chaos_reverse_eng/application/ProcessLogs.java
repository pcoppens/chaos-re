package be.pcoppens.chaos_reverse_eng.application;

import be.pcoppens.chaos_reverse_eng.application.core.SystemDiscoverTool;
import be.pcoppens.chaos_reverse_eng.application.input.LogEntry;
import be.pcoppens.chaos_reverse_eng.application.model.EndPointEntry;
import be.pcoppens.chaos_reverse_eng.application.model.Service;
import be.pcoppens.chaos_reverse_eng.application.output.dot.DotBuilder;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static be.pcoppens.chaos_reverse_eng.application.input.LogEntry.mapToLogEntry;
import static java.util.stream.Collectors.groupingBy;

/**
 * Main class.
 * Build a representation of a system from Generated System log.
 * Process:
 *  1. Read the "runDs.txt" file
 *  2. Use the SystemDiscoverTool to build a abstraction of the system.
 *  3. Write dot files ("dot/") that represent the discovered system.
 */
public class ProcessLogs {
    private static String FILENAME = "runDs.txt";
    private static String FRAGILE_FILENAME = "dot/fragile.dot";
    private static String FRAGILE_DETAILS_FILENAME = "dot/fragileDetails.dot";
    private static String DISCOVER_SV_FILENAME = "dot/discoverSv.dot";


    //saved lines
    private static List<LogEntry> lines= new ArrayList<>();

    //corr. id and list of lines
    private static Map<String, List<LogEntry>> callList ;

    //endpoints and number of call on it
    private static Map<EndPointEntry, Long> endpoints;

    // list of Sv (List of endpoints) and number of use it
    private static Map<Service, Long> services= new HashMap<>() ;


    // list of similar EndPointEntry by EndPointEntry
    private static Map<EndPointEntry, Set<EndPointEntry>> similarEndpoints = new HashMap();

    // list of similar Service by Service
    private static Map<Service, Set<Service>> similarServices= new HashMap();

    private static Set<Service> fragiles;


    private static void readData() throws IOException {

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(FILENAME)))) {
            lines= buffer.lines().map(mapToLogEntry).collect(Collectors.toList());
            callList= lines.stream().collect(groupingBy(LogEntry::getCorrelationId));
            endpoints= lines.stream().collect(groupingBy(LogEntry::getEndPointEntry, Collectors.counting()));
        }
    }

    private static void buildServiceList(){
        if(callList==null)throw new IllegalArgumentException("callList must be initialized. Call LogRepository.read() first.");
        for (List<LogEntry> list : callList.values()) {
            Service sv= new Service();
            for (LogEntry entry : list ) {
                sv.add(entry.getEndPointEntry());
            }
            services.put(sv,services.containsKey(sv)?services.get(sv)+1:1);
        }
    }

    private static void discover(){
        buildServiceList();
        SystemDiscoverTool.bayes(endpoints.keySet(), services.keySet(), similarEndpoints, similarServices);
        fragiles= SystemDiscoverTool.getFragileServices(services.keySet(), similarEndpoints, similarServices,1);

    }

    private static void sysOut(){
        //  String linesStr= lines.stream().map( l -> l.toString() ).collect( Collectors.joining( ",\n" ) );

        StringBuffer sb= new StringBuffer();
        //    callList.entrySet().stream().forEach(e-> sb.append("\t"+e.getKey()+" : "+e.getValue().size()));
        String callListStr=sb.toString();

        StringBuffer sb2= new StringBuffer();
        endpoints.entrySet().stream().forEach(e-> sb2.append(String.format(
                "\t%s -%d- : %d\n",
                e.getKey(), similarEndpoints.containsKey(e.getKey())? similarEndpoints.get(e.getKey()).size():0,e.getValue())));
        String endpointsStr= sb2.toString();

        StringBuffer sb3= new StringBuffer();
        services.entrySet().stream().forEach(sv-> sb3.append(String.format(
                "\t%s -%d- : %d\n",
                sv.getKey().getName(), similarServices.containsKey(sv.getKey())? similarServices.get(sv.getKey()).size():0,sv.getValue())));
        String servicesStr= sb3.toString();

        System.out.println( "LogRepository{\n" +
                //     "lines=" + linesStr +
                //     ",\n callList=" + callListStr +
                ",\n endpoints=" + endpointsStr +
                ",\n services=" + servicesStr +
                "\n}" );

    }

    private static void toDot()throws IOException{
        DotBuilder.servicesToDotFile(fragiles, FRAGILE_FILENAME, "Fragile");
        DotBuilder.detailledServicesToDotFile(fragiles, FRAGILE_DETAILS_FILENAME, "fragile", similarServices);
        DotBuilder.servicesToDotFile(similarEndpoints.keySet(), similarServices.keySet() , DISCOVER_SV_FILENAME);
    }


    private static void process() throws IOException{
        readData();
        discover();
        //  sysOut();
        toDot();
    }

    public static void main(String args[])throws IOException {
        process();
    }
}
