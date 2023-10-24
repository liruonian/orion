package liruonian.orion.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import liruonian.orion.Valve;
import liruonian.orion.ValveChain;
import liruonian.orion.core.engine.Invocation;
import liruonian.orion.core.engine.Pipeline;
import liruonian.orion.core.engine.RequestFaced;
import liruonian.orion.core.engine.ResponseFaced;
import liruonian.orion.core.engine.StandardPipeline;

public class StandardPipelineTestCase {

    private int i = 1;

    @Test
    public void testPipeline() {
        Pipeline pipeline = new StandardPipeline();

        pipeline.setBasic(new Valve() {
            @Override
            public void invoke(RequestFaced request, ResponseFaced response,
                    Invocation invocation, ValveChain chain) {
                assertEquals(3, i++);
                chain.invokeNext(request, response, invocation);
            }
        });

        pipeline.addValve(new Valve() {
            @Override
            public void invoke(RequestFaced request, ResponseFaced response,
                    Invocation invocation, ValveChain chain) {
                assertEquals(1, i++);
                chain.invokeNext(request, response, invocation);
            }
        });

        pipeline.addValve(new Valve() {
            @Override
            public void invoke(RequestFaced request, ResponseFaced response,
                    Invocation invocation, ValveChain chain) {
                assertEquals(2, i++);
                chain.invokeNext(request, response, invocation);
            }
        });

        pipeline.invoke(null, null, null);
    }
}
