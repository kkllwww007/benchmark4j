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

import java.io.*;
import java.util.*;
import java.net.*;
import java.text.*;

import org.jrobin.core.*;
import org.jrobin.graph.*;

import java.awt.Color;

/**
 * Handles updating Sources and updating the RRD information.  This is done so
 * that we can then generate human readable histographs from the data.  Note
 * that this is a Thread which is a bit heavy when all the datasources are local
 * benchmarks.  The problem is that we have NO idea the complexity of the Source
 * and it might block on database resources, filesystems, etc and we need to
 * make sure all the benchmarks complete at once.
 */
public class GraphTask extends Thread {

    /**
     * RRD 1.4 has a bug where they use a HashSet internall to keep track of
     * open writers which causes threads to screw up so we need to synchronize a
     * mutex.  Since this is just disk IO it should be OK.
     */
    public static final Object RRD_MUTEX = new Object();
    
    /**
     * True when we should generate tasks within the current process.
     */
    public static boolean ENABLE_GENERATE_GRAPHS=true;
    
    /**
     * Used to store RRDs and graphs.
     */
    public static String ROOT = "/var/benchmark";
    
    public long lastUpdatedSeconds = currentTimeSeconds();

    long begin = lastUpdatedSeconds;

    private String name, title = null;

    /**
     * Path to the RRD file.
     */
    private String rrd_path = null;

    /**
     * Unit title and description.
     */
    private String unit_desc, unit_name = null;

    /**
     * The given source for this task which provides getValue()
     */
    Source source = null;

    /**
     * True when this task is waiting to run again.
     */
    boolean isWaiting = false;

    /**
     * 
     * @param unit_desc The description of this unit.  Example: "Feeds Per Minute (FPM)"
     * @param unit_name The name of this unit.  Example: "FPM" 
     * @author <a href="mailto:burton@tailrank.com">Kevin A. Burton</a>
     */
    public GraphTask( String name,
                      String title,
                      String unit_desc,
                      String unit_name,
                      Source source ) {

        this.name = name;
        this.title = title;
        this.unit_desc = unit_desc;
        this.unit_name = unit_name;
        this.source = source;
        
        rrd_path = new File( ROOT, name + ".rrd" ).getPath();
        
    }

    /**
     * Get the current time in seconds for use with RRD.
     */
    public static long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * Simple time format.
     */
    public String formatTime( long v ) {

        return new SimpleDateFormat().format( new java.util.Date( v * 1000 ) );
    }

    /**
     * Generate all graphs necessary to view this RRD.
     */
    void generateGraphs() throws Exception {

        //FIXME: ideally we wouldn't have to generate these each time because
        //they waste a LOT of CPU.
        
        generateGraph( name + "-1-hour.png",
                       60 * 60,
                       title + " - 1 hour" );

        generateGraph( name + "-6-hours.png",
                       6 * 60 * 60,
                       title + " - 6 hours" );

        generateGraph( name + "-12-hours.png",
                       12 * 60 * 60,
                       title + " - 12 hours" );

        generateGraph( name + "-1-day.png",
                       24 * 60 * 60,
                       title + " - 1 day" );

        generateGraph( name + "-1-week.png",
                       7 * 24 * 60 * 60,
                       title + " - 1 week" );

        generateGraph( name + "-1-month.png",
                       30 * 24 * 60 * 60,
                       title + " - 1 month" );

        generateGraph( name + "-1-year.png",
                       365 * 24 * 60 * 60,
                       title + " - 1 year" );
        
    }

    void generateGraph( String path,
                        long interval,
                        String title ) throws Exception {

        if ( ENABLE_GENERATE_GRAPHS = false )
            return;
        
        path = new File( ROOT, path ).getPath();
        
        System.out.println( "Generating graph: " + path + " ..." );

        //FIXME: only generate the last HOURS worth of data.
        
        RrdGraphDef graphDef = new RrdGraphDef();

        long startTime, endTime = lastUpdatedSeconds;

        startTime = endTime - interval;

        System.out.println( "startTime: " + formatTime( startTime ) );
        System.out.println( "endTime: " + formatTime( endTime ) );

        graphDef.setShowSignature( false );

        graphDef.setTimePeriod( startTime, endTime );
        graphDef.setTitle( title );
        graphDef.datasource( "myspeed", rrd_path, "speed", "AVERAGE" );

        //graphDef.line( "myspeed",
        //                new Color.RED,
        //               "Feeds Per Minute (FPM)",
        //               1 );

        graphDef.area( "myspeed", Color.GREEN, unit_desc );

        //FIXME: don't include 3 decimal places of precision.  We don't need
        //this.
        graphDef.gprint( "myspeed", "AVERAGE", "Average " + unit_name + ": @0@r");
        graphDef.gprint( "myspeed", "MAX", "Max " + unit_name + ": @0@r");
        graphDef.gprint( "myspeed", "MIN", "Min " + unit_name + ": @0@r");

        //Sat Dec 25 2004 03:04 PM (burton@tailrank.com): LAST doesn't work and
        //returns NaN.  Why is this?
        //graphDef.gprint( "myspeed", "LAST", "Current " + unit_name + ": @0@r");

        //FIXME: would be NICE to have STDDEV here as well as MEDIAN as well as
        //LAST.  I could ADD this to JRobin but I'd need to do it in
        //FetchData.java and add getStandardDeviation and getMedian methods.
        //Should be easy though.cn

        //NOTE: total doesn't seem to make much sense at all times.
        //graphDef.gprint( "myspeed", "TOTAL", "Total " + unit_name + ": @0@r");

        graphDef.comment( "Last updated on: " + formatTime( endTime ) );

        RrdGraph graph = new RrdGraph( graphDef );

        //graph.saveAsGIF( path );
        graph.saveAsPNG( path );

        System.out.println( "Generating graph: " + path + " ...done" );
        //graph.close();
        
    }

