package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionOverrideException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.*;

@Component
public class SpringBeanFactoryUtil implements ApplicationContextAware, BeanFactoryAware, SmartInstantiationAwareBeanPostProcessor {
    private final static LogHelper logger = new LogHelper();
    private static ApplicationContext applicationContext;
    private static BeanFactory beanDefinitionRegistry;
    private static final List<BeanOperationListener> beanOperationListenerList = Collections.synchronizedList(new ArrayList<>());

    private SpringBeanFactoryUtil() {}

    public enum Scope {
        Singleton,
        Prototype;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (applicationContext == null) {
            applicationContext = context;
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanDefinitionRegistry == null) {
            beanDefinitionRegistry = beanFactory;
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        try {
            return SmartInstantiationAwareBeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
        } finally {
            firePostProcessAfterInitialization(bean, beanName);
        }
    }

    public static void publishEvent(ApplicationEvent event) {
        applicationContext.publishEvent(event);
    }

    /**
     * 檢查Bean是否已經在Container中
     *
     * @param <T>
     * @param beanClass
     * @param toCamelCase
     * @return
     */
    public static <T> boolean isBeanExist(final Class<T> beanClass, boolean... toCamelCase) {
        return isBeanExist(beanClass.getName(), toCamelCase);
    }

    /**
     * 檢查Bean是否已經在Container中
     *
     * @param beanClassName
     * @param toCamelCase
     * @return
     */
    public static boolean isBeanExist(final String beanClassName, boolean... toCamelCase) {
        try {
            String beanName = getBeanName(beanClassName, toCamelCase);
            return applicationContext.containsBean(beanName);
        } catch (Throwable t) {
            logger.error(t, "verify Bean exist failed, beanClassName = [", beanClassName, "]");
            fireGetOnException(beanClassName, t);
            return false;
        }
    }

    /**
     * 根據類獲取bean
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public static <T> T getBean(final Class<T> clazz) {
        return getBean(clazz, true);
    }

    /**
     * 根據類獲取bean
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public static <T> T getBean(final Class<T> clazz, boolean fireGetOnException) {
        try {
            return (T) applicationContext.getBean(clazz);
        } catch (Throwable t) {
            if (fireGetOnException) {
                logger.error(t, "get bean failed, clazz = [", clazz.getName(), "]");
                fireGetOnException(clazz.getName(), t);
            }
            return null;
        }
    }

    /**
     * 根據name獲取bean
     *
     * @param <T>
     * @param beanClassName
     * @param toCamelCase
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(final String beanClassName, boolean... toCamelCase) {
        String beanName = getBeanName(beanClassName, toCamelCase);
        try {
            return (T) applicationContext.getBean(beanName);
        } catch (Throwable t) {
            logger.error(t, "get bean failed, beanClassName = [", beanClassName, "]");
            fireGetOnException(beanClassName, t);
            return null;
        }
    }

    /**
     * 根據name獲取bean
     *
     * @param <T>
     * @param beanClassName
     * @param toCamelCase
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBeanQuietly(final String beanClassName, boolean... toCamelCase) {
        String beanName = getBeanName(beanClassName, toCamelCase);
        try {
            return (T) applicationContext.getBean(beanName);
        } catch (Throwable t) {
            LogHelper.getAdditionalLogger().error(t, "get bean failed, beanClassName = [", beanClassName, "]");
            return null;
        }
    }

    /**
     * 手動註冊一個bean
     *
     * @param <T>
     * @param beanClass
     * @param toCamelCase
     * @return
     */
    public static <T> T registerBean(final Class<T> beanClass, boolean... toCamelCase) {
        return registerBean(beanClass, Scope.Singleton, null, toCamelCase);
    }

    /**
     * 手動註冊一個bean
     *
     * @param <T>
     * @param beanClass
     * @param toCamelCase
     * @return
     */
    public static <T> T registerBean(final Class<T> beanClass, final Object[] constructorArgumentValues, boolean... toCamelCase) {
        return registerBean(beanClass, Scope.Singleton, constructorArgumentValues, toCamelCase);
    }

    /**
     * 手動註冊一個bean
     *
     * @param <T>
     * @param beanClass
     * @param scope
     * @param toCamelCase
     * @return
     */
    public static <T> T registerBean(final Class<T> beanClass, final Scope scope, boolean... toCamelCase) {
        return registerBean(beanClass, scope, null, toCamelCase);
    }

    /**
     * 手動註冊一個bean
     *
     * @param <T>
     * @param beanClass
     * @param scope
     * @param constructorArgumentValues
     * @param toCamelCase
     * @return
     */
    public static <T> T registerBean(final Class<T> beanClass, final Scope scope, final Object[] constructorArgumentValues, boolean... toCamelCase) {
        return registerBean(beanClass.getName(), scope, constructorArgumentValues, toCamelCase);
    }

