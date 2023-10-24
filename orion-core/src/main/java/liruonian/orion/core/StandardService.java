package liruonian.orion.core;

import cn.hutool.core.util.StrUtil;
import liruonian.orion.Connector;
import liruonian.orion.ConnectorFactory;
import liruonian.orion.Engine;
import liruonian.orion.EventBus;
import liruonian.orion.Server;
import liruonian.orion.Service;
import liruonian.orion.config.ConnectorConfig;
import liruonian.orion.config.ServiceConfig;
import liruonian.orion.core.bus.EventBusFactory;
import liruonian.orion.core.engine.StandardEngine;
import liruonian.orion.lifecycle.LifecycleException;
import liruonian.orion.lifecycle.LifecycleSupport;

/**
 * Service的标准实现
 *
 * @author lihao
 * @date 2020年8月6日
 * @version 1.0
 */
public class StandardService extends LifecycleSupport implements Service {

    /**
     * 所属的{@link Server}
     */
    private Server server;

    /**
     * 消息总线
     */
    private EventBus eventBus;

    /**
     * Engine的集合
     */
    private Engine engine;

    /**
     * Connector的集合
     */
    private Connector[] connectors = new Connector[0];

    /**
     * Service的配置项
     */
    private ServiceConfig serviceConfig;

    public StandardService(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    /*
     * @see liruonian.orion.Service#getServer()
     */
    @Override
    public Server getServer() {
        return server;
    }

    /*
     * @see liruonian.orion.Service#setServer(liruonian.orion.Server)
     */
    @Override
    public void setServer(Server server) {
        this.server = server;
    }

    /*
     * @see liruonian.orion.Service#getEventBus()
     */
    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    /*
     * @see liruonian.orion.Service#setEventBus(liruonian.orion.EventBus)
     */
    @Override
    public synchronized void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
        synchronizeStatus(eventBus);
    }

    /*
     * @see liruonian.orion.Service#getEngine()
     */
    @Override
    public Engine getEngine() {
        return engine;
    }

    /*
     * @see liruonian.orion.Service#setEngine(liruonian.orion.Engine)
     */
    @Override
    public synchronized void setEngine(Engine engine) {
        this.engine = engine;
        synchronizeStatus(engine);
    }

    /*
     * @see liruonian.orion.Service#addConnector(liruonian.orion.Connector)
     */
    @Override
    public void addConnector(Connector connector) {
        connector.setService(this);

        synchronized (connectors) {
            Connector[] results = new Connector[connectors.length + 1];
            System.arraycopy(connectors, 0, results, 0, connectors.length);
            results[connectors.length] = connector;
            connectors = results;

            synchronizeStatus(connector);
        }
    }

    /*
     * @see liruonian.orion.Service#getConnectors()
     */
    @Override
    public Connector[] getConnectors() {
        return connectors;
    }

    /*
     * @see liruonian.orion.Service#removeConnector(liruonian.orion.Connector)
     */
    @Override
    public void removeConnector(Connector connector) {
        synchronized (connectors) {
            int n = -1;
            for (int i = 0; i < connectors.length; i++) {
                if (connector == connectors[i]) {
                    n = i;
                    break;
                }
            }

            if (n < 0) {
                // Not found
                return;
            }

            try {
                connector.stop();
            } catch (LifecycleException e) {
                e.printStackTrace(System.err);
            }

            Connector[] results = new Connector[connectors.length - 1];
            for (int i = 0, j = 0; i < connectors.length; i++) {
                if (i != n) {
                    results[j++] = connectors[i];
                }
            }
            connectors = results;

        }
    }

    /*
     * @see liruonian.orion.LifecycleSupport#initializeInternal()
     */
    @Override
    protected void initializeInternal() throws LifecycleException {
        setName(StrUtil.blankToDefault(serviceConfig.getName(), "default-service"));

        setEventBus(EventBusFactory.createEventBus(serviceConfig.getEventBus()));

        StandardEngine engine = new StandardEngine(serviceConfig.getEngine());
        engine.setEventBus(eventBus);
        setEngine(engine);

        for (ConnectorConfig connectorConfig: serviceConfig.getConnectors()) {
            Connector connector = ConnectorFactory.createConnector(connectorConfig);
            connector.setEventBus(eventBus);
            connector.setService(this);
            addConnector(connector);
        }
    }

    /*
     * @see liruonian.orion.LifecycleSupport#startInternal()
     */
    @Override
    protected void startInternal() throws LifecycleException {
        eventBus.start();
        engine.start();

        for (int i = 0; i < connectors.length; i++) {
            connectors[i].start();
        }
    }

    /*
     * @see liruonian.orion.LifecycleSupport#stopInternal()
     */
    @Override
    protected void stopInternal() throws LifecycleException {
        for (int i = 0; i < connectors.length; i++) {
            connectors[i].stop();
        }

        engine.stop();
        eventBus.stop();
    }

}
