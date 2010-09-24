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

package org.apache.commons.benchmark;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.*;

/**
 * Used to represent metadata for a benchmark including name, timestamp,
 * started, completed, etc.
 * 
 * @author <a href="mailto:burton@tailrank.com">Kevin Burton</a>
 * @version $Id: Benchmark.java,v 1.3 2005/02/16 02:28:09 burton Exp $
 */
public class BenchmarkMeta2 {

    protected AtomicLong timestamp = new AtomicLong( -1 );
    protected AtomicLong started = new AtomicLong( 0 );
    protected AtomicLong completed = new AtomicLong( 0 );
    protected AtomicLong duration = new AtomicLong( 0 );
    protected AtomicLong value = new AtomicLong( 0 );

    protected AtomicInteger cache_hits = new AtomicInteger( 0 );
    protected AtomicInteger cache_misses = new AtomicInteger( 0 );
    protected AtomicInteger cache_sets = new AtomicInteger( 0 );

    /**
     * The time the current benchmark was started.  -1 for never started.
     */
    public long getTimestamp() {
        return timestamp.get();
    }
    
    /**
     * The current number of "started" benchmarks.  This can be analyzed at
     * runtime but its recommended that you use lastCompleted, lastStarted
     */
    public long getStarted() {
        return started.get();
    }

    /**
     * The current number of "completed" benchmarks.  This can be analyzed at
     * runtime but its recommended that you use lastCompleted, lastStarted
     */
    public long getCompleted() {
        return completed.get();
    }

    /**
     * The total amount of time (in milliseconds) that threads have spent
     * between start() and complete() methods.  Note that mean duration can be
     * computed by duration / completed.
     */
    public long getDuration() {
        return duration.get();
    }

    /**
     * Total mean duration.
     */
    public long getMeanDuration() {

        return duration.get() > 0 ? duration.get() / completed.get() : 0;
    }

    public int getCacheHits() {
        return cache_hits.get();
    }

    public int getCacheMisses() {
        return cache_hits.get();
    }

    public int getCacheSets() {
        return cache_sets.get();
    }

    public long getValue() {
        return value.get();
    }
    
    /**
     * Compute the cache interval for this benchmark.  Since the benchmark is
     * interval based this value will change as the interval rolls forward.
     */
    public double getCacheEfficiency() {

        int cache_total = cache_misses.get() + cache_hits.get();

        if ( cache_total == 0 )
            return 0;

        return ((double)cache_hits.get() / (double)cache_total) * (double)100;

    }

    public void reset() {
        started.set( 0 );
        completed.set( 0 );
        duration.set( 0 );
        value.set( 0 );
        cache_misses.set( 0 );
        cache_hits.set( 0 );
        cache_sets.set( 0 );
    }

    public String toString() {

        return 
            "started:" +
            getStarted() +
            "," +
            "completed:" +
            getCompleted() +
            "," +
            "duration:" +
            getDuration() +
            "," +
            "meanDuration:" +
            getMeanDuration() 
            ;

    }

    /**
     * Provide the benchmark metadata as a map for use with external systems.
     */
    public Map toMap() {

        Map<String,Long> map = new HashMap();

        map.put( "duration",      getDuration() );
        map.put( "meanDuration",  getMeanDuration() );
        map.put( "completed",     getCompleted() );
        map.put( "started",       getStarted() );

        return map;
        
    }
    
}
