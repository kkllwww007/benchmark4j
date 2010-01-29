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

import org.apache.commons.benchmark.proxy.*;

import java.util.*;

/**
 *
 * 
 * <p> Benchmark impl which allows one to trace the caller classname, method,
 * and line number without the complexity of hard coding this information into
 * the implementation.
 *
 * <p> Originally this pattern wasn't used and each benchmark was given a name
 * manually.
 * 
 * <code>
 * 
 * Benchmark b1 = new CallerBenchmark();
 * 
 * try {
 * 
 *     b1.start();
 *
 *     //do some stuff here.
 *
 * } finally {
 *     b1.complete();
 * }
 * 
 * </code>
 * 
 * <p>
 * 
 * @author <a href="mailto:burton@tailrank.com">Kevin Burton</a>
 * @version $Id: Benchmark.java,v 1.3 2005/02/16 02:28:09 burton Exp $
 */
public class CallerBenchmark extends Benchmark {

    /**
     * When we originally wrote this code we missed a bug in inheritance where
     * if a test() method was inhreted from Foo into Bar when we benchmarked
     * test() the signature would be Foo.test not Bar.test.
     *
     * This fixes that bug by preserving the parent during init.
     * 
     */
    public static boolean ENABLE_PARENT_DETECTION = true;

    /**
     * When non-null also register events to this target as well.  This is
     * usually done with child( true ) to log to the parent caller as well.
     */
    CallerBenchmark sink = null;

    /**
     * Used for parent class detection.
     */
    private Object parent = null;
    
    public CallerBenchmark( Object parent ) {

        super( true );

        this.parent = parent;
        
        if ( ENABLE_PARENT_DETECTION && parent != null ) {
            classname = parent.getClass().getName();
        }

    }

    /**
     * @deprecated Use CallerBenchmark( Object ) for better resolution.
     */
    public CallerBenchmark() {

        super( true );

    }

    CallerBenchmark( String name ) {
        super( name );
    }
    
    /**
     * Get the caller for this mark.
     */
    public CallerInfo getCallerInfo() {
        return new CallerInfo( lineNumber, method, classname );
    }

    public CallerBenchmark child( String name ) {
        return child( name, false );
    }

    /**
     * Create a new child benchmark based on this given benchmark name as a
     * prefix and the target name as a suffix.  If
     * <code>sendCallsToParent</code> is true we will also call methods on the
     * parent benchmark when necessary.
     */
    public CallerBenchmark child( String name,
                                  boolean sendCallsToParent ) {

        beforeMetric();

        StringBuilder buff = new StringBuilder( 128 );

        buff.append( super.getName() )
            .append( "." )
            .append( name )
            ;
        
        String key = buff.toString();

        //we have to synchronize on this hashmap I'm afraid.  I could use a
        //ConcurrentHashMap but I'm not sure of the performance advantage here
        //and if it is worth switching. 
        synchronized( benchmarks ) {
            
            CallerBenchmark child = (CallerBenchmark)benchmarks.get( key );

            if ( child == null ) {
                child = new CallerBenchmark( key );
                child.sink = this;
                registerBenchmark( key, child );
            }

            return child;

        }
        
    }

    /**
     * @see Benchmark.start
     */
    public void start() {

        if ( sink != null ) sink.start(); 

        super.start();

    }

    /**
     * @see Benchmark.complete
     */
    public void complete() {

        if ( sink != null ) sink.complete(); 

        super.complete();

    }

}