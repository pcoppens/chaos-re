package be.pcoppens.chaos_reverse_eng.output.vizceral.builder;

import be.pcoppens.chaos_reverse_eng.input.ESBEntry;
import be.pcoppens.chaos_reverse_eng.model.CallEntry;
import be.pcoppens.chaos_reverse_eng.model.EndPointEntry;
import be.pcoppens.chaos_reverse_eng.model.Service;
import be.pcoppens.chaos_reverse_eng.model.ServiceGroup;
import be.pcoppens.chaos_reverse_eng.output.vizceral.*;

import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * Structure: root -> view ->app -> {node AND subnode in the same level}
     * @param data
     * @param serviceGroup
     */
    public static void addAppToData(Data data, ServiceGroup serviceGroup){
        Node parent= makeNode(serviceGroup.getName());
        parent.getNodes().add(makeInternet(serviceGroup.getName()));
        data.getNodes().add(parent);
        Connection connection= makeConnection(INTERNET, parent.getName());
        data.getConnections().add(connection);

        //process group
        if(serviceGroup.getGroups()!=null){
            serviceGroup.getGroups().forEach(group->{
                Node node= makeNode(group.getName());
                node.getNodes().add(makeInternet(group.getName()));
                parent.getNodes().add(node);
                Connection conn= makeConnection(INTERNET, node.getName());
                parent.getConnections().add(conn);

                group.getServices().forEach(sv->addServiceToParent(node, sv));
            });
        }
    }

    public static void addServiceToNode(Node parent, Service service){
        Node node= makeNode(service.getName());
        node.getNodes().add(makeInternet(service.getName()));
        parent.getNodes().add(node);
        Connection conn= makeConnection(INTERNET, node.getName());
        parent.getConnections().add(conn);

        service.forEach(callEntry -> addCallEntry(node, callEntry));
    }

    public static void addServiceToParent(Node parent, Service service){
        Node node= makeNode(service.getName());
        node.getNodes().add(makeInternet(service.getName()));
        parent.getNodes().add(node);
        Connection conn= makeConnection(INTERNET, node.getName());
        parent.getConnections().add(conn);

        service.forEach(entry -> {
            parent.getNodes().add(makeNode(entry.getSource()));
            parent.getNodes().add(makeNode(entry.getTarget()));
            parent.getConnections().add(makeConnection(getName(entry.getSource()), getName(entry.getTarget())));
            parent.getConnections().add(makeConnection(node.getName(), getName(entry.getSource())));
        });
    }

    public static void addCallEntry(Node node, CallEntry entry){
        node.getNodes().add(makeNode(entry.getSource()));
        node.getNodes().add(makeNode(entry.getTarget()));
        node.getConnections().add(makeConnection(getName(entry.getSource()), getName(entry.getTarget())));
    }

    private static String getName(EndPointEntry entry){
        if(entry.getHost().contains("0.0.0.0"))
            return entry.getVerb()+entry.getPath().substring(10);
        return entry.getVerb()+entry.getPath();
    }


    public static Node makeNode(EndPointEntry endPointEntry) {
        Node node= makeNode(getName(endPointEntry));
        if(!endPointEntry.getHost().contains("0.0.0.0")){
            Notice notice= new Notice();
            String title= endPointEntry.getHost().substring(7)+", " +
                    endPointEntry.getSimilars().stream()
                            .map(entry->entry.getHost()).map(s->s.substring(7)).collect( Collectors.joining( ", " ));
            notice.setTitle(title);
            if(endPointEntry.getSimilars().size()<1)
                notice.setSeverity(Notice.WARNING);
            node.getNotices().add(notice);
        }

        return node;
    }

    public static void addGroupToNode(Node parent, ServiceGroup group){
        Node node= makeNode(group.getName());
        node.getNodes().add(makeInternet(group.getName()));
        parent.getNodes().add(node);
        Connection conn= makeConnection(INTERNET, node.getName());
        parent.getConnections().add(conn);

        group.getServices().forEach(sv->addServiceToNode(node, sv));
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
