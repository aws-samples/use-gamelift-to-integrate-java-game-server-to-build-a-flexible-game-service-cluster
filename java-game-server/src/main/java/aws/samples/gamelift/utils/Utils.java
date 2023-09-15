package aws.samples.gamelift.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils
{
    public static boolean isEmpty(String value)
    {
        return value == null || value.trim()
                                     .isEmpty();
    }

    public static void copy(InputStream input, OutputStream output, int bufferSize) throws IOException
    {
        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
        }
    }
}
