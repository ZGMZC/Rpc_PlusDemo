package server;

import interfaces.DataService;

import java.util.ArrayList;
import java.util.List;

//发送数据进行测试
public class DataServiceImpl implements DataService {
    @Override
    public String sendData(String body) {
        System.out.println("己收到的参数长度："+body.length());
        return "success";
    }

    @Override
    public int sum(int a, int b) {
        int c=a+b;
        System.out.println("根据接收数据产生的结果："+c);
        return c;
    }

    @Override
    public List<String> getList() {
        ArrayList arrayList = new ArrayList();
        arrayList.add("idea1");
        arrayList.add("idea2");
        arrayList.add("idea3");
        return arrayList;
    }
}
