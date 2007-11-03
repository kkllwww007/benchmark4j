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

/**
 * Represents an abstract datasource for an source for logging.
 *
 * @author <a href="mailto:burton@tailrank.com">Kevin A. Burton</a>
 * @version $Id: $
 */
public abstract class Source {

    String title = null;
    String description = null;

    /**
     * Get the current value for this counter.
     *
     * @author <a href="mailto:burton@tailrank.com">Kevin A. Burton</a>
     */
    public abstract long getValue() throws Exception;
    
}