package liruonian.orion.core.engine;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import liruonian.orion.lifecycle.LifecycleException;
import liruonian.orion.lifecycle.LifecycleSupport;
import liruonian.orion.remoting.Connection;
import liruonian.orion.remoting.message.MessageFactory;
import liruonian.orion.remoting.message.RpcRequest;
import liruonian.orion.valves.InvocationValve;
import liruonian.orion.valves.LogValve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.util.StrUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import liruonian.orion.Configs;
import liruonian.orion.Constants;
import liruonian.orion.Engine;
import liruonian.orion.EventBus;
import liruonian.orion.Service;
import liruonian.orion.Valve;
import liruonian.orion.commons.NamedThreadFactory;
import liruonian.orion.commons.StringManager;
import liruonian.orion.config.EngineConfig;
import liruonian.orion.core.bus.Event;
import liruonian.orion.core.bus.EventDiscardException;
import liruonian.orion.core.bus.RequestEvent;
import liruonian.orion.core.bus.ResponseEvent;

/**
 * 标准的引擎实现，引擎主要与{@link EventBus}关联，处理请求和响应事件
 *
 *
 * @author lihao
 * @date 2020年8月28日
 * @version 1.0
 */
public class StandardEngine extends LifecycleSupport implements Engine {

    private static final Logger logger = LoggerFactory.getLogger(StandardEngine.class);
    private static final StringManager sm = StringManager.getManager(Constants.PACKAGE);
    private static final String DOT = ".";

    private Service service;
    private EventBus eventBus;
    private Pipeline pipeline = new StandardPipeline();

    private SceneScanner scanner;
    private ConcurrentHashMap<String, Invocation> invocations = new ConcurrentHashMap<String, Invocation>();

    private ThreadPoolExecutor businessExecutor;
    private ThreadPoolExecutor ioExecutor;

    private EngineConfig engineConfig;

    public StandardEngine(EngineConfig engineConfig) {
        this.engineConfig = engineConfig;
    }

    /*
     * @see liruonian.orion.Engine#getService()
     */
    @Override
    public Service getService() {
        return service;
    }

    /*
     * @see liruonian.orion.Engine#setService(liruonian.orion.Service)
     */
    @Override
    public void setService(Service service) {
        this.service = service;
    }

    /*
     * @see liruonian.orion.Engine#getEventBus()
     */
    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    /*
     * @see liruonian.orion.Engine#setEventBus(liruonian.orion.EventBus)
     */
    @Override
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /*
     * @see liruonian.orion.Pipeline#getBasic()
     */
    @Override
    public Valve getBasic() {
        return pipeline.getBasic();
    }

    /*
     * @see liruonian.orion.Pipeline#setBasic(liruonian.orion.Valve)
     */
    @Override
    public void setBasic(Valve basic) {
        pipeline.setBasic(basic);
    }

    /*
     * @see liruonian.orion.Pipeline#addValve(liruonian.orion.Valve)
     */
    @Override
    public void addValve(Valve valve) {
        pipeline.addValve(valve);
    }

    /*
     * @see liruonian.orion.Pipeline#getValves()
     */
    @Override
    public Valve[] getValves() {
        return pipeline.getValves();
    }

    /*
     * @see liruonian.orion.Pipeline#removeValve(liruonian.orion.Valve)
     */
    @Override
    public void removeValve(Valve valve) {
        pipeline.removeValve(valve);
    }

    /*
     * @see liruonian.orion.Pipeline#invoke(liruonian.orion.RequestFaced,
     * liruonian.orion.ResponseFaced, liruonian.orion.Invocation)
     */
    @Override
    public void invoke(RequestFaced request, ResponseFaced response,
            Invocation invocation) {
        pipeline.invoke(request, response, invocation);
    }

    /*
     * @see liruonian.orion.EventListener#eventReceived(liruonian.orion.Event)
     */
    @Override
    public void eventReceived(Event event) {
        if (event instanceof RequestEvent) {
            handleReuqestEvent((RequestEvent) event);
        } else if (event instanceof ResponseEvent) {
            handleResponseEvent((ResponseEvent) event);
        }
    }

    /**
     * 处理RequestEvent
     *
     * @param event
     */
    private void handleReuqestEvent(RequestEvent event) {
        RpcRequest rpcRequest = event.getRpcRequest();

        // 当前event需要触发的服务
        String serviceName = rpcRequest.getRequestBody().getServiceName();

        // 若当前引擎未注册该服务，则返回异常信息
        if (!invocations.containsKey(serviceName)) {
            postServiceNotFoundEvent(event);
            return;
        }

        // 异步执行服务
        businessExecutor.execute(new Runnable() {

            @Override
            public void run() {
                Invocation invocation = invocations.get(serviceName);
                RequestFaced request = new RequestFaced(rpcRequest);
                ResponseFaced response = new ResponseFaced(event.getChannel());

                StandardEngine.this.invoke(request, response, invocation);
            }
        });
    }

