/*
 * The MIT License
 *
 * Copyright 2018 Pivotal Software, Inc..
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
package com.salesforce.ant;

import com.salesforce.report.CoverageReport;
import com.sforce.soap.apex.RunTestsRequest;
import com.sforce.soap.apex.RunTestsResult;
import com.sforce.soap.apex.SoapConnection;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.BuildException;

/**
 * Test apex classes.
 * @author ss
 */
public class TestTask extends SFDCAntTask {
    /** Project src directory. */
    private File srcDir;
    /** Test classes. */
    private List<RunTest> runTests = new ArrayList<>();
    @Override
    public void execute() throws BuildException {
        log("======================= RUN TESTS ==============================");
        validateAttributes();
        log("src directory [" + getSrcDir().getAbsolutePath() + "]");
        log("test classes found [" + runTests.size() + "]");
        List<String> testCase = new ArrayList<>();
        runTests.stream().forEach((t) -> {
            log("[" + (t.isTest() ? "+" : "x") + "] " + t.getClassName());
            if (t.isTest()) {
                testCase.add(t.getClassName());
            }
        });
        log("run [" + testCase.size() + "] tests");
        RunTestsResult testResult = makeRequest(testCase);
        CoverageReport report = new CoverageReport(testResult, srcDir, this);
        report.createReport();
    }
    /**
     * Run tests on server and get result.
     * @param testCase test case.
     * @return test result.
     * @throws BuildException error.
     */
    private RunTestsResult makeRequest(final List<String> testCase)
            throws BuildException {
        RunTestsRequest request = new RunTestsRequest();
        request.setAllTests(false);
        request.setClasses(testCase.toArray(new String[0]));
        try {
            log("run tests on server, please wait...");
            SoapConnection sc = getApexConnection();
            RunTestsResult result = sc.runTests(request);
            log("operation completed...");
            log("total time [" + result.getTotalTime() + "]");
            return result;
        } catch (Exception e) {
            throw new BuildException("connection problem!", e);
        }
    }
// ============================= SET & GET ====================================
    /**
     * @return the srcDir
     */
    public File getSrcDir() {
        return srcDir;
    }

    /**
     * @param srcDir the srcDir to set
     */
    public void setSrcDir(File srcDir) {
        this.srcDir = srcDir;
    }
    /**
     * @return the runTest
     */
    public List<RunTest> getRunTest() {
        return runTests;
    }
    /**
     * @param runTest the runTest to set
     */
    public void setRunTest(List<RunTest> runTest) {
        this.runTests = runTest;
    }
    /**
     * Create nested element.
     * @return new test class.
     */
    public RunTest createRunTest() {                                 // 3
        RunTest test = new RunTest();
        test.setTest(true);
        this.runTests.add(test);
        return test;
    }
// ============================================================================
    /**
     * Represent on test class.
     */
    public class RunTest {
        /** Class name. */
        private String className;
        /** Run this test or no. */
        private boolean test = true;
        /**
         * @return the text
         */
        public String getClassName() {
            return className;
        }
        /**
         * @param text the text to set
         */
        public void setClassName(String text) {
            this.className = text;
        }
        /**
         * @return the test
         */
        public boolean isTest() {
            return test;
        }
        /**
         * @param test the test to set
         */
        public void setTest(boolean test) {
            this.test = test;
        }
        /**
         * Add nested text.
         * @param text nested text.
         */
        public void addText(String text) {
            this.className = text;
        }
    }
}
