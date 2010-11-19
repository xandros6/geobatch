package it.geosolutions.geobatch.octave;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;
/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

@XStreamAlias("sheet")
@XStreamInclude({
    OctaveFunctionFile.class,
    SerializableOctaveFile.class,
    SerializableOctaveString.class,
    SerializableOctaveObject.class})
public class OctaveExecutableSheet {
    public CountDownLatch gate= null;
    
    @XStreamAlias("commands")
    private final Vector<String> commands;
    
    // variables defined by this sheet
    @XStreamAlias("definitions")
    private final Vector<SerializableOctaveObject<?>> definitions;
    
    /**
     * calculated variables:
     * Vector of variables to be checked out from the octave
     * environment after the 'sheet' execution 
     * @param com
     * @param defs
     * @param functs
     */
    @XStreamAlias("returns")
    private final Vector<SerializableOctaveObject<?>> returns;
    
    public OctaveExecutableSheet(Vector<String> com,
            Vector<SerializableOctaveObject<?>> defs,
            Vector<SerializableOctaveObject<?>> rets){
        commands=com;
        definitions=defs;
        returns=rets;
    }
    
    public OctaveExecutableSheet(){
        commands=new Vector<String>();
        definitions=new Vector<SerializableOctaveObject<?>>();
        returns=new Vector<SerializableOctaveObject<?>>();
    }
    
    public OctaveExecutableSheet(OctaveExecutableSheet es){
        commands=es.getCommands();
        definitions=es.getDefinitions();
        returns=es.getReturns();
    }
    
    public String getCommand(int i){
        return commands.get(i);
    }
    
    public Vector<String> getCommands(){
        return commands;
    }
    
    public Vector<SerializableOctaveObject<?>> getDefinitions(){
        return definitions;
    }
    
    public Vector<SerializableOctaveObject<?>> getReturns(){
        return returns;
    }
    
    // schedule command to be executed
    public void pushCommand(String src){
        commands.add(src);
    }
    
    public String popCommand(){
        if (commands.isEmpty())
            return null;
        else {
            String c=commands.firstElement();
            commands.remove(0);
            return c;
        }
    }
    
    public boolean hasDefinitions(){
        if (definitions.isEmpty())
            return false;
        else
            return true;
    }
    
    public boolean hasReturns(){
        if (returns.isEmpty())
            return false;
        else
            return true;
    }
    
    public boolean hasCommands(){
        if (commands.isEmpty())
            return false;
        else
            return true;
    }
    
    
    public SerializableOctaveObject<?> popDefinition(){
        if (definitions.isEmpty())
            return null;
        else {
            SerializableOctaveObject<?> v=definitions.firstElement();
            definitions.remove(0);
            return v;
        }
    }
    
    public SerializableOctaveObject<?> popReturn(){
        if (returns.isEmpty())
            return null;
        else {
            SerializableOctaveObject<?> r=returns.firstElement();
            returns.remove(0);
            return r;
        }
    }
    
    public void pushDefinition(SerializableOctaveObject<?> d){
        definitions.add(d);
    }
    
    public void pushDefinitions(Vector<SerializableOctaveObject<?>> ds){
        definitions.addAll(ds);
    }
    
    protected void pushReturn(SerializableOctaveObject<?> r){
        returns.add(r);
    }
    
    public void pushReturns(Vector<SerializableOctaveObject<?>> rs){
        returns.addAll(rs);
    }
}


