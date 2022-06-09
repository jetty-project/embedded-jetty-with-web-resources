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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.JarFileResource;
import org.eclipse.jetty.util.resource.JarResource;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;

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

        HandlerList handlers = new HandlerList();

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");

        Resource manifestResources = findManifestResources(ExampleServer.class.getClassLoader());
        context.setBaseResource(manifestResources);

        // Add something to serve the static files
        // It's named "default" to conform to servlet spec
        ServletHolder staticHolder = new ServletHolder("default", DefaultServlet.class);
        context.addServlet(staticHolder, "/");

        handlers.addHandler(context);
        handlers.addHandler(new DefaultHandler()); // always last handler

        server.setHandler(handlers);
        return server;
    }

    private Resource findManifestResources(ClassLoader classLoader) throws IOException
    {
        List<URL> hits = Collections.list(classLoader.getResources("META-INF/resources"));
        int size = hits.size();
        Resource[] resources = new Resource[hits.size()];
        for (int i = 0; i < size; i++)
        {
            resources[i] = Resource.newResource(hits.get(i));
        }
        return new ResourceCollection(resources);
    }

    public Server getServer()
    {
        return server;
    }

    private void ensureDirExists(Path path) throws IOException
    {
        if (!Files.exists(path))
        {
            Files.createDirectories(path);
        }
    }
}