    /**
     * Create the RRD when it doesn't exist including setting up archive
     * functions, adding datasources, etc.
     *
     * @author <a href="mailto:burton@tailrank.com">Kevin A. Burton</a>
     */
    private void doCreateRRD() throws Exception {

        synchronized( RRD_MUTEX ) {
        
            new File( ROOT ).mkdirs();
        
            System.out.println( "Creating rrd file: " + rrd_path );

            RrdDef rrdDef = new RrdDef( rrd_path );
            rrdDef.setStartTime( begin  );

            rrdDef.addDatasource( "speed",
                                  "GAUGE",
                                  GraphTaskRunner.COUNTER_INTERVAL_SECONDS,
                                  Double.NaN,
                                  Double.NaN );

            //set the step to 1 minute (the default is 5 minutes).
            rrdDef.setStep( GraphTaskRunner.COUNTER_INTERVAL_SECONDS );
        
            //keep one measurement for every single poll.

            //rrdDef.addArchive( "AVERAGE", 0.5, 1, 3600 );

            //rrdDef.addArchive( "AVERAGE", 0.5, 1, 60 );

            //FIXME: increase this to about 400... because this would yield a much
            //higher density image.
        
            //6 hours (6 minute intervals... with 60 measurements which yields 360
            //minutes which is 6 hours).

            rrdDef.addArchive( "AVERAGE", 0.5, 1, 360 );

            //12 hours
            rrdDef.addArchive( "AVERAGE", 0.5, 2, 360 );

            //24 hours
            rrdDef.addArchive( "AVERAGE", 0.5, 4, 360 );

            //1 week
            rrdDef.addArchive( "AVERAGE", 0.5, 28, 360 );

            //1 month
            rrdDef.addArchive( "AVERAGE", 0.5, 120, 360 );

            //1 year
            rrdDef.addArchive( "AVERAGE", 0.5, 1460, 360 );

            //FIXME: don't we need additional archives here?
        
            RrdDb rrdDb = new RrdDb( rrdDef );
            rrdDb.close();

        }
            
    }
    
    /**
     * Exec this task in the current thread.
     */
    public void exec() throws Exception {

        File file = new File( rrd_path );

        if ( file.exists() == false ) {
            doCreateRRD();     
        }

        synchronized( RRD_MUTEX ) {
        
            RrdDb rrdDb = new RrdDb( rrd_path );
            Sample sample = rrdDb.createSample();

            try {

                long v = source.getValue();

                //NOTE: this is the correct mechanism but it might be off by a bit due
                //to skew with running getValue() from diff impls...
                //lastUpdatedSeconds = currentTimeSeconds();

                lastUpdatedSeconds += GraphTaskRunner.COUNTER_INTERVAL_SECONDS;
        
                String time = formatTime( lastUpdatedSeconds );

                System.out.println( "time: " + time + " , " + unit_name + " value: " + v );
                System.out.println( "lastUpdated: " + lastUpdatedSeconds );
        
                sample.setAndUpdate( lastUpdatedSeconds + ":" + v );
                //sample.close();
            
            } finally {

                rrdDb.sync();
                rrdDb.close();
            }

        }
            
    }
    
    /**
     * Run this task as a dedicated thread waiting to be notified by the runner.
     */
    public void run() {

        while ( true ) {

            isWaiting = true;

            synchronized( this ) {

                try {

                    wait();
                    isWaiting = false;
                
                    exec();
                    generateGraphs();
            
                } catch ( Throwable t ) {
                    t.printStackTrace();
                }
            
            }
        }        
    }
}