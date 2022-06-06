### Rpc_PlusDemo

------


从0到1动手实现RPC框架

> 开发环境
- IDEA
- JDK1.8
- Maven 3.5+
#### 本分支所实现的内容

- 基于Zookeeper作为注册中心进行了统一的访问接口封装与实现，并且能够支持日后其它注册中心的拓展。
- 当服务提供方发生变更的时候，借助注册中心通知到客户端做本地调用表的一个更新操作。
- 当服务订阅的时候需要告知注册中心修改节点数据，方便日后针对调用者做一些数据统计与监控的功能。
- 统一将节点的更新后的相关操作通过事件的机制来实现代码解耦。
- 将项目中常用的一些缓存数据按照服务端和客户端两类角色进行分开管理。
- 将对于Netty连接的管理操作统一封装在ConnectionHandler类中，以及将之前硬编码的配置信息都迁移到了properties配置文件中，并设计了PropertiesBootst rap类进行管理。

#### 注册中心的接入与实现
完成代理层的开发后，有几个问题需要我们去思考：
- 如果同一个服务有10台不同的机器进行提供，那么客户端该从哪获取这10台目标机器的ip地址信息呢？
- 随着调用方的增加，如何对服务调用者的数据进行监控呢？
- 服务提供者下线了，该如何通知到服务调用方？

> 为了解决以上的问题，如果单纯靠代理方来进行技术实现的话，那么代理方需要考虑如何通知到所有服务提供者，以及如何让服务提供者在下线之前主动通知到自己。
> 这个时候单纯依靠代理层来进行技术实现已经不太合适，需要通过一个第三者来做这件通知的工作。

> 在框架中引入了叫注册中心层的概念
> - 能够存储数据，并且具备高可用功能。
> - 能够和各个调用方保持连接，当有服务上线、下线的时候需要通知到各个端。
    > 这里选择了Zookeeper作为注册中心
> - Zookeeper和客户端之间能够构成主动推送，能够实现服务上线和下线的通知效果。
> - Zookeeper自身提供了高可用的机制，并且对于数据节点的存储可以支持顺序、非顺序、临时、持久化的特性。
> - Zookeeper自身也是一款非常成熟的中间件，业界有很多关于它的解决方案，开源社区也比较活跃。
- 注册节点的结构设计
  和Duboo的注册结构基本相同，先定义了一个iprc的根节点，接着是不同的服务名称（类名）作为二级节点，在二级节点下划分了provider和consumer节点，provider下存放的数据已ip+端口的格式存储，consumer下边存放具体的服务调用名与地址。

#### 注册层的设计
- RegistryService接口
    - 方法：
        - register：服务注册
        - unRegistry：服务下线
        - subscribe：订阅
        - doUnSubscribe：取消订阅
  > 这四个动作可以看作是远程服务信息的四个核心原操作。

- URL 配置类
  封装IRPC的主要配置，是整个框架的核心部分。
    - 属性：application 服务应用名称（方法名），serviceName服务类（类名），parameters参数列表（地址、端口等，可以扩展）
    - 方法：
        - 各种的get/set方法
        - addParameter：添加参数
        - buildProviderUrlStr（URL）：将URL转换为写入zk的provider节点下的一段字符串
        - buildConsumerUrlStr（URL）：将URL转换为写入zk的consumer节点下的一段字符串
        - buildURLFromUrlStr（String）：将某个节点下的信息转换为一个Provider对象（ProviderNodeInfo）

          > ProviderNodeInfo：Provider节点的信息，包括serviceName、address
- AbstractRegister抽象类，实现了Registry接口
  这个抽象类对注册数据进行了统一处理，假设日后需要考虑支持多种类型的注册中心，例如Redis、etcd之类的话，所有基础的记录操作都可以统一放在抽象类里实现。
    - 方法：
        - register：将URL添加到PROVIDER_URL_SET
        - unRegistry：将URL从PROVIDER_URL_SET删除
        - subscribe：将URL添加到SUBSCRIBE_SERVICE_LIST
        - doUnSubscribe：将URL从SUBSCRIBE_SERVICE_LIST删除
        - doAfterSubscribe(URL)： 子类扩展类
        - doBeforeSubscribe（URL）：子类扩展类
        - getProviderIps（String）：子类扩展类


------
在设计框架的时候，需要用一些Map、List或者Set结合存储一些通用数据，我将其定义为Cache模块，并按照ServerCache和ClientCache区分

- CommonServerCache
    - 属性：
        - PROVIDER_CLASS_MAP：存放类方法的容器，Map<String,Object>
        - PROVIDER_URL_SET：存放服务提供者的URL的容器，Set  < URL >
- CommonClientServer
    - 属性：
        -  SEND_QUEUE：发送消息的队列，BlockingQueue< RpcInvocation >
        -  RESP_MAP：存放UUid和服务的容器，Map<String,Object>
        -  SUBSCRIBE_SERVICE_LIST：存放订阅服务的集合，List< String>
        -  SERVER_ADDRESS：服务方地址，存放ip，Set< String>
        -  CONNECT_MAP：每次进行远程调用的时候都是从这里面去选择服务提供者，Map<String, List< ChannelFutureWrapper>>

           > ChannelFutureWrapper：包装了ChannelFuture、host、port

------


