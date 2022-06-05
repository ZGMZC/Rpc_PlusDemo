package registy;

import registy.zookeeper.ProviderNodeInfo;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class URL {
    /**
     * 服务应用名称，相当于方法名
     */
    private String applicationName;
    /**
     * 注册节点到服务名称，例如：com.test.UserService 相当于类名
     */
    private String serviceName;
    /**
     * 从这里可以自定义无限进行扩展
     * 分组
     * 权重
     * 服务提供者的地址
     * 服务提供者的端口
     */
    private Map<String,String> parameters=new HashMap<>();

    public void addParameter(String key,String value){
        this.parameters.putIfAbsent(key,value);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * 将URL转换为写入zk的provider节点下的一段字符串
     * @param url
     * @return
     */
    public static String buildProviderUrlStr(URL url){
        String host=url.getParameters().get("host");
        String port=url.getParameters().get("port");
        return new String((url.getApplicationName()+";"+url.getServiceName() + ";" + host + ":" + port + ";" + System.currentTimeMillis()).getBytes(), StandardCharsets.UTF_8);

    }

    /**
     * 将URL转换为写入zk的consumer节点下的一段字符串
     * @param url
     * @return
     */
    public static String buildConsumerUrlStr(URL url){
        String host=url.getParameters().get("host");
        return new String((url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ";" + System.currentTimeMillis()).getBytes(), StandardCharsets.UTF_8);
    }

    /**
     * 将某个节点下的信息转换为一个Provider节点对象
     * @param providerNodeStr
     * @return
     */
    public static ProviderNodeInfo buildURLFromerUrlStr(String providerNodeStr){
        String[] items=providerNodeStr.split("/");
        ProviderNodeInfo providerNodeInfo=new ProviderNodeInfo();
        providerNodeInfo.setServiceName(items[2]);
        providerNodeInfo.setAddress(items[4]);
        return providerNodeInfo;
    }

}
