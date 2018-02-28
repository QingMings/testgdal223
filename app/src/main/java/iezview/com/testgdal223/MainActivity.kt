package iezview.com.testgdal223

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.gdal.gdal.gdal
import org.gdal.ogr.Feature
import org.gdal.ogr.FieldDefn
import org.gdal.ogr.Geometry
import org.gdal.ogr.ogr
import java.nio.charset.Charset

/**
 *  GDAL编译过程参考博客
 *  @see <a  href= "https://www.jianshu.com/p/b6899345d18e">GDAL for Android 从编译到读写shp</a>
 *  @see <a  href= "https://alberthumbert.github.io/2017/12/09/makeFuckingGdalSo/index.html">Android 交叉编译 Shape/KML/GeoJSON 文件读写库 Gdal 注意事项</a>
 *
 * 编译了 armeabi-v7a 和 X86 两个平台的 .so 库
 * 下面代码 来源于 @see <a  href="https://github.com/houlian0/GdalAndroid">https://github.com/houlian0/GdalAndroid</a>
 * 用来测试GDAL库是否正常工作及中文是否乱码
 *
 */
class MainActivity : AppCompatActivity() {
    val GDAL_DRIVER_NAME = "ESRI Shapefile"
    lateinit var shpPath: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        sample_text.text = stringFromJNI()
        shpstring.text = externalCacheDir.path
        shpPath = "${externalCacheDir.path}/test.shp"
        shp_write.setOnClickListener {
            shpWrite()
        }
        shp_read.setOnClickListener {
            shpRead()
        }
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

    fun shpWrite() {
        doWrite()
    }

    fun shpRead() {
        doRead()
    }

    fun doWrite() {
        ogr.RegisterAll()
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "NO")
        gdal.SetConfigOption("SHAPE_ENCODING", "UTF-8")
        val driver = ogr.GetDriverByName(GDAL_DRIVER_NAME)
        if (driver == null) {
            println("驱动不可用")
            return
        }
        val dataSource = driver.CreateDataSource(shpPath, null)
        if (dataSource == null) {
            println("创建矢量文件失败")
            return
        }
        val olayer = dataSource.CreateLayer("TestPloygon", null, ogr.wkbPolygon, null)
        if (olayer == null) {
            println("创建图层失败")
            return
        }

        val fieldId = FieldDefn("FieledId", ogr.OFTString)
        olayer.CreateField(fieldId)

        val fieldName = FieldDefn("FieledName", ogr.OFTString)
        fieldName.SetWidth(100)
        olayer.CreateField(fieldName)

        var oDefn = olayer.GetLayerDefn()

        val oFeatureTriangle = Feature(oDefn)
        oFeatureTriangle.SetField(0, 0)
        oFeatureTriangle.SetField(1, String("三角形11".toByteArray(), Charset.forName("UTF-8")))
        val geomTriangle = Geometry.CreateFromWkt("POLYGON ((0 0,20 0,10 15,0 0))")
        oFeatureTriangle.SetGeometry(geomTriangle)
        olayer.CreateFeature(oFeatureTriangle)

        val oFeatureRectangle = Feature(oDefn)
        oFeatureRectangle.SetField(0, 1)
        oFeatureRectangle.SetField(1, "矩形22")
        val geomRectangle = Geometry.CreateFromWkt("POLYGON ((30 0,60 0,60 30,30 30,30 0))")
        oFeatureRectangle.SetGeometry(geomRectangle)
        olayer.CreateFeature(oFeatureRectangle)

        val oFeaturePentagon = Feature(oDefn)
        oFeaturePentagon.SetField(0, 2)
        oFeaturePentagon.SetField(1, "五角形33")
        val geomPentagon = Geometry.CreateFromWkt("POLYGON ((70 0,85 0,90 15,80 30,65 15,70 0))")
        oFeaturePentagon.SetGeometry(geomPentagon)
        olayer.CreateFeature(oFeaturePentagon)
        try {
            olayer.SyncToDisk()
            dataSource.SyncToDisk()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("创建数据集完成")
    }

    fun doRead() {
        ogr.RegisterAll()
        val encoding = gdal.GetConfigOption("SHAPE_ENCODING", null)
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "NO")
        gdal.SetConfigOption("SHAPE_ENCODING", "UTF-8")
        val ds = ogr.Open(shpPath,0)
        if (ds == null) {
            println("打开文件失败")
            return
        }
        println("打开文件成功")
        val layer = ds.GetLayerByIndex(0)
        if (layer==null){
            println("获取第0个图层失败")
            return
        }

        layer.ResetReading()
        println("属性表结构信息")
        shpstring.append("属性表结构信息：\n")
        val oDefn = layer.GetLayerDefn()
        val iFieldCount = oDefn.GetFieldCount()
        for (iAttr in 0 until iFieldCount) {
            val oField = oDefn.GetFieldDefn(iAttr)

            val content = oField.GetNameRef() + ": " +
                    oField.GetFieldTypeName(oField.GetFieldType()) + "(" +
                    oField.GetWidth() + "." + oField.GetPrecision() + ")"
            println(content)

            shpstring.append(content + "\n")

        }
        println("要素个数： ${layer.GetFeatureCount()}")
        shpstring.append("要素个数： ${layer.GetFeatureCount()} \n")
        var ofeature: Feature? =null
        while ((layer.GetNextFeature().apply { ofeature=this })!= null){
            println("当前处理第 ${ofeature?.GetFID()} ")
            shpstring.append("当前处理第 ${ofeature?.GetFID()} \n")
            for (iField in 0 until iFieldCount) {
                val oFieldDefn = oDefn.GetFieldDefn(iField)
                val type = oFieldDefn.GetFieldType()

                when (type) {
                // 只支持下面四种
                    ogr.OFTString -> {
                        println(ofeature?.GetFieldAsString(iField) + "\t")
                        shpstring.append(ofeature?.GetFieldAsString(iField) + "　")
                    }
                    ogr.OFTReal -> {
                        println("${ofeature?.GetFieldAsDouble(iField) }\t")
                        shpstring.append("${ofeature?.GetFieldAsDouble(iField) }\t ")
                    }
                    ogr.OFTInteger -> {
                        println("${ofeature?.GetFieldAsInteger(iField) }\t")
                        shpstring.append("${ofeature?.GetFieldAsInteger(iField) }\t  ")
                    }
                    ogr.OFTDate -> {
                    }
                    else -> {
                        println("${ofeature?.GetFieldAsString(iField) }\t")
                        shpstring.append("${ofeature?.GetFieldAsString(iField) }\t")
                    }
                }//                        oFeature.GetFieldAsDateTime();
            }

            val oGeometry  =ofeature?.GetGeometryRef()
            shpstring.append("\n空间坐标：" + oGeometry?.ExportToJson())
        }
        println("数据集关闭")
    }
}
