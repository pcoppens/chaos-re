package be.pcoppens.chaos_reverse_eng.output.vizceral.builder;

import be.pcoppens.chaos_reverse_eng.input.ESBEntry;
import be.pcoppens.chaos_reverse_eng.model.EndPointEntry;
import be.pcoppens.chaos_reverse_eng.model.Service;
import be.pcoppens.chaos_reverse_eng.model.ServiceGroup;
import be.pcoppens.chaos_reverse_eng.output.vizceral.*;

import java.util.*;

public class DataBuilder {
    public static final String GLOBAL= "global";
    public static final String REGION= "region";
    public static final String NORMAL= "normal";
    public static final String WARNING= "warning";
    public static final String ESB= "ESB";
    public static final String INTERNET= "INTERNET";

    private DataBuilder() {
    }


    public static Data makeData(String name, String entryName){
        Data data= new Data();
        data.setName(name);
        data.setRenderer(REGION);
        data.getNodes().add(makeInternet(entryName));

        return data;
    }

    public static Data makeDataGlobal(String name, String entryName){
        Data data= makeData(name, entryName);
        data.setRenderer(GLOBAL);
        return data;
    }

    public static void addToData(Data data, ServiceGroup serviceGroup){
        Node node= makeNode(serviceGroup.getName());
        node.getNodes().add(makeInternet(serviceGroup.getName()));
        data.getNodes().add(node);
        Connection conn= makeConnection(INTERNET, node.getName());
        data.getConnections().add(conn);

        //process services
        if(serviceGroup.getServices()!=null){
            serviceGroup.getServices().forEach(service ->{
                addServiceToNode(node, service);
            });
        }
        //process group
        if(serviceGroup.getGroups()!=null){
            serviceGroup.getGroups().forEach(group->{
                addGroupToNode(node, group);
            });
        }
    }

    public static void addServiceToNode(Node parent, Service service){
        Node node= makeNode(service.getName());
        node.getNodes().add(makeInternet(service.getName()));
        parent.getNodes().add(node);
        Connection conn= makeConnection(INTERNET, node.getName());
        parent.getConnections().add(conn);

        //add callEntries to node
    }

    public static void addGroupToNode(Node parent, ServiceGroup group){
        Node node= makeNode(group.getName());
        node.getNodes().add(makeInternet(group.getName()));
        parent.getNodes().add(node);
        Connection conn= makeConnection(INTERNET, node.getName());
        parent.getConnections().add(conn);

        //add callEntries to node
    }

    public static Node makeNode(String name){
        Node node= new Node();
        node.setName(name);
        node.setRenderer(REGION);
        node.set_class(NORMAL);

        return node;
    }

    public static Node makeInternet(String displayName){
        Node internet= makeNode(INTERNET);
        internet.setDisplayName(displayName);

        return internet;
    }

    public static Connection makeConnection(String src, String target){
        Connection conn= new Connection();
        conn.setSource(src);
        conn.setTarget(target);
        return conn;
    }

    public static Connection makeConnection(String src, String target, double normalMetric){
        Connection conn= new Connection();
        conn.setSource(src);
        conn.setTarget(target);
        Metrics metrics= new Metrics();
        metrics.setNormal(normalMetric);
        conn.setMetrics(metrics);
        return conn;
    }

    public static Connection makeDefaultConnection(String target, boolean revertedConnection, double normalMetric) {
        Connection conn= revertedConnection?makeConnection(target, INTERNET):makeConnection(INTERNET, target);
        Metrics metrics= new Metrics();
        metrics.setNormal(normalMetric);
        conn.setMetrics(metrics);
        return conn;
    }

}
