package be.pcoppens.chaos_reverse_eng.application.model;


import java.util.*;
import java.util.function.Function;

/**
 * An EsbService is a Set of CallEntry with a name.
 */
public class EsbService extends HashSet<CallEntry> {
    private String name;

    public EsbService(int i, String name) {
        super(i);
        this.name = name;
    }

    public EsbService(String name) {
        this.name = name;
    }

    public EsbService(Collection<? extends CallEntry> collection, String name) {
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
        EsbService that = (EsbService) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), getName());
    }
    public static Function<String, EsbService> mapToService = (name) -> {
        return new EsbService(name);
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