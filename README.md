### Rpc_PlusDemo
从0到1动手实现RPC框架

> 开发环境
- IDEA
- JDK1.8
- Maven 3.5+
#### 本分支所实现的内容

- 基于Netty搭建了一个简单的服务端和客户端通信模型。
- 通过自定义协议RpcProtocol的方式来解决网络粘包和拆包的问题。
- 封装了统一的代理接口，合理引入了JDK代理来实现网络传输的功能。
- 客户端通过队列消费的异步设计来实现消息发送，通过uuid来标示请求线程和响应线程之间的数据匹配问题。

##### 协议
- RpcProtol
  - magicNumber：魔法数，主要是在做服务通讯的时候定义的一个安全检测，确认当前请求的协议是否合法。
  - contentLength：协议传输核心数据的长度。这里将长度单独拎出来设置有个好处，当服务端的接收能力有限，可以对该字段进行赋值。当读取到的网络数据包中的contentLength字段已经超过预期值的话，就不会去读取content字段。
  - content：核心的传输数据，这里核心的传输数据主要是请求的服务名称，请求服务的方法名称，请求参数内容。为了方便后期扩展，这些核心的请求数据我都统一封装到了RpcInvocation对象当中。
- RpcInvocation
  - targetMethod：请求的目标方法，相当于方法名
  - targetServiceName：请求的目标服务名称，相当于类名
  - args：请求参数信息
  - uuid：用于匹配请求和响应的一个关键值。当请求从客户端发出的时候，会有一个uuid用于记录发出的请求，待数据返回的时候通过uuid来匹配对应的请求线程，并且返回给调用线程
  - response：接口响应的数据塞入这个字段中（如果是异步调用或者void类型，这里就为空）
- RpcEncoder:序列化
  -byteBuf.writeShort(rpcProtocol.getMagicNumber()):写入魔法数
  -byteBuf.writeInt(rpcProtocol.getContentLength()):写入协议传输核心数据的长度
  -byteBuf.writeBytes(rpcProtocol.getContent()):写入协议传输核心数据
- RpcDecoder:反序列化
- 
##### Client
调用流程：客户端首先需要通过一个代理工厂获取被调用对象的代理对象，然后通过代理对象将数据放入发送队列，最后会有一个异步线程将发送队列内部的数据一个个地发送给到服务端，并且等待服务端响应对应的数据结果。
- startClientApplication():客户端的启动
  - client的简单通信模型
  - startClient(channelFuture)：队列消费的异步设计
  - rpcReference代理工厂：根据接口类型获取代理对象
    - JDKProxyFactory：代理工厂，内部是JDKClientInvocationHandler
    - JDKClientInvocationHandler：需要调用的方法名称、服务名称，参数统统都封装好到RpcInvocation当中，然后塞入到一个队列里，并且等待服务端的数据返回
    > 流程：rpcReference调用get(class)方法，内部使用了JDKProxyFactory的getClass(class)方法，其内部又调用了JDKClientInvocationHandler(class)方法，在JDKClientInvocationHandler内部将方法接口进行了封装并加入到队列中。
##### Server
- startApplication():服务端的启动
  - server的简单通信模型
- registyService(Object serviceBean):注册中心，将注册的对象统一放在一个MAP集合中进行管理









