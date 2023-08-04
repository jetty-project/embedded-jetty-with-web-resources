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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;

public class ExampleServer
{
    private Server server;

    public static void main(String[] args) throws Exception
    {
        ExampleServer exampleServer = new ExampleServer();
        Server server = exampleServer.createServer(8888);
        server.start();
        server.join();
    }

    public Server createServer(int port) throws Exception
    {
        server = new Server(port);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        server.setDefaultHandler(new DefaultHandler());

        ResourceFactory resourceFactory = ResourceFactory.of(context);
        Resource manifestResources = resourceFactory.newResource(findManifestResources(ExampleServer.class.getClassLoader()));
        context.setBaseResource(manifestResources);

        // Add something to serve the static files
        // It's named "default" to conform to servlet spec
        ServletHolder staticHolder = new ServletHolder("default", DefaultServlet.class);
        context.addServlet(staticHolder, "/");

        server.setHandler(context);
        return server;
    }

    private List<URI> findManifestResources(ClassLoader classLoader) throws IOException
    {
        return Collections.list(classLoader.getResources("META-INF/resources"))
            .stream()
            .map(ExampleServer::toURI)
            .filter(Objects::nonNull)
            .toList();
    }

    protected static URI toURI(URL url)
    {
        try
        {
            return url.toURI();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace(System.err);
            return null;
        }
    }

    public Server getServer()
    {
        return server;
    }
}
