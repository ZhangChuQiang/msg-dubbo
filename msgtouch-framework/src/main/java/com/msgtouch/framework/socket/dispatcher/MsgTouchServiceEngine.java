package com.msgtouch.framework.socket.dispatcher;

import com.msgtouch.framework.socket.annotation.MsgService;
import com.msgtouch.framework.socket.annotation.MsgMethod;
import com.msgtouch.framework.context.SpringBeanAccess;
import com.msgtouch.framework.utils.ClassUtils;
import com.msgtouch.framework.zookeeper.ZooKeeperEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dean on 2016/9/8.
 */
public class MsgTouchServiceEngine {

    private static Logger logger= LoggerFactory.getLogger(MsgTouchServiceEngine.class);

    private static MsgTouchServiceEngine serviceEngine=null;
    private MsgTouchServiceEngine(){}

    public static MsgTouchServiceEngine getInstances(){
        if(null==serviceEngine){
            synchronized (ZooKeeperEngine.class){
                if(null==serviceEngine) {
                    serviceEngine = new MsgTouchServiceEngine();
                }
            }
        }
        return serviceEngine;
    }



    public MsgTouchMethodDispatcher loadBilingService(){
        MsgTouchMethodDispatcher bilingMethodDispatcher=new MsgTouchMethodDispatcher();
        String [] beanNames=SpringBeanAccess.getInstances().getApplicationContext().getBeanDefinitionNames();
        for(String beanName:beanNames){
            Object controlClass=SpringBeanAccess.getInstances().getApplicationContext().getBean(beanName);
            Class [] interfaces=controlClass.getClass().getInterfaces();
            Class controlInterface=null;
            for(Class interfaceclass:interfaces){
                if(ClassUtils.hasAnnotation(interfaceclass,MsgService.class)){
                    controlInterface=interfaceclass;
                    break;
                }
            }
            if(null!=controlInterface) {
                List<Method> rpcMethods = ClassUtils.findMethodsByAnnotation(controlInterface, MsgMethod.class);
                if (null != rpcMethods) {

                    for (Method method : rpcMethods) {
                        MsgMethod ma = method.getAnnotation(MsgMethod.class);

                        String cmd = ma.value();
                        Class[] paramTypes = method.getParameterTypes();
                        List<String> classNames = new ArrayList<String>(paramTypes.length);
                        for (Class paramType : paramTypes) {
                            classNames.add(paramType.getName());
                        }
                        MsgTouchMethodInvoker invoker = new MsgTouchMethodInvoker(method, controlClass.getClass());
                        bilingMethodDispatcher.addBilingMethod(cmd, invoker);
                        logger.info("{} Rpc register method : {}=>{}.{}()", this, cmd, controlClass.getClass().getName(), method.getName());
                    }
                }
            }
        }

        return bilingMethodDispatcher;
    }



}