    /**
     * 发布服务为注册的事件
     *
     * @param requestEvent
     */
    private void postServiceNotFoundEvent(RequestEvent requestEvent) {
        Channel channel = requestEvent.getChannel();
        MessageFactory messageFactory = channel.attr(Connection.PROTOCOL).get()
                .getMessageFactory();
        RequestFaced request = new RequestFaced(requestEvent.getRpcRequest());

        String errorMsg = sm.getString(
                "standardEngine.postServiceNotFoundEvent.serviceNotFound",
                request.getServiceName());
        ResponseEvent exceptionEvent = new ResponseEvent(
                messageFactory.createExceptionResponse(errorMsg, request),
                requestEvent.getChannel());

        logger.warn(errorMsg);

        try {
            eventBus.postEvent(exceptionEvent);
        } catch (EventDiscardException e) {
            // 此处仅打印日志，不再重新尝试提交事件，由客户端等待超时即可？
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 处理ResponseEvent
     *
     * @param event
     */
    private void handleResponseEvent(ResponseEvent event) {

        // 异步写回响应信息即可
        ioExecutor.execute(new Runnable() {

            @Override
            public void run() {
                event.getChannel().writeAndFlush(event.getRpcResponse())
                        .addListener(new ChannelFutureListener() {

                            @Override
                            public void operationComplete(ChannelFuture future)
                                    throws Exception {
                                if (!future.isSuccess()) {
                                    logger.error(sm.getString(
                                            "standardEngine.handleResponseEvent.sendError"),
                                            future.cause());
                                }
                            }
                        });
            }
        });

    }

    /*
     * @see liruonian.orion.LifecycleSupport#initializeInternal()
     */
    protected void initializeInternal() throws LifecycleException {
        // 在事件总线中注册engine感兴趣的事件
        eventBus.registerEventListener(RequestEvent.class, this);
        eventBus.registerEventListener(ResponseEvent.class, this);

        // 标准业务处理管道，后续应该开放出来配置
        pipeline.addValve(new LogValve(eventBus));
        pipeline.setBasic(new InvocationValve(eventBus));

        setName(StrUtil.blankToDefault(engineConfig.getName(), "default-engine"));
    }

    /*
     * @see liruonian.orion.LifecycleSupport#startInternal()
     */
    @Override
    protected void startInternal() throws LifecycleException {

        businessExecutor = new ThreadPoolExecutor(
                // 核心线程池大小默认为availableProcessors + 1
                Configs.engineCoreThreadPoolSize() == -1
                        ? Runtime.getRuntime().availableProcessors() + 1
                        : Configs.engineCoreThreadPoolSize(),
                // 最大线程池大小默认为16
                Runtime.getRuntime().availableProcessors() * 2, 100,
                TimeUnit.MICROSECONDS,
                // workQueue大小默认与事件队列大小一致
                new ArrayBlockingQueue<Runnable>(Configs.eventQueueSize()),
                new NamedThreadFactory(getName() + "-biz", false));

        ioExecutor = new ThreadPoolExecutor(
                // 核心线程池大小默认为1
                1,
                // 最大线程池大小默认为availableProcessors
                Runtime.getRuntime().availableProcessors(), 100, TimeUnit.MICROSECONDS,
                // workQueue大小默认与事件队列大小一致
                new ArrayBlockingQueue<Runnable>(Configs.eventQueueSize()),
                new NamedThreadFactory(getName() + "-io", false));

        // 初始化场景扫描器并加载所有invocations
        scanner = new SceneScanner();
        try {
            SceneDefinition[] scenes = scanner.loadScenes();
            buildInvocations(scenes);
        } catch (ScannotationException e) {
            throw new LifecycleException(e);
        }
    }

    /*
     * @see liruonian.orion.LifecycleSupport#stopInternal()
     */
    @Override
    protected void stopInternal() throws LifecycleException {
        eventBus.unregisterEventListener(RequestEvent.class, this);
        eventBus.unregisterEventListener(ResponseEvent.class, this);

        businessExecutor.shutdown();
        ioExecutor.shutdown();
    }

    /**
     * 根据扫描出的场景定义，生成invocations
     *
     * @param scenes
     */
    private void buildInvocations(SceneDefinition[] scenes) {
        for (int i = 0; i < scenes.length; i++) {
            SceneDefinition scene = scenes[i];
            Class<?> sceneClass = scene.getSceneClass();

            ApiDefinition[] apis = scene.getApis();
            for (int j = 0; j < apis.length; j++) {
                ApiDefinition api = apis[j];

                // 不允许存在同名的service，如果存在同名service，则以第一个加载的为准
                String service = scene.getName() + DOT + api.getName();
                if (!invocations.containsKey(service)) {
                    Invocation invocation = new Invocation();
                    invocation.setSceneClass(sceneClass);
                    invocation.setSingleton(scene.isSingleton());
                    invocation.setApiMethod(api.getApiMethod());
                    invocation.setAsync(api.isAsync());

                    invocations.put(service, invocation);
                    if (logger.isInfoEnabled()) {
                        printHierarchicalInfo(sm.getString(
                                "standardEngine.buildInvocations.log", service));
                    }

                } else {
                    if (logger.isWarnEnabled()) {
                        logger.warn(sm.getString(
                                "standardEngine.buildInvocations.duplicate", service));
                    }
                    continue;
                }
            }
        }
    }

}
