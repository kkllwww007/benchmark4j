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

package org.apache.commons.benchmark.proxy;

import org.apache.commons.benchmark.*;

import java.lang.reflect.*;

public class BenchmarkProxyFactory {

//     public static Object newBenchmarkFactory( Class _instance,
//                                               Class _interface ) {

//         return newBenchmarkFactory( _instance.newInstance(), _interface );
        
//     }
        
    public static Object newBenchmarkFactory( Object _instance,
                                              Class _interface ) {

        Object proxy = Proxy.newProxyInstance( _interface.getClassLoader(),
                                               new Class[] { _interface },
                                               new BenchmarkInvocationHandler( _instance ) );

        return proxy;
        
    }
    
}

class BenchmarkInvocationHandler implements InvocationHandler {

    private Object target = null;
    
    public BenchmarkInvocationHandler( Object target ) {
        this.target = target;
    }

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {

        String name = method.getDeclaringClass().getName() + "." + method.getName();
        
        Benchmark benchmark = Benchmark.getBenchmark( name );

        //before our method
        benchmark.start();

        //need to set this as accessible or we can't call it when its in a diff package.
        method.setAccessible( true );
        Object result = method.invoke( target, args );

        //after our method
        benchmark.complete();
        
        return result;

    }

}