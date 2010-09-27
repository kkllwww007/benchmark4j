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

import org.apache.commons.benchmark.*;
import org.apache.commons.benchmark.xmlrpc.*;

import org.apache.xmlrpc.*;

import junit.framework.*;

import java.util.*;

/**
 */
public class Test1 extends TestCase {

    public Test1(String testName) {
        super(testName);
    }

    public void testChild() throws Exception {

        Benchmark b = new Benchmark();

        Benchmark child = b.child( "foo" );

        assertTrue( "child not setup", child.getName().indexOf( "foo" ) != -1 );

        child.start();
        child.complete();

        assertFalse( b.registered );

        assertEquals( 0, b.getTracker1().now.completed );
        assertEquals( 1, child.getTracker1().now.completed );

    }
    
    //FIXME: write unit test for PERFORMANCE.  With it enabled/disabled we
    //should be able to call it FREQUENTLY without killing the CPU.
    
    public void testBasicConstructor() throws Exception {

        resetForTests();
        
        Benchmark b = new Benchmark();

        assertEquals( b.name, "org.apache.commons.benchmark.Test1" );
        
        assertNull( Benchmark.benchmarks.get( b.name ) );

        b.start();
        b.complete();

        assertNotNull( Benchmark.benchmarks.get( b.name ) );

        resetForTests();
        
    }
        
//     public void testThreads() throws Exception {

//         int count = 100;

//         ThreadGroup tg = new ThreadGroup( "foo" );

//         System.gc();
//         long before = getUsedMemory();
        
//         for ( int i = 0; i < count; ++i ) {
//             TestThread tt = new TestThread( tg );
//             tt.start();
//         } 

//         while ( tg.activeCount() > 0 ) {

//             System.out.print( "." );
//             Thread.sleep( 100 );
            
//         } 

//         System.out.println();
        
//         System.gc();
//         long after = getUsedMemory();

//         System.out.println( "Done thread test" );

//         //500k bytes is only 500 bytes per benchmark.  We should try to thin
//         //this down a bit.  I could do MUCH better I think.  Maybe NOT keep
//         //references to strings?
//         long usedMemory = after - before;
//         System.out.println( "Total bytes used by benchmark: " + usedMemory );
//         assertTrue( usedMemory < 500 * count );

//     }

    private long getUsedMemory() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
    
    public void testNewChild() {

        Benchmark b = Benchmark.getBenchmark();

        b.start( "foo" );
        b.complete( "foo" );

        b = (Benchmark)Benchmark.benchmarks.get( "org.apache.commons.benchmark.Test1.foo" );

        assertNotNull( b );

        assertEquals( 1, b.getTracker1().now.completed );

        b.getTracker1().rollover();

        assertEquals( 1, b.getTracker1().last.completed );

    }

    public void testBenchmarkWithCaller() {

        Benchmark b = Benchmark.getBenchmark( this, "foo" );

        assertEquals( "org.apache.commons.benchmark.Test1#foo", b.getName() );

        b = Benchmark.getBenchmark( null, "foo" );

        assertEquals( "org.apache.commons.benchmark.Test1#foo", b.getName() );

        b = Benchmark.getBenchmark( "string", "foo" );

        assertEquals( "java.lang.String#foo", b.getName() );

    }

    //setup a test to verify that X benchmarks don't use more than Y
    //bytes of memory.

    public void testMemory() throws Exception {

        System.gc();
        
        long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        int count = 1000;

        for ( int i  = 0; i < count; ++i  ) {

            Benchmark b = Benchmark.getBenchmark( "foo:" + i );

            b.start();
            b.complete();
            
        }

        System.gc();

        long after = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        //500k bytes is only 500 bytes per benchmark.  We should try to thin
        //this down a bit.  I could do MUCH better I think.  Maybe NOT keep
        //references to strings?
        long usedMemory = after - before;
        System.out.println( "Total bytes used by benchmark: " + usedMemory );
        if ( false == usedMemory < 1000 * count ) {
            throw new Exception( "used memory too large: " + usedMemory / count + " bytes per metric" );
        }

        resetForTests();
        //Benchmark.benchmarks.clear();
    }
    
