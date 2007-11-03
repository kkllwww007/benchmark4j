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

package org.apache.commons.benchmark.broadcast;

import org.apache.commons.benchmark.*;

import java.io.*;
import java.net.*;

/**
 * 
 * @author <a href="mailto:burton@tailrank.com">Kevin Burton</a>
 * @version $Id: Benchmark.java,v 1.3 2005/02/16 02:28:09 burton Exp $
 */
public class GangliaBroadcaster {

    public void broadcast( Benchmark benchmark ) {

        //
        
    }
    
    public static void main( String[] args ) throws Exception {

        // here is the structure of a gmetric message in xdr spec format.

        // struct Ganglia_gmetric_message {
        //   string type<>;
        //   string name<>;
        //   string value<>;
        //   string units<>;
        //   unsigned int slope;
        //   unsigned int tmax;
        //   unsigned int dmax;
        // }; 

        // Here is the structure as a packet 

        // 00000000  00 00 00 00 00 00 00 07  75 69 6e 74 33 32 00 00  |........uint32..|
        // 00000010  00 00 00 04 66 6f 6f 00  00 00 00 04 31 30 30 00  |....foo.....100.|
        // 00000020  00 00 00 01 00 00 00 00  00 00 00 03 00 00 00 3c  |...............<|
        // 00000030  00 00 00 00                                       |....|
        // 00000034

        //FIXME: need units, slope, tmax and dmax
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        //  Either string|int8|uint8|int16|uint16|int32|uint32|float|double
        String type = "uint32";
        String name = "foo";
        int value = 100;
        int port = 8649;
        String address = "228.5.6.7";
        
        bos.write( new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x07 } );
        bos.write( type.getBytes() );
        bos.write( new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06 } );
        bos.write( name.getBytes() );
        bos.write( new byte[] { 0x00, 0x00, 0x00, 0x00, 0x04 } );
        bos.write( Integer.toString( value ).getBytes() );
        bos.write( new byte[] { 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00,
                                0x00, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00,
                                0x00, 0x3c, 0x00, 0x00, 0x00, 0x00 } );

        //this is our new packet.  Now we should just be able to broadcast it on
        //the subnet.

        byte bytes[] = bos.toByteArray();
        
        InetAddress group = InetAddress.getByName( address );
        MulticastSocket s = new MulticastSocket( port );
        s.joinGroup( group );

        DatagramPacket packet = new DatagramPacket( bytes, bytes.length, group, port ); 
        s.send( packet );
        
    }

}

