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

/**
 * <p> Handles physically tracking each benchmark and handling rollover.  Note
 * that this class is MUTABLE so if you're working with it and additional
 * threads are calling start/complete the values will change.  This is normally
 * fine but if you're trying to perform some type of benchmark analsis then you
 * shouldn't hold on to the tracker long.
 * 
 * @author <a href="mailto:burton@tailrank.com">Kevin Burton</a>
 * @version $Id: Benchmark.java,v 1.3 2005/02/16 02:28:09 burton Exp $
 */
public class BenchmarkTracker2 {

    /**
     * Mutext used to prevent threads from corrupting start/complete cycles.
     */
    private Object MUTEX = new Object();

    /**
     * The benchmark hosting this tracker.
     */
    private Benchmark parent = null; 

    /**
     * Interval (in millis) that we rollover this tracked benchmark.
     */
    private int interval = 0;
    
    // **** Last variables after the rollover ***********************************

    /**
     * Keep track of duration.
     */
    private static BenchmarkThreadLocal2 threadlocal = new BenchmarkThreadLocal2();

    BenchmarkMeta2 now = new BenchmarkMeta2();
    BenchmarkMeta2 last = new BenchmarkMeta2();

    /**
     * The interval that this value is rolled over with (1, 5, 15 minutes)
     */
    public int getInterval() {
        return interval;
    }
    
    /**
     * 
     * Create a new <code>BenchmarkTracker2</code> instance.
     */
    public BenchmarkTracker2( int interval, Benchmark parent ) {
        this.interval = interval;
        this.parent = parent;
    }

    BenchmarkTracker2 rollover() {
        rollover( System.currentTimeMillis() );
        return this;
    }

    /**
     * Do a physical rollover from now to -> last .
     *
     * @author <a href="mailto:burton@tailrank.com">Kevin A. Burton</a>
     */
    BenchmarkTracker2 rollover( long currentTimeMillis ) {

        //need to perform a swap and save the current benchmark.
        last = now;

        //FIXME: we could use a daemon thread to handle automatic rollover so
        //that we don't miss anything if a metric just isn't being called very
        //often.  Further, we should be able to read the 'last' metrics without
        //having to block.1
        
        //if we've slept for too long we have to start fresh
        if ( currentTimeMillis - last.timestamp.get() > (interval * 2) ) {
            last.reset();
        } 

        if ( last.getTimestamp() > 0 ) {
            //FIXME
            //BenchmarkListenerRegistry.applyRollover( parent, this, last );
        }
        
        //reset the benchmark (note.  We could probably save a bit of GC here by
        //swapping the last and now values since the last would just be
        //discarded anyway.
        now = new BenchmarkMeta2();
        now.timestamp.set( currentTimeMillis );

        //this isn't needed since it's a new benchmark.
        //now.reset();

        return this;

    }

    BenchmarkTracker2 rolloverWhenNecessary() {
        return rolloverWhenNecessary( System.currentTimeMillis() );
    }
    
    /**
     * Rollover stats if necessary.
     */
    BenchmarkTracker2 rolloverWhenNecessary( long currentTimeMillis ) {

        if ( isExpired( currentTimeMillis ) ) {

            synchronized( MUTEX ) {

                //double check idiom
                if ( isExpired( currentTimeMillis ) ) {
                    rollover( currentTimeMillis );
                }

            }
            
        }

        return this;
        
    }

    boolean isExpired( long currentTimeMillis ) {
        return currentTimeMillis - now.timestamp.get() > interval;
    }

    void start() {

        if ( parent != null && parent.DISABLED  )
            return;

        long currentTimeMillis = System.currentTimeMillis();

        //we need to synchronize on this individual metadata unit because if we
        //didn't then another thread could come in, and corrupt our metadata
        //about this benchmark.  Since benchmarks are often performed within
        //threads this is important.
        rolloverWhenNecessary( currentTimeMillis );
        now.started.getAndIncrement();

        doLocalStart( currentTimeMillis );

    }

    void complete() {

        if ( parent != null && parent.DISABLED  )
            return;

        long currentTimeMillis = System.currentTimeMillis();

        rolloverWhenNecessary( currentTimeMillis );
        now.completed.getAndIncrement();
        
        doLocalCompleted( currentTimeMillis );

    }
    
    public void value() {
        value( 1 );
    }

    public void value( int v ) {

        if ( parent != null && parent.DISABLED  )
            return;

        long currentTimeMillis = System.currentTimeMillis();

        rolloverWhenNecessary( currentTimeMillis );
        now.value.getAndAdd( v );

        //doLocalCompleted( currentTimeMillis );

    }

    void cache_hit() {

        if ( parent != null && parent.DISABLED  )
            return;

        long currentTimeMillis = System.currentTimeMillis();

        rolloverWhenNecessary( currentTimeMillis );
        now.cache_hits.getAndIncrement();

    }

    void cache_miss() {

        if ( parent != null && parent.DISABLED  )
            return;

        long currentTimeMillis = System.currentTimeMillis();

        rolloverWhenNecessary( currentTimeMillis );
        now.cache_misses.getAndIncrement();

    }

    void cache_set() {

        if ( parent != null && parent.DISABLED  )
            return;

        long currentTimeMillis = System.currentTimeMillis();

        rolloverWhenNecessary( currentTimeMillis );
        now.cache_sets.getAndIncrement();

    }

    void doLocalStart( long currentTimeMillis ) {

        if ( Benchmark.DISABLE_LOCAL )
            return;

        BenchmarkThreadLocal2Closure closure = getClosure();
        
        closure.startedTimeMillis = currentTimeMillis;
        
    }

    void doLocalCompleted( long currentTimeMillis ) {

        if ( Benchmark.DISABLE_LOCAL )
            return;

        BenchmarkThreadLocal2Closure closure = getClosure();
        
        closure.completedTimeMillis = System.currentTimeMillis();

        now.duration.getAndAdd( closure.completedTimeMillis - closure.startedTimeMillis );
        
    }

    /**
     * Get the lexical closure for this benchmark.
     */
    BenchmarkThreadLocal2Closure getClosure() {
        return (BenchmarkThreadLocal2Closure)threadlocal.get();
    }
    
    // **** metadata ************************************************************

    public BenchmarkMeta2 getLast() {
        return last;
    }

    public BenchmarkMeta2 getNow() {
        return now;
    }

    public String toString() {

        return "now=("  + now.toString() + ") last=(" + last.toString() + ") "
            ;

    }

}

/**
 * Threadlocal which hold BenchmarkThreadLocal2Closures.
 *
 */
class BenchmarkThreadLocal2 extends StrongThreadLocal {

    public Object initialValue() {
        return new BenchmarkThreadLocal2Closure();
    }

}

/**
 * We keep one threadlocal variable per tracker but we keep the data in a
 * lexical closure so that we don't have to have multiple thread locals each
 * with dedicated objects.
 */
class BenchmarkThreadLocal2Closure {

    /**
     * The time (in millis) that this benchmark was started.
     */
    long startedTimeMillis = 0;
    long completedTimeMillis = 0;
    
}

