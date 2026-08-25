package com.syscom.fep.web.entity;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.BeanUtils;

import com.github.pagehelper.PageInfo;

public class PageData<F, T> extends PageInfo<T> {
    private static final long serialVersionUID = 1L;

    /**
     * 查詢的表單
     */
    public F form;

    public PageData(PageInfo<T> pageInfo, F form) {
        if (pageInfo == null)
            return;
        BeanUtils.copyProperties(pageInfo, this);
        this.form = form;
    }

    public F getForm() {
        return form;
    }

    public void setForm(F formData) {
        this.form = formData;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
