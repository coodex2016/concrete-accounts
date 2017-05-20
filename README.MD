# CONCRETE-ACCOUNTS

Concrete-accounts面向concrete提供多种可扩展、开箱即用人员、账户管理模型。

## organization

组织结构式的账户模型：
模型如下
帐号 1--1 人员 1--* 职位 *--* 组织

组织 <-- 部门
组织 <-- 单位

部门 *--1 单位

单位 1--0 单位

部门 1--0 部门

职位 1--* 角色

人员 1--* 角色

### google authenticator

totp.properties
```properties
# 容错范围，1-10，按照google authenticator的设计，30秒一个间隔，前后容错各fault.tolerance个间隔
fault.tolerance=10

#核发者
app.issuer.name=coodex.org 
# 服务器名
app.server.name=
```
