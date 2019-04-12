package be.pcoppens.chaos_reverse_eng.core;

import be.pcoppens.chaos_reverse_eng.core.LevenshteinTool;
import be.pcoppens.chaos_reverse_eng.model.CallEntry;
import be.pcoppens.chaos_reverse_eng.model.EndPointEntry;
import be.pcoppens.chaos_reverse_eng.model.Service;
import be.pcoppens.chaos_reverse_eng.model.ServiceGroup;

import java.util.*;

public class SystemDiscoverTool {
    private SystemDiscoverTool(){}

    public static boolean isSimilarByPath(EndPointEntry a, EndPointEntry b){
       return
               a.getVerb().equalsIgnoreCase(b.getVerb()) &&
               a.getPath().equalsIgnoreCase(b.getPath());
    }

    /**
     * SimilarityScore is equal to 1- getLevenshteinDistance(): 1 means equals;
     * different verb return 0
     */
    public static float getSimilarityScore(EndPointEntry a, EndPointEntry b){
        if(a==null || b==null || ( ! a.getVerb().equalsIgnoreCase(b.getVerb())))
            return 1-1;
        if(isSimilarByPath(a, b))
            return 1-0;
        int score =LevenshteinTool.getLevenshteinDistance(a.getPath(), b.getPath());
        return 1-(score/Float.max(a.getPath().length(), b.getPath().length()));
    }

    public static ServiceGroup removeSimilarEnpointEntryByService(ServiceGroup group, float similarityScore){
        if(group.getServices()==null)
            throw new IllegalArgumentException("services must be not null");

        Set<EndPointEntry> entries= new HashSet<>();

        List<Service> traitedService= new ArrayList<>();
        group.getServices().forEach(service ->{
            Service sv= new Service(service.getName());
            service.forEach(entry->{

                EndPointEntry src=entry.getSource(), target=entry.getTarget();

                if(entries.isEmpty()){
                    entries.add(src);
                    entries.add(target);
                }else{
                    //is equals: do nothing
                    if(! entries.contains(entry.getSource())){
                        EndPointEntry foundSrc= entries.stream()
                                .filter(endPointEntry -> getSimilarityScore(endPointEntry, entry.getSource()) > similarityScore )
                                .findAny().orElse(null);
                        if(foundSrc!=null){
                            foundSrc.addSimilar(src);
                            src= foundSrc;
                        }else{
                            entries.add(src);
                        }
                    }
                    //is equals: do nothing
                    if(! entries.contains(entry.getTarget())){
                        EndPointEntry foundTarget= entries.stream()
                                .filter(endPointEntry -> getSimilarityScore(endPointEntry, entry.getTarget()) > similarityScore )
                                .findAny().orElse(null);
                        if(foundTarget!=null){
                            foundTarget.addSimilar(target);
                            target= foundTarget;
                        }else{
                            entries.add(target);
                        }
                    }
                }

                sv.add(new CallEntry(src, target));

            });
            traitedService.add(sv);
        });

        System.out.println("entries: "+entries.size());
        return new ServiceGroup(group.getName(), traitedService);
    }

    public static Set<EndPointEntry> getFragileEndpoint(ServiceGroup group, int fragility){
        if(group.getServices()==null)
            throw new IllegalArgumentException("services must be not null");

        Set<EndPointEntry> entries= new HashSet<>();

        group.getServices().stream().forEach(service->{
            service.forEach(callEntry -> {
                if(!callEntry.getSource().getHost().contains("0.0.0.0") && callEntry.getSource().getSimilars().size()+1<fragility){
                    entries.add(callEntry.getSource());
                }
                if(!callEntry.getTarget().getHost().contains("0.0.0.0") && callEntry.getTarget().getSimilars().size()+1<fragility){
                    entries.add(callEntry.getTarget());
                }
            });
        });

        return entries;
    }

    public static Set<Service> getFragileService(ServiceGroup group, int fragility){
        if(group.getServices()==null)
            throw new IllegalArgumentException("services must be not null");

        Set<Service> entries= new HashSet<>();

        group.getServices().stream().forEach(service->{
            service.forEach(callEntry -> {
                if(!callEntry.getSource().getHost().contains("0.0.0.0") && callEntry.getSource().getSimilars().size()+1<fragility){
                    entries.add(service);
                }
                if(!callEntry.getTarget().getHost().contains("0.0.0.0") && callEntry.getTarget().getSimilars().size()+1<fragility){
                    entries.add(service);
                }
            });
        });

        return entries;
    }
}
