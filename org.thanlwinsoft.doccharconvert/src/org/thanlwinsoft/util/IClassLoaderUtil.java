package org.thanlwinsoft.util;

public interface IClassLoaderUtil
{
    public Class<?> loadClass(String className) throws ClassNotFoundException;
}
