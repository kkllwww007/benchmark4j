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

public class CallerInfo {

    public int lineNumber;
    public String method;
    public String classname;

    public CallerInfo( int lineNumber, String method, String classname ) {
        this.lineNumber = lineNumber;
        this.method = method;
        this.classname = classname;
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

    /**
     * Pretty print this caller info so that we can use it in other tasks (MySQL
     * JDBC logging for example).
     */
    public String toString() {
        return classname + "." + method + ":" + lineNumber;
    }
    
}