    /**
     * 手動註冊一個bean
     *
     * @param <T>
     * @param beanClassName
     * @param toCamelCase
     * @return
     */
    public static <T> T registerBean(final String beanClassName, boolean... toCamelCase) {
        return registerBean(beanClassName, Scope.Singleton, null, toCamelCase);
    }

    /**
     * 手動註冊一個bean
     *
     * @param <T>
     * @param beanClassName
     * @param toCamelCase
     * @return
     */
    public static <T> T registerBean(final String beanClassName, final Object[] constructorArgumentValues, boolean... toCamelCase) {
        return registerBean(beanClassName, Scope.Singleton, constructorArgumentValues, toCamelCase);
    }

    /**
     * 手動註冊一個bean
     *
     * @param <T>
     * @param beanClassName
     * @param scope
     * @param toCamelCase
     * @return
     */
    public static <T> T registerBean(final String beanClassName, final Scope scope, boolean... toCamelCase) {
        return registerBean(beanClassName, scope, null, toCamelCase);
    }

    /**
     * 手動註冊一個bean
     *
     * @param <T>
     * @param beanClassName
     * @param scope
     * @param constructorArgumentValues
     * @param toCamelCase
     * @return
     */
    public static <T> T registerBean(final String beanClassName, final Scope scope, final Object[] constructorArgumentValues, boolean... toCamelCase) {
        String beanName = getBeanName(beanClassName, toCamelCase);
        if (applicationContext.containsBean(beanName)) {
            return getBean(beanName);
        }
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClassName);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        if (scope == Scope.Prototype) {
            beanDefinition.setScope(AbstractBeanDefinition.SCOPE_PROTOTYPE);
        }
        if (ArrayUtils.isNotEmpty(constructorArgumentValues)) {
            ConstructorArgumentValues constructor = new ConstructorArgumentValues();
            for (int i = 0; i < constructorArgumentValues.length; i++) {
                constructor.addIndexedArgumentValue(i, constructorArgumentValues[i]);
            }
            beanDefinition.setConstructorArgumentValues(constructor);
        }
        try {
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanDefinitionRegistry;
            registry.registerBeanDefinition(beanName, beanDefinition);
            logger.info("register bean succeed, beanClassName = [", beanClassName, "], scope = [", scope, "], constructorArgumentValues = [", StringUtils.join(constructorArgumentValues, ","), "]");
        } catch (BeanDefinitionOverrideException e) {
            logger.warn("already registered bean, beanClassName = [", beanClassName, "], constructorArgumentValues = [", StringUtils.join(constructorArgumentValues, ","), "]");
            return getBean(beanName);
        } catch (Throwable t) {
            logger.error(t, "register bean failed, beanClassName = [", beanClassName, "], constructorArgumentValues = [", StringUtils.join(constructorArgumentValues, ","), "]");
            fireRegisterOnException(beanClassName, t);
            return null;
        }
        try {
            return getBean(beanName);
        } catch (Throwable t) {
            unregisterBean(beanClassName, toCamelCase);
            return null;
        }
    }

    /**
     * 反註冊一個bean
     *
     * @param <T>
     * @param beanClass
     * @param toCamelCase
     */
    public static <T> boolean unregisterBean(final Class<T> beanClass, boolean... toCamelCase) {
        return unregisterBean(beanClass.getName(), toCamelCase);
    }

    /**
     * 反註冊一個bean
     *
     * @param <T>
     * @param beanClassName
     * @param toCamelCase
     */
    public static <T> boolean unregisterBean(final String beanClassName, boolean... toCamelCase) {
        String beanName = getBeanName(beanClassName, toCamelCase);
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanDefinitionRegistry;
        if (!registry.containsBeanDefinition(beanName)) {
            logger.warn("already unregister bean, beanClassName = [", beanClassName, "]");
            return false;
        }
        try {
            registry.removeBeanDefinition(beanName);
            logger.info("unregister bean succeed, beanClassName = [", beanClassName, "]");
            return true;
        } catch (Throwable t) {
            logger.error(t, "unregister bean failed, beanClassName = [", beanClassName, "]");
            fireUnregisterOnException(beanClassName, t);
            return false;
        }
    }

    /**
     * 檢查Controller是否已經在Container中
     *
     * @param <T>
     * @param beanClass
     * @param toCamelCase
     * @return
     */
    public static <T> boolean isControllerExist(final Class<T> beanClass, boolean... toCamelCase) {
        return isControllerExist(beanClass.getName(), toCamelCase);
    }

