#  GDAL 2.2.3 编译for android

### GDAL编译过程参考博客
*   <a  href= "https://www.jianshu.com/p/b6899345d18e">GDAL for Android 从编译到读写shp</a>
*   <a  href= "https://alberthumbert.github.io/2017/12/09/makeFuckingGdalSo/index.html">Android 交叉编译 Shape/KML/GeoJSON 文件读写库 Gdal 注意事项</a>
#### 编译了 armeabi-v7a 和 X86 两个平台的 .so 库
#### 目录结构如下
```bash
     $ tree --dirsfirst
    .
    ├── armeabi-v7a
    │   ├── libgdalconstjni.so
    │   ├── libgdaljni.so
    │   ├── libogrjni.so
    │   └── libosrjni.so
    ├── x86
    │   ├── libgdalconstjni.so
    │   ├── libgdaljni.so
    │   ├── libogrjni.so
    │   └── libosrjni.so
    └── gdal.jar
 ```

![图片](http://ww1.sinaimg.cn/large/005yCS4cgy1fovzl50g13j30fy0u00v9)

![图片](http://ww1.sinaimg.cn/large/005yCS4cgy1fovzl50nbsj31bk0wq419)
