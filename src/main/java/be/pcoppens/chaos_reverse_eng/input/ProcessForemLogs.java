package be.pcoppens.chaos_reverse_eng.input;

import be.pcoppens.chaos_reverse_eng.model.ServiceGroup;
import be.pcoppens.chaos_reverse_eng.output.vizceral.Data;
import be.pcoppens.chaos_reverse_eng.output.vizceral.Node;
import be.pcoppens.chaos_reverse_eng.output.vizceral.builder.DataBuilder;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static be.pcoppens.chaos_reverse_eng.input.ESBEntry.mapToESEntry;
import static java.util.stream.Collectors.groupingBy;

public class ProcessForemLogs {

    public static void main(String args[])throws IOException {
        String fileName = "forem.txt";
        String fileNameGroup = "foremApp.txt";

        ServiceGroup group= LogGroupEntry.readGroup(new FileInputStream(fileNameGroup), "Forem", false);
        ServiceGroup services= LogESBEntry.read(new FileInputStream(fileName), "Forem");

        System.out.println(services);
        System.out.println(group);


  //      Files.write(Paths.get("C:/Users/cppptr/Source/vizceral-example/src/sample_data_APP.json"), new JSONObject(globalApp).toString().getBytes());

      //  String appStr= lr.esbClient.stream().map(entry->entry.getClient()).collect(Collectors.toSet()).stream().collect( Collectors.joining( "\n" ) );
      //  Files.write(Paths.get("C:/Users/cppptr/Source/vizceral-example/src/app.txt"), appStr.getBytes());

    }

}