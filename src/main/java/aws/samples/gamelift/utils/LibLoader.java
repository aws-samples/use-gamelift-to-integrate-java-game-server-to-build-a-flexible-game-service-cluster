package aws.samples.gamelift.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class LibLoader
{
    public static String determineLibName(String libName)
    {
        return System.mapLibraryName(libName);
    }

    /**
     * @param path 是自定义路径
     * @param name 是so 文件名
     */
    public static void loadLib(Class<?> callerClass, String path, String name)
    {
        // 取出so 库文件流，getResourceAsStream 这个方法，会在DataOperate 这个类的路径基础上去找文件，
        // 这就是为什么在打包时把 so 文件和要加载的类放在同一路径的原因，加载比较方便。
        try (InputStream in = callerClass.getClassLoader().getResourceAsStream(name))
        {
            String tmpPath = System.getProperty("java.io.tmpdir") + File.separator + path; // 临时文件路径

            // 创建新文件
            File fileOutDic = new File(tmpPath);
            if (!fileOutDic.exists())
            {
                fileOutDic.mkdirs();
            }

            File fileOut = new File(fileOutDic, name);
            if (!fileOut.exists())
            {
                fileOut.createNewFile();
            }

            try (OutputStream out = Files.newOutputStream(fileOut.toPath()))
            {
                Utils.copy(in, out, 8024);
            }
            System.load(fileOut.getAbsolutePath());
        }
        catch (Exception e)
        {
            throw new RuntimeException(String.format("在%s加载%s库失败!", path, name), e);
        }
    }

}
