package liruonian.orion;

import liruonian.orion.core.engine.Invocation;
import liruonian.orion.core.engine.RequestFaced;
import liruonian.orion.core.engine.ResponseFaced;

/**
 * Valve chain
 *
 *
 * @author lihao
 * @date 2020年8月27日
 * @version 1.0
 */
public interface ValveChain {

    /**
     * 执行下一个{@link Valve}
     *
     * @param request
     * @param response
     * @param invocation
     */
    public void invokeNext(RequestFaced request, ResponseFaced response,
                           Invocation invocation);
}
