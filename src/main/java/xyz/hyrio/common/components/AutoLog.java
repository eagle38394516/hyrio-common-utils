package xyz.hyrio.common.components;

import org.slf4j.event.Level;

import java.lang.annotation.*;

/**
 * 自动记录传入的参数、返回值及异常信息（如果有）。
 *
 * @author Hyrio 2023/01/11 15:45
 * @see AutoLogAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoLog {
    Level level() default Level.DEBUG;
}
