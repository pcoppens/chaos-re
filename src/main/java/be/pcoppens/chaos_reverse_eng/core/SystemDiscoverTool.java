package be.pcoppens.chaos_reverse_eng.core;

import be.pcoppens.chaos_reverse_eng.model.CallEntry;
import be.pcoppens.chaos_reverse_eng.model.EndPointEntry;
import be.pcoppens.chaos_reverse_eng.model.EsbService;
import be.pcoppens.chaos_reverse_eng.model.ServiceGroup;

import java.util.*;
import java.util.stream.Collectors;

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
        if(group.getEsbServices()==null)
            throw new IllegalArgumentException("services must be not null");

        List<EsbService> traitedEsbService = new ArrayList<>();
        group.getEsbServices().forEach(service ->{
            Set<EndPointEntry> entries= new HashSet<>();
            EsbService sv= new EsbService(service.getName());
            if(sv.getName().equalsIgnoreCase("enretp_ext")){
                System.out.println(service);
            }
            service.forEach(entry->{

                EndPointEntry src=entry.getSource(), target=entry.getTarget();

                if(entries.isEmpty()){
                    entries.add(src);
                    entries.add(target);
                }else{
                    //is equals: do nothing
                    if(! entries.contains(entry.getSource())){
                        EndPointEntry foundSrc= getSimilar(entries, entry.getSource(), similarityScore);
                        if(foundSrc!=null){
                            foundSrc.addSimilar(src);
                            src= foundSrc;
                        }else{
                            entries.add(src);
                        }
                    }
                    //is equals: do nothing
                    if(! entries.contains(entry.getTarget())){
                        EndPointEntry foundTarget= getSimilar(entries, entry.getTarget(), similarityScore);
                        if(foundTarget!=null){
                            foundTarget.addSimilar(target);
                            target= foundTarget;
                        }else{
                            entries.add(target);
                        }
                    }

                }

            });
            service.forEach(entry->sv.add(
                    new CallEntry(getSimilar(entries, entry.getSource(), similarityScore),
                                  getSimilar(entries, entry.getTarget(), similarityScore))));


            traitedEsbService.add(sv);
        });


        return new ServiceGroup(group.getName(), traitedEsbService);
    }

    private static EndPointEntry getSimilar(Set<EndPointEntry> entries, EndPointEntry entry, float similarityScore ){
       return entries.stream()
                .filter(endPointEntry -> getSimilarityScore(endPointEntry, entry) > similarityScore )
                .findAny().orElse(null);
    }

    public static Set<EndPointEntry> getFragileEndpoint(ServiceGroup group, int fragility){
        if(group.getEsbServices()==null)
            throw new IllegalArgumentException("services must be not null");

        Set<EndPointEntry> entries= new HashSet<>();

        group.getEsbServices().stream().forEach(service->{
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
    public static List<ServiceGroup> getPseudoApp(ServiceGroup group, int prefixSize){
        if(group.getEsbServices()==null)
            throw new IllegalArgumentException("services must be not null");

        Map<String, List<EsbService>> groups= new HashMap<>();

        group.getEsbServices().stream().forEach(service->{
            String key= service.getName().substring(0, (prefixSize>service.getName().length()?service.getName().length():prefixSize));
            if(groups.containsKey(key)){
                groups.get(key).add(service);
            }else {
                List<EsbService> esbServices = new ArrayList<>();
                esbServices.add(service);
                groups.put(key, esbServices);
            }
        });
        return  groups.keySet().stream().map(key->
                new ServiceGroup(groups.get(key).size()<2?
                        groups.get(key).get(0).getName()
                        :longestCommonPrefix(groups.get(key).stream().map(service -> service.getName()).collect(Collectors.toList()).toArray(new String[0])),
                        groups.get(key))).collect(Collectors.toList());
    }

    public static String longestCommonPrefix(String[] strs) {
        if(strs==null || strs.length ==0){
            return "";
        }

        if(strs.length == 1){
            return strs[0];
        }

        int i=0;
        while(true){
            boolean flag = true;
            for(int j=1; j<strs.length; j++){
                if(strs[j].length()<=i || strs[j-1].length() <=i
                        || strs[j].charAt(i) != strs[j-1].charAt(i)){
                    flag = false;
                    break;
                }
            }

            if(flag){
                i++;
            }else{
                break;
            }
        }

        return strs[0].substring(0, i);
    }

    public static Set<EsbService> getFragileService(ServiceGroup group, int fragility){
        if(group.getEsbServices()==null)
            throw new IllegalArgumentException("services must be not null");

        Set<EsbService> entries= new HashSet<>();

        group.getEsbServices().stream().forEach(service->{
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