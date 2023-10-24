package liruonian.orion.example.scene;

import liruonian.orion.client.ServiceInvocation;
import liruonian.orion.example.dto.PayRequest;
import liruonian.orion.example.dto.PayResponse;

public interface Pay {

    @ServiceInvocation(name = "example.pay")
    public PayResponse pay(PayRequest request);
}
