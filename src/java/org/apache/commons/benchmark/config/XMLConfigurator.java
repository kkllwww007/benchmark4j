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

package org.apache.commons.benchmark.config;

import org.apache.commons.benchmark.rrd.*;

import java.util.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;

import org.apache.commons.beanutils.*;

/**
 * Simple XML based configuration tool.
 * 
 * @author <a href="mailto:burton@tailrank.com">Kevin Burton</a>
 * @version $Id: Benchmark.java,v 1.3 2005/02/16 02:28:09 burton Exp $
 */
public class XMLConfigurator {

    /**
     * Configure benchmark for the current VM.
     */
    public static void configure( String path ) throws Exception {

        DocumentBuilder builder = DocumentBuilderFactory
            .newInstance().newDocumentBuilder();

        Document document = builder.parse( path );

        Element root = document.getDocumentElement();

        NodeList tasksList = root.getElementsByTagName( "tasks" );

        if ( tasksList.getLength() > 0 ) {

            //process tasks
            Node tasksNode = tasksList.item( 0 );
            Element tasksElement = (Element)tasksNode;

            NodeList sourceList = tasksElement.getElementsByTagName( "source" );

            for ( int i = 0; i < sourceList.getLength(); ++i ) {

                Node sourceNode = sourceList.item( i );
                Element sourceElement = (Element)sourceNode;

                String classname = sourceElement.getAttribute( "classname" );
                String name = sourceElement.getAttribute( "name" );
                String title = sourceElement.getAttribute( "title" );

                String unitDescription = sourceElement.getAttribute( "unitDescription" );
                String unitName = sourceElement.getAttribute( "unitName" );

                //FIXME: process params as well.

                Class clazz = Class.forName( classname );

                Source source = (Source)clazz.newInstance();

                //set properties on the source with beansutils.
                applyParams( source, sourceElement );
                
                GraphTask task = new GraphTask( name,
                                                title,
                                                unitDescription,
                                                unitName,
                                                source );

                GraphTaskRunner.tasks.add( task );
                
            }

        }

    }

    public static void applyParams( Source source, Element sourceElement )
        throws Exception {

        NodeList paramList = sourceElement.getElementsByTagName( "param" );

        for ( int j = 0; j < paramList.getLength(); ++j ) {

            Node paramNode = paramList.item( j );
            Element paramElement = (Element)paramNode;

            String name = paramElement.getAttribute( "name" );
            String value = paramElement.getAttribute( "value" );

            if ( value == null || value.equals( "" ) ) {
                value = paramElement.getFirstChild().getNodeValue();
                
            } 
            
            PropertyUtils.setSimpleProperty( source, name, value );
            
        }

    }
    
    public static void main( String[] args ) throws Exception {
        configure( "/projects/ksa/benchmark.xml" );
    }

}