package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Field;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充逻辑处理
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点，匹配所有带有 @AutoFill 注解的方法
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {}
    /**
     * 前置通知，在数据库操作之前自动填充公共字段
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws IllegalAccessException, NoSuchFieldException {
        log.info("开始进行公共字段自动填充...");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象
        OperationType operationType = autoFill.value();//获得数据库操作类型

        //获取到当前被拦截的方法的参数
        Object entity = joinPoint.getArgs()[0];

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据当前不同的操作类型，为对应的属性通过反射来赋值
        if (operationType == OperationType.INSERT) {
            // 插入操作，填充创建时间、创建人
            setFieldValue(entity, "createTime", now);
            setFieldValue(entity, "createUser", currentId);
        }

        if (operationType == OperationType.INSERT || operationType == OperationType.UPDATE) {
            // 插入或更新操作，填充更新时间、更新人
            setFieldValue(entity, "updateTime", now);
            setFieldValue(entity, "updateUser", currentId);
        }
    }
    /**
     * 辅助方法，用于设置特定字段的值
     */
    private void setFieldValue(Object entity, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = entity.getClass().getDeclaredField(fieldName); // 获取特定字段
        field.setAccessible(true); // 设置私有字段可访问
        field.set(entity, value); // 设置字段值
    }

}
