package frameworkontology.helloworld.processor;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import frameworkontology.helloworld.ontology.concept.Concept;

@Component
public class HelloWorldBeanPostProcessor implements BeanPostProcessor {
	
	@Autowired
	private ConfigurableListableBeanFactory beanFactory;
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		Concept annotation = AnnotationUtils.findAnnotation(bean.getClass(), Concept.class);
		if(annotation == null) {
			return bean;
		}
		
		ProxyFactory proxyFactory = new ProxyFactory(bean);
		proxyFactory.addAdvice(new LoggingInterceptor(annotation));
		
		beanFactory.registerAlias(beanName, beanName + annotation.value());
		
		return proxyFactory.getProxy();
	}
	
	private class LoggingInterceptor implements org.aopalliance.intercept.MethodInterceptor {
		private static Logger LOG = LoggerFactory.getLogger(LoggingInterceptor.class);
		private Concept annotation;

		public LoggingInterceptor(Concept annotation) {
			this.annotation = annotation;
		}

		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {
			invocation.proceed(); 
			LOG.info("{}", annotation.value());
			return null;
		}

		
		
	}

}
