package be.pcoppens.generator;

import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DistribuedSystemTest {

    @Test
    void buildSystem() {
        DistribuedSystem ds= DistribuedSystem.buildSystem(1,10, 4,true);
        assertNotNull(ds);
    }

    @Test
    void buildSystemWithFailure() {
        DistribuedSystem ds= DistribuedSystem.buildSystem(1,10, 4,true,2);
        assertNotNull(ds);
    }

    @Test
    void runSystem() {
        DistribuedSystem ds= DistribuedSystem.buildSystem(1,10, 4,true, 2);
        ds.runSystem();
        ds.toDotFile("runDs.dot");
        try {
            ds.runSystem(new FileOutputStream("runDs.txt"));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void dotFile() {
        DistribuedSystem ds= DistribuedSystem.buildSystem(1,15, 5, true);
        ds.toDotFile("system.dot");
    }
    @Test
    void dotFile2() {
        DistribuedSystem ds= DistribuedSystem.buildSystem(1,15, 5,false);
        ds.toDotFile("system2.dot");
    }

    @Test
    void dotFile3() {
        DistribuedSystem ds= DistribuedSystem.buildSystem(1,15, 5,false, 2);
        ds.toDotFile("system3.dot");
    }
}