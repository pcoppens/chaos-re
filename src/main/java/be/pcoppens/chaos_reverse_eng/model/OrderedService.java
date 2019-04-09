package be.pcoppens.chaos_reverse_eng.model;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Service define by a ordered list of enpoint. Each enpoint call the next one in this order.
 */
public class OrderedService extends ArrayList<EndPointEntry> {
    private String name;

    public OrderedService(int i, String name) {
        super(i);
        this.name = name;
    }

    public OrderedService(String name) {
        this.name = name;
    }

    public OrderedService(Collection<? extends EndPointEntry> collection, String name) {
        super(collection);
        this.name = name;
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
