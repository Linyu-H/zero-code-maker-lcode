package com.commul.ailcode.core.parser;

public interface CodeParser<T> {

    /**
     * 代码解析器策略接口
     * @param codeContent
     * @return
     */
    T parseCode(String codeContent);
}
