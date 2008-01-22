/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.benchmark.xmlrpc;

import java.io.*;
import java.util.*;
import java.net.*;

import org.apache.commons.benchmark.*;

/**
 * Jakarta XMLRPC handler for working and fetching benchmarks from a remote VM.
 * 
 * @author <a href="mailto:burton@tailrank.com">Kevin A. Burton (burtonator)</a>
 * @version $Id: BenchmarkHandler.java,v 1.5 2005/03/04 00:31:08 burton Exp $
 */
public class BenchmarkHandler {

    /**
     * Get an individual benchmark as a hashtable with all values exposed.
     *
     * @author <a href="mailto:burton@tailrank.com">Kevin A. Burton</a>
     */
    public Map getBenchmarkAsHashtable( String name ) {

        Benchmark benchmark = (Benchmark)Benchmark.getBenchmarks().get( name );

        if ( benchmark == null )
            return null;

        Hashtable map = new Hashtable();

        addHashtableMetrics( map, benchmark, benchmark.getTracker1().getLast(), "1min." );
        addHashtableMetrics( map, benchmark, benchmark.getTracker5().getLast(), "5min." );
        addHashtableMetrics( map, benchmark, benchmark.getTracker15().getLast(), "15min." );
        
        return map;
        
    }

    /**
     *
     * Get all current benchmarks that have been defined in the system. We
     * return the data as a Hashtable with the name of the benchmark and the
     * associated values.
     * 
     * @author <a href="mailto:burton@tailrank.com">Kevin A. Burton</a>
     */
    public Map getBenchmarks() throws Throwable {

        String key = null;
        Hashtable result = new Hashtable();

        try {

            Iterator it = Benchmark.getBenchmarks().keySet().iterator();
            
            while ( it.hasNext() ) {
                
                key = (String)it.next();
                
                Benchmark b = Benchmark.getBenchmark( key );
                
                result.put( b.getName(), b.toString() );
                
            } 

        } catch ( Throwable t ) {
            System.out.println( "Caught exception on key: " + key );
            t.printStackTrace();
            throw t;
        }
        
        return result;
    }
    
    void addHashtableMetrics( Hashtable map, 
                              Benchmark benchmark, 
                              BenchmarkMeta meta, 
                              String prefix ) {

        map.put( prefix + "duration", new Double( meta.getDuration() ) );
        map.put( prefix + "meanDuration", new Double( meta.getMeanDuration() ) );
        map.put( prefix + "completed", new Double( meta.getCompleted() ) );
        map.put( prefix + "started", new Double( meta.getStarted() ) );

        //cache benchmarks have additional metadata.
        if ( benchmark instanceof CacheBenchmark ) {

            map.put( prefix + "cache_hits", new Integer( meta.getCacheHits() ) );
            map.put( prefix + "cache_misses", new Integer( meta.getCacheMisses() ) );
            map.put( prefix + "cache_sets", new Integer( meta.getCacheSets() ) );
            map.put( prefix + "cache_efficiency", new Double( meta.getCacheEfficiency() ) );

        }

    }

    /**
     * @deprecated use getBenchmarkAsHashtable
     */
    public Map getBenchmark( String name ) {
        return getBenchmarkAsHashtable( name );
    }

    /**
     * @deprecated use getBenchmarkAsHashtable
     */
    public Double getLastStarted( String name ) {

        return new Double( Benchmark.getBenchmark( name )
                           .getTracker1().getLast().getStarted() );
    }

    /**
     * @deprecated use getBenchmarkAsHashtable
     */
    public Double getLastCompleted( String name ) {
        return new Double( Benchmark.getBenchmark( name )
                           .getTracker1().getLast().getCompleted() );
    }

    /**
     * @deprecated use getBenchmarkAsHashtable
     */
    public Double getLastDuration( String name ) {
        return new Double( Benchmark.getBenchmark( name )
                           .getTracker1().getLast().getDuration() );
    }

    /**
     * @deprecated use getBenchmarkAsHashtable
     */
    public Double getLastMeanDuration( String name ) {
        return new Double( Benchmark.getBenchmark( name )
                           .getTracker1().getLast().getMeanDuration() );
    }

}

