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

/**
 * Used to represent metadata for a benchmark including name, timestamp,
 * started, completed, etc.
 * 
 * @author <a href="mailto:burton@tailrank.com">Kevin Burton</a>
 * @version $Id: Benchmark.java,v 1.3 2005/02/16 02:28:09 burton Exp $
 */
public class BenchmarkMethodMeta extends BenchmarkMeta{

    /**
     * The name of this metadata result (optional)
     */
    public String name = null;

    /**
     * Track the total amount of memory available BEFORE this benchmark.  -1 if
     * not available.  
     */
    public long memoryBefore = -1;
    public long memoryAfter = -1;

    public float getCompletedPerSecond() {

        long meanDuration = getMeanDuration();

        if ( meanDuration == 0 )
            return 0;
        
        return  1000 / getMeanDuration();
    }
    
    public void reset() {
        started = 0;
        completed = 0;
        duration = 0;
    }

    /**
     * Build a human readable report from the benchmark.  This should include
     * information such as mean duration, memory used, etc.
     *
     * @author <a href="mailto:burton@tailrank.com">Kevin A. Burton</a>
     */
    public String getReport() {

        StringBuffer buff = new StringBuffer();

        buff.append( "----------------------\n" );;
        buff.append( "Results from method test: " + name + "\n" );
        buff.append( "Total duration: " + duration + " milliseconds \n" );
        buff.append( "Mean duration: " + getMeanDuration() + " milliseconds \n" );
        buff.append( "Total completed: " + completed + "\n" );
        buff.append( "Total completed per second: " + getCompletedPerSecond() + "\n" );

        if ( memoryBefore > -1 ) {

            buff.append( "Used memory: "  + (memoryAfter-memoryBefore) + "\n" );

        }
        
        return buff.toString();

    }
    
}
