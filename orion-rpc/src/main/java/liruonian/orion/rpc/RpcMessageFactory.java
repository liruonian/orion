package liruonian.orion.rpc;

import java.net.InetSocketAddress;

import liruonian.orion.commons.IdSrouce;
import liruonian.orion.core.ServerException;
import liruonian.orion.core.engine.RequestFaced;
import liruonian.orion.remoting.Protocol;
import liruonian.orion.remoting.Status;
import liruonian.orion.remoting.heartbeat.HeartbeatRequest;
import liruonian.orion.remoting.heartbeat.HeartbeatResponse;
import liruonian.orion.remoting.message.MessageFactory;
import liruonian.orion.remoting.message.RequestBody;
import liruonian.orion.remoting.message.RpcRequest;
import liruonian.orion.remoting.message.RpcResponse;

/**
 * Rpc消息工厂类
 *
 *
 * @author lihao
 * @date 2020年8月19日
 * @version 1.0
 */
public class RpcMessageFactory implements MessageFactory {

    private Protocol protocol;

    public RpcMessageFactory(Protocol protocol) {
        this.protocol = protocol;
    }

    /*
     * @see liruonian.orion.remoting.MessageFactory#createRpcRequest()
     */
    @Override
    public RpcRequest createRpcRequest() {
        return createRpcRequest(null);
    }

    /*
     * @see
     * liruonian.orion.remoting.MessageFactory#createRpcRequest(liruonian.orion.
     * message.RequestBody)
     */
    @Override
    public RpcRequest createRpcRequest(RequestBody requestBody) {
        RpcRequest rpcRequest = new RpcRequest(protocol.getProtocolCode(),
                protocol.getProtocolVersion());

        rpcRequest.setReqOrRsp(true);
        rpcRequest.setHeartbeat(false);

        rpcRequest.setId(IdSrouce.nextId());

        rpcRequest.setRequestBody(requestBody);

        return rpcRequest;
    }

    /*
     * @see liruonian.orion.remoting.MessageFactory#createRpcResponse(java.lang.
     * Object, liruonian.orion.RequestFaced)
     */
    @Override
    public RpcResponse createRpcResponse(Object responseBody, RequestFaced request) {

        byte protocolCode = request.getProtocolCode();
        byte protocolVersion = request.getProtocolVersion();

        // 检验传入的请求消息协议码和版本是否与当前工厂类处理的协议匹配
        if (protocolCode != protocol.getProtocolCode()
                || protocolVersion != request.getProtocolVersion()) {
            throw new IllegalArgumentException();
        }

        RpcResponse rpcResponse = new RpcResponse(protocolCode, protocolVersion);

        rpcResponse.setReqOrRsp(false);
        rpcResponse.setOneway(false);
        rpcResponse.setHeartbeat(false);
        rpcResponse.setCrcRequired(request.isCrcRequired());
        rpcResponse.setSerializationType(request.getSerializationType());

        // 响应消息id必须与请求消息一致
        rpcResponse.setId(request.getId());

        // 无论requestObject是否为null，响应状态都为success
        rpcResponse.setStatus(Status.SUCCESS);
        rpcResponse.setResponseBody(responseBody);

        return rpcResponse;
    }

    /*
     * @see
     * liruonian.orion.remoting.MessageFactory#createExceptionResponse(java.lang.
     * String, liruonian.orion.RequestFaced)
     */
    @Override
    public RpcResponse createExceptionResponse(String errorMsg, RequestFaced request) {
        return createExceptionResponse(errorMsg, null, request);
    }

    /*
     * @see
     * liruonian.orion.remoting.MessageFactory#createExceptionResponse(java.lang.
     * String, liruonian.orion.remoting.Status, liruonian.orion.RequestFaced)
     */
    @Override
    public RpcResponse createExceptionResponse(String errorMsg, Status status,
            RequestFaced request) {
        return createExceptionResponse(null, errorMsg, null, request);
    }

