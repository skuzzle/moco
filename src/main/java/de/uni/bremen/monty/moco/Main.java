/*
 * moco, the Monty Compiler
 * Copyright (c) 2013-2014, Monty's Coconut, All rights reserved.
 *
 * This file is part of moco, the Monty Compiler.
 *
 * moco is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * moco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * Linking this program and/or its accompanying libraries statically or
 * dynamically with other modules is making a combined work based on this
 * program. Thus, the terms and conditions of the GNU General Public License
 * cover the whole combination.
 *
 * As a special exception, the copyright holders of moco give
 * you permission to link this programm and/or its accompanying libraries
 * with independent modules to produce an executable, regardless of the
 * license terms of these independent modules, and to copy and distribute the
 * resulting executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the
 * license of that module.
 *
 * An independent module is a module which is not
 * derived from or based on this program and/or its accompanying libraries.
 * If you modify this library, you may extend this exception to your version of
 * the program or library, but you are not obliged to do so. If you do not wish
 * to do so, delete this exception statement from your version.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library.
 */
package de.uni.bremen.monty.moco;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.antlr.v4.runtime.misc.TestRig;
import org.apache.commons.io.IOUtils;

import de.uni.bremen.monty.moco.antlr.MontyParser;
import de.uni.bremen.monty.moco.ast.AntlrAdapter;
import de.uni.bremen.monty.moco.ast.Package;
import de.uni.bremen.monty.moco.ast.PackageBuilder;
import de.uni.bremen.monty.moco.util.Params;
import de.uni.bremen.monty.moco.util.ParseTreePrinter;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;
import de.uni.bremen.monty.moco.visitor.CodeGenerationVisitor;
import de.uni.bremen.monty.moco.visitor.ControlFlowVisitor;
import de.uni.bremen.monty.moco.visitor.DeclarationVisitor;
import de.uni.bremen.monty.moco.visitor.DotVisitor;
import de.uni.bremen.monty.moco.visitor.NameManglingVisitor;
import de.uni.bremen.monty.moco.visitor.SetParentVisitor;
import de.uni.bremen.monty.moco.visitor.typeinf.QuantumTypeErasor9k;
import de.uni.bremen.monty.moco.visitor.typeinf.QuantumTypeResolver3000;

public class Main {

	public static void main(String[] args) throws IOException {
		final Params params = new Params(args);
		new Main().start(params);
	}

	private void start(Params params) throws IOException {

		final String inputFile = params.getInputFile();

		if (params.isDebugParseTree()) {
			debugParseTree(params, inputFile);
			return;
		}

		final PackageBuilder packageBuilder = new PackageBuilder(params);
		final Package mainPackage = packageBuilder.buildPackage();

		visitVisitors(params, mainPackage);
	}

	private void visitVisitors(Params params, Package ast) throws IOException {
        final BaseVisitor[] visitors = new BaseVisitor[] {
                new SetParentVisitor(),
                new DeclarationVisitor(),
                new QuantumTypeResolver3000(),
                new QuantumTypeErasor9k(),
                new ControlFlowVisitor(),
                new NameManglingVisitor(),
                new CodeGenerationVisitor(params)
        };

		boolean everyThingIsAwesome = true;

		for (final BaseVisitor visitor : visitors) {
			visitor.setStopOnFirstError(params.isStopOnFirstError());

			try {
				visitor.visitDoubleDispatched(ast);
			} catch (final RuntimeException exception) {
				visitor.logError(exception);
				everyThingIsAwesome = false;
				break;
			}

			if (visitor.foundError()) {
				everyThingIsAwesome = false;
				break;
			}
		}

		if (params.usePrintVisitor()) {
            try (final DotVisitor v = DotVisitor.toFile(new File("ast.dot"), false)) {
			    v.visitDoubleDispatched(ast);
			    v.finish();
			} catch (final IOException e) {
			    System.out.println(e.getMessage());
			}
		} else if (everyThingIsAwesome) {
			(new CodeGenerationVisitor(params)).visitDoubleDispatched(ast);
			generateCode(params);
		}
	}

	private void debugParseTree(Params params, String inputFile) throws IOException {
		final AntlrAdapter antlrAdapter = new AntlrAdapter();

		final File file = new File(inputFile);
		final MontyParser parser = antlrAdapter.createParser(new FileInputStream(file));
		final ParseTreePrinter parseTreePrinter = new ParseTreePrinter(parser);
		parser.addParseListener(parseTreePrinter);
		parser.compilationUnit();
		System.out.print(parseTreePrinter.toString());
		try {
			new TestRig(new String[] { "de.uni.bremen.monty.moco.antlr.Monty", "compilationUnit", "-gui",
			        params.getInputFile() }).process();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void generateCode(Params params) throws IOException {
		PrintStream resultStream;
		if (params.getOutputFile() == null) {
			resultStream = System.out;
		} else {
			resultStream = new PrintStream(new FileOutputStream(params.getOutputFile()));
		}

		if (!params.isGenerateOnlyLLVM()) {
			final String llFile = params.getLlFile();
			final ProcessBuilder processBuilder = new ProcessBuilder("lli", llFile);
			final Process start = processBuilder.start();

			final String in = IOUtils.toString(start.getInputStream());
			final String err = IOUtils.toString(start.getErrorStream());

			System.err.print(err);
			resultStream.print(in);

			if (!params.isKeepLLVMCode()) {
				if (!new File(llFile).delete()) {
					System.err.println("Warning: failed to delete file " + llFile);
				}
			}
		}
	}
}
