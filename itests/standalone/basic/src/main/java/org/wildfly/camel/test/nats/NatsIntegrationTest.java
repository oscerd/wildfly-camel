package org.wildfly.camel.test.nats;

import java.io.InputStream;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.gravia.resource.ManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nats.Connection;
import org.wildfly.extension.camel.CamelAware;

@CamelAware
@RunWith(Arquillian.class)
public class NatsIntegrationTest {

    @Deployment
    public static JavaArchive createDeployment() {
    	return ShrinkWrap.create(JavaArchive.class, "came-nats-tests.jar");
    }

    @Test
    public void testNatsRoutes() throws Exception {

        CamelContext camelctx = new DefaultCamelContext();
        try {
            camelctx.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                	from("nats://demo.nats.io:4222?topic=test&maxMessages=5")
                	.to("mock:result");
                }
            });

            camelctx.start();
            
            MockEndpoint to = camelctx.getEndpoint("mock:result", MockEndpoint.class);
            to.expectedBodiesReceivedInAnyOrder("message-0", "message-1", "message-2", "message-3","message-4");
            to.expectedMessageCount(5);

            
            Properties opts = new Properties();
            opts.put("servers", "nats://demo.nats.io:4222");

            Connection conn = Connection.connect(opts);
            for(int i=0;i<10;i++) {
                conn.publish("test", "message-" + i);
            }
            
            to.assertIsSatisfied(3000);

        } finally {
            camelctx.stop();
        }
    }
}