    /*
     * @see
     * liruonian.orion.remoting.MessageFactory#createExceptionResponse(java.lang.
     * Throwable, java.lang.String, liruonian.orion.remoting.Status,
     * liruonian.orion.RequestFaced)
     */
    @Override
    public RpcResponse createExceptionResponse(Throwable t, String errorMsg,
            Status status, RequestFaced request) {
        RpcResponse rpcResponse = new RpcResponse(protocol.getProtocolCode(),
                protocol.getProtocolVersion());

        rpcResponse.setReqOrRsp(false);
        rpcResponse.setOneway(false);
        rpcResponse.setHeartbeat(false);
        rpcResponse.setSerializationType(request.getSerializationType());

        rpcResponse.setId(request.getId());

        // 设置状态为服务端异常
        rpcResponse.setStatus(status == null ? Status.SERVER_EXCEPTION : status);
        if (t instanceof ServerException) {
            rpcResponse.setResponseBody(t);
        } else {
            rpcResponse.setResponseBody(createServerException(t, errorMsg));
        }

        return rpcResponse;
    }

    /*
     * @see
     * liruonian.orion.remoting.MessageFactory#createTimeoutResponse(java.net.
     * InetSocketAddress)
     */
    @Override
    public RpcResponse createTimeoutResponse(InetSocketAddress address) {
        RpcResponse rpcResponse = new RpcResponse(protocol.getProtocolCode(),
                protocol.getProtocolVersion());

        rpcResponse.setReqOrRsp(false);
        rpcResponse.setOneway(false);
        rpcResponse.setHeartbeat(false);

        // 设置状态为超时
        rpcResponse.setStatus(Status.TIMEOUT);

        return rpcResponse;
    }

    /*
     * @see liruonian.orion.remotingFactory#createSendFailedResponse(java.net.
     * InetSocketAddress, java.lang.Throwable)
     */
    @Override
    public RpcResponse createSendFailedResponse(InetSocketAddress address,
            Throwable throwable) {
        RpcResponse rpcResponse = new RpcResponse(protocol.getProtocolCode(),
                protocol.getProtocolVersion());

        rpcResponse.setReqOrRsp(false);
        rpcResponse.setOneway(false);
        rpcResponse.setHeartbeat(false);

        // 设置状态为发送失败
        rpcResponse.setStatus(Status.CLIENT_EXCEPTION);
        rpcResponse.setResponseBody(throwable);

        return rpcResponse;
    }

    /*
     * FIXME
     * 
     * @see
     * liruonian.orion.remoting.MessageFactory#createConnectionClosedResponse(
     * java .net.InetSocketAddress, java.lang.String)
     */
    @Override
    public RpcResponse createConnectionClosedResponse(InetSocketAddress address,
            String errorMsg) {
        RpcResponse rpcResponse = new RpcResponse(protocol.getProtocolCode(),
                protocol.getProtocolVersion());

        rpcResponse.setReqOrRsp(false);
        rpcResponse.setOneway(false);
        rpcResponse.setHeartbeat(false);

        rpcResponse.setStatus(Status.CONNECTION_CLOSED);
        rpcResponse.setResponseBody(createServerException(null, errorMsg));

        return rpcResponse;
    }

    /*
     * @see liruonian.orion.remoting.MessageFactory#createHeartbeatRequest()
     */
    @Override
    public HeartbeatRequest createHeartbeatRequest() {
        HeartbeatRequest rpcRequest = new HeartbeatRequest(protocol.getProtocolCode(),
                protocol.getProtocolVersion());

        rpcRequest.setReqOrRsp(true);
        rpcRequest.setOneway(false);
        rpcRequest.setHeartbeat(true);

        rpcRequest.setId(IdSrouce.nextId());

        return rpcRequest;
    }

    /*
     * @see
     * liruonian.orion.remoting.MessageFactory#createHeartbeatResponse(liruonian.
     * orion.message.HeartbeatRequest)
     */
    @Override
    public HeartbeatResponse createHeartbeatResponse(HeartbeatRequest request) {
        HeartbeatResponse rpcResponse = new HeartbeatResponse(protocol.getProtocolCode(),
                protocol.getProtocolVersion());

        rpcResponse.setReqOrRsp(false);
        rpcResponse.setOneway(false);
        rpcResponse.setHeartbeat(true);

        rpcResponse.setId(request.getId());

        rpcResponse.setStatus(Status.SUCCESS);

        return rpcResponse;
    }

    private ServerException createServerException(Throwable t, String errorMsg) {
        if (t == null) {
            return new ServerException(errorMsg);
        } else {
            return new ServerException(errorMsg, t);
        }
    }

}
