package be.pcoppens.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EndPointTest {

    @Test
    void getRandomString(){
        String s=EndPoint.getRandomString();
        assertNotNull(s);
    }

    @Test
    void constructor(){
        EndPoint f= new EndPoint(3);
        assertNotNull(f);
        System.out.println(f);
        System.out.println(f.getURI());
        System.out.println(f.getLogLine("fe123"));
        assertThrows(IllegalArgumentException.class, () -> new EndPoint(0));

        EndPoint f2= new EndPoint();
        assertNotNull(f2);
    }


    @Test
    void getRedondant(){
        EndPoint f= new EndPoint(3);
        assertNotNull(f);
        EndPoint f2= f.getRedondantSameServer();
        EndPoint f3= f.getRedondantServer();
        assertNotNull(f2);
        assertNotNull(f3);
    }
}
