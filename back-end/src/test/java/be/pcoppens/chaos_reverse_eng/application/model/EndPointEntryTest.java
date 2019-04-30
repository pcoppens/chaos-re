package be.pcoppens.chaos_reverse_eng.application.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EndPointEntryTest {

    @Test
    void isSimilar() {
        String path="/api/path";
        EndPointEntry endPointEntry= new EndPointEntry("GET", "host", path);
        EndPointEntry endPointEntry2= new EndPointEntry("GET", "host", path);
        EndPointEntry endPointEntry3= new EndPointEntry("GET", "host2", path);
        EndPointEntry otherEndPointEntry= new EndPointEntry("GET", "host", path+"other");
        EndPointEntry otherEndPointEntry2= new EndPointEntry("PUT", "host", path);

        //equal
        assertTrue(endPointEntry.isSimilar(endPointEntry2));
        //similar
        assertTrue(endPointEntry.isSimilar(endPointEntry3));
        //bad path
        assertFalse(endPointEntry.isSimilar(otherEndPointEntry));
        //bad verb
        assertFalse(endPointEntry.isSimilar(otherEndPointEntry2));
    }

}