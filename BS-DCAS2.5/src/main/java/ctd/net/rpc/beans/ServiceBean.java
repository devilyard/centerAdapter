// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ServiceBean.java

package ctd.net.rpc.beans;

import ctd.annotation.RpcService;
import ctd.net.rpc.Subscriber;
import ctd.net.rpc.config.MethodConfig;
import ctd.net.rpc.config.ServiceConfig;
import ctd.net.rpc.registry.ServiceRegistry;
import ctd.util.ReflectUtil;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.FactoryBean;

public class ServiceBean extends ServiceConfig
    implements FactoryBean
{

    public ServiceBean()
    {
    }

    public Object getObject()
        throws Exception
    {
        return ref;
    }

    public void setRef(Object ref)
        throws IllegalAccessException
    {
        this.ref = ref;
        Class c = ref.getClass();
        Method ls[] = c.getMethods();
        Method arr$[] = ls;
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            Method m = arr$[i$];
            if(m.isAnnotationPresent(RpcService.class))
            {
                MethodConfig mc = new MethodConfig(m);
                methods.put(mc.getDesc(), mc);
            }
        }

        if(methods.size() > 0)
        {
            String subscribe = getParameter("subscribe");
            if(!StringUtils.isEmpty(subscribe) && !ReflectUtil.isCompatible(Subscriber.class, ref))
                throw new IllegalAccessException((new StringBuilder()).append("service[").append(id).append("] has @subscribe,but [not implements ctd.net.rpc.Subscriber].").toString());
            ServiceRegistry.publish(this);
        } else
        {
            throw new IllegalAccessException((new StringBuilder()).append("service[").append(id).append("] has no method with annotation'@rpcService'.its meanless to defined as service").toString());
        }
    }

    public void setWeights(String weights)
    {
        setParameter("weights", weights);
    }

    public void setSubscribe(String subscribe)
        throws IllegalAccessException
    {
        setParameter("subscribe", subscribe);
    }

    public Class getObjectType()
    {
        if(ref != null)
            return ref.getClass();
        else
            return null;
    }

    public boolean isSingleton()
    {
        return true;
    }

    private static final long serialVersionUID = 0xf91c39b911752a5eL;
    private Object ref;
}
