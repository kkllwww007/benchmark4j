
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

/**
 * Simple API for passing caller context into applications for debug use.  API
 * is to create a new instance in code as a static object and then call init()
 * before you use it.
 * 
 */
public class CallerContext {

    private Object MUTEX = new Object();
    
    private boolean initialized = false;
    public int lineNumber;
    public String method;
    public String classname;

    public CallerContext() { }

    public CallerContext init() {

        synchronized( MUTEX ) {
        
            if ( ! initialized ) {
                
                StackTraceElement caller = getCallerStackTraceElement();
                
                lineNumber = caller.getLineNumber();
                method = caller.getMethodName();
                classname = caller.getClassName();
                
            }

        }
            
        return this;
        
    }

    public int getLineNumber() {
        
        return lineNumber;
        
    }

    public String getMethod() {
        
        return method;
    }

    public String getClassname() {
        
        return classname;
    }

    private static StackTraceElement getCallerStackTraceElement() {

        Exception e = new Exception();

        StackTraceElement trace[] = e.getStackTrace();

        for ( int i = 2; i < trace.length; ++i  ) {

            StackTraceElement current = trace[i];
            String name = current.getClassName();

            if ( name.startsWith( "org.apache.commons.benchmark" ) )
                continue;
            
            if ( name.startsWith( "org.apache.commons.feedparser.network" ) )
                continue;

            return current;

        }

        return null;

    }

}