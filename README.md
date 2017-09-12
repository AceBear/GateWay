# GateWay
![GateWay](https://travis-ci.org/HTTPS-HUB/GateWay.svg?branch=master)

交换网关

## 配置
中国内地用户可以把[gradle-sample.properties](https://github.com/HTTPS-HUB/GateWay/blob/master/gradle-sample.properties)复制为`gradle.properties`,以使用位于内地的阿里云镜像来提高下载速度

修改其中的`systemProp.spring.profiles.active`值=`product`启用`application-product.yml`配置文件

也可以在命令行启动时指定`--spring.profiles.active=product`来启用指定的配置文件
```SHELL
java -jar build/libs/GateWay-2.0.jar --spring.profiles.active=product
```