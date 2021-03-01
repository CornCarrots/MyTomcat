# MyTomcat
## 手写一个基于Tomcat实现的web服务器，支持servlet和jsp规范
自定义host、context等服务器内置对象，自定义请求体与响应体，支持cookie和session解析、post和get请求

## 服务器结构
- server服务器：包含service服务
    - service服务：具有一个engine引擎和多个connector连接器
        - connector连接器：监听某个端口，并对该端口的请求响应定制化处理
        - engine引擎：具有多个host虚拟主机
            -   host虚拟主机：具有多个context应用上下文
                - context应用上下文：指定url对应要加载的本地应用路径
## 服务器处理流程

### 服务器加载
1. 初始化web公共类加载器CommonClassLoader，用于加载服务器自身所需jar包，放入当前线程中
2. 读取*server.xml*配置文件，初始化service
    1. 读取*server.xml*配置文件，初始化connector
    2. 初始化engine：读取配置文件，初始化host
        1. 通过扫描文件目录和读取*server.xml*配置文件，加载上下文
            1. 读取*server.xml*配置文件，获取url和文件路径
            2. 根据文件路径，初始化应用类加载器WebappClassLoader，用于加载应用自身所需jar包
            3. 读取应用下*web.xml*配置文件，初始化servlet、filter、listener和url映射关系
            4. 加载配置了自启动的servlet，放入内存
            5. 创建jsp解析工厂，方便后续解析jsp
            6. 初始化过滤器链
            7. 初始化监听器
            8. 启动watcher监听器，实现热部署
        2. 启动watcher监听器，动态部署war包

### 服务器启动

启动connector线程（BIO）
1. 初始化serversoket
2. 接收socket
3. 初始化请求体
    1. 读取字节流
    2. 解析uri，获取uri对应的上下文
    3. 解析http方法
    4. 解析http参数
    5. 解析http请求头部
    6. 解析cookie
4. 启用处理线程
    1. 获取session
    2. 获取url处理的servlet（通过对象工厂获取）
        - 静态文件，直接返回静态资源
        - 动态文件，执行servlet方法（jsp也属于servlet，多了一步编译）
    3. 进入过滤器链条处理
    4. 处理响应
    5. 写入字节流，发送响应
