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
public class TestPerformance extends TestCase {

    static int TEST1_COUNT = 100000;

    static Benchmark benchmark = new Benchmark();

    public TestPerformance(String testName) {
        super(testName);
    }

    public void testTransactionsPerSecond() throws Exception {

        //FIXME: we need to re-add this.
        
//         BenchmarkMethodMeta bmeta =
//             BenchmarkUtils.benchmarkMethod( "doTest1", 1, getClass() );

//         assertEquals( benchmark.tracker1.now.started, TEST1_COUNT );
//         assertEquals( benchmark.tracker1.now.completed, TEST1_COUNT );

//         double tps = ((double)TEST1_COUNT / (double)bmeta.duration) * 1000D;

//         assertTrue( "Not meeting minimum TPS", tps > 150000 );

//         //NOW disable the whole thing and trytry again.
//         Benchmark.DISABLED=true;

//         bmeta = BenchmarkUtils.benchmarkMethod( "doTest1", 1, getClass() );

//         tps = ((double)TEST1_COUNT / (double)bmeta.duration) * 1000D;

//         assertTrue( "Not meeting minimum TPS", tps > 5000000 );

//         System.out.println( bmeta.getReport() );
        
    }

    public static void doTest1() {

        for ( int i = 0; i < TEST1_COUNT; ++i ) {

            benchmark.start();
            benchmark.complete();
            
        }

    }

    public static void main( String[] args ) throws Exception {

        TestPerformance test = new TestPerformance( null );
        test.testTransactionsPerSecond();
        
    }

}

