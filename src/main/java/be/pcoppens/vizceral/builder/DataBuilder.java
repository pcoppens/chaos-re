package be.pcoppens.vizceral.builder;

import be.pcoppens.chaos_reverse_eng.ESBEntry;
import be.pcoppens.chaos_reverse_eng.EndPointEntry;
import be.pcoppens.vizceral.*;

import java.util.*;

public class DataBuilder {
    public static final String GLOBAL= "global";
    public static final String REGION= "region";
    public static final String NORMAL= "normal";
    public static final String ESB= "ESB";
    public static final String INTERNET= "INTERNET";

    private DataBuilder() {
    }

    public static void addNode(Data data, List<ESBEntry> entries){
        Map<String, Node> nodes= new HashMap<>();
        Node internet= makeInternet(ESB);
        entries.stream().forEach(esbEntry->{
            String client= esbEntry.getClient();
            Node app=null;
            if(nodes.containsKey(client)){
                app= nodes.get(client);
            }
            else {
                app= addApp(data, client, true);
                app.getNodes().add(internet);
                nodes.put(client, app);
            }
            Node start= makeNode(esbEntry.getEsbEndpoint().toString());
            start.setDisplayName(getName(esbEntry.getEsbEndpoint()));
            Node end= makeNode(esbEntry.getBackendEntry().toString());
            end.setDisplayName(getName(esbEntry.getBackendEntry()));
            app.getNodes().add(start);
            app.getNodes().add(end);
            app.getConnections().add(makeConnection(internet.getName(), start.getName(), 10));
            app.getConnections().add(makeConnection(start.getName(), end.getName(),10));

        });
    }


    private static String getName(EndPointEntry entry){

        if(entry.getHost().contains("0.0.0.0"))
            return entry.getPath().substring(10);
        return entry.getPath();
    }

    public static Data makeData(String name){
        Data data= new Data();
        data.setName(name);
        data.setRenderer(REGION);
        data.getNodes().add(makeInternet(ESB));

        return data;
    }


    public static Data makeDataGlobal(String name){
        Data data= makeData(name);
        data.setRenderer(GLOBAL);
        return data;
    }

    public static Node addApp(Data global, String app, boolean revertedConnection){
        Node node= makeNode(app);
        global.getNodes().add(node);
        global.getConnections().add(makeDefaultConnection(app, revertedConnection));

        return node;
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

    public static Connection makeDefaultConnection(String target, boolean revertedConnection) {
        Connection conn= revertedConnection?makeConnection(target, INTERNET):makeConnection(INTERNET, target);
        Metrics metrics= new Metrics();
        metrics.setNormal(100.0);
        conn.setMetrics(metrics);
        return conn;
    }

}
