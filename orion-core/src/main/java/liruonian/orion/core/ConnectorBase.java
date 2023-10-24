package liruonian.orion.core;

import liruonian.orion.Connector;
import liruonian.orion.Constants;
import liruonian.orion.EventBus;
import liruonian.orion.Service;
import liruonian.orion.commons.StringManager;
import liruonian.orion.config.ConnectorConfig;
import liruonian.orion.lifecycle.LifecycleSupport;
import liruonian.orion.remoting.Protocol;

/**
 * Connector的骨架实现
 *
 * @author lihao
 * @date 2020年8月10日
 * @version 1.0
 */
public abstract class ConnectorBase extends LifecycleSupport implements Connector {

    protected static final StringManager sm = StringManager.getManager(Constants.PACKAGE);

    /**
     * Connector config
     */
    private ConnectorConfig config;

    /**
     * 关联的{@link Service}
     */
    private Service service;

    /**
     * 关联的{@link EventBus}
     */
    private EventBus eventBus;

    /**
     * 指定Connector的协议
     */
    private Protocol protocol;

    public ConnectorBase(ConnectorConfig connectorConfig) {
        this.config = connectorConfig;
    }

    /*
     * @see liruonian.orion.Connector#getIp()
     */
    @Override
    public String getIp() {
        return this.config.getIp();
    }

    /*
     * @see liruonian.orion.Connector#getPort()
     */
    @Override
    public int getPort() {
        return this.config.getPort();
    }

    /*
     * @see liruonian.orion.Connector#getService()
     */
    @Override
    public Service getService() {
        return service;
    }

    /*
     * @see liruonian.orion.Connector#setService(liruonian.orion.Service)
     */
    @Override
    public void setService(Service service) {
        this.service = service;
    }

    /*
     * @see liruonian.orion.Connector#getEventBus()
     */
    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    /*
     * @see liruonian.orion.Connector#setEventBus(liruonian.orion.EventBus)
     */
    @Override
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /*
     * @see liruonian.orion.Connector#getProtocol()
     */
    @Override
    public Protocol getProtocol() {
        return protocol;
    }

    /*
     * @see liruonian.orion.Connector#setProtocol(liruonian.orion.Protocol)
     */
    @Override
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }
    
    /**
     * 获取配置
     *
     * @return
     */
    public ConnectorConfig getConfig() {
        return config;
    }

}
