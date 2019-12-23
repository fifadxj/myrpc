package myrpc.spring.annotation;

import myrpc.core.client.RpcClient;
import myrpc.core.common.RpcException;
import myrpc.core.register.ZookeeperRegister;
import myrpc.core.sample.UserApi;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ClientPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class clazz = bean.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Reference annotation = AnnotationUtils.getAnnotation(field, Reference.class);
            if (annotation == null) {
                return bean;
            }

            RpcClient<Object> rpcClient = new RpcClient<>();
            rpcClient.setInterface(annotation.interfaceClass());
            ZookeeperRegister register = new ZookeeperRegister("47.96.159.210", 2181);
            rpcClient.setRegister(register);
            Object stub = rpcClient.getStubProxy();
            field.setAccessible(true);
            try {
                field.set(bean, stub);
            } catch (IllegalAccessException e) {
                throw new RpcException(e);
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return null;
    }
}
