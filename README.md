本项目是使用 gaussdb-r2dbc 驱动的示例代码。

# ConnectionFactoryExample

这个例子介绍ConnectionFactory的基础使用方法。

* 设置连接参数创建ConnectionFactory的实例。
* 通过Connection执行SQL语句。

运行例子前需要[通过镜像安装OpenGauss](#通过镜像安装OpenGauss) 

# SSLConnectionExample

这个例子介绍SSL连接的基础使用方法。

运行例子前，数据库需要启用SSL，并获取数据库提供的证书ca.pem。如果通过华为云购买GaussDB云数据库，默认
就已经启用SSL，可以从界面直接下载ca.pem。

# CentralizedLoadBalanceExample

这个例子介绍GaussDB集中式版本负载均衡。运行例子前，需要通过华为云购买集中式集群。

# DistributedLoadBalanceExample

这个例子介绍GaussDB分布式版本负载均衡。运行例子前，需要通过华为云购买分布式集群。

# 通过镜像安装OpenGauss

```shell
docker run --name opengauss --privileged=true -d -e GS_USERNAME=GaussdbExamples -e GS_PASSWORD=Gaussdb-Examples-123 -e GS_PORT=8000 -p 8000:8000 opengauss/opengauss:7.0.0-RC1.B023
```
