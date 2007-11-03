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

import java.util.*;
import java.lang.reflect.*;

/**
 * 
 * @author <a href="mailto:burton@tailrank.com">Kevin Burton</a>
 * @version $Id: Benchmark.java,v 1.3 2005/02/16 02:28:09 burton Exp $
 */
public class BenchmarkUtils {

    public static BenchmarkMethodMeta benchmarkMethod( String name ) throws Exception {
        return benchmarkMethod( name, 10 );
    }

    public static BenchmarkMethodMeta benchmarkMethod( String name,
                                                       int numIterations ) throws Exception {

        String caller = Benchmark.getCallerClassname();

        Class clazz = Class.forName( caller );

        return benchmarkMethod( name, numIterations, clazz );
    }

    /**
     * Benchmark the performance of a given method.
     * 
     * @param name The name of the method from the caller (found via reflection).
     * @author <a href="mailto:burton@tailrank.com">Kevin A. Burton</a>
     */
    public static BenchmarkMethodMeta benchmarkMethod( String name,
                                                       int numIterations,
                                                       Class clazz ) throws Exception {

        Method method = clazz.getMethod( name, null );

        if ( method == null )
            throw new Exception( "Unable to find method: " + method + " in class " + clazz );

        BenchmarkMethodMeta bmeta = new BenchmarkMethodMeta();
        bmeta.name = name;

        System.gc();
        bmeta.memoryBefore = getUsedMemory();
        long duration = 0;

        for ( int i = 0; i < numIterations; ++i ) {

            long before = System.currentTimeMillis();
            
            method.invoke( null, new Object[0] );

            if ( i == 0 && numIterations > 1 )
                continue; //don't measure the first call

            long after = System.currentTimeMillis();
            duration += after-before;
            
        }

        bmeta.duration = duration;

        bmeta.started = numIterations;
        bmeta.completed = numIterations;

        System.gc();

        bmeta.memoryAfter = getUsedMemory();
        
        float totalAvgDuration = (float)duration / (float)numIterations;

        float totalPerSecond = 1000 / totalAvgDuration;

        return bmeta;
        
    }

    private static long getUsedMemory() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

}

