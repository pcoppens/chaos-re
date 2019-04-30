package be.pcoppens.chaos_reverse_eng.application.output.vizceral;

import java.util.ArrayList;
import java.util.List;

public class Data {

    private String renderer;
    private String name;
    private List<Node> nodes = new ArrayList<>();
    private List<Connection> connections = new ArrayList<>();
    private long serverUpdateTime;

    public Data() {
    }

    public String getRenderer() {
        return renderer;
    }

    public void setRenderer(String renderer) {
        this.renderer = renderer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    public long getServerUpdateTime() {
        return serverUpdateTime;
    }

    public void setServerUpdateTime(long serverUpdateTime) {
        this.serverUpdateTime = serverUpdateTime;
    }
}

