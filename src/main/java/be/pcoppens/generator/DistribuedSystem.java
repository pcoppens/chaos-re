package be.pcoppens.generator;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class DistribuedSystem {

    private static Random rnd= new Random(System.currentTimeMillis());

    private Set<EndPoint> endpoints= new HashSet<>();
    private Map<EndPoint, List<EndPoint>> redondants= new HashMap<>();
    private List<List<EndPoint>> services= new ArrayList<>();
    private List<EndPoint> redondantFailure= new ArrayList<>();


    public static DistribuedSystem buildSystem(int redondant, int endpoint, int num, boolean avoidCrossing){
        DistribuedSystem distribuedSystem= new DistribuedSystem();
        for (int i = 0; i < endpoint; i++) {
            EndPoint f= new EndPoint();
            distribuedSystem.endpoints.add(f);
            List<EndPoint> list= new ArrayList<>(redondant);
            distribuedSystem.redondants.put(f, list);
            for (int j = 0; j < redondant; j++) {
                list.add(f.getRedondantServer());
            }
        }
        distribuedSystem.buildService(num, avoidCrossing);
        return distribuedSystem;
    }

    public static DistribuedSystem buildSystem(int redondant, int endpoint, int num, boolean avoidCrossing, int failure){
        if(failure >endpoint)
            throw new IllegalArgumentException("failure cannot be greater than endpoint.");
        DistribuedSystem distribuedSystem= buildSystem(redondant, endpoint, num, avoidCrossing);
        Iterator<EndPoint> iterator= distribuedSystem.endpoints.iterator();
        for (int i = 0; i < failure; i++) {
            distribuedSystem.redondantFailure.add(iterator.next());
        }
        return distribuedSystem;
    }

    private void buildService(int num, boolean avoidCrossing){
        List<EndPoint> available= new ArrayList<>(endpoints);
        for (int i = 0; i < num; i++) {
            List<EndPoint> service= new ArrayList<>();
            int max= (rnd.nextInt(endpoints.size()) %3)+2;
            for (int j = 0; j < max; j++) {
                EndPoint f=null;
                while(f==null && available.size()>0){
                    f=available.get(rnd.nextInt(available.size()));
                    if(service.contains(f)) //avoid same function in a service
                        f=null;
                    //avoid link to a start
                }
                if(f==null)
                    return; // no more sv available

               if(avoidCrossing) // avoid cycle
                    available.remove(f);
                service.add(f);
            }
            services.add(service);
        }

    }

    /**
     * return a Function redondant to f. Use Random
     * @param f
     * @return
     */
    private EndPoint loadBalancing(EndPoint f){
        if(redondantFailure.contains(f))
            return f;
        List<EndPoint> list= redondants.get(f);
        int index= rnd.nextInt(list.size()+1) ;
        return index>=list.size()?f:list.get(index);
    }

    public void runSystem(){
        for (List<EndPoint> service:services) {
            String correlationId=  UUID.randomUUID().toString();
            for (EndPoint f: service) {
                System.out.println(loadBalancing(f).getLogLine(correlationId));
            }
        }
    }

    public void runSystem(OutputStream out) throws IOException {
        for (List<EndPoint> service:services) {
            String correlationId=  UUID.randomUUID().toString();
            for (EndPoint f: service) {
                out.write(loadBalancing(f).getLogLine(correlationId).getBytes());
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer sb= new StringBuffer("EndPoints{\n");
        for (EndPoint f: endpoints) {
            sb.append(String.format("\t%s: %s", f.getId(), f.getURI()));
            for(EndPoint fr: redondants.get(f)){
                sb.append(String.format("\t%s: %s", fr.getId(), fr.getURI()));
            }
            sb.append("\n");
        }
        sb.append("}");

        sb.append("\nServices{\n");
        for (List<EndPoint> list: services) {
            sb.append("Sv: ");
            for (EndPoint f:list) {
                sb.append(String.format("--> %s   ", f.getId()));
            }
            sb.append("\n");
        }
        sb.append("}");

        sb.append("\nRedondant Failures{\n");
        for (EndPoint f: redondantFailure) {
            sb.append(String.format("-- %s \n", f.getId()));

        }
        sb.append("}");
        return sb.toString();
    }

    public void toDotFile(String fileName) {
        StringBuffer sb= new StringBuffer("digraph System{\n");
        // nodes
        for (EndPoint f: endpoints) {
            sb.append(String.format("\t%s ; ", f.getId()));
            for(EndPoint fr: redondants.get(f)){
                sb.append(String.format("\t%s ; ", fr.getId()));
            }
            sb.append("\n");
        }
        //edges

        int listId=1;
        for (List<EndPoint> list: services) {
            sb.append(String.format("Service_%d[shape=square];\n Service_%d->%s; ", listId, listId, list.get(0).getId()));
            if(!redondantFailure.contains(list.get(0))){
                for (EndPoint f:redondants.get(list.get(0))) {
                    sb.append(String.format("Service_%d->%s; ", listId, f.getId()));
                }
            }
            listId++;
            sb.append(list.stream().map(f->f.getId()).collect(Collectors.joining(" -> ")));
            sb.append(";\n");
            for(int i=0;i<list.size()-1;i++){ //all function exempt the last one
                if(!redondantFailure.contains(list.get(i+1))) //if not in failure list
                    for (EndPoint f:redondants.get(list.get(i+1))) {
                        sb.append(String.format("%s ->%s; ", list.get(i).getId(), f.getId())); // f -> redondant
                        if (!redondantFailure.contains(list.get(i))) {//if not in failure list
                            for (EndPoint f2 : redondants.get(list.get(i))) {
                                sb.append(String.format("%s ->%s; ", f2.getId(), f.getId()));
                                sb.append(String.format("%s ->%s; ", f2.getId(), list.get(i+1).getId()));
                            }
                        }
                    }
            }
            sb.append("\n");
        }
        sb.append("}");

        //write file
        try {
            Files.write(Paths.get(fileName), sb.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws InterruptedException {
        DistribuedSystem ds= DistribuedSystem.buildSystem(2,15,2,true,0);
        ds.toDotFile("test1.dot");

        RunSystem r1= new RunSystem(ds);
        RunSystem r2= new RunSystem(ds);

        new Thread(r1).start();
        new Thread(r2).start();
        Thread.sleep(5000);
        r1.isRunning=false;
        r2.isRunning=false;

    }

}
