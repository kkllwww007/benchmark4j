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

import org.apache.commons.benchmark.config.*;

/**
 * Simple commandline rrd grapher based on GraphTaskRunner.  This can be used by
 * itself or with another package.
 *
 * @author <a href="mailto:burton@tailrank.com">Kevin A. Burton</a>
 * @version $Id: $
 */
public class Main {

    /**
     * Default path to the configuration file for launching the RRD daemon.
     */
    public static String CONFIG = "/etc/commons-benchmark/benchmark.xml";
    
    public static void main( String[] args ) throws Exception {

        XMLConfigurator.configure( CONFIG );

        GraphTaskRunner runner = new GraphTaskRunner();
        runner.runForever();
        
    }

}