    /**
     * 檢查Controller是否已經在Container中
     *
     * @param beanClassName
     * @param toCamelCase
     * @return
     */
    public static boolean isControllerExist(final String beanClassName, boolean... toCamelCase) {
        if (!isBeanExist(beanClassName, toCamelCase)) {
            return false;
        }
        String beanName = getBeanName(beanClassName, toCamelCase);
        boolean exist = false;
        try {
            RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) applicationContext.getBean("requestMappingHandlerMapping");
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                if (getBeanName(entry.getValue().getBeanType().getName(), toCamelCase).equals(beanName)) {
                    exist = true;
                    break;
                }
            }
            return exist;
        } catch (Throwable t) {
            logger.error(t, "verify Controller exist failed, beanClassName = [", beanClassName, "]");
            fireGetOnException(beanClassName, t);
            return false;
        }
    }

    /**
     * 根據類獲取Controller
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public static <T> T getController(final Class<T> clazz) {
        return getController(clazz, true);
    }

    /**
     * 根據類獲取Controller
     *
     * @param <T>
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getController(final Class<T> clazz, boolean fireGetOnException) {
        try {
            RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) applicationContext.getBean("requestMappingHandlerMapping");
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                if (entry.getValue().getBeanType().equals(clazz)) {
                    return (T) entry.getValue().getBean();
                }
            }
        } catch (Throwable t) {
            if (fireGetOnException) {
                logger.error(t, "get Controller failed, clazz = [", clazz.getName(), "]");
                fireGetOnException(clazz.getName(), t);
            }
        }
        return null;
    }

    /**
     * 根據name獲取Controller
     *
     * @param <T>
     * @param beanClassName
     * @param toCamelCase
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getController(final String beanClassName, boolean... toCamelCase) {
        String beanName = getBeanName(beanClassName, toCamelCase);
        try {
            RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) applicationContext.getBean("requestMappingHandlerMapping");
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                if (getBeanName(entry.getValue().getBeanType().getName(), toCamelCase).equals(beanName)) {
                    return (T) entry.getValue().getBean();
                }
            }
        } catch (Throwable t) {
            logger.error(t, "get Controller failed, beanClassName = [", beanClassName, "]");
            fireGetOnException(beanClassName, t);
        }
        return null;
    }

    /**
     * 註冊一個Controller
     *
     * @param <T>
     * @param controllerClass
     * @param toCamelCase
     */
    public static <T> T registerController(final Class<T> controllerClass, boolean... toCamelCase) {
        return registerController(controllerClass.getName(), toCamelCase);
    }

    /**
     * 註冊一個Controller
     *
     * @param <T>
     * @param controllerClassName
     * @param toCamelCase
     */
    public static synchronized <T> T registerController(final String controllerClassName, boolean... toCamelCase) {
        if (isControllerExist(controllerClassName, toCamelCase)) {
            logger.warn("cannot register controller again cause exists, controllerClassName = [", controllerClassName, "]");
            return getController(controllerClassName, toCamelCase);
        }
        String beanName = getBeanName(controllerClassName, toCamelCase);
        T controller = registerBean(beanName);
        try {
            RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) applicationContext.getBean("requestMappingHandlerMapping");
            Method method = requestMappingHandlerMapping.getClass().getSuperclass().getSuperclass().getDeclaredMethod("detectHandlerMethods", Object.class);
            ReflectionUtils.makeAccessible(method);
            method.invoke(requestMappingHandlerMapping, beanName);
            logger.info("register controller succeed, controllerClassName = [", controllerClassName, "]");
        } catch (Throwable t) {
            logger.error(t, "register controller failed, controllerClassName = [", controllerClassName, "]");
            fireRegisterOnException(controllerClassName, t);
        }
        return controller;
    }

    /**
     * 反註冊一個Controller
     *
     * @param <T>
     * @param controllerClass
     * @param toCamelCase
     */
    public static <T> boolean unregisterController(final Class<T> controllerClass, boolean... toCamelCase) {
        return unregisterController(controllerClass.getName(), toCamelCase);
    }

    /**
     * 反註冊一個Controller
     *
     * @param <T>
     * @param controllerClassName
     * @param toCamelCase
     */
    public static <T> boolean unregisterController(final String controllerClassName, boolean... toCamelCase) {
        // 下面這段在Destroy時, 會有BeanCreationNotAllowedException的異常, 所以先mark
        // T controller = getBean(controllerClassName, false, toCamelCase);
        // if (controller == null) {
        //     // 等於null, 不代表BeanDefinitionRegistry中沒有, 所以還是要檢查一下
        //     if (!unregisterBean(controllerClassName, toCamelCase)) {
        //         logger.warn("already unregister controller, controllerClassName = [", controllerClassName, "]");
        //     }
        //     return false;
        // }
        try {
            RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) applicationContext.getBean("requestMappingHandlerMapping");
            // Class<?> targetClass = controller.getClass();
            Class<?> targetClass = Class.forName(controllerClassName);
            ReflectionUtils.doWithMethods(targetClass, (Method method) -> {
                Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
                try {
                    Method createMappingMethod = RequestMappingHandlerMapping.class.getDeclaredMethod("getMappingForMethod", Method.class, Class.class);
                    ReflectionUtils.makeAccessible(createMappingMethod);
                    RequestMappingInfo requestMappingInfo = (RequestMappingInfo) createMappingMethod.invoke(requestMappingHandlerMapping, specificMethod, targetClass);
                    if (requestMappingInfo != null) {
                        requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
                        logger.info("unregister controller succeed, controllerClassName = [", controllerClassName, "]");
                    }
                } catch (Exception e) {
                    logger.warn(e, "unregister controller failed, controllerClassName = [", controllerClassName, "]");
                    fireRegisterOnException(controllerClassName, e);
                }
            }, ReflectionUtils.USER_DECLARED_METHODS);
        } catch (BeanCreationNotAllowedException e) {
            logger.warn("unregister controller failed, controllerClassName = [", controllerClassName, "], error = [", e.getMessage(), "]");
            fireUnregisterOnException(controllerClassName, e);
        } catch (Throwable t) {
            logger.error(t, "unregister controller failed, controllerClassName = [", controllerClassName, "]");
            fireUnregisterOnException(controllerClassName, t);
        }
        // 最後要unregisterBean
        return unregisterBean(controllerClassName, toCamelCase);
    }

    /**
     * 根據類名獲取beanName
     *
     * @param beanClassName
     * @param toCamelCase
     * @return
     */
    private static String getBeanName(final String beanClassName, boolean... toCamelCase) {
        String beanName = beanClassName;
        if (ArrayUtils.isNotEmpty(toCamelCase) && toCamelCase[0]) {
            int index = beanClassName.lastIndexOf(".");
            if (index >= 0) {
                beanName = beanName.substring(index + 1);
            }
            beanName = StringUtil.toLowerCase(beanName, 0, 0);
        }
        return beanName;
    }

    public interface BeanOperationListener extends EventListener {

        default void registerOnException(String beanName, Throwable t) {}

        default void getOnException(String beanName, Throwable t) {}

        default void unregisterOnException(String beanName, Throwable t) {}

        default void postProcessAfterInitialization(Object bean, String beanName) {}

    }

    public static void addBeanOperationListener(BeanOperationListener listener) {
        if (!beanOperationListenerList.contains(listener)) {
            beanOperationListenerList.add(listener);
        }
    }

    public static void removeBeanOperationListener(BeanOperationListener listener) {
        beanOperationListenerList.remove(listener);
    }

    private static void fireRegisterOnException(final String beanName, final Throwable t) {
        BeanOperationListener[] listeners = new BeanOperationListener[beanOperationListenerList.size()];
        beanOperationListenerList.toArray(listeners);
        if (ArrayUtils.isNotEmpty(listeners)) {
            for (BeanOperationListener listener : listeners) {
                listener.registerOnException(beanName, t);
            }
        }
    }

    private static void fireGetOnException(final String beanName, final Throwable t) {
        BeanOperationListener[] listeners = new BeanOperationListener[beanOperationListenerList.size()];
        beanOperationListenerList.toArray(listeners);
        if (ArrayUtils.isNotEmpty(listeners)) {
            for (BeanOperationListener listener : listeners) {
                listener.getOnException(beanName, t);
            }
        }
    }

    private static void fireUnregisterOnException(final String beanName, final Throwable t) {
        BeanOperationListener[] listeners = new BeanOperationListener[beanOperationListenerList.size()];
        beanOperationListenerList.toArray(listeners);
        if (ArrayUtils.isNotEmpty(listeners)) {
            for (BeanOperationListener listener : listeners) {
                listener.unregisterOnException(beanName, t);
            }
        }
    }

    private static void firePostProcessAfterInitialization(final Object bean, final String beanName) {
        BeanOperationListener[] listeners = new BeanOperationListener[beanOperationListenerList.size()];
        beanOperationListenerList.toArray(listeners);
        if (ArrayUtils.isNotEmpty(listeners)) {
            for (BeanOperationListener listener : listeners) {
                listener.postProcessAfterInitialization(bean, beanName);
            }
        }
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static AutowireCapableBeanFactory getAutowireCapableBeanFactory() {
        return applicationContext.getAutowireCapableBeanFactory();
    }
}