- AbstractZookeeperClient抽象类
  这个抽象对Zookeeper的基本操作进行了统一封装
    - 属性：
        - zkAddress：zkClient的地址
        - baseSleepTimes：基本睡眠时间
        - maxRetryTimes：最大重传时间
    - 方法：
        - AbstractZookeeperClient(String zkAddress)：构造方法
        - AbstractZookeeperClient(String zkAddress, Integer baseSleepTimes, Integer maxRetryTimes) 构造方法
        - get和set方法
        - updateNodeData(String address, String data)：更新节点数据信息
        - getClient()：得到客户端信息
        - getNodeData(String path)：拉取节点信息
        - getChildrenData(String path)：获取指定目录下的子节点数据
        - createPersistentData(String address, String data)：创建持久化类型节点数据信息
        - createPersistentWithSeqData(String address, String data)：创建有序且持久化类型节点数据信息
        - createTemporarySeqData(String address, String data)：创建有序且临时类型节点数据信息
        - createTemporaryData(String address, String data)：创建临时节点数据类型信息
        - setTemporaryData(String address, String data)：设置某个节点的数值
        - destroy()：断开zk的客户端连接
        - listNode(String address)：展示节点下边的数据
        - deleteNode(String address);删除节点下的数据
        - existNode(String address)：判断是否存在节点
        - watchNodeData(String path, Watcher watcher)：监听path路径下某个节点的数据变化
        - watchChildNodeData(String path, Watcher watcher)：监听子节点下的数据变化
- CuratorZookeeperClient实现了AbstractZookeeperClient抽象类
  > CuratorFramework：Netflix公司开发一款连接zookeeper服务的框架，使用需要导入依赖

    - 	- 属性：
           - CuratorFramework client：客户端连接
    - 方法：
        - CuratorZookeeperClient(String zkAddress)： 构造方法
        - CuratorZookeeperClient(String zkAddress,Integer baseSleepTimes,Integer maxRetryTimes)：构造方法

          > RetryPolicy retryPolicy:主要是满足请求出错后的各种重试策略
        - 实现AbstractZookeeperClient抽象类中的抽象方法
- ZookeeperRegister，继承了AbstractRegister抽象类，实现了RegistryService接口
    - 属性：
        - AbstractZookeeperClient zkClient：负责与Zookeeper进行连接管理
        - String ROOT：节点根目录
    - 方法：
        - getProviderPath(URL url)：得到Provider的节点目录
        - getConsumerPath(URL url)：得到Consumer的节点目录
        - ZookeeperRegister(String address)：构造方法
        - 重写其父类及接口方法
        - watchChildNodeData(String newServerNodePath)：监听子节点的信息

          >  process方法是Watcher接口中的一个回调方法，当ZooKeeper向客户端发送一个Watcher事件通知时，客户端就会对相应的process方法进行回调，从而实现对事件的处理


------
在监听操作中，引入了事件的设计思路，主要目的是为了解耦。
当监听到某个节点的数据发生更新后，会发送一个节点更新的事件，然后在事件的监听端对不同的行为做不同的事件处理操作。

- IRpcEvent接口
  该事件会用于装载需要传递的数据信息

    - 方法：
        - getData()：得到数据
        - setData(Object data)：设置数据
- IRpcUpdateEvent方法，实现IRpcEvent接口
    - 属性：
        - Object data：数据
    - 方法：
        - IRpcUpdateEvent(Object data)：构造方法
        - 重写接口方法
- IRpcListener< T>接口
  当Zookeeper的某个节点发生数据变动的时候，就会发送一个变更事件，然后由对应的监听器去捕获这些数据并做处理。
    - 方法：
        - callBack()：回调方法
- IRpcListenerLoader类
  监听并发送事件
    - 属性：
        - iRpcListenerList：监听器的集合，new ArrayList<>()
        - eventThreadPool：线程池，Executors.newFixedThreadPool(2)；
    - 方法：
        - registerListener(IRpcListener iRpcListener)：注册监听器，将监听器加入到监听器集合中
        - init()：初始化，调用registerListener，加入new ServiceUpdateListener()---服务更新的监听器
        - getInterfaceT(Object o)：获取接口上的泛型
        - sendEvent(IRpcEvent iRpcEvent)：发送事件
-  ServiceUpdateListener类，实现了IRpcListener< IRpcUpdateEvent>接口
   用于服务更新的监听
    - 属性：
        - Logger LOGGER：log4j，日志相关
    - 方法：
        - 重写callback()方法：通过URLChangeWrapper在CONNECT_MAP中获得对应的服务提供者，并更新其URL

          > URLChangeWrapper：URL更新时的包装类，包括serviceName、providerUrl

------

- ConnectionHandler
  封装了连接的建立、断开、按照服务名筛选等功能，按照单一职责的设计原则，将所有与连接相关的功能都统一封装。
    - 属性：
        - Bootstrap bootstrap：核心的连接处理器，专门用于负责和服务端构建连接通信
    - 方法：
        - setBootstrap：设置bootstrap
        - connect(String providerServiceName, String providerIp)：构建单个连接通道 元操作，既要处理连接，还要统一将连接进行内存存储管理
        - createChannelFuture(String ip,Integer port)：构建ChannelFuture
        - disConnect(String providerServiceName, String providerIp)：断开连接
        - getChannelFuture(String providerServiceName)：认走随机策略获取ChannelFuture

- Server类
  服务端启动类，有了新的扩展
    - PropertiesBootstrap类：负责将properties的配置转换成本地的一个Map结构进行管理。
    - batchExportUrl()和exportService(Object serviceBean)：将服务端的具体服务都暴露到注册中心，方便客户端进行调用。
- Client类
  客户端启动类，有了新的扩展
    - doConnectServer()：增加了订阅服务的功能
    - doConnectServer()：与服务提供者进行连接