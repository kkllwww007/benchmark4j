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
 * Interface that allows external callers to obtain runtime benchmark code when
 * stats are rolled over.
 */
public interface BenchmarkListener {

    /**
     * Called when we rollover a metric. This should NOT block and should return
     * right away.  If you need to perform IO or a database operation these
     * should be done in a separate thread as they could cause the benchmark
     * code to slow.
     */
    public void onRollover( Benchmark benchmark,
                            BenchmarkTracker tracker,
                            BenchmarkMeta meta );

}