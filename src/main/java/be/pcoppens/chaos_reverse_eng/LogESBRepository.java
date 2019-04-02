package be.pcoppens.chaos_reverse_eng;

import be.pcoppens.vizceral.Data;
import be.pcoppens.vizceral.builder.DataBuilder;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static be.pcoppens.chaos_reverse_eng.ESBEntry.mapToESEntry;
import static be.pcoppens.chaos_reverse_eng.LogESBEntry.mapToLogESBEntry;
import static java.util.stream.Collectors.groupingBy;

public class LogESBRepository {
    //saved logESBEntries
    private List<String> lines = new ArrayList<>();

    //ESB Client
    private List<ESBEntry> esbClient= new ArrayList<>();


    public void read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            lines= buffer.lines().collect(Collectors.toList());
            esbClient= lines.stream().map(mapToESEntry).filter(entry->entry!=null).collect(Collectors.toList());
        }

    }

    public static void main(String args[])throws IOException {
        String fileName = "forem.txt";
        LogESBRepository lr= new LogESBRepository();
        lr.read(new FileInputStream(fileName));

        Data global= DataBuilder.makeDataGlobal("Forem");
        DataBuilder.addApp(global, "First App");

        JSONObject jo = new JSONObject(global);
        System.out.println(jo);

        System.out.println(lr);

    }


    @Override
    public String toString() {
        String appStr= esbClient.stream().map(entry->entry.toString()).collect( Collectors.joining( ",\n" ) );

        return "LogRepository{\n" +
                "lines.size=" + lines.size() +
                ", esbClient.size=" + esbClient.size() +
                ", esbClient:\n" + appStr+
                "\n}";
    }
}