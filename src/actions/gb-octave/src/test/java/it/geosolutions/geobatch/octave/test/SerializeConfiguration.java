package it.geosolutions.geobatch.octave.test;

import it.geosolutions.geobatch.octave.OctaveEnv;
import it.geosolutions.geobatch.octave.OctaveFunctionFile;

import com.thoughtworks.xstream.XStream;

public class SerializeConfiguration {
    
    public static void main(String[] args) {
        XStream stream = new XStream();
//        stream.processAnnotations(OctaveFunctionFile.class);
//        stream.processAnnotations(SerializableOctaveFile.class);
//        stream.processAnnotations(SerializableOctaveString.class);
        stream.processAnnotations(OctaveEnv.class);
        OctaveEnv oac=new OctaveEnv();

        OctaveFunctionFile off=new OctaveFunctionFile("funzione");
        //soo.setName("variable_name");
        System.out.println("-------------------------------------");
        /*
        oac.setFunction(off);
        oac.addPath("path");
        oac.addPath("path2");
        oac.getFunction().addArg(new SerializableOctaveString("variable_name2","VALUE"));
        oac.getFunction().addRet(new SerializableOctaveFile("returning_var_name","VALUE"));
        oac.getFunction().addRet(new SerializableOctaveFile("returning_var_name2","VALUE"));
        */
        System.out.println(stream.toXML(oac));
        System.out.println("-------------------------------------");
        
        
        stream.processAnnotations(OctaveFunctionFile.class);
        System.out.println(stream.toXML(off));
        System.out.println("-------------------------------------");
    }
}