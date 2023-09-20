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
     * @param path the path of library
     * @param name native library name
     */
    public static void loadLib(Class<?> callerClass, String path, String name)
    {
        try (InputStream in = callerClass.getClassLoader().getResourceAsStream(name))
        {
            String tmpPath = System.getProperty("java.io.tmpdir") + File.separator + path;

            File fileOutDic = new File(tmpPath);
            if (!fileOutDic.exists())
            {
                fileOutDic.mkdirs();
            }

            File fileOut = new File(fileOutDic, name);
            if (!fileOut.exists())
            {
                fileOut.createNewFile();
                try (OutputStream out = Files.newOutputStream(fileOut.toPath()))
                {
                    Utils.copy(in, out, 8024);
                }
            }
            System.load(fileOut.getAbsolutePath());
        }
        catch (Exception e)
        {
            throw new RuntimeException(String.format("load %s library failure with path %s!", name, path), e);
        }
    }

}
