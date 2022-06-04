package interfaces;

import java.util.List;

//测试的接口
public interface DataService {
    /**
     * 发送数据
     *
     * @param body
     */
    String sendData(String body);
    int sum(int a,int b);
    /**
     * 获取数据
     *
     * @return
     */
    List<String> getList();
}
