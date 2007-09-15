package org.thanlwinsoft.util;

public class SimpleClassLoaderUtil implements IClassLoaderUtil
{

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException
    {
        return Class.forName(className);
    }

}
