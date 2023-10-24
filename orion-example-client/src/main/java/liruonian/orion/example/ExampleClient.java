package liruonian.orion.example;

import liruonian.orion.client.proxy.ClientProxy;
import liruonian.orion.lifecycle.LifecycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liruonian.orion.client.rpc.OrionClient;
import liruonian.orion.example.dto.PayRequest;
import liruonian.orion.example.dto.PayResponse;
import liruonian.orion.example.scene.Pay;

public class ExampleClient {

    private static final Logger logger = LoggerFactory
            .getLogger(ExampleClient.class);
    
    public static void main(String[] args) throws LifecycleException {
        OrionClient client = new OrionClient();
        client.start();

        Pay payScene = ClientProxy.getProxy("127.0.0.1:10880", Pay.class, client);

        PayResponse response = payScene.pay(new PayRequest("62250000", "62251100", 10.24));

        if (response.isStatus()) {
            logger.info("succeeded");
        } else {
            logger.info("failed: " + response.getMessage());
        }

        client.stop();
    }

}
