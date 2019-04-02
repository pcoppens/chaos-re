package be.pcoppens.chaos_reverse_eng;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

import java.util.stream.Collector;
import java.util.stream.Collectors;

import static be.pcoppens.chaos_reverse_eng.LogEntry.mapToLogEntry;

public class LogRepository {
    //saved lines
    private List<LogEntry> lines= new ArrayList<>();

    //correlation id and list of lines
    private Map<String, List<LogEntry>> callList ;

    //endpoints and number of call on it
    private Map<EndPointEntry, Long> endpoints;

    // list of Sv (List of endpoints) and number of use it
    private Map<Service, Long> services= new HashMap<>() ;

    // list of similar EndPointEntry by EndPointEntry
    private Map<EndPointEntry, Set<EndPointEntry>> similarEndpoints = new HashMap();

    // list of similar Service by Service
    private Map<Service, Set<Service>> similarServices= new HashMap();


    private void read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            lines= buffer.lines().map(mapToLogEntry).collect(Collectors.toList());
            callList= lines.stream().collect(groupingBy(LogEntry::getCorrelationId));
            endpoints= lines.stream().collect(groupingBy(LogEntry::getEndPointEntry, Collectors.counting()));
        }
    }

    private void buildServiceList(){
        if(callList==null)throw new IllegalArgumentException("callList must be initialized. Call LogRepository.read() first.");
        for (List<LogEntry> list : callList.values()) {
            Service sv= new Service();
            for (LogEntry entry : list ) {
                sv.add(entry.getEndPointEntry());
            }
            services.put(sv,services.containsKey(sv)?services.get(sv)+1:1);
        }
    }

    private void bayes(){
        // endpoints
        /*
        P[f=f'|data]= P[data|f=f'] . P[f=f']
                      -------------------
                           P[data]

            P[data|f=f'] -> 1   // will be lower if data quality are low
            P[f=f']      -> f.score(f')
            P[data]      -> 1   // simplification of bayes inference

            => P[f=f'|data]= f.score(f') . 1/1
         */
        endpoints.keySet().forEach(e-> {
            boolean found=false;
            if(!similarEndpoints.containsKey(e) ){
                for (EndPointEntry similarKey: similarEndpoints.keySet()) {
                    if(e.isSimilar(similarKey)) {
                        similarEndpoints.get(similarKey).add(e) ;
                        found=true;
                    }
                }
            }
            if(!similarEndpoints.containsKey(e) && ! found){
                // found no similar endpoint -> put in the list
                similarEndpoints.put(e, new HashSet<>());
            }
        });

        //service
        /*
        P[Sv=Sv'|data]= P[data|Sv=Sv'] . P[Sv=Sv']
                      -------------------
                           P[data]

            P[data|Sv=Sv'] -> 1    // will be lower if data quality are low
            P[Sv=Sv']      -> P[f_1=f_1'] . P[f_n=f_n']
            P[data]        -> 1   // simplification of bayes inference

            P[Sv=Sv'|data]= P[f_1=f_1'] . P[f_n=f_n'] . 1/1
         */
        services.keySet().forEach(sv-> {
            boolean found=false;
            if(!similarServices.containsKey(sv) ){
                for (Service similarKey: similarServices.keySet()) {
                    //in future we can consider a missing endpoint with a very low score
                    if(sv.size()== similarKey.size()){
                        float score= 1;
                        for (int i = 0; i < sv.size(); i++) {
                            score= score * similarKey.get(i).getSimilarityScore(sv.get(i));
                        }
                        // in future the fixed value will be a user parameter (dependent of the system)
                        if(score > 0.9) {
                            similarServices.get(similarKey).add(sv) ;
                            found=true;
                        }
                    }
                }
            }
            if(!similarServices.containsKey(sv) && ! found){
                // found no similar service -> put in the list
                similarServices.put(sv, new HashSet<>());
            }
        });
    }

    /**
     *
     * @param supportedFailure int: number of supported failures
     * @return
     */
    private Set<Service> getFragileServices(int supportedFailure){
        Set<Service> result= new HashSet<>();
        List<EndPointEntry> list= new ArrayList<>(similarEndpoints.keySet());
        for (Service sv: similarServices.keySet()) {
            boolean isFragile=false;
            //check if each endpoint is redundant
            for (int i=0;!isFragile && i<sv.size();i++ ) {
                Set<EndPointEntry> redundantEnpoints= similarEndpoints.get(list.get(getId(list, sv.get(i))));
                if(redundantEnpoints.size()<supportedFailure){
                    result.add(sv);
                    isFragile=true;
                }
                //endpoint must be called by at least 1+{supportedFailure} endpoints
                else{
                    //ignore the first one
                    if(i>0){
                        final int index=i;
                        if(
                                services.keySet().stream()
                                        .filter(s->s.get(index).equals(sv.get(index)))
                                        .filter(s->similarEndpoints.get(list.get(getId(list, s.get(index))))
                                                .size()>supportedFailure).toArray().length >0){ //in future length will be used
                            result.add(sv);
                            isFragile=true;
                        }
                    }
                }
            }
        }

        return result;
    }

    private static void servicesToDotFile(Set<Service> services, String fileName, String name) throws IOException {

        StringBuffer sb= new StringBuffer("strict digraph \""+name +"\" {\n");
        int listId=1;
        for (Service service: services) {

            sb.append(String.format("Service_%d[shape=square];\n Service_%d->\"%s\"; ", listId, listId, service.get(0)));
            listId++;
            sb.append(service.stream().map(e->String.format("\"%s\"",e)).collect(Collectors.joining(" -> ")));
            sb.append(";\n");
        }
        sb.append("}");

        //write file
        Files.write(Paths.get(fileName), sb.toString().getBytes());
    }

    private void detailledServicesToDotFile(Set<Service> highLevelServices, String fileName, String name) throws IOException {

        StringBuffer sb= new StringBuffer("strict digraph \""+name +"\" {\n");
        int listId=1;
        Set<Service> set= new HashSet<>(highLevelServices);
        for (Service service: highLevelServices) {
            Service top=null;
            if(similarServices.containsKey(service))
                top= service;
            else{
                for (Service redundantService:similarServices.keySet()) {
                    if(similarServices.get(redundantService).contains(service))
                        top=redundantService;
                }
            }
            set.add(top);
            set.addAll(similarServices.get(top));
        }
        for (Service service: set) {

            sb.append(String.format("Service_%d[shape=square];\n Service_%d->\"%s\"; ", listId, listId, service.get(0)));
            listId++;
            sb.append(service.stream().map(e->String.format("\"%s\"",e)).collect(Collectors.joining(" -> ")));
            sb.append(";\n");
        }
        sb.append("}");

        //write file
        Files.write(Paths.get(fileName), sb.toString().getBytes());
    }

    private void servicesToDotFile(String fileName) throws IOException {
        List<EndPointEntry> list= new ArrayList<>(similarEndpoints.keySet());

        StringBuffer sb= new StringBuffer("digraph System{\n");
        // nodes
        list.forEach(e->sb.append(String.format("\t endpoint%d[label=\"%s\"] ;\n",list.indexOf(e), e.toString())));

        //edges
        int listId=1;
        for (Service service: similarServices.keySet()) {
            sb.append(String.format("Service_%d[shape=square];\n Service_%d->endpoint%d; ", listId, listId, getId(list,service.get(0))));
            listId++;
            sb.append(service.stream().map(e->String.format("endpoint%d",getId(list,e))).collect(Collectors.joining(" -> ")));
            sb.append(";\n");
        }
        sb.append("}");

        //write file
        Files.write(Paths.get(fileName), sb.toString().getBytes());
    }
    private int getId(List<EndPointEntry> list, EndPointEntry e){
        if(list.contains(e))
            return list.indexOf(e);
        for (EndPointEntry entry: list) {
            if(entry.isSimilar(e))
                return list.indexOf(entry);
        }
        throw new IllegalArgumentException(String.format("Endpoint (%s) must be in the list or must be similar to at least one entry", e.toString()));
    }

    /**
     * discover the system by reading the log and produce dot files
     * @param fileName the log file.
     * @param discoverSvDot containt the discovered system.
     * @param fragileDot containt all fragility points.
     * @param fragileDetailsDot containt the detail about thes fragilities.
     * @return
     */

    public void discoverSystem(String fileName, String discoverSvDot, String fragileDot, String fragileDetailsDot){
         /*
        read(new FileInputStream(fileName));
        buildServiceList();
        bayes();
        servicesToDotFile(discoverSvDot);
        Set<Service> fragiles= lr.getFragileServices(1);
        servicesToDotFile(fragiles, fragileDot, "Fragile");
        detailledServicesToDotFile(fragiles, fragileDetailsDot", "fragile");
        */
    }

    @Override
    public String toString() {
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

        return "LogRepository{\n" +
                //     "lines=" + linesStr +
                //     ",\n callList=" + callListStr +
                ",\n endpoints=" + endpointsStr +
                ",\n services=" + servicesStr +
                "\n}";
    }


    public static void main(String args[])throws IOException {
        String fileName = "runDs.txt";
        String discoverSvDot = "discoverSv.dot";
        String fragileDot = "fragile.dot";
        String fragileDetailsDot = "fragileDetails.dot";

        LogRepository lr= new LogRepository();
        lr.discoverSystem(fileName, discoverSvDot, fragileDot, fragileDetailsDot);

    }
}
