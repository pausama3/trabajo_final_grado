package com.medmanager.ServidorMedManager;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Prueba unitaria generada por la plantilla de Maven (marcador de posición).
 */
public class AppTest 
    extends TestCase
{
    /**
     * Crea el caso de prueba.
     *
     * @param testName nombre del caso de prueba
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return el conjunto de pruebas que se ejecutan
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Prueba trivial que siempre se supera.
     */
    public void testApp()
    {
        assertTrue( true );
    }
}
