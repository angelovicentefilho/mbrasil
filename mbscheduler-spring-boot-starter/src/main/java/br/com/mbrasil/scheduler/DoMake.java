package br.com.mbrasil.scheduler;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component("doMakeScheduler")
@Slf4j
public class DoMake {

	@Pointcut("@annotation(br.com.mbrasil.scheduler.annotation.MBScheduled)")
	public void aopPoint() {
	}

	public Object doRouter(ProceedingJoinPoint joinPoint) throws Throwable {
		long begin = System.currentTimeMillis();
		Method method = getMethod(joinPoint);
		try {
			return joinPoint.proceed();
		} finally {
			long end = System.currentTimeMillis();
			log.info("\nMiddleware schedule method: '{}.{}' take time(m): '{}'",
					joinPoint.getTarget().getClass().getSimpleName(), method.getName(), (end - begin));
		}
	}

	private Method getMethod(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
		Signature sig = joinPoint.getSignature();
		MethodSignature methodSignature = (MethodSignature) sig;
		return getClass(joinPoint).getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
	}

	private Class<? extends Object> getClass(JoinPoint joinPoint) throws NoSuchMethodException {
		return joinPoint.getTarget().getClass();
	}
}
