package liruonian.orion.valves;

import liruonian.orion.remoting.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;

import liruonian.orion.Constants;
import liruonian.orion.EventBus;
import liruonian.orion.ValveChain;
import liruonian.orion.commons.StringManager;
import liruonian.orion.core.engine.Invocation;
import liruonian.orion.core.engine.RequestFaced;
import liruonian.orion.core.engine.ResponseFaced;

/**
 * 日志记录的业务处理单元
 *
 * @author lihao
 * @date 2020年8月28日
 * @version 1.0
 */
public class LogValve extends ValveBase {

    private static final Logger logger = LoggerFactory.getLogger(LogValve.class);
    private static final StringManager sm = StringManager.getManager(Constants.PACKAGE);

    public LogValve(EventBus eventBus) {
        super(eventBus);
    }

    /*
     * @see liruonian.orion.Valve#invoke(liruonian.orion.RequestFaced,
     * liruonian.orion.ResponseFaced, liruonian.orion.Invocation,
     * liruonian.orion.ValveChain)
     */
    @Override
    public void invoke(RequestFaced request, ResponseFaced response,
            Invocation invocation, ValveChain chain) {
        if (logger.isInfoEnabled()) {
            logger.info(sm.getString("logValve.invoke.request",
                    new String[] { String.valueOf(request.getId()),
                            request.getServiceName(),
                            request.getParameters() == null ? "[]"
                                    : JSONArray.toJSONString(request.getParameters()) }));
        }
        chain.invokeNext(request, response, invocation);

        if (logger.isInfoEnabled()) {
            if (response.getStatus() != null) {
                if (response.getStatus() == Status.SUCCESS) {
                    logger.info(sm.getString("logValve.invoke.response", new String[] {
                            String.valueOf(request.getId()), response.getStatus().name(),
                            response.getResponseBody() == null ? ""
                                    : JSONArray
                                            .toJSONString(response.getResponseBody()) }));
                } else {
                    logger.info(sm.getString("logValve.invoke.response",
                            new String[] { String.valueOf(request.getId()),
                                    response.getStatus().name(), "" }));
                }
            }
        }

    }

}
