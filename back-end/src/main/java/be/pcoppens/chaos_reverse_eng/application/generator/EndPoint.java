package be.pcoppens.chaos_reverse_eng.application.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * Represent an endpoint.
 * Used by generator only.
 */
class EndPoint {

    private static final String HOST = "SERVER";
    private static final String[] VERBS= {"GET", "POST","DELETE", "PUT"};
    private static Random rnd= new Random(System.currentTimeMillis());
    private static int count= 0;

    //properties
    private String path;
    private String verb= VERBS[rnd.nextInt(VERBS.length)];
    private String host= HOST+getRandomString();
    private int pathLength;
    private String id= "f"+count++;

    /**
     * build a endpoint with a path.length of pathLength.
     * @pre: pathLength > 0
     * @param pathLength
     */
    public EndPoint(int pathLength){
        if(pathLength<1)
            throw new IllegalArgumentException("pathLength must be greater than 0");
        this.pathLength= pathLength;
        path= buildPath();
    }

    /**
     * build a endpoint with a path.length of random 1..5.
     */
    public EndPoint(){
        this.pathLength= rnd.nextInt(5)+1;
        path= buildPath();
    }

    private EndPoint(String path, String verb, String host){
        this.path= path;
        this.verb= verb;
        this.host= host;
    }

    /**
     *
     * @return duplicate endpoint with an another (random) host
     */
    public EndPoint getRedondantServer(){
        return new EndPoint(path, verb, HOST+getRandomString());
    }

    /**
     *
     * @return a new endpoint with the same verb & host.
     */
    public EndPoint getRedondantSameServer(){
        return new EndPoint(buildPath(), verb, host);
    }

    private String buildPath(){
        StringBuffer sb= new StringBuffer();
        for (int i = 0; i < pathLength; i++) {
            sb.append("/");
            sb.append(getRandomString());
        }
        return sb.toString();
    }

    /**
     * return a random word from the kant.txt file.
     * @return
     */
    public static String getRandomString() {
        File file = new File(EndPoint.class.getClassLoader().getResource("kant.txt").getFile());
        List<String> lines = null;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] arr = lines.toArray(new String[lines.size()])[0].split("\\s+");

        int index=0;
        while(true){
            index= rnd.nextInt(arr.length);
            if(arr[index].length()>3)
                return arr[index].replaceAll("[^\\w\\s]","");
        }
    }

    /**
     *
     * @return thr URI for the endpoint in format verb+" "+host+path
     */
    public String getURI(){
        return verb+" "+host+path;
    }

    /**
     * format: id [dateTime] server verb path correlationId
     * @return
     */
    public String getLogLine(String correlationId){
        return String.format("%s\t[%s]\t%s\t%s\t%s\t%s\n",
                id,ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),host,verb,path,correlationId);
    }

    public String getId(){return id;}

    @Override
    public String toString() {
        return "Function{" +
                "id='" + id + '\'' +
                "path='" + path + '\'' +
                ", verb='" + verb + '\'' +
                ", host='" + host + '\'' +
                '}';
    }
}
