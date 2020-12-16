
通过添加/删除如下代码启用/删除穿山甲sdk初始化代码，

![](doc/ttsdk_bug_reproduce.png)

编译安装app之后，当app运行时，在logcat中使用`MyUncaughtExceptionHandler`作为Tag关键字进行日志过滤。

* 在`App`中启用穿山甲sdk初始化代码，可以看到最后一条日志是`：MyUncaughtExceptionHandler: left 3 MyUncaughtExceptionHandler doest not invoke.`，详见下图：

![](doc/ttsdk_cause_thread_uncaught_exception_handler_not_invoke.png)


* 在`App`中删除穿山甲sdk初始化代码，可以看到最后一条日志是`：MyUncaughtExceptionHandler: all of MyUncaughtExceptionHandler has invoked.`，详见下图：

![](doc/thread_uncaught_exception_handler_chain.png)

综上，可以看到在`App`中共计添加了5个自定义的`MyUncaughtExceptionHandler`，当中初始化穿山甲sdk之后，有3个`MyUncaughtExceptionHandler`没有收到崩溃回调。


