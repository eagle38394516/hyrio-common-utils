package xyz.hyrio.common.components;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Component
@Aspect
public class AutoLogAspect {
    private static final Logger log = LoggerFactory.getLogger(AutoLogAspect.class);

    @Around("@annotation(xyz.hyrio.common.components.AutoLog)")
    public Object autoLog(ProceedingJoinPoint pjp) throws Throwable {
        Signature signature = pjp.getSignature();
        if (!(signature instanceof MethodSignature methodSignature)) {
            log.warn("method signature({}) is not a MethodSignature", signature);
            return pjp.proceed();
        }

        String methodName = methodSignature.getName();
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] parameterValues = pjp.getArgs();
        int parameterNamesLength = isEmpty(parameterNames) ? 0 : parameterNames.length;
        int parameterValuesLength = isEmpty(parameterValues) ? 0 : parameterValues.length;
        if (parameterNamesLength != parameterValuesLength) {
            log.warn("parameter names({}) and values({}) are not equal", parameterNamesLength, parameterValuesLength);
            return pjp.proceed();
        }
        List<String> parameters = new ArrayList<>(parameterNamesLength);
        for (int i = 0; i < parameterNamesLength; i++) {
            parameters.add(parameterNames[i] + "=" + parameterValues[i]);
        }

        Level level = methodSignature.getMethod().getAnnotation(AutoLog.class).level();
        try {
            Object ret = pjp.proceed();
            log.atLevel(level).log("{} => args: {}; ret: {}", methodName, parameters, ret);
            return ret;
        } catch (Throwable t) {
            log.atLevel(level).log("{} => args: {}. An error occurred.", methodName, parameters, t);
            throw t;
        }
    }
}
