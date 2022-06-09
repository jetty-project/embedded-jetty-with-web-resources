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
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        String bootstrapVersion = findVersion(ServerTest.class.getClassLoader(), "/webjars/bootstrap/");

        HttpURLConnection http = (HttpURLConnection)serverBaseURI.resolve("/webjars/bootstrap/" + bootstrapVersion + "/css/bootstrap.css").toURL().openConnection();
        http.connect();
        dumpRequestResponse(http);
        assertEquals(HttpURLConnection.HTTP_OK, http.getResponseCode());
        assertEquals("text/css", http.getHeaderField("Content-Type"));
    }

    @Test
    public void testGetJQueryResource() throws Exception
    {
        String jqueryVersion = findVersion(ServerTest.class.getClassLoader(), "/webjars/jquery/");

        HttpURLConnection http = (HttpURLConnection)serverBaseURI.resolve("/webjars/jquery/" + jqueryVersion + "/jquery.js").toURL().openConnection();
        http.connect();
        dumpRequestResponse(http);
        assertEquals(HttpURLConnection.HTTP_OK, http.getResponseCode());
        assertEquals("application/javascript", http.getHeaderField("Content-Type"));
    }

    /**
     * Find the actual version in the jar files, so we don't have to hardcode the version in the testcases.
     *
     * @param classLoader the classloader to look in
     * @param prefix the prefix webjar to look for.
     * @return the version found.
     */
    private String findVersion(ClassLoader classLoader, String prefix) throws IOException
    {
        List<URL> hits = Collections.list(classLoader.getResources("META-INF/resources" + prefix));
        for (URL hit : hits)
        {
            try (Resource res = Resource.newResource(hit))
            {
                for (String content : res.list())
                {
                    if (content.matches("^[0-9.]+.*"))
                    {
                        if (content.endsWith("/"))
                            return content.substring(0,content.length() - 1);
                        return content;
                    }
                }
            }
        }
        throw new RuntimeException("Unable to find version for " + prefix);
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
