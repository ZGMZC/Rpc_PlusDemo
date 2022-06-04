package common;

import java.util.Arrays;

public class RpcInvocation {
    //请求的目标方法，相当于方法名
    private String targetMethod;
    //请求的目标服务名称，相当于类名
    private String targetServiceName;
    //请求参数信息
    private Object[] args;
    //用于匹配请求和响应的一个关键值。当请求从客户端发出的时候，会有一个uuid用于记录发出的请求，待数据返回的时候通过uuid来匹配对应的请求线程，并且返回给调用线程
    private String uuid;
    //接口响应的数据塞入这个字段中（如果是异步调用或者void类型，这里就为空）
    private Object response;

    public String getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public String getTargetServiceName() {
        return targetServiceName;
    }

    public void setTargetServiceName(String targetServiceName) {
        this.targetServiceName = targetServiceName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }
    @Override
    public String toString() {
        return "RpcInvocation{" +
                "targetMethod='" + targetMethod + '\'' +
                ", targetServiceName='" + targetServiceName + '\'' +
                ", args=" + Arrays.toString(args) +
                ", uuid='" + uuid + '\'' +
                ", response=" + response +
                '}';
    }
}
