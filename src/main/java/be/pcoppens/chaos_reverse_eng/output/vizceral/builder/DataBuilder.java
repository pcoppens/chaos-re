package be.pcoppens.chaos_reverse_eng.output.vizceral.builder;

import be.pcoppens.chaos_reverse_eng.input.ESBEntry;
import be.pcoppens.chaos_reverse_eng.model.EndPointEntry;
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

    public static List<Node> addAll(Data data, List<ESBEntry> entries){
        Map<String, Node> nodes= new HashMap<>();

        Node internet= makeInternet(INTERNET);
        data.getNodes().add(internet);

        entries.forEach(esbEntry -> {
            String client= esbEntry.getClient();
            Node app=null;
            if(nodes.containsKey(client)){
                app= nodes.get(client);
            }
            else {
                app= makeNode(client);
                Node internalInternet= makeInternet(client);
                app.getNodes().add(internalInternet);
                nodes.put(client, app);
                data.getNodes().add(app);
                data.getConnections().add(makeConnection(internet.getName(),app.getName()));
            }

            Node start= makeNode(esbEntry.getEsbEndpoint());
            start.setDisplayName(getName(esbEntry.getEsbEndpoint()));
            Node end= makeNode(esbEntry.getBackendEntry());
            end.setDisplayName(getName(esbEntry.getBackendEntry()));

            AddNode(data.getNodes(), start);
            AddNode(data.getNodes(), end);

            data.getConnections().add(makeConnection(app.getName(),start.getName()));
            data.getConnections().add(makeConnection(start.getName(),end.getName()));

            //insert node to app
            addNodeToNode(app, start);
            addNodeToNode(app, end);
            app.getConnections().add(makeConnection(internet.getName(),start.getName()));
            app.getConnections().add(makeConnection(start.getName(),end.getName()));
        });

        //setApp warning
        final List<Node> warningList= new ArrayList<>();
        nodes.values().stream().filter(node ->hasWarning(node)).forEach(node -> {
            Notice notice= new Notice();
            notice.setSeverity(Notice.WARNING);
            node.getNotices().add(notice);
            warningList.add(node);
        });
        return warningList;
    }
    private static void AddNode(List<Node> nodes, Node child){
        if(nodes.contains(child)){
            Node node= nodes.get(nodes.indexOf(child));
            if(node.getNotices()!=null && node.getNotices().size()>0){
                Notice notice= node.getNotices().get(0);
                if(! notice.getTitle().contains(child.getNotices().get(0).getTitle()))
                    notice.setTitle(notice.getTitle()+", "+child.getNotices().get(0).getTitle());
                notice.setSeverity(Notice.DEFAULT);
            }
        }
        else {
            nodes.add(child);
        }
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
            Node start= makeNode(esbEntry.getEsbEndpoint());
            start.setDisplayName(getName(esbEntry.getEsbEndpoint()));
            Node end= makeNode(esbEntry.getBackendEntry());
            end.setDisplayName(getName(esbEntry.getBackendEntry()));

            addNodeToNode(app, start);
            addNodeToNode(app, end);

            app.getConnections().add(makeConnection(internet.getName(), start.getName(), 10));
            app.getConnections().add(makeConnection(start.getName(), end.getName(),10));
        });
    }

    public static void addNode(List<Node> nodes, List<ESBEntry> entries){
        Node internet= makeInternet(ESB);
        entries.stream().forEach(esbEntry->{
            final String client= esbEntry.getClient();
            nodes.stream().filter(n->n.getName().equalsIgnoreCase(client)).forEach(app->{
                    Node start= makeNode(esbEntry.getEsbEndpoint());
                    start.setDisplayName(getName(esbEntry.getEsbEndpoint()));
                    Node end= makeNode(esbEntry.getBackendEntry());
                    end.setDisplayName(getName(esbEntry.getBackendEntry()));

                    if(!start.getName().equalsIgnoreCase(app.getName())){
                        addNodeToNode(app, start);
                    }
                    addNodeToNode(app, end);

                    app.getConnections().add(makeConnection(internet.getName(), start.getName(), 10));
                    app.getConnections().add(makeConnection(start.getName(), end.getName(),10));
            });
        });
    }

    public static void addAllDefect(Data data, List<Node> entries){
        Node internet= makeInternet(INTERNET);

        entries.forEach(app -> {
            AddNode(data.getNodes(), app);
            data.getConnections().add(makeConnection(internet.getName(),app.getName()));
        });

    }

    public static List<Node> addNode(Data data, Map<String,List<String>> groups){
        List<Node> nodes= new ArrayList<>();

        Node internet= makeInternet(INTERNET);
        groups.keySet().stream().forEach(client->{
            Node app=addApp(data, client, false);
            app.getNodes().add(internet);
            List<String> list= groups.get(client);
            list.forEach(s->{
                Node node= makeNode(s);
                nodes.add(node);
                addNodeToNode(app, node);
                app.getConnections().add(makeConnection(internet.getName(), node.getName(), 10/list.size()));
            });
        });

        return nodes;
    }

    private static boolean hasWarning(Node node){
        List<Notice> notices= node.getNotices();

        if(notices!=null && notices.stream().anyMatch(n->n.getSeverity()==Notice.WARNING) )
                return true;

        if(node.get_class()!=null && node.get_class().equalsIgnoreCase(WARNING))
            return true;

        List<Node> nodes= node.getNodes();
        if(nodes!= null && nodes.stream().anyMatch(n->hasWarning(n)))
            return true;

        return false;
    }


    public static Node makeNode(EndPointEntry entry){
        Node node= new Node();
        node.setRenderer(REGION);

        node.setName(entry.getVerb()+entry.getPath());
        Notice notice= new Notice();
        notice.setTitle(entry.getHost());

        // local call
        if(entry.getHost().contains("0.0.0.0"))
            notice.setSeverity(Notice.DEFAULT);
        else
            notice.setSeverity(Notice.WARNING);

        node.getNotices().add(notice);

        return node;
    }

    private static void addNodeToNode(Node parent, Node child){
        List<Node> nodes= parent.getNodes();
        if(nodes.contains(child)){
            Node node= nodes.get(nodes.indexOf(child));
            if(node.getNotices()!=null && node.getNotices().size()>0){
                Notice notice= node.getNotices().get(0);
                if(! notice.getTitle().contains(child.getNotices().get(0).getTitle()))
                    notice.setTitle(notice.getTitle()+", "+child.getNotices().get(0).getTitle());
                notice.setSeverity(Notice.DEFAULT);
            }
        }
        else {
            nodes.add(child);
        }
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
        data.getNodes().add(makeInternet(INTERNET));

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
