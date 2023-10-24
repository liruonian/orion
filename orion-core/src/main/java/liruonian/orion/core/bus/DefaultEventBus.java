package liruonian.orion.core.bus;

import java.util.HashMap;

import liruonian.orion.lifecycle.LifecycleException;
import liruonian.orion.lifecycle.LifecycleSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.util.StrUtil;
import liruonian.orion.Constants;
import liruonian.orion.EventBus;
import liruonian.orion.commons.StringManager;
import liruonian.orion.config.EventBusConfig;

/**
 * 事件总线的骨架实现，支持事件发布订阅功能。
 *
 * @author lihao
 * @date 2020年8月27日
 * @version 1.0
 */
public class DefaultEventBus extends LifecycleSupport implements EventBus {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEventBus.class);
    private static final StringManager sm = StringManager.getManager(Constants.PACKAGE);

    private HashMap<Class<? extends Event>, EventListener[]> listenersMap = new HashMap<Class<? extends Event>, EventListener[]>();

    private EventBusConfig eventBusConfig;

    public DefaultEventBus() {

    }

    public DefaultEventBus(EventBusConfig eventBusConfig) {
        this.eventBusConfig = eventBusConfig;
    }

    /*
     * @see
     * liruonian.orion.EventBus#registerEventListener(liruonian.orion.EventType,
     * liruonian.orion.EventListener)
     */
    @Override
    public void registerEventListener(Class<? extends Event> interestedEvent,
            EventListener listener) {
        if (!listenersMap.containsKey(interestedEvent)) {
            listenersMap.put(interestedEvent, new EventListener[] { listener });
            return;
        }

        EventListener[] oldListeners = listenersMap.get(interestedEvent);
        EventListener[] newListeners = new EventListener[oldListeners.length + 1];
        System.arraycopy(oldListeners, 0, newListeners, 0, oldListeners.length);
        newListeners[oldListeners.length] = listener;
        listenersMap.put(interestedEvent, newListeners);
    }

    /*
     * @see
     * liruonian.orion.EventBus#unregisterEventListener(liruonian.orion.EventType,
     * liruonian.orion.EventListener)
     */
    @Override
    public void unregisterEventListener(Class<? extends Event> interestedEvent,
            EventListener listener) {
        if (!listenersMap.containsKey(interestedEvent)) {
            return;
        }

        EventListener[] oldListeners = listenersMap.get(interestedEvent);
        int k = -1;
        for (int i = 0; i < oldListeners.length; i++) {
            if (oldListeners[i] == listener) {
                k = i;
                break;
            }
        }
        if (k < 0) {
            return;
        }
        if (k >= 0 && oldListeners.length == 1) {
            listenersMap.remove(interestedEvent);
            return;
        }

        EventListener[] newListeners = new EventListener[oldListeners.length - 1];
        for (int i = 0, j = 0; i < oldListeners.length; i++) {
            if (i != k) {
                newListeners[j++] = oldListeners[i];
            }
        }
        listenersMap.put(interestedEvent, newListeners);
    }

    /**
     * 事件提交，同步进行事件投递。
     */
    public void postEvent(Event event) throws EventDiscardException {
        checkEventType(event);
        triggerEventPost(event);
    }

    /**
     * 校验是否为已订阅的事件类型，如果未注册该时间类型的监听器，则直接抛出异常。
     *
     * @param event
     * @throws EventDiscardException
     */
    protected void checkEventType(Event event) throws EventDiscardException {
        if (event == null || !listenersMap.containsKey(event.getClass())) {
            String errorMsg = sm.getString("defaultEventBus.postEvent.notInterest",
                    event.getClass().getName());
            logger.warn(errorMsg);

            throw new EventDiscardException(errorMsg);
        }
    }

    /**
     * 触发实际的事件投递动作
     *
     * @param event
     */
    protected void triggerEventPost(Event event) {
        // 触发监听器
        EventListener[] listeners = listenersMap.get(event.getClass());
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].eventReceived(event);
        }
    }

    /*
     * @see liruonian.orion.LifecycleSupport#initializeInternal()
     */
    @Override
    protected void initializeInternal() throws LifecycleException {
        setName(StrUtil.blankToDefault(eventBusConfig.getName(), "default-event-bus"));
    }

    /*
     * @see liruonian.orion.LifecycleSupport#startInternal()
     */
    @Override
    protected void startInternal() throws LifecycleException {

    }

    /*
     * @see liruonian.orion.LifecycleSupport#stopInternal()
     */
    @Override
    protected void stopInternal() throws LifecycleException {

    }

}
