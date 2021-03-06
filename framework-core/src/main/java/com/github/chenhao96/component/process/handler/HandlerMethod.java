/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.chenhao96.component.process.handler;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.SynthesizingMethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Encapsulates information about a handler method consisting of a
 * {@linkplain #getMethod() method} and a {@linkplain #getBean() bean}.
 * Provides convenient access to method parameters, the method return value,
 * method annotations, etc.
 * <p>
 * <p>The class may be created with a bean instance or with a bean name
 * (e.g. lazy-init bean, prototype bean). Use to obtain a {@code HandlerMethod}
 * instance with a bean instance resolved through the associated {@link BeanFactory}.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.1(org.springframework.web.method)
 */
public class HandlerMethod {

    private final Object bean;

    private final Class<?> beanType;

    private final Method method;

    private final Method bridgedMethod;

    private final MethodParameter[] parameters;

    /**
     * Create an instance from a bean instance and a method.
     */
    public HandlerMethod(Object bean, Method method) {
        Assert.notNull(bean, "Bean is required");
        Assert.notNull(method, "Method is required");
        this.bean = bean;
        this.beanType = ClassUtils.getUserClass(bean);
        this.method = method;
        this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
        this.parameters = initMethodParameters();
    }

    private MethodParameter[] initMethodParameters() {
        int count = this.bridgedMethod.getParameterTypes().length;
        MethodParameter[] result = new MethodParameter[count];
        for (int i = 0; i < count; i++) {
            HandlerMethodParameter parameter = new HandlerMethodParameter(i);
            GenericTypeResolver.resolveParameterType(parameter, this.beanType);
            result[i] = parameter;
        }
        return result;
    }

    /**
     * Return the bean for this handler method.
     */
    public Object getBean() {
        return this.bean;
    }

    /**
     * Return the method for this handler method.
     */
    public Method getMethod() {
        return this.method;
    }

    /**
     * This method returns the type of the handler for this handler method.
     * <p>Note that if the bean type is a CGLIB-generated class, the original
     * user-defined class is returned.
     */
    public Class<?> getBeanType() {
        return this.beanType;
    }

    /**
     * If the bean method is a bridge method, this method returns the bridged
     * (user-defined) method. Otherwise it returns the same method as {@link #getMethod()}.
     */
    protected Method getBridgedMethod() {
        return this.bridgedMethod;
    }

    /**
     * Return the method parameters for this handler method.
     */
    public MethodParameter[] getMethodParameters() {
        return this.parameters;
    }

    /**
     * Return the HandlerMethod return type.
     */
    public MethodParameter getReturnType() {
        return new HandlerMethodParameter(-1);
    }

    /**
     * Return {@code true} if the method return type is void, {@code false} otherwise.
     */
    public boolean isVoid() {
        return Void.TYPE.equals(getReturnType().getParameterType());
    }

    /**
     * Return a single annotation on the underlying method traversing its super methods
     * if no annotation can be found on the given method itself.
     * <p>Also supports <em>merged</em> composed annotations with attribute
     * overrides as of Spring Framework 4.2.2.
     *
     * @param annotationType the type of annotation to introspect the method for
     * @return the annotation, or {@code null} if none found
     * @see AnnotatedElementUtils#findMergedAnnotation
     */
    public <A extends Annotation> A getMethodAnnotation(Class<A> annotationType) {
        return AnnotatedElementUtils.findMergedAnnotation(this.method, annotationType);
    }

    /**
     * Return whether the parameter is declared with the given annotation type.
     *
     * @param annotationType the annotation type to look for
     * @see AnnotatedElementUtils#hasAnnotation
     * @since 4.3
     */
    public <A extends Annotation> boolean hasMethodAnnotation(Class<A> annotationType) {
        return AnnotatedElementUtils.hasAnnotation(this.method, annotationType);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HandlerMethod)) {
            return false;
        }
        HandlerMethod otherMethod = (HandlerMethod) other;
        return (this.bean.equals(otherMethod.bean) && this.method.equals(otherMethod.method));
    }

    @Override
    public int hashCode() {
        return (this.bean.hashCode() * 31 + this.method.hashCode());
    }

    @Override
    public String toString() {
        return this.method.toGenericString();
    }

    /**
     * A MethodParameter with HandlerMethod-specific behavior.
     */
    protected class HandlerMethodParameter extends SynthesizingMethodParameter {

        public HandlerMethodParameter(int index) {
            super(HandlerMethod.this.bridgedMethod, index);
        }

        protected HandlerMethodParameter(HandlerMethodParameter original) {
            super(original);
        }

        @Override
        public Class<?> getContainingClass() {
            return HandlerMethod.this.getBeanType();
        }

        @Override
        public <T extends Annotation> T getMethodAnnotation(Class<T> annotationType) {
            return HandlerMethod.this.getMethodAnnotation(annotationType);
        }

        @Override
        public <T extends Annotation> boolean hasMethodAnnotation(Class<T> annotationType) {
            return HandlerMethod.this.hasMethodAnnotation(annotationType);
        }

        @Override
        public HandlerMethodParameter clone() {
            return new HandlerMethodParameter(this);
        }
    }
}
