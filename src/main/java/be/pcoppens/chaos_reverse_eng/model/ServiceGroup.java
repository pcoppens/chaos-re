package be.pcoppens.chaos_reverse_eng.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ServiceGroup {
    private String name;
    private Collection<Service> services;
    private Collection<ServiceGroup> groups;

    public ServiceGroup(String name, Collection<Service> services) {
        this.name = name;
        this.services = services;
    }

    public ServiceGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Collection<Service> getServices() {
        return Collections.unmodifiableCollection(services);
    }

    public Collection<ServiceGroup> getGroups() {
        return Collections.unmodifiableCollection(groups);
    }

    public void setGroups(Collection<ServiceGroup> groups) {
        if(services==null)
            throw new IllegalArgumentException("ServiceGroup accept groups only is services is null !");
        this.groups = groups;
    }
}