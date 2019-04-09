package be.pcoppens.chaos_reverse_eng.model;

import be.pcoppens.chaos_reverse_eng.LevenshteinTool;

import java.util.regex.Pattern;

/**
 * immutable.
 */
public class EndPointEntry {

    //properties
    private String path;
    private String verb;
    private String host;

    public EndPointEntry(String verb, String host, String path){
        this.path= path;
        this.verb= verb;
        this.host= host;
    }

    public EndPointEntry(String path, String host) {
        this.path = path;
        this.host = host;
    }
    public EndPointEntry(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public String getVerb() {
        return verb!=null?verb:"";
    }

    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        return verb!=null?verb:""+" "+host!=null?host:"" + " "+ path!=null?path:"";
    }


    public boolean isSimilar(EndPointEntry other){
        boolean result=true;

        if(verb==null || other.verb==null)
            result= true;
        else
            result= this.verb.equalsIgnoreCase(other.verb);

        return result && this.path.equalsIgnoreCase(other.path);
    }

    /**
     * SimilarityScore is equal to 1- getLevenshteinDistance(): 1 means equals;
     * different verb return 0
     *
     * @param other
     * @return
     */
    public float getSimilarityScore(EndPointEntry other){
        if(other==null || ((verb==null || other.verb==null) || !this.verb.equalsIgnoreCase(other.verb)))
            return 1-1;
        if(this.isSimilar(other))
            return 1-0;
        int score =LevenshteinTool.getLevenshteinDistance(this.path, other.path);
        return 1-(score/Float.max(this.path.length(), other.path.length()));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndPointEntry that = (EndPointEntry) o;
        return path.equals(that.path) &&
                (verb==null || that.verb==null) || verb.equalsIgnoreCase(that.verb) &&
                host.equals(that.host);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (verb!=null?verb.hashCode():0);
        hash = 31 * hash + host.hashCode();
        hash = 31 * hash + (path!=null?path.hashCode():0);
        return hash;
    }

    public static EndPointEntry parse(String s){
        String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+{}&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+{}&@#/%=~_|]";
        if( ! Pattern.compile(regex).matcher(s).matches())
            throw new IllegalArgumentException("not an url");

        // http[s]://host[:port]/path
        String[] p= s.split("://");
        String protocol= p[0];
        int separator= p[1].indexOf("/");
        String host= p[1].substring(0, separator);
        String path= p[1].substring(separator);

        return new EndPointEntry(path, protocol+"://"+host);
    }
}
