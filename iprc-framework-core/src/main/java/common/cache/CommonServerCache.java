package common.cache;

import com.sun.xml.internal.ws.api.ha.HaInfo;
import registy.URL;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommonServerCache {
    //存放类方法的容器
    public static final Map<String,Object> PROVIDER_CLASS_MAP=new HashMap<>();
    //存放url的容器
    public static final Set<URL> PROVIDER_URL_SET = new HashSet<>();
}
