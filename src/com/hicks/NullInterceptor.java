package com.hicks;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import java.io.Serializable;

public class NullInterceptor extends EmptyInterceptor
{
    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
    {
        for (int i = 0; i < types.length; i++)
        {
            if ((types[i].getName().equals("string")) && state[i] == null)
            {
                state[i] = "";
            }
        }
        return true;
    }
}
