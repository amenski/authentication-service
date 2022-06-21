package it.aman.authenticationservice.aspect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.logging.LogLevel;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import it.aman.authenticationservice.annotation.Loggable;
import it.aman.authenticationservice.util.AuthConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements how to log methods annotated as {@code EthLoggable}
 * 
 * @author Amanuel
 *
 */

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class EthLoggerAspect {

    private final Environment environment;

    // The argument ProceedingJoinPoint will be an executing method which has been
    // annotated with @EthLoggable.
    @Around("@annotation(it.aman.authenticationservice.annotation.Loggable)")
    public Object logInterceptor(ProceedingJoinPoint jointPoint) throws Throwable {
        List<String> arguments = new ArrayList<>();
        String declaringMethod = EthLoggerAspect.class.getSimpleName();
        Object proceed = null;
        try {
            MethodSignature methodSignature = (MethodSignature) jointPoint.getSignature();
            Method method = methodSignature.getMethod();
            declaringMethod = methodSignature.getDeclaringType().getSimpleName() + "." + method.getName() + "()"; // like clazz.methodName()

            // argument name and value
            // if the inputs are objects, print data properly -> can print objects with toString()
            String[] argumentNames = methodSignature.getParameterNames();
            Object[] methodArguments = jointPoint.getArgs();

            // get exclusions
            Loggable loggable = method.getAnnotation(Loggable.class);
            String[] exclusions = loggable.exclusions();

            // add name and value of each parameter
            for (int i = 0; i < methodArguments.length; i++) {
                if (Arrays.asList(exclusions).contains(argumentNames[i])) {
                    arguments.add(argumentNames[i] + " = " + "****");
                    continue;
                }
                arguments.add(argumentNames[i] + " = " + methodArguments[i]);
            }

            // log
            log(declaringMethod, LogLevel.INFO, loggable.format(), AuthConstants.METHOD_START);
            log(declaringMethod, LogLevel.INFO, loggable.format(), arguments.toString());

            proceed = jointPoint.proceed();
        } catch (Exception e) {
            if (isDevEnv()) {
                log(declaringMethod, LogLevel.ERROR, AuthConstants.PARAMETER_2, e.toString());
            } else {
                log(declaringMethod, LogLevel.ERROR, AuthConstants.PARAMETER_2, e.getMessage());
            }
            throw e; // should not swallow the thrown exception
        } finally {
            log(declaringMethod, LogLevel.INFO, AuthConstants.PARAMETER_2, AuthConstants.METHOD_END);
        }
        return proceed;
    }

    public void log(String declaringMethod, LogLevel level, String format, String message) {

        switch (level) {
        case INFO:
            log.info(format, declaringMethod, message);
            break;
        case DEBUG:
            log.debug(format, declaringMethod, message);
            break;
        case WARN:
            log.warn(format, declaringMethod, message);
            break;
        case ERROR:
            log.error(format, declaringMethod, message);
            break;
        default:
            break;
        }
    }

    private boolean isDevEnv() {
        String profile = environment.getProperty("spring.profiles.active");
        return StringUtils.equalsAny(profile, "dev", "development");
    }
}
