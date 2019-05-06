package be.pcoppens.chaos_reverse_eng.application.core;

import be.pcoppens.chaos_reverse_eng.application.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core class to discover a distributed system.
 * Static methods only.
 */
public class SystemDiscoverTool {
    /**
     * Deny object creation.
     */
    private SystemDiscoverTool(){}

    /**
     * @pre: EndPointEntry a & b are not null and verb & path are not null for both.
     * @param a
     * @param b
     * @return true if a.verb==b.verb and a.path==b.path
     */
    public static boolean isSimilarByPath(EndPointEntry a, EndPointEntry b){
       return
               a.getVerb().equalsIgnoreCase(b.getVerb()) &&
               a.getPath().equalsIgnoreCase(b.getPath());
    }

    /**
     * SimilarityScore is equal to 1- getLevenshteinDistance(): 1 means equals;
     * different verb return 0
     * @param a
     * @param b
     * @return SimilarityScore
     */
    public static float getSimilarityScore(EndPointEntry a, EndPointEntry b){
        if(a==null || b==null || ( ! a.getVerb().equalsIgnoreCase(b.getVerb())))
            return 1-1;
        if(isSimilarByPath(a, b))
            return 1-0;
        int score =LevenshteinTool.getLevenshteinDistance(a.getPath(), b.getPath());
        return 1-(score/Float.max(a.getPath().length(), b.getPath().length()));
    }

    /**
     * For each service in group remove similar service (by regard of similarityScore) and
     * link similar services together.
     * @pre: group is not null and group.getEsbServices() is not null
     * @post: group are unmodified.
     * @param group
     * @param similarityScore
     * @return a new ServiceGroup
     */
    public static ServiceGroup removeSimilarEnpointEntryByService(ServiceGroup group, float similarityScore){
        if(group.getEsbServices()==null)
            throw new IllegalArgumentException("services must be not null");

        List<EsbService> traitedEsbService = new ArrayList<>();
        group.getEsbServices().forEach(service ->{
            Set<EndPointEntry> entries= new HashSet<>();
            EsbService sv= new EsbService(service.getName());

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

    /**
     *
     * @pre: group is not null and group.getEsbServices() is not null
     * @post: group are unmodified.
     * @param group
     * @param fragility
     * @return a set of EndPointEntry that (number of similar +1) < fragility
     */
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

    /**
     *
     * @pre: group is not null and group.getEsbServices() is not null
     * @post: group are unmodified.
     * @param group
     * @param prefixSize
     * @return a list of ServiceGroup. Each ServiceGroup is a group of service that share the same prefix of prefixSize
     */
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

    /**
     * @post: strs is unmodified.
     * @param strs
     * @return the longuest common prefix
     */
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

    /**
     *
     * @pre: group is not null and group.getEsbServices() is not null
     * @post: group are unmodified.
     * @param group
     * @param fragility
     * @returna set of EsbService that (number of similar +1) < fragility
     */
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

    /**
     *
     * @param supportedFailure int: number of supported failures
     * @return
     */
    public static Set<Service> getFragileServices(Set<Service> services,
                                                  Map<EndPointEntry, Set<EndPointEntry>> similarEndpoints,
                                                  Map<Service, Set<Service>> similarServices,
                                                  int supportedFailure){

        Set<Service> result= new HashSet<>();
        List<EndPointEntry> list= new ArrayList<>(similarEndpoints.keySet());
        for (Service sv: similarServices.keySet()) {
            boolean isFragile=false;
            //check if each endpoint is redundant
            for (int i=0;!isFragile && i<sv.size();i++ ) {
                Set<EndPointEntry> redundantEnpoints= similarEndpoints.get(list.get(getId(list, sv.get(i))));
                if(redundantEnpoints.size()<supportedFailure){
                    result.add(sv);
                    isFragile=true;
                }
                //endpoint must be called by at least 1+{supportedFailure} endpoints
                else{
                    //ignore the first one
                    if(i>0){
                        final int index=i;
                        if(
                                services.stream()
                                        .filter(s->s.get(index).equals(sv.get(index)))
                                        .filter(s->similarEndpoints.get(list.get(getId(list, s.get(index))))
                                                .size()>supportedFailure).toArray().length >0){ //in future length will be used
                            result.add(sv);
                            isFragile=true;
                        }
                    }
                }
            }
        }


        return result;
    }

    public static int getId(List<EndPointEntry> list, EndPointEntry e){
        if(list.contains(e))
            return list.indexOf(e);
        for (EndPointEntry entry: list) {
            if(entry.isSimilar(e))
                return list.indexOf(entry);
        }
        throw new IllegalArgumentException(String.format("Endpoint (%s) must be in the list or must be similar to at least one entry", e.toString()));
    }

    /**
     * Apply Bayes inference to discover similar services.
     * @param endpoints {IN}
     * @param services {IN}
     * @param similarEndpoints {OUT}
     * @param similarServices {OUT}
     */
    public static void bayes(Set<EndPointEntry> endpoints,
                             Set<Service> services,
                             Map<EndPointEntry, Set<EndPointEntry>> similarEndpoints,
                             Map<Service, Set<Service>> similarServices){

        // endpoints
        /*
        P[f=f'|data]= P[data|f=f'] . P[f=f']
                      -------------------
                           P[data]

            P[data|f=f'] -> 1   // will be lower if data quality are low
            P[f=f']      -> f.score(f')
            P[data]      -> 1   // simplification of bayes inference

            => P[f=f'|data]= f.score(f') . 1/1
         */
        endpoints.forEach(e-> {
            boolean found=false;
            if(!similarEndpoints.containsKey(e) ){
                for (EndPointEntry similarKey: similarEndpoints.keySet()) {
                    if(e.isSimilar(similarKey)) {
                        similarEndpoints.get(similarKey).add(e) ;
                        found=true;
                    }
                }
            }
            if(!similarEndpoints.containsKey(e) && ! found){
                // found no similar endpoint -> put in the list
                similarEndpoints.put(e, new HashSet<>());
            }
        });

        //service
        /*
        P[Sv=Sv'|data]= P[data|Sv=Sv'] . P[Sv=Sv']
                      -------------------
                           P[data]

            P[data|Sv=Sv'] -> 1    // will be lower if data quality are low
            P[Sv=Sv']      -> P[f_1=f_1'] . P[f_n=f_n']
            P[data]        -> 1   // simplification of bayes inference

            P[Sv=Sv'|data]= P[f_1=f_1'] . P[f_n=f_n'] . 1/1
         */
        services.forEach(sv-> {
            boolean found=false;
            if(!similarServices.containsKey(sv) ){
                for (Service similarKey: similarServices.keySet()) {
                    //in future we can consider a missing endpoint with a very low score
                    if(sv.size()== similarKey.size()){
                        float score= 1;
                        for (int i = 0; i < sv.size(); i++) {

                            score= score * SystemDiscoverTool.getSimilarityScore(similarKey.get(i), sv.get(i));
                        }
                        // un future the fixed value will be a user parameter (dependent of the system)
                        if(score > 0.9) {
                            similarServices.get(similarKey).add(sv) ;
                            found=true;
                        }
                    }
                }
            }
            if(!similarServices.containsKey(sv) && ! found){
                // found no similar service -> put in the list
                similarServices.put(sv, new HashSet<>());
            }
        });
    }

}