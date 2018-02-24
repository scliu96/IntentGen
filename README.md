# ServiceLeak
使用方法：
在 AndroidInstrument.java 文件中将 sootJarParh，androidJarPath，APKPath 分别改为本机上的 soot.jar 包地址，android-platforms-master 文件夹地址，要测试的 APK 地址
之后点击运行，获得的 result 变量就是所要求到的结果，其中每个 MyIntent 的 Property 属性就是一个关于 key-type 的 map，在终端已经输出一次
