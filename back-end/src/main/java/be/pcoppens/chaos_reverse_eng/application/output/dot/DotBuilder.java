package be.pcoppens.chaos_reverse_eng.application.output.dot;

import be.pcoppens.chaos_reverse_eng.application.core.SystemDiscoverTool;
import be.pcoppens.chaos_reverse_eng.application.model.EndPointEntry;
import be.pcoppens.chaos_reverse_eng.application.model.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * writer for dot file.
 */
public class DotBuilder {
    /**
     * avoid instance creation.
     */
    private DotBuilder(){}


    public static void servicesToDotFile(Set<Service> services, String fileName, String name) throws IOException {

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

    public static void detailledServicesToDotFile(Set<Service> highLevelServices, String fileName, String name, Map<Service, Set<Service>> similarServices) throws IOException {

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

    public static void servicesToDotFile(Set<EndPointEntry> similarEndpoints,
                                  Set<Service> similarServices,
                                  String fileName) throws IOException {
        List<EndPointEntry> list= new ArrayList<>(similarEndpoints);

        StringBuffer sb= new StringBuffer("digraph System{\n");
        // nodes
        list.forEach(e->sb.append(String.format("\t endpoint%d[label=\"%s\"] ;\n",list.indexOf(e), e.toString())));

        //edges
        int listId=1;
        for (Service service: similarServices) {
            sb.append(String.format("Service_%d[shape=square];\n Service_%d->endpoint%d; ", listId, listId, SystemDiscoverTool.getId(list,service.get(0))));
            listId++;
            sb.append(service.stream().map(e->String.format("endpoint%d",SystemDiscoverTool.getId(list,e))).collect(Collectors.joining(" -> ")));
            sb.append(";\n");
        }
        sb.append("}");

        //write file
        Files.write(Paths.get(fileName), sb.toString().getBytes());
    }
}
