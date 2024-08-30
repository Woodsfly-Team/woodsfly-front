# woodsfly-front

https://github.com/Woodsfly-Team/woodsfly-front.git
这是鸟林通app的前端代码仓库，基于 kotlin 开发

**感谢你为 woodsfly-front 项目做出贡献！**

## 一.运行环境

- Android 11+

## 二.项目分支

- `main` 主分支（生产环境）

	用于生产环境的线上版本代码。不允许直接向 `main` 分支提交代码，需要通过 Pull Request 从 `dev` 分支合并代码。（此操作仅由项目管理员完成）

- `dev` 开发分支（测试环境）

	用于测试新功能和最新的 bug 修改。不允许直接向 `dev` 分支提交代码，需要通过 Pull Request 从 其他 分支合并代码。

## 三.贡献指南

1. 准备新分支

	在本地拉取最新的项目代码/同步到最新的项目代码，切换到 `dev` 分支，新建一个分支 `example`。

	```bash
	# 同步远端仓库最新进度
	git fetch
	# 切换到 dev 分支
	git checkout dev
	# 拉取最新代码
	git pull
	# 新建分支
	git checkout -b examplebranch
	```

2. 修改代码并提交新分支

	在自测完成后，请提交代码。**请注意，请你再次确认你的代码已经通过了你的本地测试。**

	```bash
	# 添加修改
	git add .
	# 提交修改
	git commit -m "message"
	# 推送到远程仓库
	git push origin examplebranch
	```

	请在提交信息 `message` 处填写你本次对代码修改的内容。

3. 提交 Pull Request

	提交 Pull Request 从 `examplbranch` 到 `dev` 分支。在 Pull Request 中，请确保你的代码通过了所有的测试，没有任何冲突。在 Pull Request 中，请详细描述你的修改，以及你的修改如何解决了问题。

	你需要请求一位其他人员来 code review 你的代码。

	然后，code reviewer 将授权你的 Pull Request 请求。

4. 合并 Pull Request

	当你的 Pull Request 被授权后，你可以将你的代码合并到 `dev` 分支。在合并之前，请确保你的代码没有任何冲突，也没有任何测试失败。合并完成后，你可以安全地删除分支 `examplebranch`。

	**请注意：严格禁止直接 push 到 `dev` 分支**

## 四.前端代码注释规范

全文浏览，重点已高亮标出

### 1. 原则

1、注释形式统一

在整个应用程序中，使用具有一致的标点和结构的样式来构造注释。如果在其它项目中发现它们的注释规范与这份文档不同，按照这份规范写代码，不要试图在既成的规范系统中引入新的规范。

2、注释内容准确简洁

内容要==简单、明了、含义准确==，防止注释的多义性，错误的注释不但无益反而有害。



### 2. 注释条件

#### 2.1 基本注释（理论上必须加）

- 类（接口）的注释               每个==类必须要加块注释，必须使用`javadoc`规范==

- 构造函数的注释

- 方法的注释

- 全局变量的注释                 ==全局变量必须加==

- 字段/属性的注释


 备注：简单的代码做简单注释，注释内容简略清晰，说人话

			另外，简单函数getter、setter方法不需加注释，重复不需要加



#### 2.2 特殊必加注释（必须加）

(a)   ==典型算法==必须有注释。

(b)    在代码不明晰处必须有注释。

(c)     在==代码修改处加上修改标识的注释==。

(d)    在==循环和逻辑分支==组成的代码中加注释。

(e)    为他人提供的==接口==必须加详细注释。

 备注：此类注释格式暂无举例。具体的注释格式自行定义，要求注释内容准确简洁。





### 3. 注释格式

#### 3. 1 基本格式类型

- 单行(single-line)注释：“//……”

- 块(block)注释：“/*……*/”

- 文档注释：“/**……*/”

	

- ==`javadoc` 注释标签语法==(定义类时必须加)

@author   对类的说明 标明开发该类模块的作者

@version   对类的说明 标明该类模块的版本

@see     对类、属性、方法的说明 参考转向，也就是相关主题

@param    对方法的说明 对方法中某参数的说明

@return   对方法的说明 对方法返回值的说明

@exception  对方法的说明 对方法可能抛出的异常进行说明



#### 3.2 参考举例

1.   类（接口）注释
	例如：

```java
/**

* 类的描述

* @author Administrator

* @Time 2012-11-2014:49:01

*

*/

public classTest extends Button {

  ……

}
```

2.   构造方法注释
	例如:

```java
public class Test extends Button {

  /**

   * 构造方法 的描述

   * @param name

   * 按钮的上显示的文字

   */

  public Test(String name){

     ……

  }

}

```

3.方法注释
例如

```java
public class Test extends Button {

  /**

   * 为按钮添加颜色

   *@param color

         按钮的颜色

*@return

*@exception  (方法有异常的话加)

* @author Administrator

* @Time2012-11-20 15:02:29


   */

  public voidaddColor(String color){

     ……

  }

}
```



4.   全局变量注释
	例如：

```java
public final class String

   implements java.io.Serializable, Comparable<String>,CharSequence

{

   /** The value is used for characterstorage. */

   private final char value[];

   /** The offset is the first index of thestorage that is used. */

   private final int offset;

   /** The count is the number of charactersin the String. */

   private final int count;

   /** Cache the hash code for the string */

private int hash; // Default to 0

……

}
```

5.字段/属性注释
例如：

```java
public class EmailBody implements Serializable{

   private String id;

   private String senderName;//发送人姓名

   private String title;//不能超过120个中文字符

   private String content;//邮件正文

   private String attach;//附件，如果有的话

   private String totalCount;//总发送人数

   private String successCount;//成功发送的人数

   private Integer isDelete;//0不删除 1删除

   private Date createTime;//目前不支持定时 所以创建后即刻发送

   privateSet<EmailList> EmailList;

……

}
```

## 五.环境配置与运行

当前开发环境为 Android Studio2024 ，Gradle 8.7，minSdk = 26，targetSdk = 34

```
git clone https://github.com/Woodsfly-Team/woodsfly-front.git
```

打开项目后可以如下build apk，下载到手机上进行使用

