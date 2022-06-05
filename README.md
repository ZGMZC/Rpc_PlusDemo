### Rpc_PlusDemo
从0到1动手实现RPC框架

> 不同的分支代表不同的进度


- CAUTION！！！ 
> master 无代码实现，仅仅是简单介绍
> 
> V1.0 简单的Client和Server端
> 
> V2.0 代理层的实现
> 
> V3.0 Zookeeper作为注册中心的实现
> 
> V4.0 ...
> 
> ...

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

##### 代理层的设计
面对发送端的请求，我们可以设计一个代理层，统一将内部的细节都屏蔽起来，让调用者使用起来无感知。
Server.doRef内部实则就是一个代理的手段，内部可以设计一个统一的代理组件，辅助开发者发送远程服务的调用，并且将对应的数据结果返回。
> 代理模式
> - 代理模式在客户端与目标对象之间起到了一个中介作用和保护目标对象的作用。
> - 代理对象可以扩展目标对象的功能。
> - 代理模式能将客户端和目标对象分离，在一定程度上降低了系统的耦合度，增加了程序的可扩展性。

##### 路由层的设计
考虑一个问题：**当目标服务众多的时候，客户端需要如何确定最终请求的服务提供者是谁呢？**

在这里，引入一个叫做**路由**的角色，此时客户端的调用整体流程：客户端的请求会经过一个叫做路由层的部分，需要通过路由层内部的规则去匹配对应的provider服务。

因此在设计路由层的时候需要考虑以下几个点：
- 如何获取到provider的服务地址？
- 如何从集群服务中做筛选？
- 如何设计能够较好地兼容后期的路由扩占功能？
- provider服务地址下线之后，下线的通知需要告知路由层。
##### 协议层的设计
client端在使用RPC框架进行远程调用的时候，需要对数据信息进行统一的包装和组织，最终才能将其发送到目标机器并且被目标机器接受解析，因此对于数据的各种序列化、反序列化，协议的组装我们可以统一封装在协议层中进行实现，此时客户端的调用整体流程：router模块会负责计算好最终需要调用的服务提供者具体信息，然后将对应的地址信息、请求参数传输到protocol层，最终由protocol层对数据封装为对应的协议体，然后进行序列化处理，最终通过网络发送给目标机器。

##### 可拔插式组件设计与开发
从本地请求到protocol层发送数据，整个链路中可能还需要考虑后续的一些二次扩展设计，例如某些自定义条件的过滤、服务分组等，所以在设计的时候可以考虑在proxy和router之间加入一些链路模块，有点类似责任链模式。

##### 注册中心层的设计
当服务提供者呈现集群模式的时候，客户端需要去获取provider的诸多信息，那么在这个过程中就需要引入一个叫做注册中心的角色。

服务提供者将自己的地址、接口、分组等详细信息都上报到注册中心模块，并且当服务上线、下线都会通知到注册中心，然后服务调用方只需要订阅注册中心即可。
> 目前常见的组件有**Zookeeper、Nacos、etcd、Redis等到**

关于注册中心层，我们需要重点关注的点是：
- 如何与注册中心进行基本的连接访问？
- 如何监听服务数据在注册中心的实时变化？
- 注册中心如果出现了异常，需要有哪些安全手段？

##### 容错层的设计
在进行远程调用的过程中，通常都会出现一些异常情况，目前常见的一些容错方面的处理手段有：
- 超时重试
- 快速失败
- 无限重试
- 出现异常后回调指定方法
- 无视失败
- ······
面对这些场景，我们可以尝试将这些处理手段统一抽象出来，交给容错层去处理。

##### 服务提供者的线程池设计
当请求发送到服务提供者的时候，服务提供方需要对其进行相应的解码，然后在本地进行核心处理。
这部分的工作需要交给专门的线程去计算，这里面涉及到的相关技术点：
- IO线程和Worker线程的拆分
- 调用结果和客户端请求的唯一匹配
- 客户端请求后的同步转异步处理
- 单一请求队列和多请求队列的设计差异性

##### 接入层设计
在整套RPC组件基本设计实现后，我们需要考虑如何将其接入到实际开发项目中，而目前主要使用技术大多数基于Spring框架作为基本骨架，所以RPC框架页需要考虑到接入对应的starter组件

##### 小结
RPC框架的整体结构基本分层为：
- 代理层：负责对底层调用细节的封装
- 路由层：负责在集群目标服务中的调用筛选策略
- 协议层：负责请求数据的转码封装等作用
- 链路层：负责执行一些自定义的过滤链路，可以供后期二次扩展
- 注册中心层：关注服务的上下线，以及一些权重，配置动态调整等功能
- 序列化层：负责将不同的序列化技术嵌套在框架中
- 容错层：当服务调用出现失败之后需要有容错层的兜底辅助
- 接入层：烤炉如何与常用框架Spring接入
- 公共层：主要存放一些通用配置，工具类，缓存等信息

#### 网络通讯模型的核心

##### BIO
传统的BIO技术会在accept函数和read函数中发生堵塞，服务端创建了socket之后会堵塞在等待外界连接的accept的函数环节，当客户端连接上服务端之后，accept的堵塞状态才会放开，然后进入到read环节（读取客户端发送过来的网络数据）。

客户端如果一直没有发送数据过来，那么服务端的read调用方法就会一直处于堵塞状态，倘若数据通过网络抵达了网卡的缓冲区，此时则会将数据从内核态拷贝到用户态，然后返回给read调用。

##### NIO
当socket的服务端启动后，会对每个socket连接的对象开启一个线程，然后在一个循环里面调用read函数，此时的read函数调用不会进入阻塞状态，但是仍未解决根本性问题：每次来请求都要创建一个线程来监听客户端的请求。如果客户端在建立连接之后长时间没有传输数据，此时对于服务端而言就会造成资源浪费的情况。

我们不妨可以将accept和read分为两个模块来处理，当accept函数接收到新的连接（其实本质就是一个文件描述符fd）之后，就将其放入到一个集合，然后会有一个后台任务统一对这个集合中的fd遍历执行read函数操作。

在Linux内核中设计了一个叫做select的函数，这个函数在内核态中对fd集合进行遍历，如果对应的fd接收到客户端的抵达数据，则会返回给用户态调用方（注意用户态发送select调用的时候依然会处于堵塞状态）。

##### Netty
> Netty是一款基于NIO（Nonblocking I/O，非阻塞IO）开发的网络通信框架，对比于BIO（Blocking I/O，阻塞IO），它的并发性能得到了很大提高。

- Netty传输的高性能特点

在Java的内存中，存在有堆内存、栈内存和字符串常量池等等，其中堆内存是占用内存空间最大的一块，也是Java对象存放的地方，一般我们的数据如果需要从IO读取到堆内存，中间需要经过Socket缓冲区，也就是说一个数据会被拷贝两次才能到达它的终点，如果数据量大，就会造成不必要的资源浪费。
Netty针对这种情况，使用了NIO中的一大特性——零拷贝，当它需要接收数据的时候，它会在堆内存之外开辟一块内存，数据就直接从IO读到了那块内存中去，在netty里面通过ByteBuf可以直接对这些数据进行直接操作，从而加快了传输速度。
- Netty良好的封装性特点

相比于JDK内部提供的NIO编码格式，Netty在进行nio技术开发的时候，封装的Api更加方便开发者使用，能够降低开发者对于操控NIO技术的门槛。





