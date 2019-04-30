package be.pcoppens.chaos_reverse_eng.model;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ServiceGroup {
    private String name;
    private Collection<EsbService> esbServices;
    private Collection<ServiceGroup> groups;

    public ServiceGroup(String name, Collection<EsbService> esbServices) {
        this.name = name;
        this.esbServices = esbServices;
    }

    public ServiceGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Collection<EsbService> getEsbServices() {
        return esbServices !=null?Collections.unmodifiableCollection(esbServices):null;
    }

    public Collection<ServiceGroup> getGroups() {
        return groups!=null?Collections.unmodifiableCollection(groups):null;
    }

    public void setGroups(Collection<ServiceGroup> groups) {
        if(esbServices !=null)
            throw new IllegalArgumentException("ServiceGroup accept groups only is services is null !");
        this.groups = groups;
    }
    public static Function<String, ServiceGroup> mapToServiceGroup = (name) -> {
        return new ServiceGroup(name);
    };

    public static ServiceGroup create(String name, Collection<ServiceGroup> groups){
        ServiceGroup serviceGroup= new ServiceGroup(name);
        serviceGroup.setGroups(groups);
        return serviceGroup;
    }

    @Override
    public String toString() {
        return "ServiceGroup{" +
                "name='" + name + '\'' +
                ",\n services=[" + (esbServices ==null?"null": esbServices.stream().map(entry->entry.toString()).collect( Collectors.joining( ", " ) ) )+ "]"+
                ",\n groups=[" + (groups==null?"null":groups.stream().map(entry->entry.toString()).collect( Collectors.joining( ", " ) )  )+ "]"+
                '}';
    }
}