    /**
     * Test out all XMLRPC methods...that we have exported.
     *
     * @author <a href="mailto:burton1@rojo.com">Kevin A. Burton</a>
     */

//     public void testXmlrpc() throws Exception {

//         Benchmark.INTERVAL_1 =   1000;
//         Benchmark.INTERVAL_5 =   5000;
//         Benchmark.INTERVAL_15 = 15000;

//         WebServer webserver = new WebServer ( 2048 );

//         webserver.addHandler( "benchmark",
//                               new BenchmarkHandler() );

//         // deny all clients
//         webserver.setParanoid ( true );
//         // allow local access
//         webserver.acceptClient ( "10.*.*.*" );
//         webserver.acceptClient ( "127.*.*.*" );

//         webserver.start();

//         Thread.sleep( 100 );

//         //create a faux benchmark

//         Benchmark b = Benchmark.getBenchmark( "foo" );
//         b.start();
//         b.complete();

//         assertEquals( 1, b.getTracker1().now.completed );

//         //this should sleep long enough to rollover interval1

//         System.out.println( "going to sleep" );
//         Thread.sleep( 1500 );

//         //b.getTracker1().reset( System.currentTimeMillis() );

//         //FIXME: this isn't working.
//         assertEquals( 0, b.getTracker1().now.completed );
//         assertEquals( 1, b.getTracker1().last.completed );

//         String router = "http://localhost:2048/RPC2";

//         XmlRpcClient xmlrpc = new XmlRpcClient ( router );

//         Vector params = new Vector ();
//         params.add( "foo" );
        
//         Object result = xmlrpc.execute ( "benchmark.getLastCompleted", params );

//         assertEquals( new Double( 1 ), result );

//         //now call getBenchmark on the service to get it back as a hashmap

//         result = xmlrpc.execute ( "benchmark.getBenchmarkAsHashtable", params );
//         System.out.println( "result: " + result );

//         result = xmlrpc.execute ( "benchmark.getBenchmarks", new Vector() );

//         System.out.println( "benchmarks are now: " + result );

//     }

    public void testDuration() throws Exception {

        //Benchmark.DISABLE_LOCAL = false;
        
        Benchmark benchmark = Benchmark.getBenchmark( Test1.class );
        benchmark = benchmark.child( "testDuration" );

        benchmark.start();
        Thread.sleep( 100 );
        benchmark.complete();

        benchmark.start();
        Thread.sleep( 100 );
        benchmark.complete();

        long duration = benchmark.getTracker1().now.duration;
        long meanDuration = benchmark.getTracker1().now.getMeanDuration();

        assertTrue("duration=" + duration, duration > 150);

        assertTrue("meanDuration=" + meanDuration, meanDuration > 50);
        assertTrue("meanDuration=" + meanDuration, meanDuration < 150 );

        benchmark.clear();

    }

    public void testLongSleep() throws Exception {

        //   - Potential bug.  If we don't log anything > 5 minutes the LAST benchmark
        //   will be rotated.  I actually have to check if the last benchmark was more
        //   than INTERVAL ago and if so then delete the last benchmark

        //     - A unit test for this would be easy...  I'd just have to sleep TWICE.

        //this is cheating a bit.  We set the intervals smaller so that we can
        //get through the tests quickly
        Benchmark.INTERVAL_1 =   1000;
        Benchmark.INTERVAL_5 =   5000;
        Benchmark.INTERVAL_15 = 15000;

        Benchmark benchmark = Benchmark.getBenchmark( Test1.class );
        benchmark = benchmark.child( "testLongSleep" );
        
        benchmark.start();
        benchmark.complete();

        Thread.sleep( 1000 );
        Thread.sleep( 2000 );

        assertEquals( 0, benchmark.getTracker1().last.getStarted() );
        assertEquals( 0, benchmark.getTracker1().now.getCompleted() );
        assertEquals( 0, benchmark.getTracker1().now.getDuration() );

        benchmark.clear();
        
    }
    
