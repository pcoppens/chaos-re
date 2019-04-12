package be.pcoppens.chaos_reverse_eng.model;


import be.pcoppens.chaos_reverse_eng.model.CallEntry;
import be.pcoppens.chaos_reverse_eng.model.EndPointEntry;

import java.util.*;
import java.util.function.Function;

public class Service extends HashSet<CallEntry> {
    private String name;

    public Service(int i, String name) {
        super(i);
        this.name = name;
    }

    public Service(String name) {
        this.name = name;
    }

    public Service(Collection<? extends CallEntry> collection, String name) {
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
        if (!super.equals(o)) return false;
        Service that = (Service) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), getName());
    }
    public static Function<String, Service> mapToService = (name) -> {
        return new Service(name);
    };

    @Override
    public String toString() {
        StringBuffer sb= new StringBuffer(name+"{\n");
        for (CallEntry entry:this){
            sb.append("\t");
            sb.append(entry);
            sb.append("\n");
        }
        sb.append("\n}");
        return sb.toString();
    }
}