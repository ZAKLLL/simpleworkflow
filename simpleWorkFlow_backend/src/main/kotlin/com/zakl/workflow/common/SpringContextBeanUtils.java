package com.zakl.workflow.common;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component(value = SpringContextBeanUtils.BeanName)
@SuppressWarnings(value = {"unchecked", "all"})
public class SpringContextBeanUtils implements ApplicationContextAware, BeanFactoryAware {
    public final static String BeanName = "contextBeanUtils";

    private static ApplicationContext applicationContext;

    private static BeanFactory beanFactory;


    /**
     * 获取指定名字的bean
     *
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        return beanFactory.getBean(name);
    }


    /**
     * 获取指定类型的Bean
     *
     * @param beanType
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> beanType) {
        return beanFactory.getBean(beanType);
    }

    /**
     * 获取指定的类型的Bean
     *
     * @param name
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Class<T> t) {
        return t.cast(beanFactory.getBean(name));
    }

    /**
     * 获取指定类型的bean
     *
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T getSelfBean(Class<T> t) {
        return t.cast(beanFactory.getBean(t));

    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextBeanUtils.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        SpringContextBeanUtils.beanFactory = beanFactory;
    }
}
