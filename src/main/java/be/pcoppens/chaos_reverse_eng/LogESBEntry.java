package be.pcoppens.chaos_reverse_eng;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class LogESBEntry {
    private String src;
    private List<String> targets;

    public LogESBEntry() {
    }

    public LogESBEntry(String src, List<String> targets) {
        this.src = src;
        this.targets = targets;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public List<String> getTargets() {
        return targets;
    }

    public void setTargets(List<String> targets) {
        this.targets = targets;
    }

    @Override
    public String toString() {
        return "LogESBEntry{" +
                "src='" + src + '\'' +
                ", targets.size=" + (targets!=null?targets.size():0) +
                '}';
    }

    public static Function<String, LogESBEntry> mapToLogESBEntry = (line) -> {
        String[] p = line.split(",");
        return new LogESBEntry(p[0], p.length<2?null:Arrays.asList(p).subList(1, p.length-1));
    };
}
