//
//  ========================================================================
//  Copyright (c) 1995-2019 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.demo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerTest
{
    public static ExampleServer main;

    public static URI serverBaseURI;

    @BeforeAll
    public static void initServer() throws Exception
    {
        main = new ExampleServer();
        main.createServer(0);
        main.getServer().start();
        serverBaseURI = main.getServer().getURI().resolve("/");
    }

    @AfterAll
    public static void stopServer()
    {
        LifeCycle.stop(main.getServer());
    }

    @Test
    public void testGetMyReadmeResource() throws Exception
    {
        HttpURLConnection http = (HttpURLConnection)serverBaseURI.resolve("/MYREADME.txt").toURL().openConnection();
        http.connect();
        dumpRequestResponse(http);
        assertEquals(HttpURLConnection.HTTP_OK, http.getResponseCode());
        assertEquals("text/plain", http.getHeaderField("Content-Type"));
    }

    @Test
    public void testGetBootStrapResource() throws Exception
    {
        String bootstrapFile = findMetaInfResourceFile(ServerTest.class.getClassLoader(), "/webjars/bootstrap/", "bootstrap\\.css");

        HttpURLConnection http = (HttpURLConnection)serverBaseURI.resolve(bootstrapFile).toURL().openConnection();
        http.connect();
        dumpRequestResponse(http);
        assertEquals(HttpURLConnection.HTTP_OK, http.getResponseCode());
        assertEquals("text/css", http.getHeaderField("Content-Type"));
    }

    /**
     * Find the actual version in the jar files, so we don't have to hardcode the version in the testcases.
     *
     * @param classLoader the classloader to look in
     * @param prefix the prefix webjar to look for.
     * @param regex the regex to match the first hit against.
     * @return the found resource
     */
    private String findMetaInfResourceFile(ClassLoader classLoader, String prefix, String regex) throws IOException
    {
        List<URL> hits = Collections.list(classLoader.getResources("META-INF/resources" + prefix));
        for (URL hit : hits)
        {
            try (ResourceFactory.Closeable resourceFactory = ResourceFactory.closeable())
            {
                Resource res = resourceFactory.newResource(hit);
                Resource match = findNestedResource(res, regex);
                if (match != null)
                {
                    String rawpath = match.toString();
                    int idx;

                    // use only part after `!/`
                    idx = rawpath.lastIndexOf("!/");
                    if (idx >= 0)
                        rawpath = rawpath.substring(idx + 2);

                    // find substring starting at prefix
                    idx = rawpath.indexOf(prefix);
                    if (idx >= 0)
                        return rawpath.substring(idx);
                    return rawpath;
                }
            }
        }
        throw new RuntimeException("Unable to find resource [" + regex + "] in " + prefix);
    }

    private Resource findNestedResource(Resource res, String regex) throws IOException
    {
        for (Resource subresource : res.list())
        {
            if (subresource.getFileName().matches(regex))
                return subresource;
            if (subresource.isDirectory())
            {
                Resource nested = findNestedResource(subresource, regex);
                if (nested != null)
                    return nested;
            }
        }
        return null;
    }

    private static void dumpRequestResponse(HttpURLConnection http)
    {
        System.out.println();
        System.out.println("----");
        System.out.printf("%s %s HTTP/1.1%n", http.getRequestMethod(), http.getURL());
        System.out.println("----");
        System.out.printf("%s%n", http.getHeaderField(null));
        http.getHeaderFields().entrySet().stream()
            .filter(entry -> entry.getKey() != null)
            .forEach((entry) -> System.out.printf("%s: %s%n", entry.getKey(), http.getHeaderField(entry.getKey())));
    }
}
