package org.wildfly.camel.test.nats;

import java.io.IOException;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nats.Connection;
import org.wildfly.extension.camel.CamelAware;

@CamelAware
@RunWith(Arquillian.class)
public class NatsIntegrationTest {

	private static ProcessBuilder pb;
    private static Process p;
	private static String gnatsdEnvironmentProperty = "GNATSD_PATH";
	
    @Deployment
    public static JavaArchive createDeployment() {
    	return ShrinkWrap.create(JavaArchive.class, "camel-nats-tests.jar");
    }

    @Test
    public void testNatsComponent() throws Exception {
        CamelContext camelctx = new DefaultCamelContext();
        Endpoint endpoint = camelctx.getEndpoint("nats://localhost:4222?topic=test");
        Assert.assertNotNull(endpoint);
        Assert.assertEquals(endpoint.getClass().getName(), "org.apache.camel.component.nats.NatsEndpoint");
    }
    
    @Test
    public void testNatsRoutes() throws Exception {
    	
        String gnatsdPath = System.getenv().get(gnatsdEnvironmentProperty);

        Assume.assumeTrue(gnatsdPath != null);
        
        pb = new ProcessBuilder(gnatsdPath);
        try {
            p = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        CamelContext camelctx = new DefaultCamelContext();
        try {
            camelctx.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                	from("nats://localhost:4222?topic=test")
                	.to("mock:result");
                }
            });
            
            MockEndpoint to = camelctx.getEndpoint("mock:result", MockEndpoint.class);
            to.expectedBodiesReceivedInAnyOrder("message");
            to.expectedMessageCount(1);

            camelctx.start();
            
            Properties opts = new Properties();
            opts.put("servers", "nats://localhost:4222");

            Connection conn = Connection.connect(opts);
            conn.publish("test", "message");
            
            to.assertIsSatisfied(10000);

        } finally {
            camelctx.stop();
            if (p != null) {
                p.destroy();
            }
        }
    }
}
