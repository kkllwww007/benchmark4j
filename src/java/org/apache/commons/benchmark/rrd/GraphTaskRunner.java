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

package org.apache.commons.benchmark.rrd;

import java.util.*;

/**
 * Handles processing all tasks and updating graphs if enabled and necessary.
 *
 * @author <a href="mailto:burton@tailrank.com">Kevin A. Burton</a>
 */
public class GraphTaskRunner {

    public static final long SLEEP_INTERVAL_MILLIS = 60L * 1000L;
    public static final long COUNTER_INTERVAL_SECONDS = 60L;

    public static List tasks = new LinkedList();

    /**
     * Runs all given tasks.  Designed to be used within an infinite loop.
     */
    public void run() throws Exception {

        Iterator it = tasks.iterator();

        long lastUpdatedSeconds = GraphTask.currentTimeSeconds();

        while ( it.hasNext() ) {

            try {

                GraphTask t = (GraphTask)it.next();

                if ( t.isAlive() == false ) {
                    t.start();
                }

                while ( t.isWaiting == false ) {
                    Thread.sleep( 50 );
                }

                t.lastUpdatedSeconds = lastUpdatedSeconds;

                synchronized( t ) {
                    t.notifyAll();
                }

            } catch ( Throwable t ) {

                //FIXME: what happens here when we fail too many times..?  This
                //would be bad and we wouldn't really handle it well.
                t.printStackTrace();
            }

        } 

    }

    public void runForever() throws Exception {

        // ********************
        while ( true ) {

            //FIXME: modularize this more.
            
            long begin = System.currentTimeMillis();

            run();

            long duration = System.currentTimeMillis() - begin;

            long sleep_interval = GraphTaskRunner.SLEEP_INTERVAL_MILLIS - duration;

            if ( sleep_interval > 0 ) {

                System.out.println( "Sleeping for millis: " + sleep_interval );
                    
                //spin until the next read interval...
                Thread.sleep( sleep_interval );

            } else {

                System.out.println( "Took too long to perform tasks.  Not sleeping..." );
                    
            }

        }

    }
    
}
