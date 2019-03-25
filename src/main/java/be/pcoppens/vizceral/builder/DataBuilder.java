package be.pcoppens.vizceral.builder;

import be.pcoppens.vizceral.Connection;
import be.pcoppens.vizceral.Data;
import be.pcoppens.vizceral.Metrics;
import be.pcoppens.vizceral.Node;

public class DataBuilder {
    public static final String GLOBAL= "global";
    public static final String REGION= "region";
    public static final String NORMAL= "normal";
    public static final String ESB= "ESB";
    public static final String INTERNET= "INTERNET";

    private DataBuilder() {
    }

    public static Data makeData(String name){
        Data data= new Data();
        data.setName(name);
        data.setRenderer(REGION);

        return data;
    }


    public static Data makeDataGlobal(String name){
        Data data= new Data();
        data.setName(name);
        data.setRenderer(GLOBAL);

        data.getNodes().add(makeInternet(ESB));
        return data;
    }
    public static void addApp(Data global, String app){
        Node node= makeNode(app);
        global.getNodes().add(node);
        global.getConnections().add(makeDefaultConnection(app));
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

    public static Connection makeDefaultConnection(String target) {
        Connection conn= makeConnection(INTERNET, target);
        Metrics metrics= new Metrics();
        metrics.setNormal(100.0);
        conn.setMetrics(metrics);
        return conn;
    }

}
