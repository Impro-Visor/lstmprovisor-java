/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javapythontest;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import jep.Jep;
import jep.Run;
import jep.JepException;
/**
 *
 * @author cssummer16
 */
public class JavaPythonTest {
    public static void setup_jep_lib() {
        try {
            System.setProperty("java.library.path", "/usr/local/lib/python3.5/site-packages/jep" );
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible( true );
            fieldSysPath.set( null, null );
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load.\n" + e);
            System.exit(1);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(JavaPythonTest.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }
    
    public static void main(String[] args) throws JepException, Throwable{
        setup_jep_lib();
        try (Jep jep = new Jep(false)) {
            jep.eval("x = 'pot' + 'ato'");
            System.out.println((String) jep.getValue("x"));
            jep.eval("import sexpdata");
            System.out.println((String) jep.getValue("str(sexpdata)"));
            jep.eval("import numpy");
            System.out.println((String) jep.getValue("numpy.version.version"));
            jep.eval("import theano");
            System.out.println((String) jep.getValue("theano.version.version"));
            jep.eval("import theano.tensor as T");
            System.out.println((String) jep.getValue("str(T.roll(numpy.array([0,1,2,3,4]),2).eval())"));
        }
    }
}
