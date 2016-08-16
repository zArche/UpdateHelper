# 安卓检查更新模块
+ 简介：  
　封装的一个安卓检查更新模块。  
　实现功能有：获取最新更新信息、下载最新apk、检测包名是否合法、安装apk  
+ 项目集成方式：  
　1. 修改ApiClient中接口地址为自己的地址  
　2. 修改ApiClient中参数为自己的参数  
　3. 设置为library项目，导出JAR包。在导出JAR包时，去除选中.classpath、.project、AndroidManifest.xml、ic_launcher-web.png、proguadr-project.txt、project.properties等文件  
　4. 在项目中引用该JAR包，并声明网络请求权限、读写SD卡权限
