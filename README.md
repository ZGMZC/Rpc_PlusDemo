### Rpc_PlusDemo
从0到1动手实现RPC框架

> 开发环境
- IDEA
- JDK1.8
- Maven 3.5+
#### 简单介绍
目前常用的远程调用方式主要分为两个派别：**Http请求调用，RPC请求调用**
> RPC调用主要基于TCP/IP协议的，而Http调用主要基于HTTP协议的，HTTP协议再传输层协议TCP之上，属于应用层协议，从性能层面来说，**RPC请求的效率会比HTTP高效一些。**
> RPC技术也是远程调用的通用技术栈，例如**Dubbo、Grpc、Thrift。**
#### 技术点
- 网络通信，NIO、BIO、AIO、Netty如何选型
- 操作系统层面理解IO
- JDK内部常见队列的底层实现细节
- RPC调用过程中的同步异步转换底层实现
- RPC框架中的代理技术
- 并发编程在中间件技术的实际应用
- ······
#### 整体设计分析
通常我们将一个完整的RPC架构分为以下几个核心组件：
- Server
- Client
- Server Stub
- Client Stub
> **Server Stub:** 服务端接收到Client发送的数据之后进行消息解包，调用本地方法。
> 
> **Client Stub:** 将客户端请求的参数、服务名称、服务地址进行打包，统一发送给server方。
##### 调用流程分析
首先本地的客户端需要通知到一个本地的存根（stub），接着本地存根需要进行一些数据格式的包装，网络请求的封装，按照一定的规则将这个数据包发送到指定的目标机器上。

服务端的存根在接收到相关的数据信息之后，需要将其按照事先约定好的规则进行解码，从而识别到数据包内部的信息，然后将对应的请求转发到本地服务对应的函数中进行处理，处理完的数据需要正常返回给调用方。

调用方存根在接收到数据方数据的时候，需要进行数据解码，最后得到这次请求的最终结果。

#### 简单的初始化Demo
- EventGroup

Netty 的调度模块称为 EventLoopGroup，默认提供了 NioEventLoopGroup、OioEventLoopGroup 等多种实现。
> EventLoopGroup直译过来叫「事件循环组」，它管理着一组EventLoop
> EventLoopGroup实现了Iterable接口，可以通过迭代器遍历它管理的EventLoop
> EventLoopGroup还继承了ScheduledExecutorService接口，代表它不仅可以执行异步任务，还可以执行定时任务。不过EventLoopGroup本身不干活，当你向EventLoopGroup提交一个任务时，它会轮询出一个EventLoop，转交给它执行。
> EventLoopGroup可以看做是一个多线程的线程池，EventLoop就是真正干活的线程。
>
- ServerBootstrap

ServerBootstrap是服务端的启动类，其核心配置：
> 1.0指定线程模型： 通过.group(bossGroup, workerGroup) 给引导类配置两大线程组，这个引导类的线程模型也就定型了。其中 bossGroup 表示监听端口，accept 新连接的线程组；workerGroup 表示处理每一条连接的数据读写的线程组；
>
> 2.0指定 IO 模型： 通过.channel(NioServerSocketChannel.class) 来指定 NIO 模型。如果指定 IO 模型为 BIO，那么这里配置上 OioServerSocketChannel.class 类型即可，通常都是使用 NIO，因为 Netty 的优势就在于 NIO；
>
> 3.0指定处理逻辑： 通过 childHandler () 方法，给这个引导类创建一个 ChannelInitializer，这里主要就是定义后续每条连接的数据读写，业务处理逻辑；
>
> 4.0绑定端口号： 调用 bind (80)，端口号自定义，不要和其他应用的端口号有冲突即可。
>

##### Client
- 创建一个线程组
- 创建启动类ServerBootstrap，指定线程组，指定NIO模型，指定处理逻辑Handler,连接指定端口。
- 设置Handler，初始化Handler。
  - 向该通道中，加入译码器。
  - 不断发送数据。

##### Server
- 创建两个线程组，boss用于监听连接，worker用于工作，当监听到有IO事件时开始处理。
- 创建启动类ServerBootstrap，并指定上述两个线程组，指定NIO模型，指定处理逻辑ChildHandler，绑定端口。
- 设置childHandler，作为新建的NioSocketChannel的初始化Handler。
  - 当新建的与客户端通信的NioSocketChannel被注册到EventLoop成功时，该方法会被调用，用于添加业务Handler
  - 处理化通道，并向传输链中加入解码器，再进行接受数据。








