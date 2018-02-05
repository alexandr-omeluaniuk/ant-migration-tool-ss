/*
 * The MIT License
 *
 * Copyright 2018 ss.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.salesforce.report;

import com.sforce.soap.apex.RunTestFailure;
import com.sforce.soap.apex.RunTestSuccess;

/**
 * Test element.
 * @author ss
 */
public class TestElement implements Comparable<TestElement> {
    /** False value sign. */
    private static final String SIGN_FALSE = "&#9746;";
    /** True value sign. */
    private static final String SIGN_TRUE = "&#9745;";
    /** Class name. */
    private String className;
    /** Test class method name. */
    private String methodName;
    /** Test class method duration. */
    private double duration;
    /** Test method is failed?. */
    private boolean isFail;
    /** Fail message. */
    private String failMessage;
    /** Stacktrace. */
    private String stackTrace;
    /** Test where enabled 'See all data'. */
    private boolean seeAllData;
    /**
     * Constructor for JAXB.
     */
    public TestElement() {
    }
    /**
     * Constructor.
     * @param state test state.
     */
    public TestElement(final RunTestFailure state) {
        this.methodName = state.getMethodName();
        this.className = state.getName();
        this.isFail = true;
        this.duration = state.getTime();
        this.failMessage = state.getMessage();
        this.stackTrace = state.getStackTrace();
        this.seeAllData = state.getSeeAllData();
    }
    /**
     * Constructor.
     * @param state test state.
     */
    public TestElement(final RunTestSuccess state) {
        this.className = state.getName();
        this.methodName = state.getMethodName();
        this.isFail = false;
        this.duration = state.getTime();
        this.seeAllData = state.getSeeAllData();
    }
    /**
     * Represent as HTML row.
     * @return HTML row.
     */
    public String toHTMLRow() {
        StringBuilder sb = new StringBuilder();
        sb.append("<tr>");
        sb.append("<td class=\"col-method-name\">").append(methodName).append("</td>");
        sb.append("<td class=\"error\">").append(failMessage == null ? "" : failMessage)
                .append("</td>");
        sb.append("<td class=\"col-state ")
                .append(isFail ? "error" : "success")
                .append("\">").append(isFail ? SIGN_FALSE : SIGN_TRUE)
                .append("</td>");
        sb.append("<td class=\"col-state ")
                .append(seeAllData ? "error" : "success")
                .append("\">").append(seeAllData ? SIGN_FALSE : SIGN_TRUE)
                .append("</td>");
        sb.append("<td class=\"col-duration\">").append(duration).append("</td>");
        sb.append("</tr>");
        return sb.toString();
    }
    @Override
    public int compareTo(TestElement o) {
        return this.methodName.compareTo(o.getMethodName());
    }
// ============================================================================
    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }
    /**
     * @param methodName the methodName to set
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    /**
     * @return the duration
     */
    public double getDuration() {
        return duration;
    }
    /**
     * @param duration the duration to set
     */
    public void setDuration(double duration) {
        this.duration = duration;
    }
    /**
     * @return the isFail
     */
    public boolean isIsFail() {
        return isFail;
    }
    /**
     * @param isFail the isFail to set
     */
    public void setIsFail(boolean isFail) {
        this.isFail = isFail;
    }
    /**
     * @return the failMessage
     */
    public String getFailMessage() {
        return failMessage;
    }
    /**
     * @param failMessage the failMessage to set
     */
    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }
    /**
     * @return the stackTrace
     */
    public String getStackTrace() {
        return stackTrace;
    }
    /**
     * @param stackTrace the stackTrace to set
     */
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
    /**
     * @return the seeAllData
     */
    public boolean isSeeAllData() {
        return seeAllData;
    }
    /**
     * @param seeAllData the seeAllData to set
     */
    public void setSeeAllData(boolean seeAllData) {
        this.seeAllData = seeAllData;
    }
    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }
    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }
}
