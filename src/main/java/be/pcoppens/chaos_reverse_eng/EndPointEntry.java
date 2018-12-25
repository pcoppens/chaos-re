package be.pcoppens.chaos_reverse_eng;

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
}
