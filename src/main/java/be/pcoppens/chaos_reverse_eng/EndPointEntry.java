package be.pcoppens.chaos_reverse_eng;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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


    @Override
    public String toString() {
        return verb+" "+host + " "+ path;
    }


    public boolean isSimilar(EndPointEntry other){
        return this.verb.equalsIgnoreCase(other.verb) && this.path.equalsIgnoreCase(other.path);
    }

    /**
     * SimilarityScore is equal to 1- getLevenshteinDistance(): 1 means equals;
     * different verb return 0
     *
     * @param other
     * @return
     */
    public float getSimilarityScore(EndPointEntry other){
        if(other==null || !this.verb.equalsIgnoreCase(other.verb))
            return 1-1;
        if(this.isSimilar(other))
            return 1-0;
        int score =LevenshteinTool.getLevenshteinDistance(this.path, other.path);
        return 1-(score/Float.max(this.path.length(), other.path.length()));
    }

    public boolean shareSamePrefix(EndPointEntry other, int minimalPrefixLen){
        return this.verb.equalsIgnoreCase(other.verb) &&
                other.path.length()>= minimalPrefixLen &&
                this.path.length()>= minimalPrefixLen &&
                this.path.startsWith(other.path.substring(0, minimalPrefixLen));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndPointEntry that = (EndPointEntry) o;
        return path.equals(that.path) &&
                verb.equalsIgnoreCase(that.verb) &&
                host.equals(that.host);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + verb.hashCode();
        hash = 31 * hash + host.hashCode();
        hash = 31 * hash + path.hashCode();
        return hash;
    }
}
