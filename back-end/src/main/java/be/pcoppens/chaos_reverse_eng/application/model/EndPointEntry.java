package be.pcoppens.chaos_reverse_eng.application.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * immutable.
 */
public class EndPointEntry {

    //properties
    private String path;
    private String verb;
    private String host;

    private Set<EndPointEntry> similars= new HashSet<>();

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

    public Set<EndPointEntry> getSimilars() {
        return Collections.unmodifiableSet(similars);
    }

    public void addSimilar(EndPointEntry entry){
        similars.add(entry);
    }

    public boolean isSimilar(EndPointEntry other){
        return this.verb.equalsIgnoreCase(other.verb) && this.path.equalsIgnoreCase(other.path);
    }

    @Override
    public String toString() {
        return (verb!=null?verb:"")+" "+(host!=null?host:"") + " "+ (path!=null?path:"") +" ["+(1+similars.size())+"]";
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