    public void testList() throws Exception {

        List list = new LinkedList();
        list = (List)Benchmark.getBenchmarkAsProxy( list, List.class );

        list.add( "hello" );
        list.add( "world" );
        list.iterator();
        
        Benchmark benchmark = Benchmark.getBenchmark( "java.util.List.iterator" );
        assertNotNull( benchmark );

        System.out.println( benchmark.getName() + ": " + benchmark );
        
    }

    public void testProxy() throws Exception {

        IFoo foo = new Foo();

        foo = (IFoo)Benchmark.getBenchmarkAsProxy( foo, IFoo.class );

        foo.doSomething();

        Benchmark benchmark = Benchmark.getBenchmark( "org.apache.commons.benchmark.IFoo.doSomething" );

        assertNotNull( benchmark );

        System.out.println( "Not null! " );

        assertEquals( 1, benchmark.getTracker1().now.started );
        assertEquals( 1, benchmark.getTracker1().now.completed );

        System.out.println( Benchmark.getBenchmarks() );

    }

    public void testIntervalReset() throws Exception {

        //this is cheating a bit.  We set the intervals smaller so that we can
        //get through the tests quickly
        Benchmark.INTERVAL_1 =   1000;
        Benchmark.INTERVAL_5 =   5000;
        Benchmark.INTERVAL_15 = 15000;

        Benchmark benchmark = Benchmark.getBenchmark( Test1.class );
        benchmark = benchmark.child( "main" );
        
        benchmark.start();
        benchmark.complete();

        assertEquals( 1, benchmark.getTracker1().now.getStarted() );
        assertEquals( 1, benchmark.getTracker1().now.getCompleted() );

        assertEquals( 1, benchmark.getTracker5().now.getStarted() );
        assertEquals( 1, benchmark.getTracker15().now.getCompleted() );

        Thread.sleep( 3000 );

        assertEquals( 0, benchmark.getTracker1().now.started );

        Thread.sleep( 5000 );
        assertEquals( 0, benchmark.getTracker5().now.started );

        //reset all the trackers
        benchmark.clear();
        
    }
    
    public void testBasic() {

        Benchmark benchmark = Benchmark.getBenchmark( Test1.class );
        benchmark = benchmark.child( "main" );

        System.out.println( "name: " + benchmark.getName() );
        
        assertEquals( benchmark.getName(), "org.apache.commons.benchmark.Test1.main" );

        int total = 60;
        
        for ( int i = 0; i < total; ++i ) {

            benchmark.start();

            assertEquals( benchmark.getTracker().now.getStarted(), i+1 );
            
            benchmark.complete();

            assertEquals( benchmark.getTracker().now.getCompleted(), i+1 );

        }

        //now call the tracker to force a reset
        benchmark.getTracker().rollover( System.currentTimeMillis() );

        assertEquals( benchmark.getTracker1().getNow().getStarted(), 0 );
        assertEquals( benchmark.getTracker1().getNow().getCompleted(), 0 );

        assertEquals( benchmark.getTracker1().getLast().getStarted(), total );
        assertEquals( benchmark.getTracker1().getLast().getCompleted(), total );

    }
    
    public static void main(String args[]) {

        try { 
            
            Test1 test = new Test1( null );
            //test.testXmlrpc();
            
        } catch ( Throwable t ) {
            
            t.printStackTrace();
            
        }

        System.exit( 0 );
        
    }

    private void resetForTests() {
        Benchmark.benchmarks = new HashMap();

    }
    
}

//test for working with a proxyied benchmark

interface IFoo {

    public void doSomething();
    
}

class Foo implements IFoo {

    public void doSomething() { }
    
}

class TestThread extends Thread {

    public TestThread( ThreadGroup tg ) {
        super( tg, "foo" );
    }
    
    public void run() {
        try { 

            Random r = new Random();
            
            Benchmark b = Benchmark.getBenchmark( "foo" + r.nextInt());
            
            b.start();
            Thread.sleep( 100 );
            b.complete();
            
        } catch ( Throwable t ) {
            
            t.printStackTrace();
            
        }

    }
    
}