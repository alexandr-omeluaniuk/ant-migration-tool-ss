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
import com.sforce.soap.apex.CodeCoverageResult;
import com.sforce.soap.apex.RunTestFailure;
import com.sforce.soap.apex.RunTestsRequest;
import com.sforce.soap.apex.RunTestsResult;
import com.sforce.soap.apex.SoapConnection;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.LogLevel;

/**
 * Test apex classes.
 * @author ss
 */
public class TestTask extends SFDCAntTask {
    /** Coverage limit. */
    public static final float COVERAGE_LIMIT = 75;
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
        defineTaskState(testResult);
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
    /**
     * Define task state, fail or no.
     * @param result test execution result.
     * @throws BuildException tests failed.
     */
    private void defineTaskState(final RunTestsResult result)
            throws BuildException {
        // failed one or more tests
        RunTestFailure[] failTests = result.getFailures();
        if (failTests.length > 0) {
            StringBuilder sb = new StringBuilder("Next tests failed:\n\n");
            int count = 1;
            for (RunTestFailure rtf : failTests) {
                sb.append(count).append(". ");
                sb.append(rtf.getName()).append(".")
                        .append(rtf.getMethodName()).append(":\n");
                sb.append("Error message: ").append(rtf.getMessage())
                        .append("\n");
                sb.append(rtf.getStackTrace()).append("\n\n");
                count++;
            }
            throw new BuildException(sb.toString());
        }
        // fail by coverage parameters.
        CodeCoverageResult[] coverageResult = result.getCodeCoverage();
        StringBuilder sb = new StringBuilder(
                "Low coverage for next tests:\n\n");
        int count = 1;
        boolean fail = false;
        Set<String> classes = getProjectClassesAndTriggers();
        for (CodeCoverageResult ccr : coverageResult) {
            if (!classes.contains(ccr.getName())) {
                continue;
            }
            int coverageLines = ccr.getNumLocations()
                    - ccr.getNumLocationsNotCovered();
            float coveragePercent;
            if (ccr.getNumLocations() == 0) {
                coveragePercent = 100f;
            } else {
                coveragePercent = (((float) coverageLines)
                    / ((float) ccr.getNumLocations())) * 100;
            }
            if (coveragePercent < COVERAGE_LIMIT) {
                fail = true;
                sb.append(count).append(". ").append(ccr.getName())
                        .append(": ");
                sb.append(String.format("%.1f", coveragePercent)).append("%\n");
                count++;
            }
        }
        if (fail) {
            throw new BuildException(sb.toString());
        }
    }
    /**
     * Get project classes and triggers.
     * @return project classes and triggers names.
     */
    public Set<String> getProjectClassesAndTriggers() {
        Set<String> classes = new HashSet<>();
        File classesDir = new File(srcDir, "classes");
        if (classesDir.exists()) {
            for (File f : classesDir.listFiles()) {
                if (f.getName().endsWith(".cls")) {
                    log("add class [" + f.getName() + "]",
                            LogLevel.DEBUG.getLevel());
                    classes.add(f.getName().replace(".cls", ""));
                }
            }
        }
        File triggersDir = new File(srcDir, "triggers");
        if (triggersDir.exists()) {
            for (File f : triggersDir.listFiles()) {
                if (f.getName().endsWith(".trigger")) {
                    log("add trigger [" + f.getName() + "]",
                            LogLevel.DEBUG.getLevel());
                    classes.add(f.getName().replace(".trigger", ""));
                }
            }
        }
        log("classes found [" + classes.size() + "]");
        return classes;
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
