
// Author: Pierce Brooks

package com.piercelbrooks.common;

public interface BasicServiceUser <T extends BasicService<T>>
{
    public Class<?> getServiceClass();
    public T getService();
}
