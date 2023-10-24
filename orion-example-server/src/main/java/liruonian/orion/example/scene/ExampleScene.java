package liruonian.orion.example.scene;

import liruonian.orion.core.engine.Api;
import liruonian.orion.core.engine.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liruonian.orion.example.dto.PayRequest;
import liruonian.orion.example.dto.PayResponse;

@Scene(name = "example")
public class ExampleScene {

    private static final Logger logger = LoggerFactory
            .getLogger(ExampleScene.class);

    @Api(name = "pay")
    public PayResponse pay(PayRequest request) {
        logger.info("payment...");
        logger.info("PayAcct: " + request.getPayAcct());
        logger.info("RecvAcct: " + request.getRecvAcct());
        logger.info("Amount: " + request.getAmount());

        return PayResponse.success();
    }

}
