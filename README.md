本项目是使用 gaussdb-r2dbc 驱动的示例代码。

# ConnectionFactoryExample

这个例子介绍ConnectionFactory的基础使用方法。

* 设置连接参数创建ConnectionFactory的实例。
* 通过Connection执行SQL语句。

运行例子前需要[通过镜像安装OpenGauss](#通过镜像安装OpenGauss) 

# 通过镜像安装OpenGauss

```shell
docker run --name opengauss --privileged=true -d -e GS_USERNAME=GaussdbExamples -e GS_PASSWORD=Gaussdb-Examples-123 -e GS_PORT=8000 -p 8000:8000 opengauss/opengauss:7.0.0-RC1.B023
```
