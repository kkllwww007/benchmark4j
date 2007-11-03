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
 * <p>
 * Benchmark that allows cheap and lightweight "benchmarking" (go figure) of
 * arbitrary code.  All you have to do is call start() every time a method
 * starts which will then increment the benchmark and perform any operations
 * necessary to maintain the benchmark.  Just call complete() when your method
 * is done.  
 * 
 * <p>
 * This class is lightweight (only requires a hashmap entry, and 32 bytes per
 * benchmark of storage with no external requirements.  This class is also
 * threadsafe so if you need to call this from multithreaded code to benchmark
 * then you'll be ok.
 *
 * <p>
 * The benchmark is maintained as number of starts and completes per minute.
 * This can be any type of operation you want.  Technically the interval can be
 * longer than a minute but we will end up with stale data.  That's the tradeoff
 * with this type of benchmark.  Its cheap and easy to maintain but anything
 * more than 60 seconds worth of data and you'll end up with a stale benchmark.
 *
 * <p> Internally we use an incremented value which is accumulated and reset
 * ever 60 seconds.  When we reset the benchmark we reset the current value so
 * that we can start accumulating again.
 * 
 * <code>
 * 
 * Benchmark b1 = new CallerBenchmark();
 * 
 * try {
 * 
 *     b1.start();
 * 
 *     //do something expensive
 * 
 * } finally {
 *     b1.complete();
 * }
 * 
 * </code>
 * 
 * <p> The framework also supports caching frameworks benchmarking including
 * cache hits/misses and computing cache efficiency.  Simply call
 * benchmark.cache_hit() and benchmark.cache_miss() in your caching framework.
 * This was designed for use within Java LRU caching frameworks and for use
 * within Memcached.
 * 
 * <p>
 * The method overhead is very light. One a modern machine you can perform about
 * 1M benchmarks per second.  For code thats only called a few thousand times
 * you won't notice any performance overhead.
 * 
 * @author <a href="mailto:burton@tailrank.com">Kevin Burton</a>
 * @version $Id: Benchmark.java,v 1.3 2005/02/16 02:28:09 burton Exp $
 */
public class Benchmark {

    /**
     * Mutex to prevent exception during init across multiple threads.
     */
    public Object FULL_INIT_MUTEX = new Object();

    /**
     * How often should we maintain/reset the benchmark.
     */
    public static int INTERVAL_1 = 60 * 1000;

    /**
     * The 5 minute benchmark interval.
     */
    public static int INTERVAL_5 = 5 * 60 * 1000;

    /**
     * The 15 minute benchmark interval.
     */
    public static int INTERVAL_15 = 15 * 60 * 1000;

    /**
     * Disable all logging of benchmarks.  This essentially makes the
     * performance overhead zero.
     */
    public static boolean DISABLED = false;

    /**
     * We support keeping track of local values between start/complete such as
     * duration. We can disable this for additional throughput.  Enabling this
     * feature uses threadlocal variables so there's a constant overhead per
     * thread.
     */
    public static boolean DISABLE_LOCAL = false;
    
    /**
     * Maintain a metadata map between the name and BMeta classes.
     */
    static HashMap benchmarks = new HashMap();

    /**
     * The current name of this benchmark.
     */
    String name = null;

    //FIXME: we need a tracker for the ENTIRE lifecycle of a VM.  this shouldn't
    //take up much more memory and won't ever be rolled over.  This will come in
    //handy for stats like the number of times a queue is reloaded 
    
    /**
     * The current benchmark.
     */
    BenchmarkTracker tracker1 = null;
    BenchmarkTracker tracker5 = null;
    BenchmarkTracker tracker15 = null;

    /**
     * When benchmarks are created with a constructor we only store them as a
     * valid benchmark when start/complete is called without a given operation.
     */
    boolean registered = false;
    
    /**
     *  True if we need the start() method to introspect on the first call.
     */
    boolean requiresFullInit = false;

    protected int lineNumber = -1;
    protected String method = null;
    protected String classname = null;

    /**
     * Create a benchmark for the current class as caller.
     */
    public Benchmark() {

        this( getCallerClassname() );

    }

    /**
     * Create a new benchmark with a given name.
     */
    public Benchmark( String name ) {

        this.name = name;

        clear();
        
    }

    /**
     * Use the FULL classname lazy init on the first start().  For example you
     * can use this method to determine the classname, method, and line number
     * that the first start() method is called to avoid giving the benchmark a
     * name.  The downside of this method is that the method info will change
     * each time.  NOTE that we might add a canonicalization mechanism for this
     * in the future.
     */
    public Benchmark( boolean full ) {
        requiresFullInit = full;

        clear();

    }

    /**
     * Reset this benchmark.  Used primarily with unit tests.
     *
     * @author <a href="mailto:burton@tailrank.com">Kevin A. Burton</a>
     */
    void clear() {

        tracker1 = new BenchmarkTracker( INTERVAL_1, this );
        tracker5 = new BenchmarkTracker( INTERVAL_5, this );
        tracker15 = new BenchmarkTracker( INTERVAL_15, this );

    }
    
    // **** metadata about this benchmark ***************************************

    /**
     * Get the tracker for this benchmark which includes all metadata related to
     * this benchmark including total completed/started and current values.
     * 
     */
    public BenchmarkTracker getTracker() {
        return getTracker1();
    }

    /**
     * Get the tracker with a 1 minute interval.
     *
     */
    public BenchmarkTracker getTracker1() {
        return tracker1.rolloverWhenNecessary();
    }

    /**
     * Get the tracker with a 5 minute interval.
     *
     */
    public BenchmarkTracker getTracker5() {
        return tracker5.rolloverWhenNecessary();
    }

    /**
     * Get the tracker with a 15 minute interval.
     *
     */
    public BenchmarkTracker getTracker15() {
        return tracker15.rolloverWhenNecessary();
    }

    /**
     * Get the name of this benchmark.  Usually the classname or method name of
     * the caller.
     */
    public String getName() {
        return name;
    }

    // **** implementation code for start/complete **********************************

    /**
     * @deprecated use CallerBenchmark
     */
    public void start( String name ) {
        Benchmark child = child( name );
        child.start();
    }

    /**
     * @deprecated use CallerBenchmark
     */
    public void complete( String name ) {
        Benchmark child = child( name );
        child.complete();
    }

    /**
     * Tell the benchmark that its has been started for this interval.
     *
     */
    public void start() {

        if ( DISABLED  )
            return;

        //only allow one concurrent init so that 
        synchronized( FULL_INIT_MUTEX ) {

            if ( requiresFullInit ) {
                initCaller( true );
                clear();
            }
        }

        doRegisterWhenNecessary();

        tracker1.start();
        tracker5.start();
        tracker15.start();

    }

    /**
     * Tell the benchmark that its has been completed for this interval.
     * 
     *
     */
    public void complete() {

        if ( DISABLED  )
            return;

        doRegisterWhenNecessary();

        //this could happen if start() isn't called first.
        if ( tracker1 == null )
            return;
        
        tracker1.complete();
        tracker5.complete();
        tracker15.complete();
        
    }

    public void cache_hit() {

        if ( DISABLED  )
            return;

        if ( requiresFullInit ) {
            initCaller( true );
            clear();
        }

        doRegisterWhenNecessary();
        
        tracker1.cache_hit();
        tracker5.cache_hit();
        tracker15.cache_hit();
        
    }

    public void cache_miss() {

        if ( DISABLED  )
            return;

        if ( requiresFullInit ) {
            initCaller( true );
            clear();
        }

        doRegisterWhenNecessary();
        
        tracker1.cache_miss();
        tracker5.cache_miss();
        tracker15.cache_miss();
        
    }

    public void cache_set() {

        if ( DISABLED  )
            return;

        if ( requiresFullInit ) {
            initCaller( true );
            clear();
        }

        doRegisterWhenNecessary();
        
        tracker1.cache_set();
        tracker5.cache_set();
        tracker15.cache_set();
        
    }

    private void doRegisterWhenNecessary() {

        if ( registered == false && name != null ) {
            benchmarks.put( name, this );
            registered = true;
        }

    }
    
    /**
     * Return a child benchmark of the current method.  This can be used to
     * return a benchmark for a specific method based on a benchmark for a
     * class.  The resulting name will have parent#name semantics.
     *
     * @deprecated use CallerBenchmark
     * @author <a href="mailto:burton@tailrank.com">Kevin A. Burton</a>
     */
    public Benchmark child( String name ) {

        return getBenchmark( this.name + "." + name );
        
    }

    /**
     * Get the name of the calling classname..
     *
     */
    public void initCaller( boolean full ) {
        
        StackTraceElement caller = getCallerStackTraceElement();

        this.name = classname;

        if ( this.name == null )
            this.name = caller.getClassName();

        lineNumber = caller.getLineNumber();
        method = caller.getMethodName();
        classname = caller.getClassName();
        
        if ( full ) {
            this.name = this.name + "." + caller.getMethodName();
        }
        
        requiresFullInit = false;

    }

    // **** static code *********************************************************

    public static String getCallerClassname() {
        return getCallerStackTraceElement().getClassName();
    }

    public static StackTraceElement getCallerStackTraceElement() {

        Exception e = new Exception();

        StackTraceElement trace[] = e.getStackTrace();

        for ( int i = 2; i < trace.length; ++i  ) {

            StackTraceElement current = trace[i];
            String name = current.getClassName();

            //FIXME: warning..... this package name isn't portable if we need to rename it.
            
            if ( name.startsWith( "org.apache.commons.benchmark" ) == false ||
                 name.indexOf( "Test" ) != -1 ) {

                return current;

            }

        }

        return null;

    }

    public static Object getBenchmarkAsProxy( Object _instance, Class _interface ) {

        return BenchmarkProxyFactory.newBenchmarkFactory( _instance, _interface );
        
    }

    /**
     * Factory method for obtaining a benchmark.  This method uses the callers
     * classname by performing a quick stack analysis.  Note that this is slow
     * and you should cache your benchmarks in a static variable.
     *
     * @deprecated use CallerBenchmark
     */
    public static Benchmark getBenchmark() {

        String name = getCallerClassname();
        return getBenchmark( name );

    }

    /**
     * Factory method for obtaining a benchmark by classname
     * 
     * @deprecated use CallerBenchmark instead of static instance.
     */
    public static Benchmark getBenchmark( Class clazz ) {
        return getBenchmark( clazz.getName() );
    }

    /**
     * Create a benchmark on a the caller when performing a specific operation
     * on a given target object..  For example if your class is 'org.apache.Foo'
     * and the operation is 'bar' then the resulting benchmark name will be
     * 'org.apache.Foo#bar'.  When the target is null the name of the caller is
     * used. This means we won't throw an NPE and it also means we have similar
     * operation to child(),
     * 
     * @deprecated use CallerBenchmark instead of static instance.
     */
    public static Benchmark getBenchmark( Object target,
                                          String operation ) {

        String prefix = null;

        if ( target == null ) {
            prefix = getCallerClassname();
        } else {
            prefix = target.getClass().getName();
        }

        String name = prefix + "#" + operation;

        return getBenchmark( name );
        
    }
    
    /**
     * Factory method for obtaining a benchmark by name
     *
     * @deprecated use CallerBenchmark
     */
    public static Benchmark getBenchmark( String name ) {

        //we have to synchronize on this hashmap I'm afraid.  I could use a
        //ConcurrentHashMap but I'm not sure of the performance advantage here.
        synchronized( benchmarks ) {

            Benchmark benchmark = (Benchmark)benchmarks.get( name );

            if ( benchmark == null ) {
                benchmark= new Benchmark( name );
                benchmarks.put( name, benchmark );
                benchmark.registered = true;
            }

            return benchmark;
            
        }

    }	

    /**
     * Return a map of all known benchmarks.
     *
     * @author <a href="mailto:burton@tailrank.com">Kevin A. Burton</a>
     */
    public static Map getBenchmarks() {
        return benchmarks;
    }

    // **** Object methods ******************************************************

    public String toString() {
        return "1min: {" + toString( getTracker1() ) + "}" + " " +
               "5min: {" + toString( getTracker5() ) + "}" + " " +
               "15min: {" + toString( getTracker15() ) + "}" +
               ", line number: " + 
               lineNumber 
        ;

    }

    public String toString( BenchmarkTracker tracker ) {

        return 
            "now=("  +
            "started:" +
            tracker.now.getStarted() +
            "," +
            "completed:" +
            tracker.now.getCompleted() +
            "," +
            "duration:" +
            tracker.now.getDuration() +
            "," +
            "meanDuration:" +
            tracker.now.getMeanDuration() +
            ")" + 
            " " +
            "last=("  +
            "started:" +
            tracker.last.getStarted() +
            "," +
            "completed:" +
            tracker.last.getCompleted() +
            "," +
            "duration:" +
            tracker.last.getDuration() +
            "," +
            "meanDuration:" +
            tracker.last.getMeanDuration() +
            ") "
            ;

    }
    
}
