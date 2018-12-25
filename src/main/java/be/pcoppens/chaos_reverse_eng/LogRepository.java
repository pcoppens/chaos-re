package be.pcoppens.chaos_reverse_eng;

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


    public void read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            lines= buffer.lines().map(mapToLogEntry).collect(Collectors.toList());
            callList= lines.stream().collect(groupingBy(LogEntry::getCorrelationId));
            endpoints= lines.stream().collect(groupingBy(LogEntry::getUri, Collectors.counting()));
        }
    }

    public static void main(String args[])throws IOException {
        String fileName = "runDs.txt";
        LogRepository lr= new LogRepository();
        lr.read(new FileInputStream(fileName));
        System.out.println(lr);

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

        return "LogRepository{\n" +
                "lines=" + linesStr +
                ",\n callList=" + callListStr +
                ",\n endpoints=" + endpointsStr +
                "\n}";
    }
}
