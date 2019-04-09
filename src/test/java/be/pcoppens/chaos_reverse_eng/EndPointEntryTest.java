package be.pcoppens.chaos_reverse_eng;

import be.pcoppens.chaos_reverse_eng.model.EndPointEntry;
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

    @Test
    void getSimilarityScore() {
        String path="/api/path";
        EndPointEntry endPointEntry= new EndPointEntry("GET", "host", path+"/456");
        EndPointEntry endPointEntry2= new EndPointEntry("GET", "host", path+"/123");
        EndPointEntry otherEndPointEntry= new EndPointEntry("PUT", "host", path);

        //equals
        assertEquals(1, endPointEntry.getSimilarityScore(endPointEntry));
        assertTrue(endPointEntry.getSimilarityScore(endPointEntry2)>0);
        assertTrue(endPointEntry.getSimilarityScore(endPointEntry2)<1);
        System.out.println("Score: "+ endPointEntry.getSimilarityScore(endPointEntry2));
        //bad verb
        assertEquals(0, endPointEntry.getSimilarityScore(otherEndPointEntry));
        // compare to null
        assertEquals(0, endPointEntry.getSimilarityScore(null));
    }

    @Test
    void samePrefix() {
        String path="/api/path";
        EndPointEntry endPointEntry= new EndPointEntry("GET", "host", path+"/456");
        EndPointEntry endPointEntry2= new EndPointEntry("GET", "host", path+"/123");
        EndPointEntry otherEndPointEntry2= new EndPointEntry("PUT", "host", path);

        assertTrue(endPointEntry.shareSamePrefix(endPointEntry2,8));
        assertFalse(endPointEntry.shareSamePrefix(endPointEntry2,11));
        assertFalse(endPointEntry.shareSamePrefix(endPointEntry2,99));
        //bad verb
        assertFalse(endPointEntry.isSimilar(otherEndPointEntry2));
    }
}