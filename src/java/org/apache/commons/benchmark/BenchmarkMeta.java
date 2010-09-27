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

/**
 * Used to represent metadata for a benchmark including name, timestamp,
 * started, completed, etc.
 * 
 * @author <a href="mailto:burton@tailrank.com">Kevin Burton</a>
 * @version $Id: Benchmark.java,v 1.3 2005/02/16 02:28:09 burton Exp $
 */
public class BenchmarkMeta {

    long timestamp = -1;

    long started = 0;

    long completed = 0;

    long duration = 0;

    int cache_hits = 0;
    int cache_misses = 0;
    int cache_sets = 0;
    
    /**
     * The time the current benchmark was started.  -1 for never started.
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * The current number of "started" benchmarks.  This can be analyzed at
     * runtime but its recommended that you use lastCompleted, lastStarted
     */
    public long getStarted() {
        return started;
    }

    /**
     * The current number of "completed" benchmarks.  This can be analyzed at
     * runtime but its recommended that you use lastCompleted, lastStarted
     */
    public long getCompleted() {
        return completed;
    }

    /**
     * The total amount of time (in milliseconds) that threads have spent
     * between start() and complete() methods.  Note that mean duration can be
     * computed by duration / completed.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Total mean duration.
     */
    public long getMeanDuration() {
        return duration > 0 ? duration / completed : 0;
    }

    public void reset() {
        started = 0;
        completed = 0;
        duration = 0;
        cache_misses = 0;
        cache_hits = 0;
        cache_sets = 0;
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
