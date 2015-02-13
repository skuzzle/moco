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
package de.uni.bremen.monty.moco.util;

import java.io.File;

public class Params {
	private String inputFolder;
	private String mainModule;

	private boolean debugParseTree;
	private String inputFile;
	private String outputFile;
	private boolean usePrintVisitor;
	private boolean generateOnlyLLVM;
	private boolean keepLLVMCode;
	private boolean stopOnFirstError;
	private String llFile;

	public Params(String[] args) {
		if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
			printHelp();
		}

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("-p")) {
				this.usePrintVisitor = true;
			} else if (arg.equals("-ll")) {
				this.generateOnlyLLVM = true;
			} else if (arg.equals("-o")) {
				this.outputFile = args[++i];
			} else if (arg.equals("-k")) {
				this.keepLLVMCode = true;
			} else if (arg.equals("-s")) {
				this.debugParseTree = true;
			} else if (arg.equals("-e")) {
				this.stopOnFirstError = true;
			} else {
				if (new File(args[i]).isDirectory()) {
					this.inputFolder = args[i];
				} else if (this.inputFolder != null) {
					this.mainModule = args[i];
				} else {
					this.inputFile = args[i];
				}
			}
		}

		if (this.inputFile == null && (this.inputFolder == null || this.mainModule == null)) {
			printHelp();
		}
	}

    public Params() {}

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public void setInputFolder(String inputFolder) {
        this.inputFolder = inputFolder;
    }

	public String getInputFile() {
		return this.inputFile;
	}

	public String getOutputFile() {
		return this.outputFile;
	}

	public boolean usePrintVisitor() {
		return this.usePrintVisitor;
	}

	public boolean isGenerateOnlyLLVM() {
		return this.generateOnlyLLVM;
	}

	public boolean isKeepLLVMCode() {
		return this.keepLLVMCode;
	}

	public boolean isStopOnFirstError() {
		return this.stopOnFirstError;
	}

	public boolean isDebugParseTree() {
		return this.debugParseTree;
	}

	public void printHelp() {
		System.out.println("moco [args] inputFile [-o outputFile]");
		System.out.println("execute monty File");

		System.out.println("-s\tdebug ANTLR parse Tree");
		System.out.println("-p\tprint AST without code generation");
		System.out.println("-ll\tgenerate only LLVM code");
		System.out.println("-k\tkeep LLVM Code");
		System.out.println("-e\tstop on first error");
		System.exit(0);
	}

	public void setLlFile(String llFile) {
		this.llFile = llFile;
	}

	public String getLlFile() {
		return this.llFile;
	}

	public String getInputFolder() {
		return this.inputFolder;
	}

	public String getMainModule() {
		return this.mainModule;
	}
}
