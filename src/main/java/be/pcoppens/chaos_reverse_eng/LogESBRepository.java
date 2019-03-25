package be.pcoppens.chaos_reverse_eng;

import be.pcoppens.vizceral.Data;
import be.pcoppens.vizceral.builder.DataBuilder;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static be.pcoppens.chaos_reverse_eng.LogESBEntry.mapToLogESBEntry;
import static java.util.stream.Collectors.groupingBy;

public class LogESBRepository {
    //saved lines
    private List<LogESBEntry> lines= new ArrayList<>();

    //ESB Client
    private List<String> esbClient= new ArrayList<>();


    public void read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            lines= buffer.lines().map(mapToLogESBEntry).collect(Collectors.toList());
        }
    }

    public void buildEsbClient(){
        for (LogESBEntry entry : lines) {
            if(entry.getTargets()==null && ! esbClient.contains(entry.getSrc()))
                esbClient.add(entry.getSrc());
        }
    }



    public static void main(String args[])throws IOException {
        String fileName = "forem.txt";
        LogESBRepository lr= new LogESBRepository();
        lr.read(new FileInputStream(fileName));
        lr.buildEsbClient();

        Data global= DataBuilder.makeDataGlobal("Forem");
        DataBuilder.addApp(global, "First App");

        JSONObject jo = new JSONObject(global);
        System.out.println(jo);

      //  System.out.println(lr);

    }



    @Override
    public String toString() {
        String appStr= esbClient.stream().collect( Collectors.joining( ",\n" ) );

        return "LogRepository{\n" +
                "lines.size=" + lines.size() +
                ", esbClient.size=" + esbClient.size() +
                ", esbClient:\n" + appStr+
                "\n}";
    }
}
