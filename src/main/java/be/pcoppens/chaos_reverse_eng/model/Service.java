package be.pcoppens.chaos_reverse_eng.model;


import java.util.*;

public class Service extends ArrayList<EndPointEntry> {
    private static int count=1;
    private String name;

    public Service(int i) {
        super(i);
        this.name = "Sv"+count++;
    }

    public Service() {
        this.name = "Sv"+count++;
    }

    public Service(Collection<? extends EndPointEntry> collection) {
        super(collection);
        this.name = "Sv"+count++;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        List other= (List)o;
        if(other.size() != this.size()) return false;
        for (int i=0; i<this.size();i++){
            if(!this.get(i).equals(other.get(i)))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        for (EndPointEntry entry:this){
            hash=31* hash+ entry.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuffer sb= new StringBuffer(name+"{\n");
        for (EndPointEntry entry:this){
            sb.append("\t");
            sb.append(entry);
            sb.append("\n");
        }
        sb.append("\n}");
        return sb.toString();
    }
}
