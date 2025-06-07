package com.cutec.utils;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.Objects;

public class CopySkipNull {

    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();
        return java.util.Arrays.stream(pds)
                .filter(pd -> src.getPropertyValue(pd.getName()) == null || Objects.requireNonNull(src.getPropertyValue(pd.getName())).toString().isEmpty())
                .map(PropertyDescriptor::getName)
                .toArray(String[]::new);
    }
}
