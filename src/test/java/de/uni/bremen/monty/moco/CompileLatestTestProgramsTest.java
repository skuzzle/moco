/*
 * moco, the Monty Compiler Copyright (c) 2013-2014, Monty's Coconut, All rights
 * reserved.
 *
 * This file is part of moco, the Monty Compiler.
 *
 * moco is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * moco is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * Linking this program and/or its accompanying libraries statically or
 * dynamically with other modules is making a combined work based on this
 * program. Thus, the terms and conditions of the GNU General Public License
 * cover the whole combination.
 *
 * As a special exception, the copyright holders of moco give you permission to
 * link this programm and/or its accompanying libraries with independent modules
 * to produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting executable
 * under terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that module.
 *
 * An independent module is a module which is not derived from or based on this
 * program and/or its accompanying libraries. If you modify this library, you
 * may extend this exception to your version of the program or library, but you
 * are not obliged to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 *
 * You should have received a copy of the GNU General Public License along with
 * this library.
 */

package de.uni.bremen.monty.moco;

import static de.uni.bremen.monty.moco.IntegrationTestUtils.expectedErrorFromFile;
import static de.uni.bremen.monty.moco.IntegrationTestUtils.expectedResultFromFile;
import static de.uni.bremen.monty.moco.IntegrationTestUtils.getOutput;
import static de.uni.bremen.monty.moco.IntegrationTestUtils.outputFileExists;
import static de.uni.bremen.monty.moco.IntegrationTestUtils.setStdErr;
import static de.uni.bremen.monty.moco.IntegrationTestUtils.setStdout;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CompileLatestTestProgramsTest extends CompileFilesBaseTest {

    public CompileLatestTestProgramsTest(File file, String fileName) {
        super(file, fileName);
    }

    @Parameters(name = "Program: {1}")
    public static Collection<Object[]> data() throws Exception {

        File[] programFiles = { getLastModifiedMontyFile("testPrograms/") };
        return buildParameterObject(Arrays.asList(programFiles));
    }

    @Test
    public void compileProgramTest() throws IOException {
        Assume.assumeFalse("Skipping Compile tests due to system property", SKIP_COMPILE);

        final PrintStream bufferOut = System.out;
        final PrintStream bufferErr = System.err;
        final ByteArrayOutputStream outStream = setStdout();
        final ByteArrayOutputStream errorStream = setStdErr(this.file);

        Main.main(new String[] { "-k", this.file.getAbsolutePath(), "-e" });

        if (outputFileExists(this.file)) {
            assertThat(getOutput(errorStream), is(isEmptyString()));
            assertThat(getOutput(outStream), is(expectedResultFromFile(this.file)));
        } else {
            assertThat(getOutput(errorStream), is(expectedErrorFromFile(this.file)));
            assertThat(getOutput(outStream), is(isEmptyString()));
        }
        System.setOut(bufferOut);
        System.setErr(bufferErr);
    }
}
