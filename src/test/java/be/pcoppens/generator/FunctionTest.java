package be.pcoppens.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrows;


import java.io.IOException;

class FunctionTest {

    @Test
    void getRandomString(){
        String s=Function.getRandomString();
        assertNotNull(s);
    }

    @Test
    void constructor(){
        Function f= new Function(3);
        assertNotNull(f);
        System.out.println(f);
        System.out.println(f.getURI());
        System.out.println(f.getLogLine("fe123"));
        assertThrows(IllegalArgumentException.class, () -> new Function(0));

        Function f2= new Function();
        assertNotNull(f2);
    }


    @Test
    void getRedondant(){
        Function f= new Function(3);
        assertNotNull(f);
        Function f2= f.getRedondantSameServer();
        Function f3= f.getRedondantServer();
        assertNotNull(f2);
        assertNotNull(f3);
    }
}
