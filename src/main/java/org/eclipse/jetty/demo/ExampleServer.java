package org.eclipse.jetty.demo;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

public class ExampleServer
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);

        HandlerList handlers = new HandlerList();

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");

        URL url = ExampleServer.class.getResource("/META-INF/resources/MYREADME.txt");
        if(url == null)
            throw new FileNotFoundException("Unable to find required META-INF/resources");

        URI baseURI = url.toURI().resolve("./"); // resolve to directory itself.
        System.out.println("Base Resource URI is " + baseURI);

        context.setBaseResource(Resource.newResource(baseURI));

        // Add something to serve the static files
        // It's named "default" to conform to servlet spec
        ServletHolder staticHolder = new ServletHolder("default", DefaultServlet.class);
        context.addServlet(staticHolder, "/");

        handlers.addHandler(context);
        handlers.addHandler(new DefaultHandler()); // always last handler

        server.setHandler(handlers);
        server.start();

        System.out.println("Your server is started on " + server.getURI());
        server.join();
    }
}
