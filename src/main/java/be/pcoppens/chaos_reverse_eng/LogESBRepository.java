package be.pcoppens.chaos_reverse_eng;

import be.pcoppens.vizceral.Data;
import be.pcoppens.vizceral.Node;
import be.pcoppens.vizceral.builder.DataBuilder;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static be.pcoppens.chaos_reverse_eng.ESBEntry.mapToESEntry;
import static java.util.stream.Collectors.groupingBy;

public class LogESBRepository {

    //save group
    private Map<String,List<String>> groups= new HashMap<>();

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

    public void readGroups(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            List<String> list= buffer.lines().collect(Collectors.toList());
            list.forEach(line->{
                String[] s= line.split(",");
                groups.put(s[0], Arrays.asList(s).subList(1, s.length));
            });
        }
    }


    public static void main(String args[])throws IOException {
        String fileName = "forem.txt";
        String fileNameGroup = "foremApp.txt";
        LogESBRepository lr= new LogESBRepository();
        lr.read(new FileInputStream(fileName));
        lr.readGroups(new FileInputStream(fileNameGroup));

        //all no group
        Data all= DataBuilder.makeData("Forem");
        List<Node> warningList= DataBuilder.addAll(all, lr.esbClient);
        Files.write(Paths.get("C:/Users/cppptr/Source/vizceral-example/src/sample_data.json"), new JSONObject(all).toString().getBytes());
        //defect
    //    Data defect= DataBuilder.makeData("Fragile Service ?");
    //    DataBuilder.addAllDefect(defect, warningList);
    //    Files.write(Paths.get("C:/Users/cppptr/Source/vizceral-example/src/sample_data.json"), new JSONObject(defect).toString().getBytes());

        // by app
        Data globalApp= DataBuilder.makeData("Forem");
        DataBuilder.addNode(globalApp, lr.esbClient);
        Files.write(Paths.get("C:/Users/cppptr/Source/vizceral-example/src/sample_data_APP.json"), new JSONObject(globalApp).toString().getBytes());

        //by group
        Data global= DataBuilder.makeData("Forem");
        List<Node> nodes= DataBuilder.addNode(global, lr.groups);
        DataBuilder.addNode(nodes, lr.esbClient);
        Files.write(Paths.get("C:/Users/cppptr/Source/vizceral-example/src/sample_data_Group.json"), new JSONObject(global).toString().getBytes());

      //  String appStr= lr.esbClient.stream().map(entry->entry.getClient()).collect(Collectors.toSet()).stream().collect( Collectors.joining( "\n" ) );
      //  Files.write(Paths.get("C:/Users/cppptr/Source/vizceral-example/src/app.txt"), appStr.getBytes());
        System.out.println(lr);
    }


    @Override
    public String toString() {
        String appStr= esbClient.stream().map(entry->entry.toString()).collect( Collectors.joining( ",\n" ) );

        return "LogRepository{\n" +
                "lines.size=" + lines.size() +
                ", esbClient:\n" + appStr+ "\n"+
                ", esbClient.size=" + esbClient.size() +
                "\n}";
    }
}