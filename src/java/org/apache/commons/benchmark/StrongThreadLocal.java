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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 */
public abstract class StrongThreadLocal<T>  {

    private Map<Thread,T> delegate = new ConcurrentHashMap();

    public T get() {

        Thread t = Thread.currentThread();
        
        T value = delegate.get( t );

        if ( value == null ) {
            value = initialValue();
            delegate.put( t, value );
        }

        return value;
        
    }

    protected abstract T initialValue();
    
}